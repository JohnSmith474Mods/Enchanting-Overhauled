### Added

* **Forge Support & Core Library Split**:

    * Added full support for the **Forge** mod loader alongside **Fabric** and **NeoForge**.

    * Extracted the configuration framework and core enchantment/spell-field systems into standalone dependencies: **Config Overhauled** and **Enchantment Core**.

* **Scribe's Rise Structure**:

    * Added a new, procedurally generated multi-story Overworld tower structure (`enchanting_overhauled:scribes_rise`) spawning in mountain, hill, and forest biomes (`#enchanting_overhauled:has_structure/scribes_rise`).

    * Added the `enchanting_overhauled:updating_jigsaw` structure type, which performs post-placement neighbor shape updates across jigsaw boundaries so stairs, walls, fences, and glass panes connect seamlessly.

    * Features 5 procedural degradation states (`pristine`, `weathered`, `damaged`, `collapsed`, `ruined`) across modular sections (`foundation`, `balcony`, `gallery`, `habitat`, `roof`) and 7 structure loot tables.

* **Scribing Lectern Mini-Game**:

    * Interacting with a Lectern holding an `Enchanted Book` or `Enchanted Tome` now opens the **Scribe** menu, allowing players to scribe enchantments from catalyst sources (primarily `Enchanted Page` items) into the book on the Lectern using `#enchanting_overhauled:enchanting_fuel` (Lapis Lazuli).

    * Added two selectable minigame variants (`scribe_minigame_variant`):

        * `CLASSIC`: Standard Minesweeper board generation after the first revealed cell.

        * `SUPERPOSITION`: A quantum constraint-solving Minesweeper variant that dynamically resolves mine placements asynchronously to forgive unavoidable 50/50 guesses while punishing unforced guesses.

    * Added configurable conversion rules (`tome_conversion_upgrade_condition` and `tome_conversion_append_condition`) that transform an `Enchanted Book` on a Lectern into an `Enchanted Tome` when upgrading or appending enchantments.

    * Added extensive configuration options for lethal failure explosions (`lethal_scribing`), source consumption/refund policies on abort (`abortion_refund_policy`, `refund_enchant_fuel`), mine scaling per enchantment level (`scribe_mine_scalar`), fuel cost scaling (`scribe_fuel_cost_scalar`), and XP rewards (`scribe_xp_reward_scalar`).

    * Lecterns now synchronize their held book to the client and dynamically render custom 3D `Enchanted Book` and `Enchanted Tome` models with enchantment glint.

* **Enchanted Tome & Enchanted Book Entities**:

    * Added the **Enchanted Tome** (`enchanting_overhauled:enchanted_tome`) and **Enchanted Book** (`enchanting_overhauled:enchanted_book`) flying hostile mobs, complete with Spawn Eggs, custom animations, and custom loot tables:

        * **Spawning**: Enchanted Tomes spawn in Enchanted Libraries and Stronghold Libraries (persisting indefinitely and immune to fire and explosions). Enchanted Books spawn naturally in `#enchanting_overhauled:spawns_enchanted_book` biomes, Stronghold Libraries, or when summoned by an Enchanted Tome.

        * **Roosting & Altar Deception**: Idle Enchanted Tomes roost atop Deactivated or Disturbed Enchanting Altars to mimic a normal table book before awakening when players linger nearby. Idle Enchanted Books roost on empty Lecterns and Bookshelves, and summoned minions shelve themselves back into bookshelves once combat de-escalates.

        * **Armor Pacification & Cover Shielding**: Unprovoked flying books ignore players wearing enchanted armor. When their covers are closed, flying books gain bonus armor and deflect projectiles; striking them while their pages are wide open (such as while chanting) doubles the chance to shed `Enchanted Page` drops.

        * **Minion Summoning**: Enchanted Tomes approach nearby bookshelves and chant to summon `Enchanted Book` minions with matching or cycle-sibling Schools of Magic and Themes.

        * **Telekinetic Bartering**: Players can toss or right-click enchanting fuel and an enchantable item or recipe catalyst (`Book`, `Paper`, etc.) onto an unprovoked Enchanted Tome to barter for enchanted equipment, pages, books, or tomes matching the Tome's Theme and School.

