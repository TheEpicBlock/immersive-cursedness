package nl.theepicblock.immersive_cursedness;

import net.minecraft.block.Block;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.poi.PointOfInterest;

import java.util.List;
import java.util.stream.Stream;

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

    public static void sendParticle(ServerPlayerEntity player, Vec3d pos) {
        player.networkHandler.sendPacket(new ParticleS2CPacket(new DustParticleEffect(0,1,0,1), true, pos.x, pos.y, pos.z, 0, 0, 0, 0, 0));
    }

    public static BlockPos makeBlockPos(double x, double y, double z) {
        return new BlockPos((int)Math.round(x), (int)Math.round(y), (int)Math.round(z));
    }
}
