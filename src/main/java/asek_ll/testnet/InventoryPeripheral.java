package asek_ll.testnet;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nullable;

import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;

public class InventoryPeripheral implements NetworkPeripheral {
    private static final AtomicInteger counter = new AtomicInteger();
    private final int size;
    private final String name;
    private final ItemStack[] items;

    @Nullable
    private Network network;

    private final Map<String, PeripheralMethod> methods = new HashMap<>();

    {
        methods.put("size", this::sizeRaw);
        methods.put("list", this::listRaw);
        methods.put("getItemDetail", this::getItemDetailRaw);
        methods.put("getItemLimit", this::getItemLimitRaw);
        methods.put("pushItems", this::pushItemsRaw);
        methods.put("pullItems", this::pullItemsRaw);
    }

    public InventoryPeripheral(String prefix, int size) {
        this.size = size;
        name = "inventory_" + prefix + counter.getAndIncrement();
        items = new ItemStack[size];
    }


    @Override
    public String getType() {
        return "inventory";
    }

    @LuaFunction
    public final int size() {
        return size;
    }


    public Object sizeRaw(IArguments args) {
        return size();
    }

    @LuaFunction
    public final Map<Integer, Object> list() {
        Map<Integer, Object> result = new HashMap<>();

        for (int i = 1; i <= size; i++) {
            ItemStack stack = getSlot(i);
            if (stack != null) {
                result.put(i, stack.toItem());
            }
        }

        return result;
    }

    public Object listRaw(IArguments args) {
        return list();
    }

    @LuaFunction
    public final Object getItemDetail(int slot) {
        ItemStack stack = getSlot(slot);
        if (stack == null) {
            return null;
        }
        return stack.toItemDetails();
    }

    public Object getItemDetailRaw(IArguments args) throws LuaException {
        return getItemDetail(args.getInt(0));
    }

    @LuaFunction
    public final int getItemLimit(int slot) {
        return getSlot(slot).maxCount();
    }

    public Object getItemLimitRaw(IArguments args) throws LuaException {
        return getItemLimit(args.getInt(0));
    }

    public Object pushItemsRaw(IArguments args) throws LuaException {
        return pushItems(args.getString(0), args.getInt(1), args.optInt(2), args.optInt(3));
    }

    @LuaFunction
    public final int pushItems(String toName, int fromSlot, Optional<Integer> limit, Optional<Integer> toSlot) {
        if (network == null) {
            return 0;
        }
        Optional<InventoryPeripheral> targetO = network.getRemote(toName, InventoryPeripheral.class);
        return targetO
                .map(inventoryPeripheral -> transfer(this, fromSlot, limit, inventoryPeripheral, toSlot))
                .orElse(0);

    }

    public Object pullItemsRaw(IArguments args) throws LuaException {
        return pullItems(args.getString(0), args.getInt(1), args.optInt(2), args.optInt(3));
    }

    @LuaFunction
    public final int pullItems(String fromName, int fromSlot, Optional<Integer> limit, Optional<Integer> toSlot) {
        if (network == null) {
            return 0;
        }
        Optional<InventoryPeripheral> fromO = network.getRemote(fromName, InventoryPeripheral.class);
        return fromO
                .map(inventoryPeripheral -> transfer(inventoryPeripheral, fromSlot, limit, this, toSlot))
                .orElse(0);

    }

    public static int transfer(InventoryPeripheral source,
                               int sourceSlot,
                               Optional<Integer> limit,
                               InventoryPeripheral target,
                               Optional<Integer> targetSlot) {

        ItemStack sourceStack = source.getSlot(sourceSlot);
        if (sourceStack == null) {
            return 0;
        }

        int sourceLimit = limit.filter(l -> l < sourceStack.count()).orElse(sourceStack.count());

        if (targetSlot.isPresent()) {
            return move(target, targetSlot.get(), source, sourceSlot, sourceStack, sourceLimit);
        }

        int toTransfer = sourceLimit;
        for (int i = 0; i < target.size; i++) {
            toTransfer -= move(target, i, source, sourceSlot, sourceStack, sourceLimit);
            if (toTransfer == 0) {
                break;
            }
        }
        return sourceLimit - toTransfer;
    }

    private static int move(InventoryPeripheral target,
                            int targetSlot,
                            InventoryPeripheral source,
                            int sourceSlot,
                            ItemStack sourceStack,
                            int sourceLimit) {
        if (target.getSlot(targetSlot) != null && !target.getSlot(targetSlot).isSame(sourceStack)) {
            return 0;
        }

        int targetCount = target.getSlot(targetSlot) != null ? target.getSlot(targetSlot).count() : 0;

        int targetLimit = sourceStack.maxCount() - targetCount;

        int resultLimit = Math.min(sourceLimit, targetLimit);

        target.setSlot(targetSlot, new ItemStack(sourceStack.name(),
                targetCount + resultLimit,
                sourceStack.nbt(),
                sourceStack.maxCount()
        ));

        int resultSourceCount = sourceStack.count() - resultLimit;
        if (resultSourceCount > 0) {
            source.setSlot(sourceSlot, new ItemStack(sourceStack.name(),
                    resultSourceCount,
                    sourceStack.nbt(),
                    sourceStack.maxCount()
            ));
        } else {
            source.setSlot(sourceSlot, null);
        }

        return resultLimit;
    }

    @Override
    public void setNetwork(Network network) {
        this.network = network;
    }

    @Override
    public String getNameLocal() {
        return name;
    }

    @Override
    public Set<String> getMethods() {
        return methods.keySet();
    }

    @Override
    public Object call(IArguments arguments) throws LuaException {
        String method = arguments.getString(0);
        return methods.get(method).call(arguments.drop(1));
    }

    public void setSlot(int slot, ItemStack stack) {
        items[slot - 1] = stack;
    }

    public ItemStack getSlot(int slot) {
        return items[slot - 1];
    }
}
