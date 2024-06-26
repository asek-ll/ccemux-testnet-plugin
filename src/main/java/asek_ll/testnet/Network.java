package asek_ll.testnet;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class Network {
//    private final int id;
    private final Map<String, NetworkPeripheral> peripherals = new HashMap<>();
    private final Map<String, ModemPeripheral> modems = new HashMap<>();

    public Network(String s) {
    }

//    public Network(Integer integer) {
//    }


//    public Network(int id) {
//        this.id = id;
//    }

//    public int getId() {
//        return id;
//    }

//	public void transmit(Packet packet) {
//	}

    public Set<String> getNamesRemote() {
        return peripherals.keySet();
    }

    public NetworkPeripheral getRemote(String name) {
        return peripherals.get(name);
    }

    @SuppressWarnings("unchecked")
    public <T extends NetworkPeripheral> Optional<T> getRemote(String name, Class<T> type) {
        NetworkPeripheral per = peripherals.get(name);
        if (type.isInstance(per)) {
            return Optional.of((T) per);
        }
        return Optional.empty();
    }

    public void addPeripheral(NetworkPeripheral peripheral) {
        peripheral.setNetwork(this);
        if (peripheral instanceof ModemPeripheral) {
            modems.put(peripheral.getNameLocal(), (ModemPeripheral) peripheral);
        } else {
            peripherals.put(peripheral.getNameLocal(), peripheral);
        }
    }

    public ModemPeripheral getModem(String name) {
        return modems.get(name);
    }
}
