package asek_ll.testnet;

import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.LuaException;

public interface PeripheralMethod {
	Object call(IArguments args) throws LuaException;
}
