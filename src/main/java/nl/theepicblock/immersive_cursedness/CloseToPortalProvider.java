package nl.theepicblock.immersive_cursedness;

import net.minecraft.server.network.ServerPlayerEntity;

public interface CloseToPortalProvider {
	void setCloseToPortal(boolean v);
	boolean getCloseToPortal();

	static boolean get(ServerPlayerEntity player) {
		return ((CloseToPortalProvider)player).getCloseToPortal();
	}
}
