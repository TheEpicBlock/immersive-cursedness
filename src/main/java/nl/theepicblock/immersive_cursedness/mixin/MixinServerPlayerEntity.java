package nl.theepicblock.immersive_cursedness.mixin;

import net.minecraft.server.network.ServerPlayerEntity;
import nl.theepicblock.immersive_cursedness.CloseToPortalProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ServerPlayerEntity.class)
public class MixinServerPlayerEntity implements CloseToPortalProvider {
	@Unique private volatile boolean isCloseToPortal;

	@Override
	public void setCloseToPortal(boolean v) {
		isCloseToPortal = v;
	}

	@Override
	public boolean getCloseToPortal() {
		return isCloseToPortal;
	}
}
