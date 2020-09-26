package nl.theepicblock.immersive_cursedness;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.NetherPortalBlock;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.poi.PointOfInterest;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PortalManager {
    private final ArrayList<BlockPos> checked = new ArrayList<>();
    private final ArrayList<BlockPos> newChecked = new ArrayList<>();

    public void update(ServerPlayerEntity player) {
        ServerWorld world = player.getServerWorld();

        Stream<PointOfInterest> portalStream = getPortalsInChunkRadius(world.getPointOfInterestStorage(), player.getBlockPos(), CursednessServer.PORTAL_RENDER_DISTANCE);
        PointOfInterest[] portals = portalStream.toArray(PointOfInterest[]::new);
        newChecked.clear();

        for (PointOfInterest portal : portals) {
            try {
                BlockPos portalP = portal.getPos();
                newChecked.add(portalP);
                if (checked.contains(portalP)) return;

                BlockState bs = world.getBlockState(portalP);
                Direction.Axis axis = bs.get(NetherPortalBlock.AXIS);

                int lowestPoint = portalP.getY()-Util.follow(portals, portalP, Direction.DOWN);
                int highestPoint = portalP.getY()+Util.follow(portals, portalP, Direction.UP);
                int rightOffset = Util.follow(portals, portalP, Direction.from(axis, Direction.AxisDirection.POSITIVE));
                int leftOffset = Util.follow(portals, portalP, Direction.from(axis, Direction.AxisDirection.NEGATIVE));

                BlockPos upperRight;
                BlockPos lowerLeft;
                if (axis == Direction.Axis.X) {
                    upperRight = new BlockPos(portalP.getX()+rightOffset, highestPoint, portalP.getZ());
                    lowerLeft = new BlockPos(portalP.getX()-leftOffset, lowestPoint, portalP.getZ());
                } else {
                    upperRight = new BlockPos(portalP.getX(), highestPoint, portalP.getZ()+rightOffset);
                    lowerLeft = new BlockPos(portalP.getX(), lowestPoint, portalP.getZ()-leftOffset);
                }

                Portal p = new Portal(upperRight, lowerLeft, axis);

            } catch (IllegalArgumentException ignored) {}
        }

        checked.clear();
        checked.addAll(newChecked);
    }

    private Stream<PointOfInterest> getPortalsInChunkRadius(PointOfInterestStorage storage, BlockPos pos, int radius) {
        return ChunkPos.stream(new ChunkPos(pos), radius).flatMap((chunkPos) -> {
            return storage.getInChunk((poi) -> poi == PointOfInterestType.NETHER_PORTAL, chunkPos, PointOfInterestStorage.OccupationStatus.ANY);
        });
    }
}
