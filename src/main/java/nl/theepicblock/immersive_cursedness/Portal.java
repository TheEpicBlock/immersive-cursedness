package nl.theepicblock.immersive_cursedness;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import javax.sound.sampled.Port;

public class Portal {
    //Right is defined as the most positive point in whatever axis this is
    private BlockPos upperRight;
    private BlockPos lowerLeft;
    private Direction.Axis axis;

    public Portal(BlockPos upperRight, BlockPos lowerLeft, Direction.Axis axis) {
        this.upperRight = upperRight;
        this.lowerLeft = lowerLeft;
        this.axis = axis;
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
        return new FlatStandingRectangle(
                this.getTop()+1,
                this.getBottom(),
                this.getLeft(),
                this.getRight()+1,
                Util.get(this.getUpperRight(),Util.rotate(axis))+0.5,
                Util.rotate(axis)
        );
    }
}
