package nl.theepicblock.immersive_cursedness;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

public interface PlayerInterface {
	void immersivecursedness$setCloseToPortal(boolean v);
	boolean immersivecursedness$getCloseToPortal();

	static boolean isCloseToPortal(ServerPlayerEntity player) {
		return ((PlayerInterface)player).immersivecursedness$getCloseToPortal();
	}

	void immersivecursedness$fakeWorld(World world);
	void immersivecursedness$deFakeWorld();
	ServerWorld immersivecursedness$getUnfakedWorld();

	void immersivecursedness$setEnabled(boolean v);
	boolean immersivecursedness$getEnabled();
}
