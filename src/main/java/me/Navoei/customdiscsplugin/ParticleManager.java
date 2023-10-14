package me.Navoei.customdiscsplugin;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Jukebox;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;

public class ParticleManager extends BukkitRunnable {

    static PlayerManager playerManager = PlayerManager.instance();
    static HashMap<Location, ParticleManager> locationParticleManager = new HashMap<>();

        public static void start(Jukebox jukebox) {
            ParticleManager particleManager = new ParticleManager();
            particleManager.jukebox = jukebox;
            if (locationParticleManager.containsKey(jukebox.getLocation())) return;
            locationParticleManager.put(jukebox.getLocation(), particleManager);
            locationParticleManager.get(jukebox.getLocation()).runTaskTimer(CustomDiscs.getInstance(), 0, 20);
        }

        //private float seconds;
        private Jukebox jukebox;


        @Override
        public void run() {

                if (!playerManager.isAudioPlayerPlaying(jukebox.getLocation())) {
                    locationParticleManager.remove(jukebox.getLocation());
                    cancel();
                } else {
                    //if (!jukebox.isPlaying()) {
                        jukebox.getLocation().getWorld().spawnParticle(Particle.NOTE, jukebox.getLocation().add(0.5, 1.1, 0.5), 1);
                    //}
                }
        }

}
