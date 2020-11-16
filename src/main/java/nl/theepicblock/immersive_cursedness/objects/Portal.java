package nl.theepicblock.immersive_cursedness.objects;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import nl.theepicblock.immersive_cursedness.Util;

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

    public boolean isBlockposBehind(BlockPos p, Vec3d originContext) {
        if (!isBehind(originContext, p)) return false;
        FlatStandingRectangle rect = this.toFlatStandingRectangle();
        FlatStandingRectangle rect2 = rect.expandAbsolute(Util.get(p, rect.axis), originContext);
        return rect2.contains(p);
    }

    private boolean isBehind(Vec3d origin, BlockPos p) {
        Direction.Axis rot = Util.rotate(axis);
        int a = Util.get(p, rot);
        double b = Util.get(origin, rot);
        double middle = Util.get(upperRight, rot);
        return a < middle ^ middle > b;
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

    public boolean isCloserThan(Vec3d p, int i) {
        double lrp = Util.get(p, axis);
        double lrmin = Util.get(lowerLeft, axis);
        double lrmax = Util.get(upperRight, axis)+1;
        double lrd;
        if (lrp > lrmax) {
            lrd = lrp-lrmax;
        } else if (lrp < lrmin) {
            lrd = lrmin-lrp;
        } else {
            lrd = 0;
        }
        if (lrd > i) return false;

        double yp = p.y;
        double ymin = lowerLeft.getY();
        double ymax = upperRight.getY()+1;
        double yd;
        if (yp > ymax) {
            yd = yp-ymax;
        } else if (yp < ymin) {
            yd = ymin-yp;
        } else {
            yd = 0;
        }
        if (yd > i) return false;

        Direction.Axis other = Util.rotate(axis);
        double od = Math.abs(Util.get(p, other)-Util.get(lowerLeft,other));
        if (od > i) return false;
        return (lrd*lrd+yd*yd*od*od)<i*i;
    }
}
