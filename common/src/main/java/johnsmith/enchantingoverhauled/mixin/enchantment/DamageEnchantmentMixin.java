package johnsmith.enchantingoverhauled.mixin.enchantment;

import johnsmith.enchantingoverhauled.config.Config;
import johnsmith.enchantingoverhauled.lib.EnchantmentLib;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.DamageEnchantment;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(DamageEnchantment.class)
public class DamageEnchantmentMixin {

    @Shadow
    @Final
    private Optional<TagKey<EntityType<?>>> targets;

    /**
     * Overrides compatibility checks to allow stacking different damage enchantments
     * (e.g., Sharpness + Smite), preventing only the exact same enchantment instance.
     */
    @Inject(method = "checkCompatibility", at = @At("HEAD"), cancellable = true)
    public void checkCompatibility(Enchantment other, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(other != (Object) this);
    }

    /**
     * Swaps the supported items tag from TRIDENT_ENCHANTABLE to SHARP_WEAPON
     * during the construction of the enchantment.
     */
    @ModifyArg(method = "<init>",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/item/enchantment/Enchantment;<init>(Lnet/minecraft/world/item/enchantment/Enchantment$EnchantmentDefinition;)V"),
            index = 0)
    private static Enchantment.EnchantmentDefinition modifySupportedItems(Enchantment.EnchantmentDefinition definition) {
        // Check if the original definition targets Tridents
        if (definition.supportedItems().equals(ItemTags.TRIDENT_ENCHANTABLE)) {
            // Return a new definition that targets our custom "Sharp Weapon" tag instead
            return new Enchantment.EnchantmentDefinition(
                    ItemTags.SHARP_WEAPON_ENCHANTABLE,
                    definition.primaryItems(),
                    definition.weight(),
                    definition.maxLevel(),
                    definition.minCost(),
                    definition.maxCost(),
                    definition.anvilCost(),
                    definition.requiredFeatures(),
                    definition.slots()
            );
        }
        return definition;
    }

    /**
     * Overrides damage calculation to implement diminishing returns logic
     * and configurable multipliers for specific targets.
     */
    @Inject(method = "getDamageBonus", at = @At("HEAD"), cancellable = true)
    public void getDamageBonus(int level, @Nullable EntityType<?> entityType, CallbackInfoReturnable<Float> cir) {
        // 1. Calculate Base Damage (Sharpness logic)
        float baseDamageBonus = Config.SHARPNESS_INITIAL_DAMAGE;
        float diminishingReturns = Config.SHARPNESS_DIMINISHING_RETURNS;
        float damageBonus = 0;

        for (int i = 0; i < level; i++) {
            damageBonus += baseDamageBonus;
            // Clamp the base damage for the next level
            baseDamageBonus = Math.max(baseDamageBonus - diminishingReturns, Config.SHARPNESS_MINIMUM_DAMAGE_INCREMENT);
        }

        // 2. Apply Multipliers based on Enchantment Type
        if (this.targets.isEmpty()) {
            // Sharpness (No specific target tag) -> Apply raw calculated bonus
            cir.setReturnValue(damageBonus);
        } else {
            // Targeted Enchantments (Smite / Bane of Arthropods)
            // We only return a bonus if the entity matches the tag.
            if (entityType != null && entityType.is(this.targets.get())) {

                float multiplier = 1.0f;

                // Identify the type based on the target tag
                if (this.targets.get() == EntityTypeTags.UNDEAD) {
                    multiplier = Config.SMITE_MULTIPLIER;
                } else if (this.targets.get() == EntityTypeTags.ARTHROPOD) {
                    multiplier = Config.EXTERMINATION_MULTIPLIER;
                }

                cir.setReturnValue(damageBonus * multiplier);
            } else {
                cir.setReturnValue(0.0F);
            }
        }
    }
}