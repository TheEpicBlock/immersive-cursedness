package nl.theepicblock.immersive_cursedness.mixin.interdimensionalpackets;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import static net.minecraft.util.math.BlockPos.*;

@Mixin(PlayerActionC2SPacket.class)
public class PlayerActionC2SMixin {
	@Redirect(method = "<init>(Lnet/minecraft/network/packet/c2s/play/PlayerActionC2SPacket$Action;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/BlockPos;toImmutable()Lnet/minecraft/util/math/BlockPos;"))
	public BlockPos toImmutableInject(BlockPos pos) {
		return new BlockPos.Mutable(pos.getX(), pos.getY(), pos.getZ());
	}

	@Redirect(method = "read", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketByteBuf;readBlockPos()Lnet/minecraft/util/math/BlockPos;"))
	public BlockPos readBlockPosAsMutable(PacketByteBuf packetByteBuf) {
		long l = packetByteBuf.readLong();
		return new BlockPos.Mutable(unpackLongX(l), unpackLongY(l), unpackLongZ(l));
	}
}
