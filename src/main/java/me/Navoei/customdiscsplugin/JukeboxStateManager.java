package me.Navoei.customdiscsplugin;

import org.bukkit.Location;
import org.bukkit.block.Jukebox;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;

public class JukeboxStateManager extends BukkitRunnable {

    static PlayerManager playerManager = PlayerManager.instance();
    static HashMap<Location, JukeboxStateManager> locationParticleManager = new HashMap<>();

        public static void start(Jukebox jukebox) {
            JukeboxStateManager jukeboxStateManager = new JukeboxStateManager();
            jukeboxStateManager.jukebox = jukebox;
            if (locationParticleManager.containsKey(jukebox.getLocation())) return;
            locationParticleManager.put(jukebox.getLocation(), jukeboxStateManager);
            locationParticleManager.get(jukebox.getLocation()).runTaskTimer(CustomDiscs.getInstance(), 0, 1);
        }

        //private float seconds;
        private Jukebox jukebox;


        @Override
        public void run() {

                if (!playerManager.isAudioPlayerPlaying(jukebox.getLocation())) {
                    jukebox.stopPlaying();
                    locationParticleManager.remove(jukebox.getLocation());
                    cancel();
                } else {
                    if (!jukebox.isPlaying()) {
                        jukebox.startPlaying();
                    }
                }
        }

}
