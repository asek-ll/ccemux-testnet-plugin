package asek_ll.testnet;

import java.util.Set;

import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.LuaException;
import net.clgd.ccemux.api.peripheral.Peripheral;

public interface NetworkPeripheral extends Peripheral {
	void setNetwork(Network network);

	String getNameLocal();

	Set<String> getMethods();

	Object call(IArguments arguments) throws LuaException;
}
