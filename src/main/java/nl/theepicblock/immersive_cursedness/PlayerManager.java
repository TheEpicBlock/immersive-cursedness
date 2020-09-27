package nl.theepicblock.immersive_cursedness;

import net.minecraft.block.Blocks;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.poi.PointOfInterest;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestType;

import java.util.stream.Stream;

public class PlayerManager {
    private final ServerPlayerEntity player;
    private final PortalManager portalManager;

    public PlayerManager(ServerPlayerEntity player) {
        this.player = player;
        portalManager = new PortalManager();
    }

    public void tick() {
        portalManager.update(player);

        portalManager.getPortals().forEach(portal -> {
            FlatStandingRectangle rect = portal.toFlatStandingRectangle();
            for (int i = -20; i < 20; i++) {
                FlatStandingRectangle rect2 = rect.expand(i, player.getCameraPosVec(1));
                Util.sendParticle(player, rect2.getBottomRight());
                Util.sendParticle(player, rect2.getBottomLeft());
                Util.sendParticle(player, rect2.getTopLeft());
                Util.sendParticle(player, rect2.getTopRight());
            }
        });
    }
}
