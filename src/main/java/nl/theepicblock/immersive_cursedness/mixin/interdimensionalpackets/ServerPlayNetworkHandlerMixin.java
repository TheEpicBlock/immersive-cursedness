package nl.theepicblock.immersive_cursedness.mixin.interdimensionalpackets;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.network.MessageType;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSignC2SPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import nl.theepicblock.immersive_cursedness.PlayerInterface;
import nl.theepicblock.immersive_cursedness.PlayerManager;
import nl.theepicblock.immersive_cursedness.Util;
import nl.theepicblock.immersive_cursedness.objects.TransformProfile;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin {
	@Shadow public ServerPlayerEntity player;

	@Shadow @Final private MinecraftServer server;

	@Shadow private int requestedTeleportId;

	@Shadow private Vec3d requestedTeleportPos;

	@Shadow
	private static boolean canPlace(ServerPlayerEntity player, ItemStack stack) {
		return false;
	}

	@Inject(method = "onPlayerInteractBlock", at = @At("HEAD"), cancellable = true)
	public void onInteractBlock(PlayerInteractBlockC2SPacket packet, CallbackInfo ci) {
		if (PlayerInterface.isCloseToPortal(player)) {
			PlayerManager manager = Util.getManagerFromPlayer(player);
			if (manager == null) return;
			BlockHitResult hitResult = packet.getBlockHitResult();
			BlockPos oldPos = hitResult.getBlockPos();
			TransformProfile transformProfile = manager.getTransformProfile(oldPos);
			if (transformProfile == null) return;
			BlockPos newPos = transformProfile.transform(oldPos);

			ServerWorld destination = Util.getDestination(player);
			Direction placementSide = hitResult.getSide();
			this.player.updateLastActionTime();
			int worldHeight = this.player.getServerWorld().getHeight();
			if (newPos.getY() < worldHeight) {
				if (this.requestedTeleportPos == null && this.player.squaredDistanceTo((double)oldPos.getX() + 0.5D, (double)oldPos.getY() + 0.5D, (double)oldPos.getZ() + 0.5D) < 64.0D && destination.canPlayerModifyAt(this.player, newPos)) {
					Hand hand = packet.getHand();
					ItemStack holding = this.player.getStackInHand(hand);
					destination.getServer().execute(() -> {
						((PlayerInterface)player).fakeWorld(destination);
						ActionResult actionResult = this.player.interactionManager.interactBlock(this.player, destination, holding, hand, new BlockHitResult(Util.add(Util.getCenter(newPos),placementSide,0.5), placementSide, newPos, hitResult.isInsideBlock()));
						((PlayerInterface)player).deFakeWorld();
						if (!actionResult.isAccepted() && placementSide == Direction.UP && newPos.getY() >= worldHeight - 1 && canPlace(this.player, holding)) {
							Text text = (new TranslatableText("build.tooHigh", worldHeight)).formatted(Formatting.RED);
							this.player.networkHandler.sendPacket(new GameMessageS2CPacket(text, MessageType.GAME_INFO, net.minecraft.util.Util.NIL_UUID));
						} else if (actionResult.shouldSwingHand()) {
							this.player.swingHand(hand, true);
						}
					});
				}
			} else {
				Text text2 = (new TranslatableText("build.tooHigh", worldHeight)).formatted(Formatting.RED);
				this.player.networkHandler.sendPacket(new GameMessageS2CPacket(text2, MessageType.GAME_INFO, net.minecraft.util.Util.NIL_UUID));
			}

			this.player.networkHandler.sendPacket(new BlockUpdateS2CPacket(oldPos, transformProfile.rotateState(Util.getBlockAsync(destination, newPos))));
			BlockState offsetBlock = transformProfile.transformAndGetFromWorld(oldPos.offset(placementSide), destination);
			if (offsetBlock.getBlock() != Blocks.NETHER_PORTAL) {
				this.player.networkHandler.sendPacket(new BlockUpdateS2CPacket(oldPos.offset(placementSide), offsetBlock));
			}
			ci.cancel();
		}
	}
}
