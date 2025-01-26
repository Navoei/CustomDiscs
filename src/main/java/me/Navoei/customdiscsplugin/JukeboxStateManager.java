package me.Navoei.customdiscsplugin;

import org.bukkit.Location;
import org.bukkit.block.Jukebox;

import java.util.ArrayList;
import java.util.List;

public class JukeboxStateManager {

    static CustomDiscs plugin = CustomDiscs.getInstance();
    static PlayerManager playerManager = PlayerManager.instance();
    static List<Location> jukeboxLocations = new ArrayList<>();

        public static void start(Jukebox jukebox) {
            if (jukeboxLocations.contains(jukebox.getLocation()) || !playerManager.isAudioPlayerPlaying(jukebox.getLocation())) return;
            jukeboxLocations.add(jukebox.getLocation());
            plugin.getServer().getRegionScheduler().runAtFixedRate(plugin, jukebox.getLocation(), scheduledTask -> {
                if (playerManager.isAudioPlayerPlaying(jukebox.getLocation())) {
                    if (!jukebox.isPlaying()) {
                        jukebox.startPlaying();
                    }
                } else {
                    jukebox.stopPlaying();
                    jukeboxLocations.remove(jukebox.getLocation());
                    scheduledTask.cancel();
                }
            }, 0, 1);
        }

}
