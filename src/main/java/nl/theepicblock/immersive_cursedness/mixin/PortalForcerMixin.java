package nl.theepicblock.immersive_cursedness.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PortalForcer;
import nl.theepicblock.immersive_cursedness.ImmersiveCursedness;
import nl.theepicblock.immersive_cursedness.PortalManager;
import nl.theepicblock.immersive_cursedness.Util;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PortalForcer.class)
public class PortalForcerMixin {
	@Shadow @Final private ServerWorld world;

	@SuppressWarnings("UnresolvedMixinReference")
	@Redirect(method = "method_30480", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;getBlockState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;"))
	public BlockState lambdaMixin(ServerWorld world, BlockPos p) {
		if (PortalManager.portalForcerMixinActivate &&
				Thread.currentThread() == ImmersiveCursedness.cursednessThread) {
			return Util.getBlockAsync(world, p);
		}
		return world.getBlockState(p);
	}
}
