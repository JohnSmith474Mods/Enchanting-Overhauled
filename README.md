# Enchanting Overhauled

Enchanting Overhauled is a comprehensive magic progression overhaul for Minecraft across Fabric, Forge, and NeoForge environments. Built atop [Enchantment Core](https://modrinth.com/mod/enchantment-core) and [Config Overhauled](https://modrinth.com/mod/config-overhauled), it replaces vanilla RNG-based enchanting with modular altars, lectern scribing mechanics, autonomous spellcasting book entities, and exploration-driven structures.

While primarily a gameplay overhaul rather than a standalone library, Enchanting Overhauled exposes a suite of **data-driven mod registries** alongside **reusable general-purpose worldgen and loot utilities** (such as spatial gradient structure processors, neighbor-updating jigsaw structures, and cross-platform loot table injection). All registries and extensions are governed by [DataFixerUpper](https://github.com/Mojang/DataFixerUpper) (DFU) codecs and can be extended via standard Minecraft datapacks without writing Java code.

## Quickstart Guide 1.21.5

Welcome to the Enchanting Overhauled Quickstart Guide for add-on and modpack developers!

If you are new to the project's data-driven systems, we highly recommend reviewing the full schema documentation on our project [Wiki](https://github.com/JohnSmith474Mods/Enchanting-Overhauled/wiki) first. It provides exhaustive field references, built-in datapack breakdowns, and advanced configuration examples.

### Installation

For add-on developers integrating against Enchanting Overhauled's registries, custom value providers, or worldgen/loot APIs, add the Modrinth Maven repository and the mod dependencies to `build.gradle`. Replace `[MOD_VERSION]`, `[GAME_VERSION]`, and `[PLATFORM]` with the target release versions.

```groovy
repositories {
    maven {
        name = "Modrinth"
        url = "https://api.modrinth.com/maven"
    }
}

dependencies {
    modImplementation "maven.modrinth:enchanting-overhauled:[MOD_VERSION]+[GAME_VERSION]-[PLATFORM]"

    // Required transitive library dependencies
    modImplementation "maven.modrinth:enchantment-core:[EC_VERSION]+[GAME_VERSION]-[PLATFORM]"
    modImplementation "maven.modrinth:config-overhauled:[CO_VERSION]+[GAME_VERSION]-[PLATFORM]"
}
```

# Data-Driven Mod Registries

Enchanting Overhauled registers several custom dynamic datapack registries under the `enchanting_overhauled` namespace. Custom entries placed inside any active datapack are automatically loaded and synchronized on world boot and `/reload`.

### Enchantment Themes (`enchanting_overhauled:enchantment_theme`)

Enchantment Themes gate specialized enchantments behind environmental catalysts placed near an activated Enchanting Altar (such as `arcane`, `infernal`, `numinous`, `ominous`, `volatile`, and `corrupted`).

- **Definition Path:** `data/<namespace>/enchantment_theme/<theme_id>.json`
- **Assignment Tag:** `data/<namespace>/tags/enchantment/theme/<theme_id>.json`
- **Capabilities:** Define altar power providers, block-state or inventory-based power bonuses (`add_if_block_state`, `multiply_by_block_state`, `count_items_in_inventory`), theme display colors, and ambient particle/sound feedback.

To assign third-party or custom enchantments to an existing theme, append them to the corresponding enchantment tag:

```json
{
  "replace": false,
  "values": [
    "example_mod:custom_enchantment"
  ]
}
```

### Magic Schools (`enchanting_overhauled:magic_school`)

Magic Schools categorize enchantments into nine arcane disciplines (`abjuration`, `obliteration`, `perpetuation`, `deception`, `coercion`, `divination`, `conjuration`, `translation`, `transmutation`, plus the `aberration` fallback) that drive dynamic item naming (`"<Theme> <Item> of <School>"`) and flying book spell affinities.

- **Definition Path:** `data/<namespace>/magic_school/<school_id>.json`
- **Assignment Tag:** `data/<namespace>/tags/enchantment/school/<school_id>.json`
- **Cycle Tags:** `data/enchanting_overhauled/tags/magic_school/cycle/{vitality,cognition,material}.json`

When an operator logs in or runs `/reload`, `AdminSchoolValidator` automatically audits the registry and reports any enchantments left unassigned (`Aberration`) or bound to conflicting schools.

### Enchant Recipes (`enchanting_overhauled:enchant_recipe`)

Enchant Recipes define data-driven item transmutations when enchantments are inscribed, depleted, or disenchanted in a Grindstone (such as `minecraft:book` $\rightarrow$ `minecraft:enchanted_book` or `minecraft:paper` $\rightarrow$ `enchanting_overhauled:enchanted_page`).

- **Definition Path:** `data/<namespace>/enchant_recipe/<recipe_id>.json`
- **Attachment Modes:** Supports `stored`, `applied`, and `void` enchantment attachment behaviors, custom base enchantability overrides, and automatic reverse transmutation upon source depletion or disenchantment.

### AI Spell Fields (`enchanting_overhauled:ai_spell_field`)

Autonomous `Enchanted Book` and `Enchanted Tome` entities cast spells built directly on [Enchantment Core](https://modrinth.com/mod/enchantment-core)'s spatial `spell_field` engine.

- **Definition Path:** `data/<namespace>/ai_spell_field/<spell_id>.json`
- **Targeting & Gating:** Configure `SpellRole`, `SpellTarget`, `CastType`, `DifficultyGate` (`EASY`, `NORMAL`, `HARD`), `mobGriefing` checks, and weighted affinities tied to the caster's dominant Theme and Magic School.
- **Mod-Specific Extensions:**
    - `enchanting_overhauled:difficulty`: A `LevelBasedValue` provider that scales numerical parameters by world difficulty.
    - `enchanting_overhauled:summon_mirage`: A `SpellFieldEffect` that spawns orbital illusory decoys mirroring the caster's model and redirecting mob aggro.
    - `enchanting_overhauled:caster_hostile`: A loot condition checking whether the casting entity is currently hostile.

### Item & Entity Tags

Customize item progression, lectern scribing, and entity interactions through standard data tags under `data/enchanting_overhauled/tags/`:

- **Enchantment Sources:** `#enchanting_overhauled:enchantment_source/persistent` (retained after application, e.g., Tomes) and `#enchanting_overhauled:enchantment_source/fleeting` (reverts to base item after single-tier application, e.g., Pages).
- **Altar & Lectern Catalysts:** `#enchanting_overhauled:enchanting_fuel` (default: `minecraft:lapis_lazuli`) and `#minecraft:lectern_books` (items permitted in the Scribing Lectern menu).
- **Entity Behavior:** `#enchanting_overhauled:enchanted_book_idle_target` (roosting blocks), `#enchanting_overhauled:enchanted_tome_allies`, `#enchanting_overhauled:enchanted_tome_neutral`, and `#enchanting_overhauled:immune_to_magic_missile`.

# Reusable Worldgen & Loot APIs

Enchanting Overhauled bundles general-purpose, multi-loader utilities for worldgen and loot table manipulation that can be used independently in modpacks and add-on mods.

### Cross-Platform Loot Injection (`enchanting_overhauled:loot_injection`)

Replaces loader-specific Global Loot Modifiers (Forge/NeoForge) and `LootTableEvents` (Fabric) with a unified, data-driven registry that injects arbitrary nested loot tables into any target loot table across all three loaders.

- **Definition Path:** `data/<namespace>/loot_injection/<injection_id>.json`
- **Custom Loot Functions:** Includes `enchanting_overhauled:enchant_randomly`, `enchanting_overhauled:enchant_source`, `enchanting_overhauled:enchant_fleeting_source`, and `enchanting_overhauled:enchant_persistent_source` for populating containers with themed books, pages, and tomes.

```json
{
  "target": "minecraft:chests/stronghold_library",
  "inject": "example_mod:inject/custom_arcane_loot"
}
```

### Structure Processors

Enchanting Overhauled registers flexible structure processors under the `enchanting_overhauled` namespace for procedural structure degradation, rule-based block replacement, and list composition:

- **`enchanting_overhauled:rule`:** Evaluates block-state rules with property copying, deterministic block-position seeding, and pluggable output providers:
    - `constant`: Always resolves to a single block state.
    - `weighted`: Selects from weighted block-state entries.
    - `gradient`: Evaluates spatial interpolation along a `SpatialVector` using `linear`, `sigmoid`, `logarithmic`, `polynomial`, or `exponential` curves—ideal for weathering towers from pristine bases to ruined roofs.
- **`enchanting_overhauled:processor_list`:** Chains multiple existing `minecraft:worldgen/processor_list` definitions sequentially inside a single processor entry.
- **`enchanting_overhauled:overgrowth` & `enchanting_overhauled:vines`:** Procedural vegetation and moss placement processors.

### Updating Jigsaw Structures (`enchanting_overhauled:updating_jigsaw`)

Standard jigsaw structures can leave stairs, walls, fences, and glass panes disconnected across piece boundaries. The `enchanting_overhauled:updating_jigsaw` structure type extends vanilla jigsaw generation by executing post-placement neighbor shape updates across jigsaw boundaries so architectural trim connects seamlessly.

# Modular Built-in Packs & Tooltip Integration

### Modular Datapacks

Built-in content is decoupled into 13 modular datapacks and 1 resource pack (`DataPacks` / `ResourcePacks`), each individually toggleable in `config/enchanting_overhauled.toml` via `PackInclusionType` (`REQUIRED`, `ACTIVE`, or `OPTIONAL`):

- **Gameplay & Mechanics:** `enchanting_altar`, `scribe_menu`, `adapted_enchantments`, `configurable_adapted_enchantments`, `enchantable_golden_apples`, `enchantable_potions`
- **Themes, Schools & AI:** `default_enchantment_themes`, `default_enchantment_schools`, `ai_spells`
- **Worldgen & Loot:** `enchanted_library_structure`, `scribes_rise_structure`, `enchanted_page_loot_injection`, `enchanted_tome_loot_injection`

### Dynamic Tooltip Descriptions

Enchantment tooltips are rendered via [Enchantment Core](https://modrinth.com/mod/enchantment-core)'s `EnchantmentTooltipFormatter` API (`OverhauledTooltipFormatter`). Custom enchantments can define both `precise` (numeric) and `vague` (qualitative) description keys in `en_us.json` using `<val:pointer>` and `<array:pointer>` interpolation tags.