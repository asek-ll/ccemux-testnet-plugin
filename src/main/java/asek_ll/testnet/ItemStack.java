package asek_ll.testnet;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public record ItemStack(
	String name,
	int count,
	String nbt,
	int maxCount
) {
	public boolean isSame(ItemStack other) {
		return name.equals(other.name) && Objects.equals(nbt, other.nbt);
	}

	public Map<String, Object> toItem() {
		Map<String, Object> table = new HashMap<>();

		table.put("name", name);
		table.put("count", count);
		table.put("nbt", nbt);

		return table;
	}

	public Map<String, Object> toItemDetails() {
		Map<String, Object> table = new HashMap<>();

		table.put("name", name);
		table.put("count", count);
		table.put("nbt", nbt);
		table.put("maxCount", maxCount);

		return table;
	}
}
