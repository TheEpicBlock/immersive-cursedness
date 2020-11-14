package nl.theepicblock.immersive_cursedness.mixin.interdimensionalpackets;

import net.minecraft.network.packet.c2s.play.UpdateSignC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import nl.theepicblock.immersive_cursedness.CloseToPortalProvider;
import nl.theepicblock.immersive_cursedness.PlayerManager;
import nl.theepicblock.immersive_cursedness.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {
	@Shadow public ServerPlayerEntity player;

	@Redirect(method = "onSignUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/packet/c2s/play/UpdateSignC2SPacket;getPos()Lnet/minecraft/util/math/BlockPos;"))
	public BlockPos redirectSignPos(UpdateSignC2SPacket updateSignC2SPacket) {
		if (CloseToPortalProvider.get(player)) {
			BlockPos pos = updateSignC2SPacket.getPos();
			PlayerManager manager = Util.getManagerFromPlayer(player);
			if (manager == null) return pos;
			BlockPos z = manager.transform(pos);
			if (z == null) {
				return pos;
			} else {
				return z;
			}
		}
		return updateSignC2SPacket.getPos();
	}

	@Redirect(method = "onSignUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;getServerWorld()Lnet/minecraft/server/world/ServerWorld;"))
	public ServerWorld redirectSignWorld(ServerPlayerEntity player, UpdateSignC2SPacket updateSignC2SPacket) {
		if (CloseToPortalProvider.get(player)) {
			PlayerManager manager = Util.getManagerFromPlayer(player);
			if (manager == null) return player.getServerWorld();
			BlockPos z = manager.transform(updateSignC2SPacket.getPos());
			if (z == null) {
				return player.getServerWorld();
			} else {
				return Util.getDestination(player);
			}
		}
		return player.getServerWorld();
	}
}
