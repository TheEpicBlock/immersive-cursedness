package nl.theepicblock.immersive_cursedness;

import com.google.common.collect.Iterables;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class RectangleWithCutoutCorners extends FlatStandingRectangle{
	static private final double ERROR_MARGIN = 0.25D;
	final double percentageCornerWidth;
	final double percentageCornerHeight;

	public RectangleWithCutoutCorners(double top, double bottom, double left, double right, double other, Direction.Axis axis) {
		this(top, bottom, left, right, other, axis, 0.5D);
	}

	public RectangleWithCutoutCorners(double top, double bottom, double left, double right, double other, Direction.Axis axis, double singleCornerSizeInBlocks) {
		super(top, bottom, left, right, other, axis);
		percentageCornerHeight = (singleCornerSizeInBlocks+ERROR_MARGIN)/(top-bottom);
		percentageCornerWidth = (singleCornerSizeInBlocks+ERROR_MARGIN)/(right-left);
	}

	public RectangleWithCutoutCorners(double top, double bottom, double left, double right, double other, Direction.Axis axis, double percentageCornerWidth, double percentageCornerHeight) {
		super(top, bottom, left, right, other, axis);
		this.percentageCornerWidth = percentageCornerWidth;
		this.percentageCornerHeight = percentageCornerHeight;
	}

	@Override
	public FlatStandingRectangle expand(int i, Vec3d source) {
		double distance = Util.get(source, this.axis)-this.other;
		double sourceForPrimaryAxis = Util.get(source,Util.rotate(axis));
		double newOther = this.other<Util.get(source,axis) ? this.other-i : this.other+i;
		double newDistance = Util.get(source, this.axis)-newOther;
		return new RectangleWithCutoutCorners(
				source.y+(this.top-source.y)/distance*newDistance,
				source.y+(this.bottom-source.y)/distance*newDistance,
				sourceForPrimaryAxis+(this.left-sourceForPrimaryAxis)/distance*newDistance,
				sourceForPrimaryAxis+(this.right-sourceForPrimaryAxis)/distance*newDistance,
				newOther,
				this.axis,
				percentageCornerWidth,
				percentageCornerHeight
		);
	}

	@Override
	public boolean contains(Vec3d pos) {
		if (super.contains(pos)) {
			double height = (this.top-this.bottom);
			double width = (this.right-this.left);
			double hPerc = (pos.y-this.bottom)/height;
			double wPerc = (Util.get(pos, Util.rotate(axis))-this.left)/width;

			return (hPerc>percentageCornerHeight&&hPerc<(1-percentageCornerHeight)) || (wPerc>percentageCornerWidth&&wPerc<(1-percentageCornerWidth));
		}
		return false;
	}

	@Override
	public Iterable<BlockPos> iterateClamped(Vec3d center, int limit) {
		return Iterables.filter(super.iterateClamped(center, limit), (blockPos) -> {
			return contains(Util.getCenter(blockPos));
		});
	}
}
