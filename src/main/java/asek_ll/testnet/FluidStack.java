package asek_ll.testnet;

import java.util.HashMap;
import java.util.Map;

public record FluidStack(
        String name,
        int amount,
        int maxAmount
) {

    public boolean isSame(FluidStack other) {
        return name.equals(other.name);
    }

    public FluidStack copy(int amount) {
        return new FluidStack(name, amount, maxAmount);
    }

    public Map<String, Object> toDetails() {
        Map<String, Object> table = new HashMap<>();

        table.put("name", name);
        table.put("amount", amount);

        return table;
    }
}
