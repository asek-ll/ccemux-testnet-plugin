package asek_ll.testnet;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import asek_ll.turtle.Recipe;
import asek_ll.turtle.RecipeRegistry;
import asek_ll.turtle.Turtle;
import asek_ll.turtle.TurtleAPI;
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
    private final RecipeRegistry recipeRegistry = new RecipeRegistry();
    private final Map<Integer, Turtle> turtles = new HashMap<>();

    private static final AtomicInteger counter = new AtomicInteger();

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
            computer.addApi(new TestNetAPI.API(emu, computer, peripherals, computerById, recipeRegistry, turtles));
            Turtle turtle = turtles.get(computer.getID());
            if (turtle != null) {
                computer.addApi(new TurtleAPI(computer, turtle));
            }
        });
    }

    public static class API implements ILuaAPI {
        private final Emulator emu;
        private final EmulatedComputer computer;
        private final NetworkPeripherals peripherals;
        private final Map<Integer, EmulatedComputer> computerById;
        private final RecipeRegistry recipeRegistry;
        private final Map<Integer, Turtle> turtles;

        public API(Emulator emu,
                   EmulatedComputer computer,
                   NetworkPeripherals peripherals,
                   Map<Integer, EmulatedComputer> computerById,
                   RecipeRegistry recipeRegistry,
                   Map<Integer, Turtle> turtles
        ) {
            this.emu = emu;
            this.computer = computer;
            this.peripherals = peripherals;
            this.computerById = computerById;
            this.recipeRegistry = recipeRegistry;
            this.turtles = turtles;
        }

        @LuaFunction
        public final String createModem(String networkId) throws LuaException {
            ModemPeripheral modem = new ModemPeripheral();
            peripherals.addPeripheral(networkId, modem);
            return modem.getNameLocal();
        }

        @LuaFunction
        public final String createInventory(IArguments arguments) throws LuaException {
            String networkId = arguments.getString(0);
            String name = arguments.optString(1)
                    .orElseGet(() -> "inventory_" + counter.getAndIncrement());

            Inventory inventory = new Inventory(27);
            InventoryPeripheral inventoryPeripheral = new InventoryPeripheral(name, inventory);

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

            ItemStack stack = parseStack(item);

            inventory.getInventory().setSlot(slot, stack);
        }

        private static ItemStack parseStack(Map<?, ?> item) {
            return new ItemStack(
                    (String) item.get("name"),
                    ((Number) item.get("count")).intValue(),
                    (String) item.get("nbt"),
                    ((Number) item.get("maxCount")).intValue()
            );
        }

        @LuaFunction
        public final boolean attachPeripheral(IArguments arguments) throws LuaException {
            ComputerSide side = arguments.getEnum(0, ComputerSide.class);
            String modemName = arguments.getString(1);
            Optional<Integer> computerId = arguments.optInt(2);

            Optional<NetworkPeripheral> peripheralO = peripherals.getPeripheral(modemName);

            if (peripheralO.isEmpty()) {
                return false;
            }
            NetworkPeripheral peripheral = peripheralO.get();

            EmulatedComputer comp = computerId.map(computerById::get).orElse(computer);

            if (peripheral instanceof InventoryPeripheral) {
                peripheral = new InventoryPeripheral(side.getName(), ((InventoryPeripheral) peripheral).getInventory());
                Network localNetwork = peripherals.getOrCreateNetwork("loca_comp_%d".formatted(comp.getID()));
                localNetwork.addPeripheral(peripheral);
            }

            comp.getEnvironment().setPeripheral(side, peripheral);
            return true;
        }

        @LuaFunction
        public final boolean enableTurtleApi(int computerId) {
            turtles.computeIfAbsent(computerId, (id) -> new Turtle());
            return true;
        }

        @LuaFunction
        public final void attachWorkbench(IArguments arguments) throws LuaException {
            ComputerSide side = arguments.getEnum(0, ComputerSide.class);
            if (side != ComputerSide.LEFT && side != ComputerSide.RIGHT) {
                throw new LuaException("Invalid side");
            }
            Optional<Integer> computerId = arguments.optInt(1);
            EmulatedComputer comp = computerId.map(computerById::get).orElse(computer);

            Turtle turtle = turtles.get(comp.getID());
            if (turtle == null) {
                throw new LuaException("Workbench can be attached only to turtle");
            }
            WorkbenchPeripheral peripheral = new WorkbenchPeripheral(turtle, recipeRegistry);

            comp.getEnvironment().setPeripheral(side, peripheral);
        }

        @LuaFunction
        public final void registerRecipe(Map<?, ?> recipe) {
            ItemStack result = parseStack((Map<?, ?>) recipe.get("result"));
            ItemStack[] reagents = new ItemStack[9];
            Map<?, ?> ingBySlot = (Map<?, ?>) recipe.get("ingredients");
            for (Map.Entry<?, ?> entry : ingBySlot.entrySet()) {
                reagents[((Number) entry.getKey()).intValue() - 1] = parseStack((Map<?, ?>) entry.getValue());
            }
            recipeRegistry.register(new Recipe(Arrays.asList(reagents), result));
        }

        @Override
        public String[] getNames() {
            return new String[]{"testnet"};
        }
    }
}
