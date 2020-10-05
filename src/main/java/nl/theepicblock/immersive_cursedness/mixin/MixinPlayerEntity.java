package nl.theepicblock.immersive_cursedness.mixin;

import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(PlayerEntity.class)
public abstract class MixinPlayerEntity {
    /**
     * @reason makes it so the portal always takes 1 tick to go through. Even when in survival
     * @author TheEpicBlock_TEB
     */
    @Overwrite
    public int getMaxNetherPortalTime() {
        return 1;
    }
}
