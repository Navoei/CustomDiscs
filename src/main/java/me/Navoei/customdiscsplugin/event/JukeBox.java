package me.Navoei.customdiscsplugin.event;

import de.maxhenkel.voicechat.api.audiochannel.AudioChannel;
import de.maxhenkel.voicechat.api.audiochannel.AudioPlayer;
import de.maxhenkel.voicechat.api.audiochannel.LocationalAudioChannel;
import me.Navoei.customdiscsplugin.CustomDiscs;
import me.Navoei.customdiscsplugin.VoicePlugin;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.sound.SoundStop;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.TileState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
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

    @EventHandler
    public void onInsert(PlayerInteractEvent event) throws IOException {

        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        UUID id = UUID.randomUUID();

        if (event.getAction().isRightClick() && isCustomMusicDisc(player) && Objects.requireNonNull(block).getType().equals(Material.JUKEBOX)) {

            Component soundFileComponent = Objects.requireNonNull(player.getInventory().getItemInMainHand().getItemMeta().lore()).get(1).asComponent();
            String soundFileName = PlainTextComponentSerializer.plainText().serialize(soundFileComponent);

            Path soundFilePath = Path.of(CustomDiscs.getInstance().getDataFolder() + "\\musicdata\\" + soundFileName);
            if (soundFilePath.toFile().exists()) {
                Component songNameComponent = Objects.requireNonNull(player.getInventory().getItemInMainHand().getItemMeta().lore()).get(0).asComponent();
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
                throw new FileNotFoundException("ERROR: Sound file is missing!");
            }
        }

        //if (event.getAction().isRightClick() && Objects.requireNonNull(block).getType().equals(Material.JUKEBOX) && jukeboxContainsDisc(block)) {
        //
        //}
    }

    //private boolean jukeboxContainsDisc(Block block) {
    //    Block b = block;
    //    BlockState blockState = b.getState();
    //    if (blockState instanceof TileState) {
    //
    //    }
    //    return false;
    //}

    public static short[] readSoundFile(Path file) throws UnsupportedAudioFileException, IOException {
        AudioInputStream inputStream = AudioSystem.getAudioInputStream(file.toFile());
        AudioInputStream convertedInputStream = AudioSystem.getAudioInputStream(FORMAT, inputStream);
        return VoicePlugin.voicechatApi.getAudioConverter().bytesToShorts(convertedInputStream.readAllBytes());
    }

    public boolean isCustomMusicDisc(Player p) {

        if (
                p.getInventory().getItemInMainHand().hasItemFlag(ItemFlag.HIDE_ENCHANTS) &&
                (
                p.getInventory().getItemInMainHand().getType().equals(Material.MUSIC_DISC_13) ||
                p.getInventory().getItemInMainHand().getType().equals(Material.MUSIC_DISC_CAT) ||
                p.getInventory().getItemInMainHand().getType().equals(Material.MUSIC_DISC_BLOCKS) ||
                p.getInventory().getItemInMainHand().getType().equals(Material.MUSIC_DISC_CHIRP) ||
                p.getInventory().getItemInMainHand().getType().equals(Material.MUSIC_DISC_FAR) ||
                p.getInventory().getItemInMainHand().getType().equals(Material.MUSIC_DISC_MALL) ||
                p.getInventory().getItemInMainHand().getType().equals(Material.MUSIC_DISC_MELLOHI) ||
                p.getInventory().getItemInMainHand().getType().equals(Material.MUSIC_DISC_STAL) ||
                p.getInventory().getItemInMainHand().getType().equals(Material.MUSIC_DISC_STRAD) ||
                p.getInventory().getItemInMainHand().getType().equals(Material.MUSIC_DISC_WARD) ||
                p.getInventory().getItemInMainHand().getType().equals(Material.MUSIC_DISC_11) ||
                p.getInventory().getItemInMainHand().getType().equals(Material.MUSIC_DISC_WAIT) ||
                p.getInventory().getItemInMainHand().getType().equals(Material.MUSIC_DISC_OTHERSIDE) ||
                p.getInventory().getItemInMainHand().getType().equals(Material.MUSIC_DISC_5) ||
                p.getInventory().getItemInMainHand().getType().equals(Material.MUSIC_DISC_PIGSTEP)
                )
        ) {
            return true;
        }
        return false;
    }

}
