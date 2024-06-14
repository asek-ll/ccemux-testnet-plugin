package asek_ll.turtle;

import java.util.HashMap;
import java.util.Map;

public class RecipeRegistry {
    private final Map<RecipeKey, Recipe> recipes = new HashMap<>();

    public void register(Recipe recipe) {
        recipes.put(recipe.key(), recipe);
    }

    public Recipe get(RecipeKey key) {
        return recipes.get(key);
    }
}
