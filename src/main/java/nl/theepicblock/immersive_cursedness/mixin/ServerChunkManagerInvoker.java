package nl.theepicblock.immersive_cursedness.mixin;

import com.mojang.datafixers.util.Either;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.concurrent.CompletableFuture;

@Mixin(ServerChunkManager.class)
public interface ServerChunkManagerInvoker {
    @Invoker("getChunkFuture")
    CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>> ic$callGetChunkFuture(int chunkX, int chunkZ, ChunkStatus leastStatus, boolean create);
}
