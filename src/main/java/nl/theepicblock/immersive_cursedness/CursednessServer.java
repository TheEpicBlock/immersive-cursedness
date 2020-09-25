package nl.theepicblock.immersive_cursedness;

import net.minecraft.block.Blocks;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import java.util.concurrent.atomic.AtomicBoolean;

public class CursednessServer {
    private final MinecraftServer server;
    private volatile boolean isServerActive = true;
    private long nextTick;

    public CursednessServer(MinecraftServer server) {
        this.server = server;
    }

    public void start() {
        System.out.println("Starting cursedness immersedness server");
        while (isServerActive) {
            if (System.currentTimeMillis() < nextTick) continue;
            tick();
            System.out.println("TICK "+nextTick);
            nextTick = System.currentTimeMillis()+1000;//todo change this to 50
        }
    }

    public void stop() {
        System.out.println("Stopping cursedness immersedness server");
        isServerActive = false;
    }

    public void tick() {
        server.getPlayerManager().getPlayerList().forEach((player) -> {
            player.networkHandler.sendPacket(new BlockUpdateS2CPacket(new BlockPos(0,4,0), Blocks.BROWN_MUSHROOM_BLOCK.getDefaultState()));
        });
    }
}
