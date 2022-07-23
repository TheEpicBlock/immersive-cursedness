package nl.theepicblock.immersive_cursedness;

import com.mojang.brigadier.Command;
import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1u.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ImmersiveCursedness implements ModInitializer {
    public static final Logger LOGGER = LogManager.getLogger("ImmersiveCursedness");
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

        CommandRegistrationCallback.EVENT.register((dispatcher, b, c) -> {
            dispatcher.register(CommandManager.literal("portal")
                    .then(CommandManager.literal("toggle").executes((context) -> {
                        PlayerInterface pi = (PlayerInterface)context.getSource().getPlayer();
                        pi.immersivecursedness$setEnabled(!pi.immersivecursedness$getEnabled());
                        context.getSource().sendFeedback(Text.literal("you have now "+ (pi.immersivecursedness$getEnabled() ? "enabled" : "disabled") +" immersive portals"), false);
                        if (pi.immersivecursedness$getEnabled() == false) {
                            Util.getManagerFromPlayer(context.getSource().getPlayer()).purgeCache();
                        }
                        return Command.SINGLE_SUCCESS;
                    })));
        });
    }
}
