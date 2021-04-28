package nl.theepicblock.immersive_cursedness.mixin;

import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityPositionS2CPacket.class)
public interface EntityPositionS2CPacketAccessor {
    @Accessor("x")
    void ic$setX(double v);

    @Accessor("y")
    void ic$setY(double v);
}
