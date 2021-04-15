package nl.theepicblock.immersive_cursedness.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import net.minecraft.util.math.ChunkSectionPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChunkDeltaUpdateS2CPacket.class)
public interface ChunkDeltaUpdateAccessor {
    @Accessor
    void setSectionPos(ChunkSectionPos v);
    @Accessor
    void setPositions(short[] v);
    @Accessor
    void setBlockStates(BlockState[] v);
    @Accessor
    void setField_26749(boolean v);
}
