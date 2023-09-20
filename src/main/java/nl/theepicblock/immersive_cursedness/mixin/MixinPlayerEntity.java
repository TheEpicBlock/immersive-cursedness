package nl.theepicblock.immersive_cursedness.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(PlayerEntity.class)
public abstract class MixinPlayerEntity extends LivingEntity {
    public MixinPlayerEntity(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(EntityType.PLAYER, world);
    }

    /**
     * @reason makes it so the portal always takes 1 tick to go through. Even when in survival
     * @author TheEpicBlock_TEB
     */
    @Inject(method = "getMaxNetherPortalTime", at = @At("HEAD"), cancellable = true)
    public void handleGetMaxNetherPortalTime(CallbackInfoReturnable<Integer> cir) {

    }
}
