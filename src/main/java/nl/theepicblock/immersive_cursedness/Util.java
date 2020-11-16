package nl.theepicblock.immersive_cursedness;

import com.mojang.datafixers.util.Either;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.*;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.poi.PointOfInterest;
import nl.theepicblock.immersive_cursedness.mixin.ServerChunkManagerInvoker;

import java.util.Optional;

public class Util {
    public static int follow(PointOfInterest[] list, BlockPos start, Direction direction) {
        for (int i = 1; i < 50; i++) {
            BlockPos np = start.offset(direction, i);

            if (!contains(list,np)) return i-1;
        }
        return 50;
    }

    public static boolean contains(PointOfInterest[] list, BlockPos b) {
        for (PointOfInterest poi : list) {
            if (poi.getPos().equals(b)) return true;
        }
        return false;
    }

    public static int get(BlockPos b, Direction.Axis axis) {
        switch (axis) {
            case X:
                return b.getX();
            case Y:
                return b.getY();
            case Z:
                return b.getZ();
            default:
                return 0;
        }
    }

    public static double get(Vec3d b, Direction.Axis axis) {
        switch (axis) {
            case X:
                return b.getX();
            case Y:
                return b.getY();
            case Z:
                return b.getZ();
            default:
                return 0;
        }
    }

    public static void set(BlockPos.Mutable b, int i, Direction.Axis axis) {
        switch (axis) {
            case X:
                b.setX(i);
                break;
            case Y:
                b.setY(i);
                break;
            case Z:
                b.setZ(i);
                break;
        }
    }

    public static Direction.Axis rotate(Direction.Axis axis) {
        switch (axis) {
            case X:
                return Direction.Axis.Z;
            case Y:
                return Direction.Axis.Y;
            case Z:
                return Direction.Axis.X;
            default:
                return Direction.Axis.Z;
        }
    }

    public static void sendBlock(ServerPlayerEntity player, BlockPos pos, Block block) {
        player.networkHandler.sendPacket(new BlockUpdateS2CPacket(pos, block.getDefaultState()));
    }

    public static void sendParticle(ServerPlayerEntity player, Vec3d pos, float r, float g, float b) {
        player.networkHandler.sendPacket(new ParticleS2CPacket(new DustParticleEffect(r,g,b,1), true, pos.x, pos.y, pos.z, 0, 0, 0, 0, 0));
    }

    public static BlockPos makeBlockPos(double x, double y, double z) {
        return new BlockPos((int)Math.round(x), (int)Math.round(y), (int)Math.round(z));
    }

    public static Vec3d getCenter(BlockPos p) {
        return new Vec3d(
                p.getX()+0.5d,
                p.getY()+0.5d,
                p.getZ()+0.5d
        );
    }

    public static Vec3d add(Vec3d v, Direction d, double b) {
        return v.add(d.getOffsetX()*b, d.getOffsetY()*b, d.getOffsetZ()*b);
    }

    private static final BlockState AIR = Blocks.AIR.getDefaultState();
    public static BlockState getBlockAsync(ServerWorld world, BlockPos pos) {
        Optional<Chunk> chunkOptional = getChunkAsync(world, pos.getX() >> 4, pos.getZ() >> 4);
        if (!chunkOptional.isPresent()) return AIR;
        return chunkOptional.get().getBlockState(pos);
    }

    public static Optional<Chunk> getChunkAsync(ServerWorld world, int x, int z) {
        ServerChunkManagerInvoker chunkManager = (ServerChunkManagerInvoker)world.getChunkManager();
        Either<Chunk,ChunkHolder.Unloaded> either = chunkManager.callGetChunkFuture(x, z, ChunkStatus.FULL, false).join();
        return either.left();
    }

    public static ServerWorld getDestination(ServerPlayerEntity player) {
        return getDestination(((PlayerInterface)player).getUnfakedWorld());
    }

    public static ServerWorld getDestination(ServerWorld serverWorld) {
        MinecraftServer minecraftServer = serverWorld.getServer();
        RegistryKey<World> registryKey = serverWorld.getRegistryKey() == World.NETHER ? World.OVERWORLD : World.NETHER;
        return minecraftServer.getWorld(registryKey);
    }

    /**
     * Normally 0.5 gets added to the distance of blockpos. This method doesn't do that.
     * @see Vec3i#getSquaredDistance(double, double, double, boolean)
     */
    public static double getDistance(BlockPos a, BlockPos b) {
        int x = a.getX() - b.getX();
        int y = a.getY() - b.getY();
        int z = a.getZ() - b.getZ();

        double xx = (double)x*(double)x;
        double yy = (double)y*(double)y;
        double zz = (double)z*(double)z;

        return xx + yy + zz;
    }

    public static PlayerManager getManagerFromPlayer(ServerPlayerEntity player) {
        return ImmersiveCursedness.cursednessServer.getManager(player);
    }
}
