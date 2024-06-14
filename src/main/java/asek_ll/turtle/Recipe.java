package asek_ll.turtle;

import java.util.List;

import asek_ll.testnet.ItemStack;

public record Recipe(
        List<ItemStack> reagents,
        ItemStack result
) {
    public RecipeKey key() {
        return RecipeKey.fromStacks(reagents);
    }
}
