package nl.theepicblock.immersive_cursedness.objects;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import nl.theepicblock.immersive_cursedness.Util;

public class TransformProfile {
    private final int originalX;
    private final int originalY;
    private final int originalZ;
    private final int targetX;
    private final int targetY;
    private final int targetZ;
    private final int rotation;

    public TransformProfile(BlockPos original, BlockPos target, int originalRot, int targetRot) {
        this.originalX = original.getX();
        this.originalY = original.getY();
        this.originalZ = original.getZ();
        this.targetX = target.getX();
        this.targetY = target.getY();
        this.targetZ = target.getZ();

        int rotation = targetRot-originalRot;
        if (rotation == -270) rotation = 90;
        if (rotation == 270) rotation = -90;
        if (rotation == -180) rotation = 180;
        this.rotation = rotation;
    }

    public int transformYOnly(int y) {
        return y-originalY+targetY;
    }

    public int unTransformYOnly(int y) {
        return y-targetY+originalY;
    }

    // rot(in - original) + target
    public BlockPos transform(BlockPos in) {
        int x = in.getX();
        int y = in.getY();
        int z = in.getZ();

        // Swap x and z
        if (this.rotation != 0) {
            var temp = x;
            x = z;
            z = temp;
        }

        // We have to flip the x or the y their sign in certain situations
        var rotbitX = (this.rotation == -90 || this.rotation == 180) ? -1 : 1;
        var rotbitY = (this.rotation == 90 || this.rotation == 180) ? -1 : 1;

        return new BlockPos(
                rotbitX * (x - originalX) + targetX,
                (y - originalY) + targetY,
                rotbitY * (z - originalZ) + targetZ
        );
    }

    public Vec3d transform(Vec3d in) {
        double x = in.getX();
        double y = in.getY();
        double z = in.getZ();

        // Swap x and z
        if (this.rotation != 0) {
            var temp = x;
            x = z;
            z = temp;
        }

        // We have to flip the x or the y their sign in certain situations
        var rotbitX = (this.rotation == -90 || this.rotation == 180) ? -1 : 1;
        var rotbitY = (this.rotation == 90 || this.rotation == 180) ? -1 : 1;

        return new Vec3d(
                rotbitX * (x - originalX) + targetX,
                (y - originalY) + targetY,
                rotbitY * (z - originalZ) + targetZ
        );
    }

    public BlockState rotateState(BlockState in) {
        return switch (rotation) {
            default -> in;
            case 90 -> in.rotate(BlockRotation.COUNTERCLOCKWISE_90);
            case -90 -> in.rotate(BlockRotation.CLOCKWISE_90);
            case 180 -> in.rotate(BlockRotation.CLOCKWISE_180);
        };
    }

    public Direction rotate(Direction in) {
        return switch (in) {
            default -> in;
            case NORTH -> switch (this.rotation) {
                default -> in;
                case 90 -> Direction.WEST;
                case -90 -> Direction.EAST;
                case 190 -> Direction.SOUTH;
            };
            case WEST -> switch (this.rotation) {
                default -> in;
                case 90 -> Direction.SOUTH;
                case -90 -> Direction.NORTH;
                case 190 -> Direction.EAST;
            };
            case EAST -> switch (this.rotation) {
                default -> in;
                case 90 -> Direction.NORTH;
                case -90 -> Direction.SOUTH;
                case 190 -> Direction.WEST;
            };
            case SOUTH -> switch (this.rotation) {
                default -> in;
                case 90 -> Direction.EAST;
                case -90 -> Direction.WEST;
                case 190 -> Direction.NORTH;
            };
        };
    }

    public BlockState transformAndGetFromWorld(BlockPos pos, AsyncWorldView world) {
        BlockPos transformedPos = this.transform(pos);
        BlockState state = world.getBlock(transformedPos);
        return this.rotateState(state);
    }
}
