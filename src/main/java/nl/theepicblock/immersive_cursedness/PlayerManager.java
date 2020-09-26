package nl.theepicblock.immersive_cursedness;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.poi.PointOfInterest;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestType;

import java.util.stream.Stream;

public class PlayerManager {
    private final ServerPlayerEntity player;
    private final PortalManager portalManager;

    public PlayerManager(ServerPlayerEntity player) {
        this.player = player;
        portalManager = new PortalManager();
    }

    public void tick() {
        portalManager.update(player);
    }
}
