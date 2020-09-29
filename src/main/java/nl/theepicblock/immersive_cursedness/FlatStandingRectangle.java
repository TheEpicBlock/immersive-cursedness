package nl.theepicblock.immersive_cursedness;

import com.google.common.primitives.Doubles;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class FlatStandingRectangle {
    private final double top,bottom,left,right,other;
    private final Direction.Axis axis;

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
        return createBlockPos(clamp(bottom,0,255), left);
    }

    public BlockPos getTopRightBlock() {
        return createBlockPos(clamp(top-1,0,255), right-1);
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

    private Vec3d createVec3d(double y, double primaryAxis) {
        if (axis == Direction.Axis.X) {
            return new Vec3d(other, y, primaryAxis);
        } else {
            return new Vec3d(primaryAxis, y, other);
        }
    }

    private BlockPos createBlockPos(double y, double primaryAxis) {
        if (axis == Direction.Axis.X) {
            return new BlockPos(other, y, primaryAxis);
        } else {
            return new BlockPos(primaryAxis, y, other);
        }
    }

    private double clamp(double val, double min, double max) {
        return Math.max(min, Math.min(max, val));
    }
}
