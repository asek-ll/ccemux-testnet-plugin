package asek_ll.testnet;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.core.util.LuaUtil;

@ParametersAreNonnullByDefault
public class ModemPeripheral implements NetworkPeripheral {
    private static final AtomicInteger counter = new AtomicInteger();
    private final String name;
    @Nullable
    private Network network;

    private final Set<Integer> channels = new HashSet<>();
    private boolean open = false;

    public ModemPeripheral() {
        this.name = String.format("modem_%d", counter.getAndIncrement());
    }

    public void setNetwork(Network network) {
        this.network = network;
    }

    @Override
    public String getType() {
        return "modem";
    }

    @Override
    public Set<String> getAdditionalTypes() {
        return Set.of("peripheral_hub");
    }

    private static void checkChannel(int channel) throws LuaException {
        if (channel < 0 || channel > 65535) {
            throw new LuaException("Expected number in range 0-65535");
        }
    }

    @LuaFunction
    public final void open(int channel) throws LuaException {
        checkChannel(channel);
        synchronized (this) {
            if (!channels.contains(channel)) {
                if (channels.size() >= 128) {
                    throw new LuaException("Too many open channels");
                }

                channels.add(channel);
                open = true;
            }
        }
    }

    @LuaFunction
    public final boolean isOpen(int channel) throws LuaException {
        checkChannel(channel);
        synchronized (this) {
            return channels.contains(channel);
        }
    }

    @LuaFunction
    public final void close(int channel) throws LuaException {
        checkChannel(channel);
        synchronized (this) {
            if (channels.remove(channel) && channels.isEmpty()) {
                open = false;
            }
        }
    }

    @LuaFunction
    public final void closeAll() {
        synchronized (this) {
            if (!channels.isEmpty()) {
                channels.clear();
                open = false;
            }
        }
    }


//	@LuaFunction
//	public final void transmit(int channel, int replyChannel, Object payload) throws LuaException {
//		checkChannel(channel);
//		checkChannel(replyChannel);
//
//		Packet packet = new Packet(channel, replyChannel, payload, this);
//		network.ifPresent(n -> n.transmit(packet));
//	}

    @LuaFunction
    public final boolean isWireless() {
        return false;
    }

    @LuaFunction
    public final Set<String> getNamesRemote() {
        if (network == null) {
            return Set.of();
        }
        return network.getNamesRemote();
    }

    @LuaFunction
    public final boolean isPresentRemote(String name) {
        if (network == null) {
            return false;
        }
        return network.getNamesRemote().contains(name);
    }

    @LuaFunction
    public final Object[] getTypeRemote(String name) {
        if (network == null) {
            return null;
        }
        NetworkPeripheral remote = network.getRemote(name);
        return LuaUtil.consArray(remote.getType(), remote.getAdditionalTypes());
    }

    @LuaFunction
    public final boolean hasTypeRemote(String name, String type) {
        if (network == null) {
            return false;
        }
        NetworkPeripheral remote = network.getRemote(name);
        return remote.getType().equals(type) || remote.getAdditionalTypes().contains(type);
    }


    @LuaFunction
    public final Set<String> getMethodsRemote(String name) {
        if (network == null) {
            return Set.of();
        }
        NetworkPeripheral remote = network.getRemote(name);
        if (remote == null) {
            return Set.of();
        }
        return remote.getMethods();
    }

    @LuaFunction
    public final Object callRemote(IArguments arguments) throws LuaException {
        if (network == null) {
            return null;
        }
        String name = arguments.getString(0);
        NetworkPeripheral remote = network.getRemote(name);
        if (remote == null) {
            return null;
        }
        IArguments args = arguments.drop(1);
        return remote.call(args);
    }

    @LuaFunction
    public final String getNameLocal() {
        return name;
    }

    @Override
    public Set<String> getMethods() {
        return Set.of();
    }

    @Override
    public Object call(IArguments arguments) throws LuaException {
        return null;
    }
}
