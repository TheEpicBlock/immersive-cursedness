package nl.theepicblock.immersive_cursedness;

import net.minecraft.block.Blocks;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class CursednessServer {
    public static final int PORTAL_RENDER_DISTANCE = 3; //measured in chunks
    private final MinecraftServer server;
    private volatile boolean isServerActive = true;
    private long nextTick;
    private int tickCount;
    private final Map<ServerPlayerEntity, PlayerManager> playerManagers = new HashMap<>();

    public CursednessServer(MinecraftServer server) {
        this.server = server;
    }

    public void start() {
        System.out.println("Starting cursedness immersedness server");
        while (isServerActive) {
            if (System.currentTimeMillis() < nextTick) continue;
            try {
                tick();
                tickCount++;
            } catch (Exception e) {
                System.out.println("Exception occurred whilst ticking the immersive cursedness thread");
                e.printStackTrace();
            }
            nextTick = System.currentTimeMillis()+50;//todo change this to 50
        }
    }

    public void stop() {
        System.out.println("Stopping cursedness immersedness server");
        isServerActive = false;
    }

    public void tick() {
        //Sync player managers
        List<ServerPlayerEntity> playerList = server.getPlayerManager().getPlayerList();

        playerManagers.entrySet().removeIf(i -> !playerList.contains(i.getKey()));
        for (ServerPlayerEntity player : playerList) {
            if (!playerManagers.containsKey(player)) {
                playerManagers.put(player, new PlayerManager(player));
            }
        }

        //Tick player managers
        playerManagers.forEach((player, manager) -> {
            manager.tick(tickCount);
        });
    }
}
