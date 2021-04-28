package nl.theepicblock.immersive_cursedness.objects;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import net.minecraft.block.BlockState;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import nl.theepicblock.immersive_cursedness.mixin.ChunkDeltaUpdateAccessor;

public class BlockUpdateMap extends Long2ObjectOpenHashMap<Short2ObjectMap<BlockState>> {
    public void put(BlockPos p, BlockState t) {
        long cp = getChunkPos(p);
        Short2ObjectMap<BlockState> map = this.get(cp);
        if (map == null) {
            map = new Short2ObjectOpenHashMap<>();
            this.put(cp, map);
        }
        map.put(ChunkSectionPos.packLocal(p),t);
    }

    public BlockState get(BlockPos p) {
        Short2ObjectMap<BlockState> map = this.get(getChunkPos(p));
        if (map == null) return null;
        return map.get(ChunkSectionPos.packLocal(p));
    }

    public void sendTo(ServerPlayerEntity player) {
        this.forEach((chunkSection, chunkContents) -> {
            ChunkDeltaUpdateS2CPacket packet = new ChunkDeltaUpdateS2CPacket();
            ChunkDeltaUpdateAccessor accessor = (ChunkDeltaUpdateAccessor)packet;

            accessor.ic$setSectionPos(ChunkSectionPos.from(chunkSection));
            accessor.ic$setPositions(chunkContents.keySet().toShortArray());
            accessor.ic$setBlockStates(chunkContents.values().toArray(new BlockState[0]));
            accessor.ic$setField_26749(false);

            player.networkHandler.sendPacket(packet);
        });
    }

    private long getChunkPos(BlockPos p) {
        long l = 0;
        l |= ((p.getY() >> 4) & 0b11111111111111111111);
        l |= (long)((p.getZ() >> 4) & 0b1111111111111111111111) << 20;
        l |= (long)((p.getX() >> 4) & 0b1111111111111111111111) << 42;
        return l;
    }
}
