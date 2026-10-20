package johnsmith.enchantingoverhauled.data.datagen;

import johnsmith.enchantingoverhauled.api.enchantment.school.MagicSchools;
import johnsmith.enchantingoverhauled.api.enchantment.spellfield.AiSpellFields;

import johnsmith.enchantingoverhauled.data.datagen.AiSpellFieldRegistryBootstrap;
import johnsmith.enchantingoverhauled.data.datagen.EnchantmentRegistryBootstrap;
import johnsmith.enchantingoverhauled.data.datagen.MagicSchoolRegistryBootstrap;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;

import org.jetbrains.annotations.NotNull;

public class FabricDataGeneratorEntrypoint implements DataGeneratorEntrypoint {

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();

        pack.addProvider((output, registries) -> new FabricDynamicRegistryProvider(output, registries) {
            @Override
            protected void configure(HolderLookup.Provider registries, Entries entries) {
                entries.addAll(registries.lookupOrThrow(Registries.ENCHANTMENT));
                entries.addAll(registries.lookupOrThrow(AiSpellFields.REGISTRY_KEY));
            }

            @Override
            public @NotNull String getName() {
                return "Enchanting Overhauled Dynamic Registries";
            }
        });
    }

    @Override
    public void buildRegistry(RegistrySetBuilder registryBuilder) {
        registryBuilder.add(Registries.ENCHANTMENT, EnchantmentRegistryBootstrap::bootstrap);
        registryBuilder.add(MagicSchools.REGISTRY_KEY, MagicSchoolRegistryBootstrap::bootstrap);
        registryBuilder.add(AiSpellFields.REGISTRY_KEY, AiSpellFieldRegistryBootstrap::bootstrap);
    }
}