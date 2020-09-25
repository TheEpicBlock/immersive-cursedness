package nl.theepicblock.immersive_cursedness;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public class ImmersiveCursedness implements ModInitializer {
    public static Thread cursednessThread;
    public static CursednessServer cursednessServer;

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(minecraftServer -> {
            cursednessThread = new Thread(() -> {
                cursednessServer = new CursednessServer(minecraftServer);
                cursednessServer.start();
            });
            cursednessThread.start();
        });
        ServerLifecycleEvents.SERVER_STOPPED.register(minecraftServer -> {
            cursednessServer.stop();
        });
    }
}
