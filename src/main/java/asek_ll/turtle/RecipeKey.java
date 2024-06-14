package asek_ll.turtle;

import java.util.ArrayList;
import java.util.List;

import asek_ll.testnet.ItemStack;

public record RecipeKey(
        StackId i1,
        StackId i2,
        StackId i3,
        StackId i4,
        StackId i5,
        StackId i6,
        StackId i7,
        StackId i8,
        StackId i9
) {

    public static RecipeKey fromStacks(List<ItemStack> ids) {
        List<StackId> list = new ArrayList<>(ids.size());
        for (ItemStack id : ids) {
            list.add(StackId.of(id));
        }
        return from(list);
    }

    public static RecipeKey from(List<StackId> ids) {
        if (ids.size() != 9) {
            throw new IllegalArgumentException("Invalid reagents shape");
        }
        return new RecipeKey(
                ids.get(0), ids.get(1), ids.get(2),
                ids.get(3), ids.get(4), ids.get(5),
                ids.get(6), ids.get(7), ids.get(8)
        );
    }
}
