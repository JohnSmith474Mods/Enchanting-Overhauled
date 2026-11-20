# Enchanting Overhauled

Welcome to Enchanting Overhauled, a free-to-use multi-loader Minecraft mod that completely restructures the enchanting system! It relies on the [Enchantment Core](https://modrinth.com/mod/enchantment-core) and [Config Overhauled](https://modrinth.com/mod/config-overhauled) frameworks to deliver a unified experience across Fabric, Forge, and NeoForge environments.

Enchanting Overhauled reimagines Minecraft's magic system to eliminate RNG and make progression deliberate. It replaces the obsolete Enchanting Table with a deterministic, multi-stage Enchanting Altar and introduces persistent, highly potent Enchanted Tomes found exclusively through exploration. The mod comprehensively rebalances the entire arcane ecosystem—including strict capacity limits, streamlined experience scaling, thematic arcane gating, and zero-cost mundane anvil repairs.

## Mod Versioning Schema

To keep things predictable, our releases follow a strict `[API]`.`[FEATURE]`.`[PATCH]` format (for example, `2.7.3`). Understanding this schema is crucial for keeping your modpacks and dependent mods compatible:

- **`API` :** This number increments whenever structurally incompatible changes are introduced. If you're developing an addon or dependent mod, your required `API` version must match identically!
- **`FEATURE` :** This number increases when we add shiny new content and tools without breaking existing compatibility. Modpack configurations and dependent setups can safely update to newer `FEATURE` versions.
- **`PATCH` :** This final number signifies minor internal bug squashing and error corrections, imposing no strict compatibility requirements at all.

To summarize: your `API` version must match identically for dependencies, the `FEATURE` version is backwards compatible, and the `PATCH` version requires no strict alignment.

## Repository Structure & Branching

This default branch is the central management hub. It hosts important structural documentation, licensing details, and GitHub workflow configurations, but you won't find any actual source code here.

Because Minecraft modding moves through a linear path with minor API changes between versions, all of our source code lives exclusively in dedicated, version-specific branches. We use a linear progression model: whenever we make a modification or bug fix, we integrate it into the earliest applicable branch and merge it upward through each successive version branch. This makes it a breeze to propagate updates and patches smoothly.

## Contributions & Pull Requests

Talk is cheap, send patches! If you're submitting a pull request, please make sure to target the **oldest active version branch** that your change applies to. Since our branches flow upwards, forward-merging will naturally carry your changes into newer versions.

Please note that all contributions require a signed Contributor License Agreement (CLA). Our automated workflow will prompt you to sign this agreement when you open your first pull request. We highly recommend reviewing the [CLA](CLA.md) before you begin any substantial work.

*A quick note:* Please ensure you do not submit code changes to this management branch, as any such pull requests will be rejected.

## Version Support Matrix

Here is a quick overview of our current branches, their support status, and the latest mod version available for each:

- **Active:** Under full active development—receiving new features, bug fixes, and occasional API changes.
- **Life Support:** Maintenance mode—receiving critical bug fixes only, with no new features or structural changes planned.
- **Abandoned:** End of life—no longer maintained and will not receive any further updates.

| Branch                                                                                              | Game Version Range | Support Status | Latest Mod Version |
| ----------------------------------------------------------------------------------------------------- | ------------------ | -------------- | ---------------- |
| [deprecated-1.20.6](https://github.com/JohnSmith474Mods/Enchanting-Overhauled/tree/deprecated-1.20.6) | 1.20.6           |   Abandoned    | 2.0.4              | 
| [1.21](https://github.com/JohnSmith474Mods/Enchanting-Overhauled/tree/1.21)                         | 1.21 - 1.21.1      | Life Support   | 3.0.0              |
| [1.21.2](https://github.com/JohnSmith474Mods/Enchanting-Overhauled/tree/1.21.2)                     | 1.21.2 - 1.21.3    | Life Support   | 3.0.0              |
| [1.21.4](https://github.com/JohnSmith474Mods/Enchanting-Overhauled/tree/1.21.4)                     | 1.21.4             | Life Support   | 3.0.0              |
| [1.21.5](https://github.com/JohnSmith474Mods/Enchanting-Overhauled/tree/1.21.5)                     | 1.21.5             | Life Support   | 3.0.0              |
| [1.21.6](https://github.com/JohnSmith474Mods/Enchanting-Overhauled/tree/1.21.6)                     | 1.21.6 - 1.21.8    | Life Support   | 3.0.0              |
| [1.21.9](https://github.com/JohnSmith474Mods/Enchanting-Overhauled/tree/1.21.9)                     | 1.21.9 - 1.21.10   | Life Support   | 3.0.0              |
| [1.21.11](https://github.com/JohnSmith474Mods/Enchanting-Overhauled/tree/1.21.11)                   | 1.21.11            | Life Support   | 3.0.0              |
| [26.1](https://github.com/JohnSmith474Mods/Enchanting-Overhauled/tree/26.1)                         | 26.1 - 26.1.2      | Active         | 3.0.0              |
| [26.2](https://github.com/JohnSmith474Mods/Enchanting-Overhauled/tree/26.2)                         | 26.2               | Active         | 3.0.0              |

## Third-Party Licenses & Credits

This project contains or relies on third-party software components and tools.

- **[MultiLoader-Template](https://github.com/jaredlll08/MultiLoader-Template)** by [jaredlll08](https://modrinth.com/user/jaredlll08) is utilized as the structural foundation for the cross-platform build environment under the CC0 1.0 Universal license.
- **[Isometric Renders](https://modrinth.com/mod/isometric-renders)** by [glisco](https://modrinth.com/user/glisco) was used to create the mod's isometric renders.