* **AI Spellcasting, Magic Missiles & Mirage Decoys**:

    * Added the data-driven `ai_spell_field` registry (`data/<namespace>/ai_spell_field/`), enabling flying books to cast offensive, defensive, and supportive spells gated by world difficulty (`EASY`, `NORMAL`, `HARD`) and `mobGriefing`, weighted by their Enchantment Theme and School of Magic affinities.

    * Added 9 default AI spells across the 9 Schools of Magic: `deflect_projectiles` (*Abjuration*), `detonate` (*Obliteration*), `heal` (*Perpetuation*), `illusory_copy` (*Deception*), `hold` (*Coercion*), `forecast_lightning` (*Divination*), `icy_tomb` (*Conjuration*), `gust` (*Translation*), and `sinkhole` (*Transmutation*).

    * Added the **Magic Missile** projectile entity (`enchanting_overhauled:magic_missile`) and damage type, featuring headshot critical hit detection and impact spell-field deployment.

    * Added the **Mirage** entity (`enchanting_overhauled:mirage`) and `enchanting_overhauled:summon_mirage` spell field effect, which summons an orbital formation of illusory decoys that mirror their caster's model and animations, redirect hostile mob aggro, and shatter upon taking damage.

    * Added the `enchanting_overhauled:difficulty` level-based value type (`DifficultyBasedValue`) and `enchanting_overhauled:caster_hostile` loot condition (`CasterHostileCondition`).

    * Added custom tintable Standard Galactic Alphabet rune particles (`enchanting_overhauled:enchant`).

* **Schools of Magic & Dynamic Arcane Naming**:

    * Added the data-driven `magic_school` registry (`data/<namespace>/magic_school/`), enchantment school tags (`#enchanting_overhauled:school/*`), and magic cycle tags (`#enchanting_overhauled:cycle/vitality`, `cognition`, and `material`).

    * Added 9 default Schools of Magic (`abjuration`, `obliteration`, `perpetuation`, `deception`, `coercion`, `divination`, `conjuration`, `translation`, `transmutation`) plus the `aberration` fallback school.

    * `Enchanted Book`, `Enchanted Page`, and `Enchanted Tome` items (as well as flying book entities) now dynamically format and color their display names based on their dominant Enchantment Theme prefix and Magic School suffix (e.g., `"<Theme> <Item> of <School>"`).

    * Added `AdminSchoolValidator` to automatically notify server operators upon login or datapack reload if any registered enchantments are unassigned (`Aberration`) or assigned to multiple Schools of Magic.

* **Enchanted Page Item**:

    * Added the **Enchanted Page** (`enchanting_overhauled:enchanted_page`), a fleeting enchantment source (`#enchanting_overhauled:enchantment_source/fleeting`) that holds a single enchantment at its minimum level and reverts to `minecraft:paper` upon consumption.

* **Enchant Recipes**:

    * Added the data-driven `enchant_recipe` registry (`data/<namespace>/enchant_recipe/`) to define item transmutations when enchantments are applied (`stored`, `applied`, or `void` attachment) and reverse transmutations in Grindstones or upon source depletion.

    * Added built-in recipes for `Book -> Enchanted Book` and `Paper -> Enchanted Page`, plus optional built-in datapacks for `Golden Apple -> Enchanted Golden Apple` (`enchantable_golden_apples`) and `Splash Potion -> Bottle o' Enchanting` (`enchantable_potions`).

* **Librarian Villager Trade Overhaul**:

    * Added configurable Librarian villager trades for `Enchanted Page` and `Enchanted Tome` items, with settings for profession levels, emerald cost bounds, secondary item costs, villager XP, max stock, restocking rules, and an option to disable default `Enchanted Book` trades at specified levels.

* **Custom Loot Functions**:

    * Added `enchanting_overhauled:enchant_randomly`, `enchanting_overhauled:enchant_fleeting_source`, `enchanting_overhauled:enchant_source`, and `enchanting_overhauled:enchant_persistent_source` loot functions.

* **Structure Processors**:

    * Added `enchanting_overhauled:rule` (supporting property copying, deterministic block-pos seeding, and `constant`, `weighted`, and `gradient` outputs with `linear`, `sigmoid`, `logarithmic`, `polynomial`, and `exponential` curves) and `enchanting_overhauled:processor_list` (for chaining multiple processor lists).

### Reworked

* **Enchanted Library Structure**:

    * Reworked the former biome-split `library_ruins` structures into a unified **Enchanted Library** (`enchanting_overhauled:enchanted_library`) structure generating in `#enchanting_overhauled:has_structure/enchanted_library` biomes.

    * Replaced legacy `block_age` and `desertify` processors with modular `pristine`, `weathered`, `damaged`, `collapsed`, and `ruined` template pools and gradient rule processors.

    * Replaced the previous Evoker structure spawn with `enchanting_overhauled:enchanted_tome`.

