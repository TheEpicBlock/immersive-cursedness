package nl.theepicblock.immersive_cursedness;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.poi.PointOfInterest;

import java.util.List;
import java.util.stream.Stream;

public class Util {
    public static int follow(PointOfInterest[] list, BlockPos start, Direction direction) {
        for (int i = 1; i < 50; i++) {
            BlockPos np = start.offset(direction, i);

            if (!contains(list,np)) return i-1;
        }
        return 50;
    }

    public static boolean contains(PointOfInterest[] list, BlockPos b) {
        for (PointOfInterest poi : list) {
            if (poi.getPos().equals(b)) return true;
        }
        return false;
    }
}
