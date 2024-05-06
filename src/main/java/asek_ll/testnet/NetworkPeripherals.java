package asek_ll.testnet;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class NetworkPeripherals {
	private final Map<Integer, Network> networks = new HashMap<>();
	private final Map<String, NetworkPeripheral> peripherals = new HashMap<>();

	public Network getOrCreateNetwork(Integer id) {
		return networks.computeIfAbsent(id, Network::new);
	}

	public void addPeripheral(int networkId, NetworkPeripheral peripheral) {
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
