package nl.theepicblock.immersive_cursedness.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import nl.theepicblock.immersive_cursedness.PlayerInterface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ServerPlayerEntity.class)
public abstract class MixinServerPlayerEntity extends PlayerEntity implements PlayerInterface {
	public MixinServerPlayerEntity(World world, BlockPos pos, float yaw, GameProfile profile) {
		super(world, pos, yaw, profile);
	}

	@Shadow public abstract ServerWorld getServerWorld();

	@Unique private volatile boolean isCloseToPortal;
	@Unique private World unFakedWorld;

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
}
