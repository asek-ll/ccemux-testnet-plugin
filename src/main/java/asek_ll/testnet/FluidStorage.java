package asek_ll.testnet;

public class FluidStorage {
    private final FluidStack[] fluidStacks;
    private final int[] tankSizes;

    public FluidStorage(int... tankSizes) {
        this.fluidStacks = new FluidStack[tankSizes.length];
        this.tankSizes = new int[tankSizes.length];
        System.arraycopy(tankSizes, 0, this.tankSizes, 0, tankSizes.length);
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

    public int getCapacity(int idx) {
        return tankSizes[idx - 1];
    }

    public int getSize() {
        return fluidStacks.length;
    }
}
