package nl.theepicblock.immersive_cursedness;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import javax.sound.sampled.Port;

public class Portal {
    //Right is defined as the most positive point in whatever axis this is
    private final BlockPos upperRight;
    private final BlockPos lowerLeft;
    private final Direction.Axis axis;
    private final boolean hasCorners;
    private final TransformProfile transformProfile;

    public Portal(BlockPos upperRight, BlockPos lowerLeft, Direction.Axis axis, boolean hasCorners, TransformProfile transformProfile) {
        this.upperRight = upperRight;
        this.lowerLeft = lowerLeft;
        this.axis = axis;
        this.hasCorners = hasCorners;
        this.transformProfile = transformProfile;
    }

    public double getDistance(BlockPos pos) {
        return upperRight.getSquaredDistance(pos);
    }

    /**
     * Returns true if this rectangle fully encloses b
     */
    public boolean contains(Portal b) {
        if (this.getAxis() != b.getAxis()) return false;
        if (this.getTop() < b.getTop() ||
                this.getBottom() > b.getBottom()) return false;
        Direction.Axis axis = this.getAxis();
        return this.getRight() >= b.getRight() &&
                this.getLeft() <= b.getLeft();
    }

    public BlockPos getUpperRight() {
        return upperRight;
    }

    public BlockPos getLowerLeft() {
        return lowerLeft;
    }

    public Direction.Axis getAxis() {
        return axis;
    }

    public TransformProfile getTransformProfile() {
        return transformProfile;
    }

    public int getLeft() {
        return Util.get(this.getLowerLeft(), this.getAxis());
    }

    public int getRight() {
        return Util.get(this.getUpperRight(), this.getAxis());
    }

    public int getTop() {
        return this.getUpperRight().getY();
    }

    public int getBottom() {
        return this.getLowerLeft().getY();
    }

    public FlatStandingRectangle toFlatStandingRectangle() {
        if (this.hasCorners) {
            return new FlatStandingRectangle(
                    this.getTop()+1.5,
                    this.getBottom()-0.5,
                    this.getLeft()-0.5,
                    this.getRight()+1.5,
                    Util.get(this.getUpperRight(),Util.rotate(axis)),
                    Util.rotate(axis)
            );
        } else {
            return new RectangleWithCutoutCorners(
                    this.getTop()+1.5,
                    this.getBottom()-0.5,
                    this.getLeft()-0.5,
                    this.getRight()+1.5,
                    Util.get(this.getUpperRight(),Util.rotate(axis)),
                    Util.rotate(axis)
            );
        }
    }

    public int getYawRelativeTo(BlockPos pos) {
        if (this.axis == Direction.Axis.Z) {
            if (pos.getX()-this.lowerLeft.getX()<0) {
                return -90;
            } else {
                return 90;
            }
        } else {
            if (pos.getZ()-this.lowerLeft.getZ()<0) {
                return 0;
            } else {
                return 180;
            }
        }
    }
}