* **Enchanting Altar & Enchant Menu**:

    * Refactored the Enchanting Altar menu and screen architecture with dedicated custom network synchronization (`EnchantmentMenuSyncPayload`).

    * Enchantment generation now evaluates each enchantment's `min_cost` and `max_cost` (clamped by configurable global `min_cost` and `max_cost` bounds) against available enchanting power to determine eligibility and deterministic per-slot XP requirements.

    * Increased default maximum enchanting power from `25` to `50` and made it configurable (`max_enchanting_power`).

    * Added a configurable enchanting power halving mechanic (`enchant_power_halving_type`: `NONE`, `PER_TABLE_ROLL`, `PER_SLOT`) across progressive slots.

    * The target item's base enchantability (or its `EnchantRecipe` enchantability) is now added to the available enchanting power.

    * Added configurable upgrade catalyst requirements (`upgrade_source_type`: `NONE`, `EQUAL`, `GREATER`) and conflict resolution priority (`upgrade_priority_type`: `TRANSFER`, `UPGRADE`) when a source item has a higher level than the target item.

    * Selective catalyst consumption: persistent sources (`#enchanting_overhauled:enchantment_source/persistent`) are preserved, fleeting sources (`#enchanting_overhauled:enchantment_source/fleeting`) revert to their base item, and multi-enchantment books now only lose the specific transferred/upgraded enchantment rather than the entire book.

    * Added marquee text scrolling to enchantment buttons and page lists for overflowing text.

    * Enchanting Table block entities now persist their stored `Enchanted Tome` item stack to NBT and synchronize it to clients.

* **Enchantment Themes**:

    * Added the **Corrupted** (Sculk) theme (`enchanting_overhauled:corrupted`), which contains `minecraft:swift_sneak` and is powered by placing a `minecraft:sculk_catalyst` near an activated Enchanting Altar.

    * Renamed `default` to `common`, `magic` to `arcane`, `nether` to `infernal`, `soul` to `numinous`, `trial` to `ominous`, and `force` to `volatile`.

    * Added `minecraft:structure_void` as a debug block that grants maximum enchanting power across all themes.

* **Enchantment Tooltips**:

    * Rebuilt tooltip formatting via Enchantment Core's `EnchantmentTooltipFormatter` API, adding dynamic `precise` and `vague` descriptions that evaluate live level-based values, themes, and levels in real time.

    * Added granular configuration for obfuscation (`obfuscation_level`: `NONE`, `FLEETING`, `UNTAGGED`, `FULL`), curse concealment (`curse_concealment_mode`: `NONE`, `APPLIED`, `STORED`, `ALL`), section headers (`enchantment_header_mode`), theme/level coloring modes (`coloring_mode`), GUI format application (`format_application_mode`), level-1 numeral display (`show_first_level`), line wrapping width, and indentation depth.

* **Anvil & Grindstone Mechanics**:

    * Added configuration options to toggle Anvil enchanting (`enchanting`), toggle Anvil XP costs (`xp_cost`), and customize the "Too Expensive!" limit (`max_xp_cost`).

    * Updated Anvils and Grindstones to support `EnchantRecipe` transmutations (and reverse transmutations in Grindstones while preserving curses), custom `STORED_ENCHANTMENTS` items, persistent enchantment sources, and overleveled enchantments.

* **Built-in Data & Resource Packs**:

    * Separated built-in content into 13 modular built-in datapacks and 1 built-in resource pack, with their default inclusion behavior each individually configurable in the config (`PackInclusionType`: `REQUIRED`, `ACTIVE`, `OPTIONAL`).

    * Split adapted vanilla enchantments into `adapted_enchantments` (active by default) and `configurable_adapted_enchantments` (optional).

* **Loot Injection**:

    * Removed the hardcoded loot chance configuration properties and platform-specific loot modifiers in favor of a unified, data-driven `loot_injection` registry (`data/<namespace>/loot_injection/`) that supports injecting arbitrary nested loot tables into target loot tables.

* **Adapted Enchantments & Experience**:

    * Updated `Infinity` to use the `#enchanting_overhauled:all_ammunition` tag (supporting both arrows and firework rockets) and `Multishot` to include a level-based probability roll for additional projectiles.

    * Increased the default maximum player XP level (`xp_max_level`) from `100` to `474`.

### Removed

* Removed the custom `enchanting_overhauled:magic_protection` and `enchanting_overhauled:physical_protection` enchantments and the custom protection damage calculation formula (`CombatRulesMixin`).

* Removed the legacy `enchanting_overhauled:block_age` and `enchanting_overhauled:desertify` structure processors (superseded by `enchanting_overhauled:rule` and `enchanting_overhauled:processor_list`).