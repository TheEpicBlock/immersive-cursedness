package nl.theepicblock.immersive_cursedness;

import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1u.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public class ImmersiveCursedness implements ModInitializer {
    public static Thread cursednessThread;
    public static CursednessServer cursednessServer;

    @Override
    public void onInitialize() {
        AutoConfig.register(Config.class, JanksonConfigSerializer::new);

        ServerLifecycleEvents.SERVER_STARTED.register(minecraftServer -> {
            cursednessThread = new Thread(() -> {
                cursednessServer = new CursednessServer(minecraftServer);
                cursednessServer.start();
            });
            cursednessThread.start();
            cursednessThread.setName("Immersive Cursedness Thread");
        });
        ServerLifecycleEvents.SERVER_STOPPED.register(minecraftServer -> {
            cursednessServer.stop();
        });
    }
}
