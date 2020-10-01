package nl.theepicblock.immersive_cursedness;

import net.minecraft.block.BlockState;
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
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

public class PlayerManager {
    private final static int SEND_LIMIT = 31;
    private final static int SEND_LAYERS = 30;
    private final static double ATMOSPHERE_DISTANCE = Math.pow(29,2);
    private final ServerPlayerEntity player;
    private final PortalManager portalManager;
    private HashMap<BlockPos,BlockState> sentBlocks = new HashMap<>();

    public PlayerManager(ServerPlayerEntity player) {
        this.player = player;
        portalManager = new PortalManager();
    }

    public void tick() {
        portalManager.update(player);
        ServerWorld serverWorld = this.player.getServerWorld();
        ServerWorld destination = this.getDestination();

        HashMap<BlockPos,BlockState> newSentBlocks = new HashMap<>();

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

            BlockState atmosphereBlock = (serverWorld.getRegistryKey() == World.NETHER ? Blocks.BLUE_CONCRETE : Blocks.NETHER_WART_BLOCK).getDefaultState();
            BlockState atmosphereBetweenBlock = (serverWorld.getRegistryKey() == World.NETHER ? Blocks.BLUE_STAINED_GLASS : Blocks.RED_STAINED_GLASS).getDefaultState();

            //iterate through all layers behind the portal
            FlatStandingRectangle rect = portal.toFlatStandingRectangle();
            for (int i = 1; i < SEND_LAYERS; i++) {
                FlatStandingRectangle rect2 = rect.expand(i, player.getCameraPosVec(1));
                BlockPos pos1 = rect2.getBottomLeftBlockClamped(player.getPos(), SEND_LIMIT);
                BlockPos pos2 = rect2.getTopRightBlockClamped(player.getPos(), SEND_LIMIT);

                //go through all blocks in this layer and use the transformProfile to get the correct block in the nether. Then send it to the client
                BlockPos.iterate(pos1, pos2).forEach(pos -> {
                    BlockPos imPos = pos.toImmutable();

                    double dist = imPos.getSquaredDistance(player.getBlockPos());
                    if (dist > ATMOSPHERE_DISTANCE + 100) return;

                    BlockState ret;

                    if (dist > ATMOSPHERE_DISTANCE) {
                        ret = atmosphereBlock;
                    } else if (dist > ATMOSPHERE_DISTANCE - 100) {
                        ret = atmosphereBetweenBlock;
                    } else {
                        ret = transformProfile.transformAndGetFromWorld(imPos, destination);
                    }

                    newSentBlocks.put(imPos, ret);
                    if (!(sentBlocks.get(pos) == ret)) {
                        player.networkHandler.sendPacket(new BlockUpdateS2CPacket(imPos, ret));
                    }
                });
            }

        });

        //get all of the old blocks and remove them
        sentBlocks.entrySet().removeIf(entry -> newSentBlocks.containsKey(entry.getKey()));
        sentBlocks.forEach((pos,i) -> {
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
