package nl.theepicblock.immersive_cursedness.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import net.minecraft.util.math.ChunkSectionPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChunkDeltaUpdateS2CPacket.class)
public interface ChunkDeltaUpdateAccessor {
    @Accessor("sectionPos")
    void ic$setSectionPos(ChunkSectionPos v);
    @Accessor("positions")
    void ic$setPositions(short[] v);
    @Accessor("blockStates")
    void ic$setBlockStates(BlockState[] v);
    @Accessor("field_26749")
    void ic$setField_26749(boolean v);
}
