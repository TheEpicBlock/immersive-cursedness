package nl.theepicblock.immersive_cursedness;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TransformProfile {
    private final BlockPos transform;
    private final BlockPos target;
    private final int rotation;

    public TransformProfile(BlockPos original, BlockPos target, int originalRot, int targetRot) {
        this.transform = target.subtract(original);
        this.target = target;
        int rotation = targetRot-originalRot;
        if (rotation == -270) rotation = 90;
        if (rotation == 270) rotation = -90;
        if (rotation == -180) rotation = 180;
        this.rotation = rotation;
    }

    public BlockPos transform(BlockPos in) {
        BlockPos ret = in.add(transform);
        ret = ret.subtract(target);
        ret = rotate(ret);
        return ret.add(target);
    }

    public BlockPos rotate(BlockPos in) {
        switch (rotation) {
            default:
                return in;
            case 90:
                return new BlockPos(-in.getZ(), in.getY(), in.getX());
            case -90:
                return new BlockPos(in.getZ(), in.getY(), -in.getX());
            case 180:
                return new BlockPos(-in.getZ(), in.getY(), -in.getX());
        }
    }

    public BlockState rotateState(BlockState in) {
        switch (rotation) {
            default:
                return in;
            case 90:
                return in.rotate(BlockRotation.CLOCKWISE_90);
            case -90:
                return in.rotate(BlockRotation.COUNTERCLOCKWISE_90);
            case 180:
                return in.rotate(BlockRotation.CLOCKWISE_180);
        }
    }

    public BlockState transformAndGetFromWorld(BlockPos pos, World world) {
        BlockPos transformedPos = this.transform(pos);
        BlockState state = world.getBlockState(transformedPos);
        return this.rotateState(state);
    }
}
