package johnsmith.mixin.enchantment;

import johnsmith.config.Config;
import johnsmith.entity.damage.DamageTypeTagRegistry;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.PiercingEnchantment;
import net.minecraft.enchantment.ProtectionEnchantment.Type;
import net.minecraft.enchantment.ProtectionEnchantment;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.registry.tag.ItemTags;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("all")
@Mixin(ProtectionEnchantment.class)
public class ProtectionEnchantmentMixin extends Enchantment {
    @Unique
    private static final EquipmentSlot[] CHEST_ONLY = new EquipmentSlot[]{EquipmentSlot.CHEST};

    @Shadow
    @Final
    public Type protectionType;

    public ProtectionEnchantmentMixin(Properties properties) {
        super(properties);
    }

    @Inject(method = "canAccept(Lnet/minecraft/enchantment/Enchantment;)Z",
                at = @At("HEAD"),
       cancellable = true)
    public void canAccept(Enchantment other, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(!(other instanceof ProtectionEnchantment protectionEnchantment && this.protectionType == protectionEnchantment.protectionType));
        return;
    }

    /**
     * Modifies the base 'Protection' (Type.ALL) enchantment's protection amount.
     * This is separate from the other multipliers because vanilla returns (level * 1).
     *
     * This mixin now ALSO restricts Type.ALL to only "physical" damage, i.e.,
     * damage that is NOT covered by the other protection types or magic.
     *
     * @param level The enchantment level.
     * @param source The damage source.
     * @param cir The callback info to cancel the method.
     */
    @Inject(method = "getProtectionAmount(ILnet/minecraft/entity/damage/DamageSource;)I", at = @At("HEAD"), cancellable = true)
    private void modifyBaseProtectionAmount(int level, DamageSource source, CallbackInfoReturnable<Integer> cir) {
        // First, check if it's the base 'Protection' type
        if (this.protectionType == ProtectionEnchantment.Type.ALL) {

            // Bypass if the source bypasses enchantments (updated tag)
            if (source.isIn(DamageTypeTags.BYPASSES_ENCHANTMENTS)) {
                cir.setReturnValue(0);
                return;
            }

            // Physical Damage check
            if (!source.isIn(DamageTypeTagRegistry.PHYSICAL_DAMAGE))
            {
                cir.setReturnValue(0);
                return;
            }

            // If we are here, it's "physical" damage.
            // Check if a valid config multiplier is set
            if (Config.PHYSICAL_PROTECTION_STRENGTH > 0) {
                // Return the new configurable amount
                cir.setReturnValue(level * Config.PHYSICAL_PROTECTION_STRENGTH);
            } else {
                // Fallback to vanilla behavior for Type.ALL (which is just 'level')
                cir.setReturnValue(level);
            }
        }

        // If it's not Type.ALL, do nothing and let the original method
        // continue to the 'if/else if' chain for Fire, Fall, etc.
    }

    /**
     * Modifies the protection multiplier for FIRE, EXPLOSION, and PROJECTILE.
     * This method is an instance mixin and uses the shadowed 'protectionType'
     * field to determine which config value to return.
     *
     * @param originalValue The original constant value (which is 2).
     * @return The new multiplier from the config.
     */
    @ModifyConstant(
            method = "getProtectionAmount(ILnet/minecraft/entity/damage/DamageSource;)I",
            constant = @Constant(intValue = 2)
    )
    private int modifyProtectionMultiplierTwo(int originalValue) {
        // Use a switch on the instance's type
        return switch (this.protectionType) {
            case FIRE -> (Config.FIRE_PROTECTION_STRENGTH > 0) ? Config.FIRE_PROTECTION_STRENGTH : originalValue;
            case EXPLOSION -> (Config.BLAST_PROTECTION_STRENGTH > 0) ? Config.BLAST_PROTECTION_STRENGTH : originalValue;
            case PROJECTILE -> (Config.PROJECTILE_PROTECTION_STRENGTH > 0) ? Config.PROJECTILE_PROTECTION_STRENGTH : originalValue;
            default -> originalValue;
        };
    }

    /**
     * Modifies the protection multiplier for FALL (Feather Falling).
     *
     * @param originalValue The original constant value (which is 3).
     * @return The new multiplier from the config.
     */
    @ModifyConstant(
            method = "getProtectionAmount(ILnet/minecraft/entity/damage/DamageSource;)I",
            constant = @Constant(intValue = 3)
    )
    private int modifyProtectionMultiplierThree(int originalValue) {
        // This injector only targets the 'level * 3' line for FALL.
        if (this.protectionType == ProtectionEnchantment.Type.FALL) {
            return (Config.FEATHER_FALLING_STRENGTH > 0) ? Config.FEATHER_FALLING_STRENGTH : originalValue;
        }
        // Fallback, though this should not be reachable
        return originalValue;
    }

    /**
     * This method captures the 'properties' argument (index 1) at the
     * HEAD of the constructor. It can also capture 'type' (index 2)
     * to perform the conditional logic.
     */
    @ModifyArg(method = "<init>(Lnet/minecraft/enchantment/Enchantment$Properties;Lnet/minecraft/enchantment/ProtectionEnchantment$Type;)V",
                        at = @At(value = "INVOKE",
                                target = "Lnet/minecraft/enchantment/Enchantment;<init>(Lnet/minecraft/enchantment/Enchantment$Properties;)V"),
                index = 0)
    private static Enchantment.Properties modifyProtectionSlots(Enchantment.Properties properties) {

        // Check if it's Feather Falling. If so, return the original properties.
        if (properties.supportedItems() == ItemTags.FOOT_ARMOR_ENCHANTABLE) {
            return properties;
        }

        // It's Physical Protection, Fire Protection, Blast Protection, Projectile Protection, or Magic Protection.
        return new Enchantment.Properties(
                ItemTags.CHEST_ARMOR_ENCHANTABLE, // Only allow on items with this tag
                properties.primaryItems(),
                properties.weight(),
                properties.maxLevel(),
                properties.minCost(),
                properties.maxCost(),
                properties.anvilCost(),
                properties.requiredFeatures(),
                CHEST_ONLY // Apply only to the chest slot
        );
    }
}
