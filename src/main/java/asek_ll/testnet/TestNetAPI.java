package asek_ll.testnet;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.auto.service.AutoService;
import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.ILuaAPI;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.core.computer.ComputerSide;
import net.clgd.ccemux.api.emulation.EmulatedComputer;
import net.clgd.ccemux.api.emulation.Emulator;
import net.clgd.ccemux.api.plugins.Plugin;
import net.clgd.ccemux.api.plugins.PluginManager;
import net.clgd.ccemux.api.plugins.hooks.ComputerCreated;
import org.jetbrains.annotations.NotNull;

@AutoService(Plugin.class)
public class TestNetAPI extends Plugin {

    private final NetworkPeripherals peripherals = new NetworkPeripherals();
    private final Map<Integer, EmulatedComputer> computerById = new HashMap<>();

    @NotNull
    @Override
    public String getName() {
        return "CCEmuX TestNet API";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "";
    }

    @NotNull
    @Override
    public Optional<String> getVersion() {
        return Optional.empty();
    }

    @NotNull
    @Override
    public Collection<String> getAuthors() {
        return List.of();
    }

    @NotNull
    @Override
    public Optional<String> getWebsite() {
        return Optional.empty();
    }

    @Override
    public void setup(@NotNull PluginManager manager) {
        registerHook((ComputerCreated) (emu, computer) -> {
            computerById.put(computer.getID(), computer);
            computer.addApi(new TestNetAPI.API(emu, computer, peripherals, computerById));
        });
    }

    public static class API implements ILuaAPI {
        private final Emulator emu;
        private final EmulatedComputer computer;
        private final NetworkPeripherals peripherals;
        private final Map<Integer, EmulatedComputer> computerById;

        public API(Emulator emu, EmulatedComputer computer, NetworkPeripherals peripherals, Map<Integer,
                EmulatedComputer> computerById) {
            this.emu = emu;
            this.computer = computer;
            this.peripherals = peripherals;
            this.computerById = computerById;
        }

        @LuaFunction
        public final String createModem(int networkId) {
            ModemPeripheral modem = new ModemPeripheral();
            peripherals.addPeripheral(networkId, modem);
            return modem.getNameLocal();
        }

        @LuaFunction
        public final String createInventory(int networkId) {
            InventoryPeripheral inventoryPeripheral = new InventoryPeripheral(27);
            peripherals.addPeripheral(networkId, inventoryPeripheral);
            return inventoryPeripheral.getNameLocal();
        }

        @LuaFunction
        public final void setItem(String inventoryName, int slot, Map<?, ?> item) {
            Optional<InventoryPeripheral> peripheral = peripherals
                    .getPeripheral(inventoryName, InventoryPeripheral.class);

            if (peripheral.isEmpty()) {
                return;
            }
            InventoryPeripheral inventory = peripheral.get();

            ItemStack stack = new ItemStack(
                    (String) item.get("name"),
                    ((Number) item.get("count")).intValue(),
                    (String) item.get("nbt"),
                    ((Number) item.get("maxCount")).intValue()
            );

            inventory.setSlot(slot, stack);
        }

        @LuaFunction
        public final boolean attachPeripheral(IArguments arguments) throws LuaException {
            ComputerSide side = arguments.getEnum(0, ComputerSide.class);
            String modemName = arguments.getString(1);
            Optional<Integer> computerId = arguments.optInt(2);

            Optional<NetworkPeripheral> peripheral = peripherals.getPeripheral(modemName);

            if (peripheral.isEmpty()) {
                return false;
            }

            EmulatedComputer comp = computerId.map(computerById::get).orElse(computer);
            comp.getEnvironment().setPeripheral(side, peripheral.get());
            return true;
        }

        @Override
        public String[] getNames() {
            return new String[]{"testnet"};
        }
    }
}
