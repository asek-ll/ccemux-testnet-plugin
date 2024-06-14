package asek_ll.turtle;

import java.util.Optional;

import asek_ll.testnet.InventoryPeripheral;
import asek_ll.testnet.ItemStack;
import dan200.computercraft.api.lua.LuaException;

public class Turtle {
    private int selectedSlot = 1;
    private final ItemStack[] slots = new ItemStack[16];

    public final Object getItemDetail(Optional<Integer> slotO) {
        int slot = slotO.orElse(selectedSlot);
        if (getSlot(slot) == null) {
            return null;
        }
        return getSlot(slot).toItemDetails();
    }

    public final int getItemCount(Optional<Integer> slotO) throws LuaException {
        int slot = slotO.orElse(selectedSlot);
        if (getSlot(slot) == null) {
            return 0;
        }
        return getSlot(slot).count();
    }

    public final boolean select(int slot) throws LuaException {
        selectedSlot = slot;
        return true;
    }


    public boolean suck(Optional<Integer> maxO, InventoryPeripheral inv) throws LuaException {
        int sourceSlot = 1;
        while (sourceSlot < inv.size() && inv.getSlot(sourceSlot) == null) {
            sourceSlot += 1;
        }

        if (sourceSlot >= inv.size()) {
            return false;
        }

        ItemStack sourceItem = inv.getSlot(sourceSlot);

        int max = maxO.orElse(64);

        int count = Math.min(max, sourceItem.count());

        if (count == 0) {
            return false;
        }

        int added = addStack(sourceItem.copy(count));

        if (sourceItem.count() == added) {
            inv.setSlot(sourceSlot, null);
        } else {
            inv.setSlot(sourceSlot, sourceItem.copy(sourceItem.count() - added));
        }

        return true;
    }

    public boolean drop(Optional<Integer> max, InventoryPeripheral inv) throws LuaException {
        ItemStack stack = getSlot(selectedSlot);
        if (stack == null) {
            return false;
        }

        int toTransfer = Math.min(stack.count(), max.orElse(64));
        int remain = toTransfer;
        for (int i = 1; i <= inv.size(); i += 1) {
            ItemStack target = inv.getSlot(i);
            if (target == null) {
                inv.setSlot(i, stack.copy(remain));
                remain = 0;
                break;
            }
            if (target.isSame(stack)) {
                int free = target.maxCount() - target.count();
                int toMove = Math.min(free, remain);
                inv.setSlot(i, target.copy(target.count() + toMove));
                remain -= toMove;

                if (remain == 0) {
                    break;
                }
            }
        }

        int sourceStackSize = stack.count() - toTransfer + remain;

        if (sourceStackSize == 0) {
            setSlot(selectedSlot, null);
        } else {
            setSlot(selectedSlot, stack.copy(sourceStackSize));
        }

        return remain != toTransfer;
    }

    public int addStack(ItemStack stack) {
        int remain = stack.count();
        for (int i = 0; i < 16; i += 1) {
            int slot = (((selectedSlot + i) - 1) % 16) + 1;
            if (getSlot(slot) == null) {
                setSlot(slot, stack.copy(remain));
                return stack.count();
            }
            ItemStack target = getSlot(slot);
            if (target.isSame(stack)) {
                int free = target.maxCount() - target.count();
                int toMove = Math.min(free, remain);
                if (toMove > 0) {
                    remain -= toMove;
                    setSlot(slot, target.copy(target.count() + toMove));
                }
                if (remain == 0) {
                    return stack.count();
                }
            }
        }
        return remain;
    }

    public void setSlot(int slot, ItemStack stack) {
        slots[slot - 1] = stack;
    }

    public ItemStack getSlot(int slot) {
        return slots[slot - 1];
    }
}
