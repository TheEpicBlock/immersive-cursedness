package nl.theepicblock.immersive_cursedness;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

public interface PlayerInterface {
	void setCloseToPortal(boolean v);
	boolean getCloseToPortal();

	static boolean isCloseToPortal(ServerPlayerEntity player) {
		return ((PlayerInterface)player).getCloseToPortal();
	}

	void fakeWorld(World world);
	void deFakeWorld();
	ServerWorld getUnfakedWorld();
}
