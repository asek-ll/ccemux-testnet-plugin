package asek_ll.testnet;

import java.util.ArrayList;
import java.util.List;

import asek_ll.turtle.Recipe;
import asek_ll.turtle.RecipeKey;
import asek_ll.turtle.RecipeRegistry;
import asek_ll.turtle.Turtle;
import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import net.clgd.ccemux.api.peripheral.Peripheral;

public class WorkbenchPeripheral implements Peripheral {
    private final Turtle turtle;
    private final RecipeRegistry recipeRegistry;

    public WorkbenchPeripheral(Turtle turtle, RecipeRegistry recipeRegistry) {
        this.turtle = turtle;
        this.recipeRegistry = recipeRegistry;
    }

    @Override
    public String getType() {
        return "workbench";
    }


    @LuaFunction
    public final boolean craft(IArguments args) throws LuaException {
        List<Integer> slots = List.of(
                1, 2, 3,
                5, 6, 7,
                9, 10, 11
        );

        List<ItemStack> grid = new ArrayList<>(9);
        for (Integer slot : slots) {
            grid.add(turtle.getSlot(slot));
        }

        RecipeKey key = RecipeKey.fromStacks(grid);
        Recipe recipe = recipeRegistry.get(key);
        if (recipe == null) {
            return false;
        }

        int minCraftCount = args.optInt(0, 64);
        for (int i = 0; i < 9; i += 1) {
            if (grid.get(i) == null) {
                continue;
            }
            int craftCount = grid.get(i).count() / recipe.reagents().get(i).count();
            minCraftCount = Math.min(minCraftCount, craftCount);
        }

        if (minCraftCount == 0) {
            return false;
        }

        for (int i = 0; i < 9; i += 1) {
            ItemStack reagent = grid.get(i);
            if (reagent == null) {
                continue;
            }
            int used = minCraftCount * recipe.reagents().get(i).count();
            if (used == reagent.count()) {
                turtle.setSlot(slots.get(i), null);
            } else {
                turtle.setSlot(slots.get(i), reagent.copy(reagent.count() - used));
            }
        }

        ItemStack result = recipe.result().copy(minCraftCount * recipe.result().count());
        turtle.addStack(result);
        return true;
    }


}
