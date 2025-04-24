package com.github.puregero.mapidsyncer;

import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapIndex;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class MapIndexFilter extends MapIndex {

    private final Executor ASYNC_EXECUTOR;
    private final JavaPlugin plugin;
    private CompletableFuture<MapId> nextMapId = CompletableFuture.failedFuture(new RuntimeException("No next map id"));
    private MapId lastMapId;

    public MapIndexFilter(JavaPlugin plugin, MapId initialNextMapId) {
        super(initialNextMapId.id());
        this.lastMapId = initialNextMapId;
        this.plugin = plugin;

        this.ASYNC_EXECUTOR = r -> Thread.ofVirtual().name("MapIdSyncer").start(r);

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
            desiredMapId = super.getNextMapId();
        }

        if (desiredMapId.id() > this.lastMapId.id()) {
            this.lastMapId = desiredMapId;
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

    public MapIndex toVanillaMapIndex() {
        MapIndex mapIndex = new MapIndex(this.lastMapId.id());
        mapIndex.setDirty(this.isDirty());
        return mapIndex;
    }

    @Override
    public MapId getNextMapId() {
        return generateNextMapId(null).join();
    }
}
