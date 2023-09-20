package nl.theepicblock.immersive_cursedness;

import net.minecraft.block.BlockState;
import net.minecraft.block.NetherPortalBlock;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.poi.PointOfInterest;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestTypes;
import nl.theepicblock.immersive_cursedness.objects.DummyEntity;
import nl.theepicblock.immersive_cursedness.objects.Portal;
import nl.theepicblock.immersive_cursedness.objects.TransformProfile;

import java.util.ArrayList;
import java.util.stream.Stream;

public class PortalManager {
    private final ArrayList<BlockPos> checked = new ArrayList<>();
    private ArrayList<Portal> portals = new ArrayList<>();
    private final ServerPlayerEntity player;
    private final Config config;
    public static boolean portalForcerMixinActivate = false;

    public PortalManager(ServerPlayerEntity player, Config config) {
        this.player = player;
        this.config = config;
    }

    public void update() {
        ServerWorld world = ((PlayerInterface)player).immersivecursedness$getUnfakedWorld();

        Stream<PointOfInterest> portalStream = getPortalsInChunkRadius(world.getPointOfInterestStorage(), player.getBlockPos(), config.renderDistance);
        PointOfInterest[] portals = portalStream.toArray(PointOfInterest[]::new);

        checked.clear();
        ArrayList<Portal> newPortals = new ArrayList<>();

        ServerWorld destination = Util.getDestination(player);

        for (PointOfInterest portal : portals) {
            try {
                BlockPos portalP = portal.getPos();
                if (checked.contains(portalP)) continue;

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

                boolean hasCorners = hasCorners(world, upperRight, lowerLeft, axis);
                TransformProfile transformProfile = createTransformProfile(lowerLeft, destination);
                Portal p = new Portal(upperRight, lowerLeft, axis, hasCorners, transformProfile);
                newPortals.add(p);

                BlockPos.iterate(upperRight,lowerLeft).forEach((pos) -> {
                    checked.add(pos.toImmutable());
                });
            } catch (IllegalArgumentException ignored) {}
        }
        this.portals = newPortals;
    }

    private static boolean hasCorners(ServerWorld world, BlockPos upperRight, BlockPos lowerLeft, Direction.Axis axis) {
        int frameLeft = Util.get(lowerLeft, axis)-1;
        int frameRight = Util.get(upperRight, axis)+1;
        int frameTop = upperRight.getY()+1;
        int frameBottom = lowerLeft.getY()-1;
        int oppositeAxis = Util.get(upperRight, Util.rotate(axis));

        BlockPos.Mutable mutPos = new BlockPos.Mutable();
        Util.set(mutPos, oppositeAxis, Util.rotate(axis));

        mutPos.setY(frameBottom);
        Util.set(mutPos, frameLeft, axis);
        if (!isValidCornerBlock(world,mutPos)) return false;
        Util.set(mutPos, frameRight, axis);
        if (!isValidCornerBlock(world,mutPos)) return false;

        mutPos.setY(frameTop);
        Util.set(mutPos, frameLeft, axis);
        if (!isValidCornerBlock(world,mutPos)) return false;
        Util.set(mutPos, frameRight, axis);
        if (!isValidCornerBlock(world,mutPos)) return false;

        return true;
    }

    private static boolean isValidCornerBlock(ServerWorld world, BlockPos pos) {
        return Util.getBlockAsync(world, pos).isFullCube(world, pos);
    }

    private TransformProfile createTransformProfile(BlockPos pos, ServerWorld destination) {
        DummyEntity dummyEntity = new DummyEntity(((PlayerInterface)player).immersivecursedness$getUnfakedWorld(), pos);
        dummyEntity.setBodyYaw(0);
        portalForcerMixinActivate = true;
        TeleportTarget teleportTarget = dummyEntity.getTeleportTargetB(destination);
        portalForcerMixinActivate = false;

        if (teleportTarget == null) {
            return null;
        }

        return new TransformProfile(
                pos,
                new BlockPos(MathHelper.floor(teleportTarget.position.x), MathHelper.floor(teleportTarget.position.y), MathHelper.floor(teleportTarget.position.z)),
                0,
                (int)teleportTarget.yaw);
    }

    private void garbageCollect(ServerPlayerEntity player) {
        portals.removeIf(portal ->
            portal.getDistance(player.getBlockPos()) > config.renderDistance*16
        );
        portals.removeIf(portal -> {
            for (Portal portal1 : portals) {
                if (portal1.contains(portal)) return true;
            }
            return false;
        });
    }

    public ArrayList<Portal> getPortals() {
        return portals;
    }

    private static Stream<PointOfInterest> getPortalsInChunkRadius(PointOfInterestStorage storage, BlockPos pos, int radius) {
        return ChunkPos.stream(new ChunkPos(pos), radius).flatMap((chunkPos) -> {
            return storage.getInChunk((poi) -> poi.matchesKey(PointOfInterestTypes.NETHER_PORTAL), chunkPos, PointOfInterestStorage.OccupationStatus.ANY);
        });
    }
}
