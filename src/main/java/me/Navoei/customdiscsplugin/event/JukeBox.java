package me.Navoei.customdiscsplugin.event;

import de.maxhenkel.voicechat.api.audiochannel.AudioChannel;
import de.maxhenkel.voicechat.api.audiochannel.AudioPlayer;
import de.maxhenkel.voicechat.api.audiochannel.LocationalAudioChannel;
import me.Navoei.customdiscsplugin.CustomDiscs;
import me.Navoei.customdiscsplugin.VoicePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Jukebox;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class JukeBox implements Listener {

    private final Map<UUID, AudioPlayer> playerMap = new ConcurrentHashMap<>();
    public static AudioFormat FORMAT = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 48000.0F, 16, 1, 2, 48000.0F, false);

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInsert(PlayerInteractEvent event) throws IOException {

        Player player = event.getPlayer();
        Block block = event.getClickedBlock();

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getClickedBlock() == null || event.getItem() == null || event.getItem().getItemMeta() == null || block == null) return;
        if (event.getClickedBlock().getType() != Material.JUKEBOX) return;

        if (isCustomMusicDisc(event)) {

            Component soundFileNameComponent = Objects.requireNonNull(event.getItem().getItemMeta().lore()).get(1).asComponent();
            String soundFileName = PlainTextComponentSerializer.plainText().serialize(soundFileNameComponent);

            UUID id = UUID.nameUUIDFromBytes(block.getLocation().toString().getBytes());

            Path soundFilePath = Path.of(CustomDiscs.getInstance().getDataFolder().getPath(), "musicdata", soundFileName);

            if (soundFilePath.toFile().exists()) {
                Component songNameComponent = Objects.requireNonNull(event.getItem().getItemMeta().lore()).get(0).asComponent();
                String songName = PlainTextComponentSerializer.plainText().serialize(songNameComponent);

                LocationalAudioChannel audioChannel = VoicePlugin.voicechatServerApi.createLocationalAudioChannel(id, VoicePlugin.voicechatApi.fromServerLevel(block.getLocation().getWorld()), VoicePlugin.voicechatApi.createPosition(block.getLocation().getX() + 0.5d, block.getLocation().getY() + 0.5d, block.getLocation().getZ() + 0.5d));

                try {
                    AudioPlayer audioPlayer = VoicePlugin.voicechatServerApi.createAudioPlayer((AudioChannel) audioChannel, VoicePlugin.voicechatApi.createEncoder(), readSoundFile(soundFilePath));
                    playerMap.put(id, audioPlayer);
                    audioPlayer.startPlaying();
                    player.sendMessage(ChatColor.GOLD + "Now playing: " + songName);
                } catch (Exception e) {
                    player.sendMessage(ChatColor.RED + "An error occurred while trying to play the music!");
                }
            } else {
                player.sendMessage(ChatColor.RED + "Sound file not found.");
                event.setCancelled(true);
                throw new FileNotFoundException("Sound file is missing!");
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEject(PlayerInteractEvent event) {

        Player player = event.getPlayer();
        Block block = event.getClickedBlock();

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getClickedBlock() == null || block == null) return;
        if (event.getClickedBlock().getType() != Material.JUKEBOX) return;

        if (jukeboxContainsCustomDisc(block)) {

            UUID id = UUID.nameUUIDFromBytes(block.getLocation().toString().getBytes());

            stopAudioPlayer(playerMap, id);
        }

    }

    private boolean jukeboxContainsCustomDisc(Block b) {
        BlockState blockState = b.getState();
        Jukebox jukebox = (Jukebox) blockState;

        if (jukebox.getRecord().hasItemMeta()) {
            return jukebox.getRecord().getItemMeta().hasItemFlag(ItemFlag.HIDE_ENCHANTS);
        } else {
            return false;
        }
    }

    public static short[] readSoundFile(Path file) throws UnsupportedAudioFileException, IOException {
        AudioInputStream inputStream = AudioSystem.getAudioInputStream(file.toFile());
        AudioInputStream convertedInputStream = AudioSystem.getAudioInputStream(FORMAT, inputStream);
        return VoicePlugin.voicechatApi.getAudioConverter().bytesToShorts(convertedInputStream.readAllBytes());
    }

    public boolean isCustomMusicDisc(PlayerInteractEvent e) {

        if (
                e.getItem().getItemMeta().hasItemFlag(ItemFlag.HIDE_ENCHANTS) &&
                (
                        e.getItem().getType().equals(Material.MUSIC_DISC_13) ||
                        e.getItem().getType().equals(Material.MUSIC_DISC_CAT) ||
                        e.getItem().getType().equals(Material.MUSIC_DISC_BLOCKS) ||
                        e.getItem().getType().equals(Material.MUSIC_DISC_CHIRP) ||
                        e.getItem().getType().equals(Material.MUSIC_DISC_FAR) ||
                        e.getItem().getType().equals(Material.MUSIC_DISC_MALL) ||
                        e.getItem().getType().equals(Material.MUSIC_DISC_MELLOHI) ||
                        e.getItem().getType().equals(Material.MUSIC_DISC_STAL) ||
                        e.getItem().getType().equals(Material.MUSIC_DISC_STRAD) ||
                        e.getItem().getType().equals(Material.MUSIC_DISC_WARD) ||
                        e.getItem().getType().equals(Material.MUSIC_DISC_11) ||
                        e.getItem().getType().equals(Material.MUSIC_DISC_WAIT) ||
                        e.getItem().getType().equals(Material.MUSIC_DISC_OTHERSIDE) ||
                        e.getItem().getType().equals(Material.MUSIC_DISC_5) ||
                        e.getItem().getType().equals(Material.MUSIC_DISC_PIGSTEP)
                )
        ) {
            return true;
        }
        return false;
    }

    private boolean isAudioPlayerPlaying(Map<UUID, AudioPlayer> playerMap, UUID id) {
        AudioPlayer audioPlayer = playerMap.get(id);
        System.out.println("Is music disc playing???");
        if (audioPlayer == null) {
            return false;
        } else {
            return audioPlayer.isPlaying();
        }
    }

    private void stopAudioPlayer(Map<UUID, AudioPlayer> playerMap, UUID id) {
        AudioPlayer audioPlayer = playerMap.get(id);
        if (audioPlayer != null && audioPlayer.isPlaying()) {
            audioPlayer.stopPlaying();
        }
    }
}
