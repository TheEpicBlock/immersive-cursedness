package nl.theepicblock.immersive_cursedness.objects;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import nl.theepicblock.immersive_cursedness.Util;

public class TransformProfile {
    private final ImPos transform;
    private final ImPos target;
    private final int rotation;

    public TransformProfile(BlockPos original, BlockPos target, int originalRot, int targetRot) {
        this.transform = new ImPos(target.subtract(original));
        this.target = new ImPos(target);
        int rotation = targetRot-originalRot;
        if (rotation == -270) rotation = 90;
        if (rotation == 270) rotation = -90;
        if (rotation == -180) rotation = 180;
        this.rotation = rotation;
    }

    public BlockPos transform(BlockPos in) {
        ImPos pos = new ImPos(
                in.getX()+transform.getX()-target.getX(),
                in.getY()+transform.getY()-target.getY(),
                in.getZ()+transform.getZ()-target.getZ()
        );
        pos = rotate(pos);
        return pos.addIntoBlockPos(target);
    }

    public BlockPos untransform(BlockPos in) {
        ImPos pos = new ImPos(
                in.getX()-target.getX(),
                in.getY()-target.getY(),
                in.getZ()-target.getZ()
        );
        pos = unrotate(pos);
        return new BlockPos(
                pos.x+target.getX()-transform.getX(),
                pos.y+target.getY()-transform.getY(),
                pos.z+target.getZ()-transform.getZ()
        );
    }

    public ImPos rotate(ImPos in) {
        switch (rotation) {
            default:
                return in;
            case 90:
                return new ImPos(-in.getZ(), in.getY(), in.getX());
            case -90:
                return new ImPos(in.getZ(), in.getY(), -in.getX());
            case 180:
                return new ImPos(-in.getZ(), in.getY(), -in.getX());
        }
    }

    public ImPos unrotate(ImPos in) {
        switch (rotation) {
            default:
                return in;
            case -90:
                return new ImPos(-in.getZ(), in.getY(), in.getX());
            case 90:
                return new ImPos(in.getZ(), in.getY(), -in.getX());
            case 180:
                return new ImPos(-in.getZ(), in.getY(), -in.getX());
        }
    }

    public BlockState rotateState(BlockState in) {
        switch (rotation) {
            default:
                return in;
            case 90:
                return in.rotate(BlockRotation.COUNTERCLOCKWISE_90);
            case -90:
                return in.rotate(BlockRotation.CLOCKWISE_90);
            case 180:
                return in.rotate(BlockRotation.CLOCKWISE_180);
        }
    }

    public BlockState transformAndGetFromWorld(BlockPos pos, AsyncWorldView world) {
        BlockPos transformedPos = this.transform(pos);
        BlockState state = world.getBlock(transformedPos);
        return this.rotateState(state);
    }

    private static class ImPos {
        final private int x;
        final private int y;
        final private int z;

        public ImPos(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public ImPos(BlockPos p) {
            this(p.getX(), p.getY(), p.getZ());
        }

        public BlockPos addIntoBlockPos(ImPos v) {
            return new BlockPos(x+v.x,y+v.y,z+v.z);
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public int getZ() {
            return z;
        }
    }
}
