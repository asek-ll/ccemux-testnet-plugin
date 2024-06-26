package asek_ll.testnet;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import dan200.computercraft.api.lua.LuaException;

public class NetworkPeripherals {
    private final Map<String, Network> networks = new HashMap<>();
    private final Map<String, NetworkPeripheral> peripherals = new HashMap<>();

    public Network getOrCreateNetwork(String id) {
        return networks.computeIfAbsent(id, Network::new);
    }

    public void addPeripheral(String networkId, NetworkPeripheral peripheral) throws LuaException {
        if (peripherals.containsKey(peripheral.getNameLocal())) {
            throw new LuaException("Duplicate peripheral name '" + peripheral.getNameLocal() + "'");
        }
        peripherals.put(peripheral.getNameLocal(), peripheral);
        Network network = getOrCreateNetwork(networkId);
        network.addPeripheral(peripheral);
    }

    public Optional<NetworkPeripheral> getPeripheral(String name) {
        return Optional.ofNullable(peripherals.get(name));
    }

    @SuppressWarnings("unchecked")
    public <T extends NetworkPeripheral> Optional<T> getPeripheral(String name, Class<T> type) {
        NetworkPeripheral peripheral = peripherals.get(name);
        if (type.isInstance(peripheral)) {
            return Optional.of((T) peripheral);
        }
        return Optional.empty();
    }
}
