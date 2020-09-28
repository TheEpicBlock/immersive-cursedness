package nl.theepicblock.immersive_cursedness;

import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
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
        ServerWorld serverWorld = this.player.getServerWorld();
        ServerWorld destination = this.getDestination();

        portalManager.getPortals().forEach(portal -> {
            DummyEntity dummyEntity = new DummyEntity(serverWorld, portal.getLowerLeft());
            TeleportTarget teleportTarget = dummyEntity.getTeleportTargetB(destination);

            System.out.println(teleportTarget.position);

            FlatStandingRectangle rect = portal.toFlatStandingRectangle();
            for (int i = 0; i < 20; i++) {
                FlatStandingRectangle rect2 = rect.expand(i, player.getCameraPosVec(1));
                //BlockPos pos1 = rect2.getBottomLeftBlock();
                //BlockPos pos2 = rect2.getTopRightBlock();
            }
        });
    }

    private ServerWorld getDestination() {
        ServerWorld serverWorld = this.player.getServerWorld();
        MinecraftServer minecraftServer = serverWorld.getServer();
        RegistryKey<World> registryKey = serverWorld.getRegistryKey() == World.NETHER ? World.OVERWORLD : World.NETHER;
        return minecraftServer.getWorld(registryKey);
    }
}
