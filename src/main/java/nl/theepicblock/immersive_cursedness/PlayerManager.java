package nl.theepicblock.immersive_cursedness;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import nl.theepicblock.immersive_cursedness.objects.*;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class PlayerManager {
    private final Config config;
    private final ServerPlayerEntity player;
    private final PortalManager portalManager;
    private BlockCache blockCache = new BlockCache();
    private final List<UUID> hiddenEntities = new ArrayList<>();
    private ServerWorld previousWorld;

    public PlayerManager(ServerPlayerEntity player, Config config) {
        this.player = player;
        portalManager = new PortalManager(player, config);
        this.config = config;
    }

    public void tick(int tickCount) {
        if (((PlayerInterface)player).immersivecursedness$getEnabled() == false) {
            return;
        }

        ServerWorld sourceWorld = ((PlayerInterface)player).immersivecursedness$getUnfakedWorld();
        ServerWorld destinationWorld = Util.getDestination(sourceWorld);
        AsyncWorldView sourceView = new AsyncWorldView(sourceWorld);
        AsyncWorldView destinationView = new AsyncWorldView(destinationWorld);

        boolean justWentThroughPortal = false;
        if (sourceWorld != previousWorld) {
            blockCache = new BlockCache();
            justWentThroughPortal = true;
        }
        var bottomOfWorld = sourceWorld.getBottomY();

        if (tickCount % 30 == 0 || justWentThroughPortal) {
            portalManager.update();
        }

        List<FlatStandingRectangle> sentLayers = new ArrayList<>(portalManager.getPortals().size()*config.portalDepth);
        Chunk2IntMap sentBlocks = new Chunk2IntMap();
        BlockUpdateMap toBeSent = new BlockUpdateMap();

        List<Entity> entities;
        try {
            entities = this.getEntitiesInRange(sourceWorld);
            if (tickCount % 200 == 0) removeNoLongerExistingEntities(entities);
        } catch (ConcurrentModificationException ignored) { entities = new ArrayList<>(0); } // Not such a big deal, we'll get the entities next tick

        BlockState atmosphereBlock = (sourceWorld.getRegistryKey() == World.NETHER ? Blocks.BLUE_CONCRETE : Blocks.NETHER_WART_BLOCK).getDefaultState();
        BlockState atmosphereBetweenBlock = (sourceWorld.getRegistryKey() == World.NETHER ? Blocks.BLUE_STAINED_GLASS : Blocks.RED_STAINED_GLASS).getDefaultState();

        if (player.hasNetherPortalCooldown())return;

        boolean isCloseToPortal = false;
        //iterate through all portals
        for (Portal portal : portalManager.getPortals()) {
            if (portal.isCloserThan(player.getPos(), 6)) {
                isCloseToPortal = true;
            }
            TransformProfile transformProfile = portal.getTransformProfile();
            if (transformProfile == null) continue;

            if (tickCount % 40 == 0 || justWentThroughPortal) {
                //replace the portal blocks in the center of the portal with air
                BlockPos.iterate(portal.getLowerLeft(), portal.getUpperRight()).forEach(pos -> {
                    toBeSent.put(pos.toImmutable(), Blocks.AIR.getDefaultState());
                });
            }

            //iterate through all layers behind the portal
            FlatStandingRectangle rect = portal.toFlatStandingRectangle();
            for (int i = 1; i < config.portalDepth; i++) {
                FlatStandingRectangle rect2 = rect.expand(i, player.getCameraPosVec(1));
                sentLayers.add(rect2);
                if (config.debugParticles) rect2.visualise(player);

                entities.removeIf((entity) -> {
                    if (rect2.contains(entity.getPos())) {
                        for (UUID uuid : hiddenEntities) {
                            if (entity.getUuid().equals(uuid)) {
                                return true; //cancel if the uuid is already in hiddenEntities
                            }
                        }
                        //If we've reached this point. The entity isn't hidden yet. So we should hide it
                        EntityPositionS2CPacket packet = createEntityPacket(entity, entity.getX() + 50, Double.MAX_VALUE);
                        player.networkHandler.sendPacket(packet);
                        hiddenEntities.add(entity.getUuid());
                        return true;
                    }
                    return false;
                });

                //go through all blocks in this layer and use the transformProfile to get the correct block in the nether. Then send it to the client
                rect2.iterateClamped(player.getPos(), config.horizontalSendLimit, Util.calculateMinMax(sourceWorld, destinationWorld, transformProfile), (pos) -> {
                    double dist = Util.getDistance(pos, portal.getLowerLeft());
                    if (dist > config.squaredAtmosphereRadiusPlusOne) return;

                    BlockState ret;

                    if (dist > config.squaredAtmosphereRadius) {
                        ret = atmosphereBlock;
                    } else if (dist > config.squaredAtmosphereRadiusMinusOne) {
                        ret = atmosphereBetweenBlock;
                    } else {
                        ret = transformProfile.transformAndGetFromWorld(pos, destinationView);
                    }

                    if (pos.getY() == bottomOfWorld+1) ret = atmosphereBetweenBlock;
                    if (pos.getY() == bottomOfWorld) ret = atmosphereBlock;

                    BlockPos imPos = pos.toImmutable();
                    sentBlocks.increment(imPos);
                    if (!(blockCache.get(imPos) == ret)) {
                        if (!ret.isAir() || !sourceView.getBlock(pos).isAir()) {
                            blockCache.put(imPos, ret);
                            toBeSent.put(imPos, ret);
                        }
                    }
                });
            }
        }
        ((PlayerInterface)player).immersivecursedness$setCloseToPortal(isCloseToPortal);

        //get all of the old blocks and remove them
        blockCache.purge(sentBlocks, sentLayers, (pos, cachedState) -> {
            BlockState originalBlock = sourceView.getBlock(pos);
            if (originalBlock != cachedState) {
                toBeSent.put(pos, originalBlock);
            }
            if (config.debugParticles) Util.sendParticle(player, Util.getCenter(pos), 1, 0, originalBlock != cachedState ? 0 : 1);
        });

        entities.forEach(entity -> {
            for (UUID uuid : hiddenEntities) {
                if (entity.getUuid().equals(uuid)) {
                    hiddenEntities.remove(uuid);
                    player.networkHandler.sendPacket(new EntityPositionS2CPacket(entity));
                    return;
                }
            }
        });
        toBeSent.sendTo(this.player);
        previousWorld = sourceWorld;
    }

    public BlockPos transform(BlockPos p) {
        for (Portal portal : portalManager.getPortals()) {
            if (portal.isBlockposBehind(p, player.getPos()) && portal.getTransformProfile() != null) {
                return portal.getTransformProfile().transform(p);
            }
        }
        return null;
    }

    public TransformProfile getTransformProfile(BlockPos p) {
        for (Portal portal : portalManager.getPortals()) {
            if (portal.isBlockposBehind(p, player.getPos()) && portal.getTransformProfile() != null) {
                return portal.getTransformProfile();
            }
        }
        return null;
    }

    public void purgeCache() {
        BlockUpdateMap packetStorage = new BlockUpdateMap();
        ((PlayerInterface)player).immersivecursedness$setCloseToPortal(false);
        blockCache.purgeAll((pos, cachedState) -> {
            BlockState originalBlock = Util.getBlockAsync(player.getWorld(), pos);
            if (originalBlock != cachedState) {
                packetStorage.put(pos, originalBlock);
            }
            if (config.debugParticles) Util.sendParticle(player, Util.getCenter(pos), 1, 0, originalBlock != cachedState ? 0 : 1);
        });
        for (Portal portal : portalManager.getPortals()) {
            BlockPos.iterate(portal.getLowerLeft(), portal.getUpperRight()).forEach(pos -> {
                packetStorage.put(pos.toImmutable(), Util.getBlockAsync(player.getWorld(), pos));
            });
        }
        packetStorage.sendTo(this.player);
    }

    private List<Entity> getEntitiesInRange(ServerWorld world) {
        return world.getEntitiesByType(
                new AllExceptPlayer(),
                new Box(
                        player.getX() - config.renderDistance*16,
                        player.getY() - config.renderDistance*16,
                        player.getZ() - config.renderDistance*16,
                        player.getX() + config.renderDistance*16,
                        player.getY() + config.renderDistance*16,
                        player.getZ() + config.renderDistance*16
                ),
                (entity) -> true
        );
    }

    private void removeNoLongerExistingEntities(List<Entity> existingEntities) {
        hiddenEntities.removeIf((uuid) ->
                existingEntities.stream().noneMatch(entity -> uuid.equals(entity.getUuid())));
    }

    private static EntityPositionS2CPacket createEntityPacket(Entity entity, double x, double y) {
        var buf = PacketByteBufs.create();
        buf.writeVarInt(entity.getId());
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(entity.getZ());
        buf.writeByte((byte)((int)(entity.getYaw() * 256.0F / 360.0F)));
        buf.writeByte((byte)((int)(entity.getPitch() * 256.0F / 360.0F)));
        buf.writeBoolean(false);

        return new EntityPositionS2CPacket(buf);
    }

    private static class AllExceptPlayer implements TypeFilter<Entity, Entity> {

        @Nullable
        @Override
        public Entity downcast(Entity obj) {
            return obj;
        }

        @Override
        public Class<? extends Entity> getBaseClass() {
            return Entity.class;
        }
    }
}
