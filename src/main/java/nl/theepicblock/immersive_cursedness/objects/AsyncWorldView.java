package nl.theepicblock.immersive_cursedness.objects;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldView;
import net.minecraft.world.chunk.Chunk;
import nl.theepicblock.immersive_cursedness.Util;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class AsyncWorldView {
	private final Map<ChunkPos,Chunk> chunkCache = new HashMap<>();
	private final ServerWorld world;
	private static final BlockState AIR = Blocks.AIR.getDefaultState();

	public AsyncWorldView(ServerWorld world) {
		this.world = world;
	}

	public BlockState getBlock(BlockPos pos) {
		Chunk chunk = getChunk(pos);
		if (chunk == null) return AIR;

		return chunk.getBlockState(pos);
	}

	public BlockEntity getBlockEntity(BlockPos pos) {
		Chunk chunk = getChunk(pos);
		if (chunk == null) return null;

		return chunk.getBlockEntity(pos);
	}

	public Chunk getChunk(BlockPos p) {
		return getChunk(new ChunkPos(p));
	}

	public Chunk getChunk(ChunkPos chunkPos) {
		Chunk chunk = chunkCache.get(chunkPos);
		if (chunk == null) {
			Optional<Chunk> chunkO = Util.getChunkAsync(world, chunkPos.x, chunkPos.z);
			if (chunkO.isPresent()) {
				chunk = chunkO.get();
				chunkCache.put(chunkPos, chunk);
			} else {
				return null;
			}
		}
		return chunk;
	}
}
