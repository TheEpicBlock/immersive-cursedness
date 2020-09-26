package nl.theepicblock.immersive_cursedness;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class Portal extends AxisAlignedRectangle {
    public Portal(BlockPos upperRight, BlockPos lowerLeft, Direction.Axis axis) {
        super(upperRight, lowerLeft, axis);
    }
}
