package asek_ll.testnet;

public class FluidStorage {
    private final FluidStack[] fluidStacks;

    public FluidStorage(int tanksCount) {
        this.fluidStacks = new FluidStack[tanksCount];
    }

    public FluidStorage() {
        this(1);
    }

    public void setTank(int idx, FluidStack stack) {
        fluidStacks[idx - 1] = stack;
    }

    public FluidStack getTank(int idx) {
        return fluidStacks[idx - 1];
    }

    public int getSize() {
        return fluidStacks.length;
    }
}
