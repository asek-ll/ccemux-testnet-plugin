package asek_ll.testnet;

public class Inventory {
    private final ItemStack[] items;
    private final int size;

    public Inventory(int size) {
        this.size = size;
        items = new ItemStack[size];
    }

    public int getSize() {
        return size;
    }

    public void setSlot(int slot, ItemStack stack) {
        items[slot - 1] = stack;
    }

    public ItemStack getSlot(int slot) {
        return items[slot - 1];
    }
}
