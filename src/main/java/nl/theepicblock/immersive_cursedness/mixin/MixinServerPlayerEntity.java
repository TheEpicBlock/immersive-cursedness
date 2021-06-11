package nl.theepicblock.immersive_cursedness.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import nl.theepicblock.immersive_cursedness.PlayerInterface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("PointlessBooleanExpression")
@Mixin(ServerPlayerEntity.class)
public abstract class MixinServerPlayerEntity extends PlayerEntity implements PlayerInterface {
	public MixinServerPlayerEntity(World world, BlockPos pos, float yaw, GameProfile profile) {
		super(world, pos, yaw, profile);
	}

	@Shadow public abstract ServerWorld getServerWorld();

	@Unique private volatile boolean isCloseToPortal;
	@Unique private World unFakedWorld;
	@Unique private boolean enabled = true;

	@Override
	public void setCloseToPortal(boolean v) {
		isCloseToPortal = v;
	}

	@Override
	public boolean getCloseToPortal() {
		return isCloseToPortal;
	}

	@Override
	public void fakeWorld(World world) {
		unFakedWorld = this.world;
		this.world = world;
	}

	@Override
	public void deFakeWorld() {
		this.world = unFakedWorld;
		unFakedWorld = null;
	}

	@Override
	public ServerWorld getUnfakedWorld() {
		if (unFakedWorld != null) return (ServerWorld)unFakedWorld;
		return this.getServerWorld();
	}

	@Inject(method = "writeCustomDataToNbt", at = @At("HEAD"))
	public void writeInject(NbtCompound tag, CallbackInfo ci) {
		if (enabled == false) {
			tag.putBoolean("immersivecursednessenabled", enabled);
		}
	}

	@Inject(method = "readCustomDataFromNbt", at = @At("HEAD"))
	public void readInject(NbtCompound tag, CallbackInfo ci) {
		if (tag.contains("immersivecursednessenabled")) {
			enabled = tag.getBoolean("immersivecursednessenabled");
		}
	}

	@Override
	public void setEnabled(boolean v) {
		enabled = v;
	}

	@Override
	public boolean getEnabled() {
		return enabled;
	}
}
