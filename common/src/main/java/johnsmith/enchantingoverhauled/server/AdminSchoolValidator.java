package johnsmith.enchantingoverhauled.server;

import johnsmith.enchantingoverhauled.Constants;
import johnsmith.enchantingoverhauled.api.enchantment.school.MagicSchool;
import johnsmith.enchantingoverhauled.api.enchantment.school.MagicSchools;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Validates the {@link Enchantment} registry against the 9 Schools of Magic
 * tags
 * on server startup, datapack reload, and when an operator joins the server.
 * <p>
 * Alerts operators (permission level &ge; 2) if any enchantments resolve to
 * {@code ABERRATION}
 * (unassigned) or violate the single-school rule by belonging to multiple
 * school tags.
 */
public final class AdminSchoolValidator {

    private AdminSchoolValidator() {}

    public record ValidationReport(
            List<Identifier> aberrantEnchantments,
            Map<Identifier, List<Identifier>> multiSchoolEnchantments) {
        public boolean hasIssues() {
            return !this.aberrantEnchantments.isEmpty() || !this.multiSchoolEnchantments.isEmpty();
        }
    }

    /**
     * Scans the active {@link Enchantment} registry and builds a report of any
     * unassigned (Aberrant)
     * or multi-assigned enchantments.
     */
    public static ValidationReport evaluate(RegistryAccess registryAccess) {
        List<Identifier> aberrant = new ArrayList<>();
        Map<Identifier, List<Identifier>> multiAssigned = new LinkedHashMap<>();

        var enchantmentRegistryOpt = registryAccess.lookup(Registries.ENCHANTMENT);
        if (enchantmentRegistryOpt.isEmpty()) {
            return new ValidationReport(aberrant, multiAssigned);
        }

        Registry<Enchantment> enchantmentRegistry = enchantmentRegistryOpt.get();
        for (Holder.Reference<Enchantment> holder : enchantmentRegistry.listElements().toList()) {
            Identifier enchId = holder.key().identifier();
            List<ResourceKey<MagicSchool>> matchedSchools = MagicSchools.getMatchingSchools(registryAccess, holder);

            if (matchedSchools.isEmpty()) {
                aberrant.add(enchId);
            } else if (matchedSchools.size() > 1) {
                List<Identifier> schoolIds = matchedSchools.stream()
                        .map(ResourceKey::identifier)
                        .toList();
                multiAssigned.put(enchId, schoolIds);
            }
        }

        return new ValidationReport(aberrant, multiAssigned);
    }

    /**
     * Runs validation on server start or datapack reload, logging warnings to the
     * server console
     * and notifying all currently connected operators.
     */
    public static void validateAndAlert(MinecraftServer server) {
        if (server == null)
            return;
        RegistryAccess registryAccess = server.registryAccess();
        ValidationReport report = evaluate(registryAccess);

        if (!report.hasIssues()) {
            return;
        }

        if (!report.aberrantEnchantments().isEmpty()) {
            String listStr = report.aberrantEnchantments().stream()
                    .map(Identifier::toString)
                    .collect(Collectors.joining(", "));
            Constants.LOG.warn(
                    "[Enchanting Overhauled] Detected {} unassigned (Aberration) enchantment(s): [{}]",
                    report.aberrantEnchantments().size(),
                    listStr);
        }

        if (!report.multiSchoolEnchantments().isEmpty()) {
            String details = report.multiSchoolEnchantments().entrySet().stream()
                    .map(e -> e.getKey() + " -> " + e.getValue())
                    .collect(Collectors.joining("; "));
            Constants.LOG.warn(
                    "[Enchanting Overhauled] Detected {} enchantment(s) assigned to multiple Schools of Magic: {}",
                    report.multiSchoolEnchantments().size(),
                    details);
        }

        List<Component> messages = buildAlertMessages(report);
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)) {
                for (Component msg : messages) {
                    player.sendSystemMessage(msg);
                }
            }
        }
    }

    /**
     * Checks if the joining player is an operator (permission level &ge; 2) and
     * sends them
     * the formatted chat alert if any datapack school assignment issues exist.
     */
    public static void validateAndAlertPlayer(ServerPlayer player) {
        if (player == null || player.level().getServer() == null || !player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)) {
            return;
        }

        ValidationReport report = evaluate(player.level().getServer().registryAccess());
        if (!report.hasIssues()) {
            return;
        }

        for (Component msg : buildAlertMessages(report)) {
            player.sendSystemMessage(msg);
        }
    }

    private static List<Component> buildAlertMessages(ValidationReport report) {
        List<Component> messages = new ArrayList<>();

        MutableComponent header = Component.translatable("chat.enchanting_overhauled.school_validator.header")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);
        messages.add(header);

        if (!report.aberrantEnchantments().isEmpty()) {
            String ids = report.aberrantEnchantments().stream()
                    .map(Identifier::toString)
                    .collect(Collectors.joining(", "));
            MutableComponent aberrantMsg = Component.translatable(
                    "chat.enchanting_overhauled.school_validator.aberrant",
                    Component.literal(String.valueOf(report.aberrantEnchantments().size()))
                            .withStyle(ChatFormatting.YELLOW),
                    Component.literal(ids).withStyle(ChatFormatting.GRAY)).withStyle(ChatFormatting.RED);
            messages.add(aberrantMsg);
        }

        if (!report.multiSchoolEnchantments().isEmpty()) {
            String conflicts = report.multiSchoolEnchantments().entrySet().stream()
                    .map(e -> e.getKey() + " ("
                            + e.getValue().stream().map(Identifier::toString).collect(Collectors.joining(" & "))
                            + ")")
                    .collect(Collectors.joining(", "));
            MutableComponent multiMsg = Component.translatable(
                    "chat.enchanting_overhauled.school_validator.multi_school",
                    Component.literal(String.valueOf(report.multiSchoolEnchantments().size()))
                            .withStyle(ChatFormatting.YELLOW),
                    Component.literal(conflicts).withStyle(ChatFormatting.GRAY)).withStyle(ChatFormatting.RED);
            messages.add(multiMsg);
        }

        return messages;
    }
}