package nl.theepicblock.immersive_cursedness;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Packet;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;

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

    public TeleportTarget getTeleportTargetB(ServerWorld destination) {
        this.setInNetherPortal(this.getBlockPos());
        return this.getTeleportTarget(destination);
    }
}
