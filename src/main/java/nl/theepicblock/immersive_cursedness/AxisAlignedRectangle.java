package nl.theepicblock.immersive_cursedness;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class AxisAlignedRectangle {
    //Right is defined as the most positive point in whatever axis this is
    private BlockPos upperRight;
    private BlockPos lowerLeft;
    private Direction.Axis axis;

    public AxisAlignedRectangle(BlockPos upperRight, BlockPos lowerLeft, Direction.Axis axis) {
        this.upperRight = upperRight;
        this.lowerLeft = lowerLeft;
        this.axis = axis;
    }
}
