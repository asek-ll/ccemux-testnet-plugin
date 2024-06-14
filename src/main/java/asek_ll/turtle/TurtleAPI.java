package asek_ll.turtle;

import asek_ll.testnet.InventoryPeripheral;
import asek_ll.testnet.ItemStack;
import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.ILuaAPI;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.core.computer.ComputerSide;
import net.clgd.ccemux.api.emulation.EmulatedComputer;

public class TurtleAPI implements ILuaAPI {
    private final EmulatedComputer computer;
    private final Turtle turtle;

    public TurtleAPI(EmulatedComputer computer, Turtle turtle) {
        this.computer = computer;
        this.turtle = turtle;
    }

    @Override
    public String[] getNames() {
        return new String[]{"turtle"};
    }

    @LuaFunction
    public final Object getItemDetail(IArguments args) throws LuaException {
        return turtle.getItemDetail(args.optInt(0));
    }

    @LuaFunction
    public final int getItemCount(IArguments args) throws LuaException {
        return turtle.getItemCount(args.optInt(0));
    }

    @LuaFunction
    public final boolean select(int slot) throws LuaException {
        return turtle.select(slot);
    }

    @LuaFunction
    public final boolean dropUp(IArguments args) throws LuaException {
        IPeripheral peripheral = computer.getEnvironment().getPeripheral(ComputerSide.TOP);
        if (!(peripheral instanceof InventoryPeripheral inv)) {
            return false;
        }

        return turtle.drop(args.optInt(0), inv);
    }

    @LuaFunction
    public final boolean dropDown(IArguments args) throws LuaException {
        IPeripheral peripheral = computer.getEnvironment().getPeripheral(ComputerSide.BOTTOM);
        if (!(peripheral instanceof InventoryPeripheral inv)) {
            return false;
        }

        return turtle.drop(args.optInt(0), inv);
    }

    @LuaFunction
    public final boolean drop(IArguments args) throws LuaException {
        IPeripheral peripheral = computer.getEnvironment().getPeripheral(ComputerSide.FRONT);
        if (!(peripheral instanceof InventoryPeripheral inv)) {
            return false;
        }

        return turtle.drop(args.optInt(0), inv);
    }

    @LuaFunction
    public final boolean suckUp(IArguments args) throws LuaException {
        IPeripheral peripheral = computer.getEnvironment().getPeripheral(ComputerSide.TOP);
        if (!(peripheral instanceof InventoryPeripheral inv)) {
            return false;
        }

        return turtle.suck(args.optInt(0), inv);
    }

    @LuaFunction
    public final boolean suckDown(IArguments args) throws LuaException {
        IPeripheral peripheral = computer.getEnvironment().getPeripheral(ComputerSide.BOTTOM);
        if (!(peripheral instanceof InventoryPeripheral inv)) {
            return false;
        }

        return turtle.suck(args.optInt(0), inv);
    }

    @LuaFunction
    public final boolean suck(IArguments args) throws LuaException {
        IPeripheral peripheral = computer.getEnvironment().getPeripheral(ComputerSide.FRONT);
        if (!(peripheral instanceof InventoryPeripheral inv)) {
            return false;
        }

        return turtle.suck(args.optInt(0), inv);
    }


    @LuaFunction
    public final boolean craft(IArguments args) throws LuaException {
        return false;
    }
}
