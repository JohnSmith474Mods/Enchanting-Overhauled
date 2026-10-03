# Enchanting Overhauled

Enchanting Overhauled is a cross-platform modification for Minecraft. This mod restructures the enchanting system and relies on the `Enchantment Core` and `Config Overhauled` framework for Fabric, Forge, and NeoForge environments.

Enchanting Overhauled reimagines Minecraft's magic system to remove RNG and make progression deliberate. It replaces the obsolete Enchanting Table with a deterministic, multi-stage Enchanting
Altar and introduces persistent, highly potent Enchanted Tomes found exclusively through exploration. The modification comprehensively rebalances the entire arcane ecosystem—including strict capacity limits,
streamlined experience scaling, thematic arcane gating, and zero-cost mundane anvil repairs.

## Mod Versioning Schema

Releases follow a strict `[API]`.`[FEATURE]`.`[PATCH]` format (e.g., `2.7.3`).

* **`API`:** Increments when structurally incompatible changes are introduced. Required `API` versions for dependent mods must match exactly.
* **`FEATURE`:** Increases for new content and tools without breaking existing compatibility. Modpack configurations can update to newer `FEATURE` versions safely.
* **`PATCH`:** Designates internal bug fixes and corrections. Enforces no strict compatibility guidelines.

The `API` version must match exactly for dependencies, the `FEATURE` version is backwards compatible, and the `PATCH` version is freely selectable.

## Repository Structure & Branching

This default branch functions as the central management hub. It hosts structural documentation, license details, and GitHub workflow configurations. It contains no source code.

Source code resides exclusively in dedicated, version-specific branches. A linear progression model is utilized: a modification or bug fix is integrated into the oldest applicable branch and successively merged upward into all newer branches.

## Contributions & Pull Requests

Target pull requests to the **oldest active version branch** relevant to the change. Forward-merging transfers these changes into newer versions.

All contributions require a signed Contributor License Agreement (CLA). The automated workflow prompts for signature upon the initial pull request. Review the [CLA](CLA.md) prior to writing code.

Code changes submitted to the management branch will be rejected.

## Version Support Matrix

| Branch | Game Version Range | Support Status |
|-------| ------------------ |----------------|
| 1.21  | 1.21 - 1.21.1      | WIP            |
| 1.21.2 | 1.21.2 - 1.21.3    | WIP       |
| 1.21.4 | 1.21.4             | WIP       |
| 1.21.5 | 1.21.5             | WIP       |
| 1.21.6 | 1.21.6 - 1.21.8    | WIP       |
| 1.21.9 | 1.21.9 - 1.21.10   | WIP       |
| 1.21.11 | 1.21.11            | WIP       |
| 26.1  | 26.1 - 26.1.2      | WIP       |
| 26.2  | 26.2               | WIP       |

## Third-Party Licenses & Credits

This project relies on structural templates.

* **[MultiLoader-Template](https://github.com/jaredlll08/MultiLoader-Template)** by [jaredlll08](https://modrinth.com/user/jaredlll08) is utilized as the structural foundation for the cross-platform build environment under the CC0 1.0 Universal license.
- The isometric renders were made using [glisco](https://modrinth.com/user/glisco)'s [Isometric Renders](https://modrinth.com/mod/isometric-renders) mod.
