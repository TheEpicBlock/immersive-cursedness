package nl.theepicblock.immersive_cursedness.mixin;

import com.mojang.authlib.GameProfile;
import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.encryption.PlayerPublicKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import nl.theepicblock.immersive_cursedness.Config;
import nl.theepicblock.immersive_cursedness.PlayerInterface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("PointlessBooleanExpression")
@Mixin(ServerPlayerEntity.class)
public abstract class MixinServerPlayerEntity extends MixinPlayerEntity implements PlayerInterface {
	public MixinServerPlayerEntity(World world, BlockPos pos, float yaw, GameProfile profile, PlayerPublicKey publicKey) {
		super(world, pos, yaw, profile);
	}

	@Unique
	private volatile boolean isCloseToPortal;
	@Unique
	private World unFakedWorld;
	@Unique
	private boolean enabled = true;

	@Override
	public void immersivecursedness$setCloseToPortal(boolean v) {
		isCloseToPortal = v;
	}

	@Override
	public boolean immersivecursedness$getCloseToPortal() {
		return isCloseToPortal;
	}

	@Override
	public void immersivecursedness$fakeWorld(World world) {
		unFakedWorld = this.getWorld();
		this.setWorld(world);
	}

	@Override
	public void immersivecursedness$deFakeWorld() {
		if (unFakedWorld != null) {
			setWorld(unFakedWorld);
			unFakedWorld = null;
		}
	}

	@Override
	public ServerWorld immersivecursedness$getUnfakedWorld() {
		if (unFakedWorld != null) return (ServerWorld) unFakedWorld;
		return (ServerWorld) getWorld();
	}

	@Inject(method = "writeCustomDataToNbt", at = @At("HEAD"))
	public void writeInject(NbtCompound tag, CallbackInfo ci) {
		if (enabled != AutoConfig.getConfigHolder(Config.class).getConfig().defaultEnabled) {
			tag.putBoolean("immersivecursednessenabled", enabled);
		}
	}

	@Inject(method = "readCustomDataFromNbt", at = @At("HEAD"))
	public void readInject(NbtCompound tag, CallbackInfo ci) {
		if (tag.contains("immersivecursednessenabled")) {
			enabled = tag.getBoolean("immersivecursednessenabled");
		} else {
			enabled = AutoConfig.getConfigHolder(Config.class).getConfig().defaultEnabled;
		}
	}

	@Override
	public void immersivecursedness$setEnabled(boolean v) {
		enabled = v;

	}

	@Override
	public boolean immersivecursedness$getEnabled() {
		return enabled;
	}

	@Override
	public void handleGetMaxNetherPortalTime(CallbackInfoReturnable<Integer> cir) {
		if (enabled)
			cir.setReturnValue(1);
	}
}
