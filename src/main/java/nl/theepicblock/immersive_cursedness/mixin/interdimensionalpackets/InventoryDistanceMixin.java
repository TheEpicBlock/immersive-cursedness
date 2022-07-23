package nl.theepicblock.immersive_cursedness.mixin.interdimensionalpackets;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.BrewingStandBlockEntity;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import nl.theepicblock.immersive_cursedness.PlayerInterface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({LootableContainerBlockEntity.class, BrewingStandBlockEntity.class})
public abstract class InventoryDistanceMixin extends LockableContainerBlockEntity {
	protected InventoryDistanceMixin(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
		super(blockEntityType, blockPos, blockState);
	}

	@Inject(method = "canPlayerUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;squaredDistanceTo(DDD)D"), cancellable = true)
	public void playerUseRedirect(PlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
		if (((PlayerInterface)player).immersivecursedness$getUnfakedWorld() != this.world) {
			if (player instanceof ServerPlayerEntity) {
				cir.setReturnValue(PlayerInterface.isCloseToPortal((ServerPlayerEntity)player));
			}
		}
	}
}
