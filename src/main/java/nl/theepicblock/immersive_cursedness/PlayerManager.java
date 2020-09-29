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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class PlayerManager {
    private final ServerPlayerEntity player;
    private final PortalManager portalManager;
    private List<BlockPos> sentBlocks = new ArrayList<>();

    public PlayerManager(ServerPlayerEntity player) {
        this.player = player;
        portalManager = new PortalManager();
    }

    public void tick() {
        portalManager.update(player);
        ServerWorld serverWorld = this.player.getServerWorld();
        ServerWorld destination = this.getDestination();

        List<BlockPos> newSentBlocks = new ArrayList<>();

        //iterate through all portals
        portalManager.getPortals().forEach(portal -> {
            //get the corresponding location in the nether and make a TransformProfile
            int yaw = portal.getYawRelativeTo(player.getBlockPos());
            DummyEntity dummyEntity = new DummyEntity(serverWorld, portal.getLowerLeft());
            dummyEntity.setYaw(yaw);
            TeleportTarget teleportTarget = dummyEntity.getTeleportTargetB(destination);

            TransformProfile transformProfile = new TransformProfile(
                    portal.getLowerLeft(),
                    new BlockPos(teleportTarget.position),
                    yaw,
                    (int)teleportTarget.yaw);

            //replace the portal blocks in the center of the portal with air
            BlockPos.iterate(portal.getLowerLeft(), portal.getUpperRight()).forEach(pos -> {
                player.networkHandler.sendPacket(new BlockUpdateS2CPacket(pos.toImmutable(), Blocks.AIR.getDefaultState()));
            });

            //iterate through all layers behind the portal
            FlatStandingRectangle rect = portal.toFlatStandingRectangle();
            for (int i = 1; i < 20; i++) {
                FlatStandingRectangle rect2 = rect.expand(i, player.getCameraPosVec(1));
                BlockPos pos1 = rect2.getBottomLeftBlock();
                BlockPos pos2 = rect2.getTopRightBlock();

                //go through all blocks in this layer and use the transformProfile to get the correct block in the nether. Then send it to the client
                BlockPos.iterate(pos1, pos2).forEach(pos -> {
                    BlockPos imPos = pos.toImmutable();
                    newSentBlocks.add(imPos);
                    player.networkHandler.sendPacket(
                            new BlockUpdateS2CPacket(imPos, transformProfile.transformAndGetFromWorld(imPos, destination)));
                });
            }

        });

        //get all of the old blocks and remove them
        sentBlocks.removeAll(newSentBlocks);
        sentBlocks.forEach(pos -> {
            player.networkHandler.sendPacket(new BlockUpdateS2CPacket(pos.toImmutable(), serverWorld.getBlockState(pos)));
        });

        sentBlocks = newSentBlocks;
    }

    private ServerWorld getDestination() {
        ServerWorld serverWorld = this.player.getServerWorld();
        MinecraftServer minecraftServer = serverWorld.getServer();
        RegistryKey<World> registryKey = serverWorld.getRegistryKey() == World.NETHER ? World.OVERWORLD : World.NETHER;
        return minecraftServer.getWorld(registryKey);
    }
}
