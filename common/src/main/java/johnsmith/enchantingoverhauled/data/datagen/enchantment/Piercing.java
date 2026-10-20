package johnsmith.enchantingoverhauled.data.datagen.enchantment;

import johnsmith.enchantingoverhauled.api.tag.Tags;

import johnsmith.enchantmentcore.api.enchantment.value.PointerValue;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.AddValue;

import java.util.function.Function;

public class Piercing {
    public static final Function<BootstrapContext<Enchantment>, Enchantment.EnchantmentDefinition> DEFINITION = (context) -> Enchantment.definition(
            context.lookup(Registries.ITEM).getOrThrow(Tags.ALL_RANGED),
            10,
            AdaptedEnchantments.ENCHANTMENT_MAX_LEVEL,
            Enchantment.dynamicCost(1, 10),
            Enchantment.constantCost(50),
            1,
            EquipmentSlotGroup.MAINHAND
    );

    public static final Function<BootstrapContext<Enchantment>, Enchantment> ENCHANTMENT = (context) ->
            Enchantment.enchantment(Piercing.DEFINITION.apply(context))
                    .withEffect(EnchantmentEffectComponents.PROJECTILE_PIERCING, new AddValue(new PointerValue("piercing.projectile_piercing", LevelBasedValue.perLevel(1.5F))))
                    .build(AdaptedEnchantments.PIERCING.location());
}