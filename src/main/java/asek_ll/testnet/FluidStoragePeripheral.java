package asek_ll.testnet;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;

import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;

public class FluidStoragePeripheral implements NetworkPeripheral {
    private final String name;
    private final FluidStorage storage;
    private final Set<String> additionalTypes;

    public FluidStoragePeripheral(String name, FluidStorage storage, Set<String> additionalTypes) {
        this.name = name;
        this.storage = storage;
        this.additionalTypes = additionalTypes;
    }

    @Nullable
    private Network network;

    private final Map<String, PeripheralMethod> methods = new HashMap<>();

    {
        methods.put("tanks", this::tanksRaw);
        methods.put("pushFluid", this::pushFluidRaw);
        methods.put("pullFluid", this::pullFluidRaw);
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

    @Override
    public String getType() {
        return "fluid_storage";
    }

    @Override
    public Set<String> getAdditionalTypes() {
        return additionalTypes;
    }

    public FluidStorage getStorage() {
        return storage;
    }

    @LuaFunction
    public final Map<Integer, Object> tanks() {
        Map<Integer, Object> result = new HashMap<>();

        for (int i = 1; i <= storage.getSize(); i++) {
            FluidStack stack = storage.getTank(i);
            if (stack != null) {
                result.put(i, stack.toDetails());
            }
        }

        return result;
    }

    public Object tanksRaw(IArguments args) {
        return tanks();
    }

    private FluidStoragePeripheral getFluidStoragePeripheral(String toName) throws LuaException {
        Optional<FluidStoragePeripheral> targetO = Optional.ofNullable(network)
                .flatMap(n -> n.getRemote(toName, FluidStoragePeripheral.class));

        if (targetO.isEmpty()) {
            throw new LuaException("Fluid storage '" + toName + "' not exists");
        }

        return targetO.get();
    }

    @LuaFunction
    public final int pullFluid(String fromName, Optional<Integer> limit, Optional<String> fluidName) throws LuaException {
        return transfer(getFluidStoragePeripheral(fromName), limit, this, fluidName);
    }

    public Object pullFluidRaw(IArguments args) throws LuaException {
        return pullFluid(args.getString(0), args.optInt(1), args.optString(2));
    }


    @LuaFunction
    public final int pushFluid(String toName, Optional<Integer> limit, Optional<String> fluidName) throws LuaException {
        return transfer(this, limit, getFluidStoragePeripheral(toName), fluidName);
    }

    public Object pushFluidRaw(IArguments args) throws LuaException {
        return pushFluid(args.getString(0), args.optInt(1), args.optString(2));
    }

    private static int move(FluidStorage source, int sourceTankSlot,
                            FluidStorage target, int targetTankSlot,
                            int amount) {
        if (source == null) {
            return 0;
        }
        FluidStack sourceTank = source.getTank(sourceTankSlot);
        FluidStack targetTank = target.getTank(targetTankSlot);
        if (targetTank != null && !targetTank.isSame(sourceTank)) {
            return 0;
        }
        int toMove = Math.min(sourceTank.amount(), amount);
        if (targetTank != null) {
            toMove = Math.min(toMove, target.getCapacity(targetTankSlot) - targetTank.amount());
        }

        if (toMove == sourceTank.amount()) {
            source.setTank(sourceTankSlot, null);
        } else {
            source.setTank(sourceTankSlot, sourceTank.copy(sourceTank.amount() - toMove));
        }

        if (targetTank != null) {
            target.setTank(targetTankSlot, targetTank.copy(targetTank.amount() + toMove));
        } else {
            target.setTank(targetTankSlot, sourceTank.copy(toMove));
        }

        return toMove;
    }

    public static int transfer(FluidStoragePeripheral source,
                               Optional<Integer> limit,
                               FluidStoragePeripheral target,
                               Optional<String> fluidName) {

        int sourceSlot = 0;
        for (int i = 1; i <= source.storage.getSize(); i++) {
            FluidStack tank = source.storage.getTank(i);
            if (tank != null && (fluidName.isEmpty() || fluidName.get().equals(tank.name()))) {
                sourceSlot = i;
                break;
            }
        }

        if (sourceSlot == 0) {
            return 0;
        }

        int sourceLimit = source.storage.getTank(sourceSlot).amount();
        if (limit.isPresent()) {
            sourceLimit = Math.min(sourceLimit, limit.get());
        }

        int toTransfer = sourceLimit;
        for (int i = 1; i <= target.storage.getSize(); i++) {
            toTransfer -= move(source.storage, sourceSlot, target.storage, i, toTransfer);
            if (toTransfer == 0) {
                break;
            }
        }

        return sourceLimit - toTransfer;
    }
}
