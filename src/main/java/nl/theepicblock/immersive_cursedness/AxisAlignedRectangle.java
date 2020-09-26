package nl.theepicblock.immersive_cursedness;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public interface AxisAlignedRectangle<T> {
    double getDistance(T pos);

    T getUpperRight();

    T getLowerLeft();

    Direction.Axis getAxis();
}
