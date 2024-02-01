package de.dafuqs.matchbooks.recipe;

import de.dafuqs.matchbooks.Matchbooks;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class MatchbookRecipeTypes {

    public static void init() {
        Registry.register(Registries.RECIPE_SERIALIZER, Matchbooks.id("item_damaging"), ItemDamagingRecipe.Serializer.INSTANCE);
    }
}
