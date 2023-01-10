package nl.theepicblock.immersive_cursedness.objects;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import net.minecraft.world.BlockLocating;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.dimension.AreaHelper;
import net.minecraft.world.dimension.DimensionType;
import nl.theepicblock.immersive_cursedness.Util;

import java.util.Optional;

@SuppressWarnings("EntityConstructor")
public class DummyEntity extends Entity {
    public DummyEntity(World world, BlockPos pos) {
        super(EntityType.BLAZE, world);
        this.setPos(pos.getX()+0.5,pos.getY(),pos.getZ()+0.5);
    }

    @Override
    protected void initDataTracker() {
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound tag) {
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound tag) {
    }

    @Override
    public Packet<?> createSpawnPacket() {
        return null;
    }

    @Override
    public void setBodyYaw(float yaw) {
        this.setYaw(yaw);
    }

    public TeleportTarget getTeleportTargetB(ServerWorld destination) {
        this.setInNetherPortal(this.getBlockPos());
        return this.getTeleportTarget(destination);
    }

    @Override
    protected TeleportTarget getTeleportTarget(ServerWorld destination) {
        boolean bl3 = destination.getRegistryKey() == World.NETHER;
        if (this.world.getRegistryKey() != World.NETHER && !bl3) {
            return null;
        } else {
            double coordinateScale = DimensionType.getCoordinateScaleFactor(this.world.getDimension(), destination.getDimension());
            BlockPos blockPos3 = new BlockPos(this.getX() * coordinateScale, this.getY(), this.getZ() * coordinateScale);
            WorldBorder worldBorder = destination.getWorldBorder();
            Optional<BlockLocating.Rectangle> portalPosA = this.getPortalRect(destination, blockPos3, bl3, worldBorder);
            if (portalPosA.isPresent()) {
                BlockState blockState = Util.getBlockAsync((ServerWorld)this.world, this.lastNetherPortalPosition);
                Direction.Axis axis2;
                Vec3d vec3d2;
                if (blockState.contains(Properties.HORIZONTAL_AXIS)) {
                    axis2 = blockState.get(Properties.HORIZONTAL_AXIS);
                    BlockLocating.Rectangle portalPos = BlockLocating.getLargestRectangle(this.lastNetherPortalPosition, axis2, 21, Direction.Axis.Y, 21, (blockPos) -> Util.getBlockAsync((ServerWorld)this.world, blockPos) == blockState);
                    vec3d2 = this.positionInPortal(axis2, portalPos);
                } else {
                    return null;
                }

                return AreaHelper.getNetherTeleportTarget(destination, portalPosA.get(), axis2, vec3d2, this.getDimensions(this.getPose()), this.getVelocity(), this.getYaw(), this.getPitch());
            } else {
                return null;
            }
        }
    }
}
