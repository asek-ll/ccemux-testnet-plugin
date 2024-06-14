package asek_ll.turtle;

import asek_ll.testnet.ItemStack;

public record StackId(String name, String nbt) {
    public static StackId of(ItemStack stack) {
        if (stack == null) {
            return null;
        }
        return new StackId(stack.name(), stack.nbt());
    }
}
