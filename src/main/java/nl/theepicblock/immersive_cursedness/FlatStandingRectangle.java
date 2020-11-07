package nl.theepicblock.immersive_cursedness;

import com.google.common.collect.Iterators;
import com.google.common.primitives.Doubles;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.Collections;
import java.util.Iterator;

public class FlatStandingRectangle {
    //Right is defined as the most positive point in whatever axis this is
    protected final double top,bottom,left,right,other;
    protected final Direction.Axis axis;

    public FlatStandingRectangle(double top, double bottom, double left, double right, double other, Direction.Axis axis) {
        this.top = top;
        this.bottom = bottom;
        this.left = left;
        this.right = right;
        this.other = other;
        this.axis = axis;
    }

    public Vec3d getBottomRight() {
        return createVec3d(bottom, right);
    }

    public Vec3d getBottomLeft() {
        return createVec3d(bottom, left);
    }

    public Vec3d getTopRight() {
        return createVec3d(top, right);
    }

    public Vec3d getTopLeft() {
        return createVec3d(top, left);
    }

    public BlockPos getBottomLeftBlock() {
        return createBlockPos(bottom, left);
    }

    public BlockPos getTopRightBlock() {
        return createBlockPos(top-1, right-1);
    }

    public BlockPos getBottomLeftBlockClamped(Vec3d center, int limit) {
        double centerP = Util.get(center, Util.rotate(axis));
        return createBlockPos(clamp(bottom,0,255), clamp(left,centerP-limit,centerP+limit));
    }

    public BlockPos getTopRightBlockClamped(Vec3d center, int limit) {
        double centerP = Util.get(center, Util.rotate(axis));
        return createBlockPos(clamp(top-1,0,255), clamp(right-1,centerP-limit,centerP+limit));
    }

    public FlatStandingRectangle expand(int i, Vec3d source) {
        double distance = Util.get(source, this.axis)-this.other;
        double sourceForPrimaryAxis = Util.get(source,Util.rotate(axis));
        double newOther = this.other<Util.get(source,axis) ? this.other-i : this.other+i;
        double newDistance = Util.get(source, this.axis)-newOther;
        return new FlatStandingRectangle(
                source.y+(this.top-source.y)/distance*newDistance,
                source.y+(this.bottom-source.y)/distance*newDistance,
                sourceForPrimaryAxis+(this.left-sourceForPrimaryAxis)/distance*newDistance,
                sourceForPrimaryAxis+(this.right-sourceForPrimaryAxis)/distance*newDistance,
                newOther,
                this.axis
        );
    }

    public boolean contains(Vec3d pos) {
        return  pos.y > this.bottom &&
                pos.y < this.top &&
                Util.get(pos, Util.rotate(axis)) > this.left &&
                Util.get(pos, Util.rotate(axis)) < this.right &&
                Util.get(pos, axis) < this.other+0.5 &&
                Util.get(pos, axis) > this.other-0.5;
    }

    public boolean isBeside(Vec3d pos) {
        return  Util.get(pos, axis) < this.other+0.5 &&
                Util.get(pos, axis) > this.other-0.5;
    }

    public Iterable<BlockPos> iterateClamped(Vec3d center, int limit) {
        BlockPos pos1 = this.getBottomLeftBlockClamped(center, limit);
        BlockPos pos2 = this.getTopRightBlockClamped(center, limit);
        Direction.Axis reverseAxis = Util.rotate(this.axis);
        if (Util.get(pos1, reverseAxis) == Util.get(pos2, reverseAxis))
            return Collections::emptyIterator;

        return BlockPos.iterate(pos1, pos2);
    }

    private Vec3d createVec3d(double y, double primaryAxis) {
        if (axis == Direction.Axis.X) {
            return new Vec3d(other, y, primaryAxis);
        } else {
            return new Vec3d(primaryAxis, y, other);
        }
    }

    private BlockPos createBlockPos(double y, double primaryAxis) {
        if (axis == Direction.Axis.X) {
            return Util.makeBlockPos(other, y, primaryAxis);
        } else {
            return Util.makeBlockPos(primaryAxis, y, other);
        }
    }

    private double clamp(double val, double min, double max) {
        return Math.max(min, Math.min(max, val));
    }
}
