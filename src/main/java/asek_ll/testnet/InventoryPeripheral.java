package asek_ll.testnet;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;

import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;

public class InventoryPeripheral implements NetworkPeripheral {
    private final String name;
    private final Inventory inventory;

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

    public InventoryPeripheral(String name, Inventory inventory) {
        this.name = name;
        this.inventory = inventory;
    }

    public Inventory getInventory() {
        return inventory;
    }

    @Override
    public String getType() {
        return "inventory";
    }

    @LuaFunction
    public final int size() {
        return inventory.getSize();
    }


    public Object sizeRaw(IArguments args) {
        return size();
    }

    @LuaFunction
    public final Map<Integer, Object> list() {
        Map<Integer, Object> result = new HashMap<>();

        for (int i = 1; i <= size(); i++) {
            ItemStack stack = inventory.getSlot(i);
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
        ItemStack stack = inventory.getSlot(slot);
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
        return inventory.getSlot(slot).maxCount();
    }

    public Object getItemLimitRaw(IArguments args) throws LuaException {
        return getItemLimit(args.getInt(0));
    }

    public Object pushItemsRaw(IArguments args) throws LuaException {
        return pushItems(args.getString(0), args.getInt(1), args.optInt(2), args.optInt(3));
    }

    @LuaFunction
    public final int pushItems(String toName, int fromSlot, Optional<Integer> limit, Optional<Integer> toSlot)
            throws LuaException {
        return transfer(this, fromSlot, limit, getInventoryPeripheral(toName), toSlot);
    }

    private InventoryPeripheral getInventoryPeripheral(String toName) throws LuaException {
        Optional<InventoryPeripheral> targetO = Optional.ofNullable(network)
                .flatMap(n -> n.getRemote(toName, InventoryPeripheral.class));

        if (targetO.isEmpty()) {
            throw new LuaException("Inventory '" + toName + "' not exists");
        }

        return targetO.get();
    }

    public Object pullItemsRaw(IArguments args) throws LuaException {
        return pullItems(args.getString(0), args.getInt(1), args.optInt(2), args.optInt(3));
    }

    @LuaFunction
    public final int pullItems(String fromName, int fromSlot, Optional<Integer> limit, Optional<Integer> toSlot) throws LuaException {
        return transfer(getInventoryPeripheral(fromName), fromSlot, limit, this, toSlot);
    }

    public static int transfer(InventoryPeripheral source,
                               int sourceSlot,
                               Optional<Integer> limit,
                               InventoryPeripheral target,
                               Optional<Integer> targetSlot) {

        ItemStack sourceStack = source.inventory.getSlot(sourceSlot);
        if (sourceStack == null) {
            return 0;
        }

        int sourceLimit = limit.filter(l -> l < sourceStack.count()).orElse(sourceStack.count());

        if (targetSlot.isPresent()) {
            return move(target, targetSlot.get(), source, sourceSlot, sourceStack, sourceLimit);
        }

        int toTransfer = sourceLimit;
        for (int slot = 1; slot <= target.size(); slot++) {
            toTransfer -= move(target, slot, source, sourceSlot, sourceStack, sourceLimit);
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
        if (target.inventory.getSlot(targetSlot) != null && !target.inventory.getSlot(targetSlot).isSame(sourceStack)) {
            return 0;
        }

        int targetCount = target.inventory.getSlot(targetSlot) != null ?
                target.inventory.getSlot(targetSlot).count() : 0;

        int targetLimit = sourceStack.maxCount() - targetCount;

        int resultLimit = Math.min(sourceLimit, targetLimit);

        target.inventory.setSlot(targetSlot, new ItemStack(sourceStack.name(),
                targetCount + resultLimit,
                sourceStack.nbt(),
                sourceStack.maxCount()
        ));

        int resultSourceCount = sourceStack.count() - resultLimit;
        if (resultSourceCount > 0) {
            source.inventory.setSlot(sourceSlot, new ItemStack(sourceStack.name(),
                    resultSourceCount,
                    sourceStack.nbt(),
                    sourceStack.maxCount()
            ));
        } else {
            source.inventory.setSlot(sourceSlot, null);
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
}
