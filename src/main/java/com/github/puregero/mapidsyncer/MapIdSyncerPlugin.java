package com.github.puregero.mapidsyncer;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapIndex;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class MapIdSyncerPlugin extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        this.saveDefaultConfig();

        Database.HOST = getConfig().getString("mysql.host", "localhost");
        Database.PORT = getConfig().getString("mysql.port", "3306");
        Database.DATABASE = getConfig().getString("mysql.database", "survival");
        Database.USERNAME = getConfig().getString("mysql.username", "root");
        Database.PASSWORD = getConfig().getString("mysql.password", "");

        try (PreparedStatement statement = Database.prepareStatement("CREATE TABLE IF NOT EXISTS `mapidsyncer` (`rowid` INT PRIMARY KEY, `nextmapid` INT)")) {
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        try (PreparedStatement statement = Database.prepareStatement("INSERT IGNORE INTO `mapidsyncer` SET `rowid` = 0, `nextmapid` = 1")) {
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        MapId nextMapId = MinecraftServer.getServer().overworld().getFreeMapId(); // Load the idcounts data storage
        MinecraftServer.getServer().overworld().getDataStorage().set(MapIndex.TYPE, new MapIndexFilter(this, nextMapId));
    }

    @Override
    public void onDisable() {
        MinecraftServer.getServer().overworld().getDataStorage().cache.get(MapIndex.TYPE).ifPresent(previousMapIndex -> {
            if (previousMapIndex instanceof MapIndexFilter mapIndexFilter) {
                MinecraftServer.getServer().overworld().getDataStorage().set(MapIndex.TYPE, mapIndexFilter.toVanillaMapIndex());
            }
        });

    }

}
