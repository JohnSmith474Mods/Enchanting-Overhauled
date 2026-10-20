package johnsmith.enchantingoverhauled.api.tag;

import johnsmith.enchantingoverhauled.Constants;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;

/**
 * Central holder for custom {@link TagKey} definitions used by Enchanting Overhauled
 * across items, blocks, entity types, and biomes.
 */
public class Tags {

    /**
     * Item tag identifying items that can be used as enchanting fuel in the Enchanting Altar,
     * Scribe Menu, and Tome Bartering (e.g., Lapis Lazuli).
     * <p>
     * Tag ID: {@code #enchanting_overhauled:enchanting_fuel}
     */
    public static final TagKey<Item> ENCHANTING_FUEL = TagKey.create(
            Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "enchanting_fuel")
    );

    /**
     * Item tag identifying persistent enchantment sources (such as Enchanted Tomes) that are
     * not consumed when transferring or scribing enchantments and are exempt from default obfuscation.
     * <p>
     * Tag ID: {@code #enchanting_overhauled:enchantment_source/persistent}
     */
    public static final TagKey<Item> PERSISTENT_ENCHANTMENT_SOURCE = TagKey.create(
            Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "enchantment_source/persistent")
    );

    /**
     * Item tag identifying fleeting enchantment sources (such as Enchanted Pages) that hold a single
     * minimum-level enchantment and are completely consumed/reverted upon use.
     * <p>
     * Tag ID: {@code #enchanting_overhauled:enchantment_source/fleeting}
     */
    public static final TagKey<Item> FLEETING_ENCHANTMENT_SOURCE = TagKey.create(
            Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "enchantment_source/fleeting")
    );

    /**
     * Item tag grouping all melee weapons and tools eligible for melee damage enchantments
     * (such as Sharpness, Smite, Bane of Arthropods, and Impaling).
     * <p>
     * Tag ID: {@code #enchanting_overhauled:enchantable/all_melee}
     */
    public static final TagKey<Item> ALL_MELEE = TagKey.create(
            Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "enchantable/all_melee")
    );

    /**
     * Item tag grouping all ranged weapons (such as Bows and Crossbows) eligible for ranged
     * enchantments (such as Power, Punch, Flame, Infinity, Multishot, Piercing, and Quick Charge).
     * <p>
     * Tag ID: {@code #enchanting_overhauled:enchantable/all_ranged}
     */
    public static final TagKey<Item> ALL_RANGED = TagKey.create(
            Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "enchantable/all_ranged")
    );

    /**
     * Item tag grouping all ammunition items (such as Arrows and Firework Rockets) affected
     * by the adapted Infinity enchantment.
     * <p>
     * Tag ID: {@code #enchanting_overhauled:all_ammunition}
     */
    public static final TagKey<Item> ALL_AMMUNITION = TagKey.create(
            Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "all_ammunition")
    );

    /**
     * Block tag identifying blocks that flying arcane books ({@link johnsmith.enchantingoverhauled.entity.EnchantedBookEntity}
     * and {@link johnsmith.enchantingoverhauled.entity.EnchantedTomeEntity}) seek out to roost on or shelve into when idle.
     * <p>
     * Tag ID: {@code #enchanting_overhauled:enchanted_book_idle_target}
     */
    public static final TagKey<Block> ENCHANTED_BOOK_IDLE_TARGET = TagKey.create(
            Registries.BLOCK,
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "enchanted_book_idle_target")
    );

    /**
     * Entity type tag identifying entities that cannot be hit or damaged by
     * {@link johnsmith.enchantingoverhauled.entity.projectile.MagicMissileEntity} projectiles.
     * <p>
     * Tag ID: {@code #enchanting_overhauled:immune_to_magic_missile}
     */
    public static final TagKey<EntityType<?>> IMMUNE_TO_MAGIC_MISSILE = TagKey.create(
            Registries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "immune_to_magic_missile")
    );

    /**
     * Entity type tag identifying entities treated as allies by Enchanted Tomes and Enchanted Books
     * for targeting, rallying, and supportive spellcasting.
     * <p>
     * Tag ID: {@code #enchanting_overhauled:enchanted_tome_allies}
     */
    public static final TagKey<EntityType<?>> ENCHANTED_TOME_ALLIES = TagKey.create(
            Registries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "enchanted_tome_allies")
    );

    /**
     * Entity type tag identifying neutral or passive entities that Enchanted Tomes and Enchanted Books
     * will not target or provoke against, and that are protected from offensive AI spell fields.
     * <p>
     * Tag ID: {@code #enchanting_overhauled:enchanted_tome_neutral}
     */
    public static final TagKey<EntityType<?>> ENCHANTED_TOME_NEUTRAL = TagKey.create(
            Registries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "enchanted_tome_neutral")
    );

    /**
     * Entity type tag identifying entities (such as Creepers, Bosses, and Golems) that flying books
     * avoid hitting as collateral damage when casting area-of-effect spells.
     * <p>
     * Tag ID: {@code #enchanting_overhauled:enchanted_tome_avoid_collateral}
     */
    public static final TagKey<EntityType<?>> ENCHANTED_TOME_AVOID_COLLATERAL = TagKey.create(
            Registries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "enchanted_tome_avoid_collateral")
    );

    /**
     * Biome tag identifying biomes where {@link johnsmith.enchantingoverhauled.entity.EnchantedBookEntity}
     * mobs can naturally spawn.
     * <p>
     * Tag ID: {@code #enchanting_overhauled:spawns_enchanted_book}
     */
    public static final TagKey<Biome> SPAWNS_ENCHANTED_BOOK = TagKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "spawns_enchanted_book")
    );
}