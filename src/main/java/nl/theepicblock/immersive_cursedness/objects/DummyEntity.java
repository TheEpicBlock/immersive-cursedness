package nl.theepicblock.immersive_cursedness.objects;

import net.minecraft.block.BlockState;
import net.minecraft.class_5459;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Packet;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
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
    protected void readCustomDataFromTag(CompoundTag tag) {

    }

    @Override
    protected void writeCustomDataToTag(CompoundTag tag) {

    }

    @Override
    public Packet<?> createSpawnPacket() {
        return null;
    }

    @Override
    public void setYaw(float yaw) {
        this.yaw = yaw;
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
            double coordinateScale = DimensionType.method_31109(this.world.getDimension(), destination.getDimension());
            BlockPos blockPos3 = new BlockPos(this.getX() * coordinateScale, this.getY(), this.getZ() * coordinateScale);
            Optional<class_5459.class_5460> portalPosA = this.method_30330(destination, blockPos3, bl3);
            if (portalPosA.isPresent()) {
                BlockState blockState = Util.getBlockAsync((ServerWorld)this.world, this.lastNetherPortalPosition);
                Direction.Axis axis2;
                Vec3d vec3d2;
                if (blockState.contains(Properties.HORIZONTAL_AXIS)) {
                    axis2 = blockState.get(Properties.HORIZONTAL_AXIS);
                    class_5459.class_5460 portalPos = class_5459.method_30574(this.lastNetherPortalPosition, axis2, 21, Direction.Axis.Y, 21, (blockPos) -> Util.getBlockAsync((ServerWorld)this.world, blockPos) == blockState);
                    vec3d2 = this.method_30633(axis2, portalPos);
                } else {
                    return null;
                }

                return AreaHelper.method_30484(destination, portalPosA.get(), axis2, vec3d2, this.getDimensions(this.getPose()), this.getVelocity(), this.yaw, this.pitch);
            } else {
                return null;
            }
        }
    }
}
