package com.github.puregero.mapidsyncer;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapIndex;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class MapIndexFilter extends MapIndex {

    private final Executor ASYNC_EXECUTOR;
    private final JavaPlugin plugin;
    private final MapIndex mapIndex;
    private CompletableFuture<MapId> nextMapId = CompletableFuture.failedFuture(new RuntimeException("No next map id"));

    public MapIndexFilter(JavaPlugin plugin, MapId initialNextMapId, MapIndex mapIndex) {
        this.plugin = plugin;
        this.mapIndex = Objects.requireNonNull(mapIndex, "mapIndex");

        this.ASYNC_EXECUTOR = r -> this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, r);

        generateNextMapId(initialNextMapId);
    }

    private synchronized CompletableFuture<MapId> generateNextMapId(MapId desiredMapId) {
        CompletableFuture<MapId> oldNextMapId = this.nextMapId;

        this.nextMapId = CompletableFuture.supplyAsync(
                () -> sqlNextMapId(desiredMapId),
                ASYNC_EXECUTOR);

        return oldNextMapId;
    }

    private MapId sqlNextMapId(MapId desiredMapId) {
        while (desiredMapId == null || !sqlAcquireMapId(desiredMapId.id())) {
            desiredMapId = this.mapIndex.getFreeAuxValueForMap();
        }

        this.plugin.getLogger().info("Acquired map id " + desiredMapId.id());
        return desiredMapId;
    }

    private boolean sqlAcquireMapId(int id) {
        this.plugin.getLogger().info("Trying to acquire map id " + id);
        try (PreparedStatement statement = Database.prepareStatement("UPDATE `mapidsyncer` SET `nextmapid` = ? WHERE `nextmapid` < ?")) {
            statement.setInt(1, id);
            statement.setInt(2, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public MapId getFreeAuxValueForMap() {
        return generateNextMapId(null).join();
    }

    @Override
    public CompoundTag save(CompoundTag nbt, HolderLookup.Provider registryLookup) {
        return this.mapIndex.save(nbt, registryLookup);
    }

    @Override
    public void setDirty() {
        this.mapIndex.setDirty();
    }

    @Override
    public void setDirty(boolean var0) {
        this.mapIndex.setDirty(var0);
    }

    @Override
    public boolean isDirty() {
        return this.mapIndex.isDirty();
    }
}
