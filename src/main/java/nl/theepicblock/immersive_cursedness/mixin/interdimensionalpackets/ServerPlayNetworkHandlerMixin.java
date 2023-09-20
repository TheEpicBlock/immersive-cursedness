package nl.theepicblock.immersive_cursedness.mixin.interdimensionalpackets;

import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import nl.theepicblock.immersive_cursedness.PlayerInterface;
import nl.theepicblock.immersive_cursedness.PlayerManager;
import nl.theepicblock.immersive_cursedness.Util;
import nl.theepicblock.immersive_cursedness.objects.TransformProfile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin {
	@Shadow public ServerPlayerEntity player;

	@Unique
	private TransformProfile transformProfile;

	@Inject(method = "onPlayerInteractBlock", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/server/world/ServerWorld;)V"))
	public void setTransformProfile(PlayerInteractBlockC2SPacket packet, CallbackInfo ci) {
		if (PlayerInterface.isCloseToPortal(this.player)) {
			PlayerManager manager = Util.getManagerFromPlayer(player);
			if (manager != null) {
				// Potentially dangerous with dimensional threading
				var hitresult = packet.getBlockHitResult();
				this.transformProfile = manager.getTransformProfile(hitresult.getBlockPos().offset(hitresult.getSide()));
				if (transformProfile != null) {
					((PlayerInterface)player).immersivecursedness$fakeWorld(Util.getDestination(player));
				}
			} else {
				transformProfile = null;
			}
		} else {
			transformProfile = null;
		}
	}

	@Redirect(method = "onPlayerInteractBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;getServerWorld()Lnet/minecraft/server/world/ServerWorld;"))
	public ServerWorld modifyWorld(ServerPlayerEntity player, PlayerInteractBlockC2SPacket packet) {
		if (transformProfile != null) {
			return Util.getDestination(player);
		}
		return player.getServerWorld();
	}

	@ModifyArg(method = "onPlayerInteractBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;canPlayerModifyAt(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/math/BlockPos;)Z"))
	public BlockPos modifyCanBreakCheck(BlockPos pos) {
		if (transformProfile != null) {
			return transformProfile.transform(pos);
		}
		return pos;
	}

	@ModifyArg(method = "onPlayerInteractBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V"))
	public Packet<?> modifyReturnPacket(Packet<?> packet) {
		if (transformProfile != null && packet instanceof BlockUpdateS2CPacket blockUpdate) {
			var oldPos = blockUpdate.getPos();
			var newPos = transformProfile.transform(oldPos);
			return new BlockUpdateS2CPacket(oldPos, player.getWorld().getBlockState(newPos));
		}
		return packet;
	}

	@ModifyArg(method = "onPlayerInteractBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerInteractionManager;interactBlock(Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/Hand;Lnet/minecraft/util/hit/BlockHitResult;)Lnet/minecraft/util/ActionResult;"))
	public BlockHitResult modifyUse(BlockHitResult original) {
		if (transformProfile != null) {
			var side = transformProfile.rotate(original.getSide());
			return new BlockHitResult(transformProfile.transform(original.getPos()), side, transformProfile.transform(original.getBlockPos()), original.isInsideBlock());
		}
		return original;
	}

	@Inject(method = "onPlayerInteractBlock", at = @At("RETURN"))
	public void clearPortalTransform(PlayerInteractBlockC2SPacket packet, CallbackInfo ci) {
 		((PlayerInterface)player).immersivecursedness$deFakeWorld();
		transformProfile = null;
	}
}
