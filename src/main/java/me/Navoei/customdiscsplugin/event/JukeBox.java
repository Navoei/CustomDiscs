package me.Navoei.customdiscsplugin.event;

import de.maxhenkel.voicechat.api.audiochannel.AudioPlayer;
import de.maxhenkel.voicechat.api.audiochannel.LocationalAudioChannel;
import javazoom.spi.mpeg.sampled.convert.MpegFormatConversionProvider;
import javazoom.spi.mpeg.sampled.file.MpegAudioFileReader;
import me.Navoei.customdiscsplugin.CustomDiscs;
import me.Navoei.customdiscsplugin.VoicePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Jukebox;
import org.bukkit.block.TileState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class JukeBox implements Listener{

    private final Map<UUID, AudioPlayer> playerMap = new ConcurrentHashMap<>();
    public static AudioFormat FORMAT = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 48000F, 16, 1, 2, 48000F, false);

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInsert(PlayerInteractEvent event) throws IOException {

        Player player = event.getPlayer();
        Block block = event.getClickedBlock();

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getClickedBlock() == null || event.getItem() == null || event.getItem().getItemMeta() == null || block == null) return;
        if (event.getClickedBlock().getType() != Material.JUKEBOX) return;

        TileState tileState = (TileState) block.getState();
        PersistentDataContainer container = tileState.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(CustomDiscs.getInstance(),
                "CustomDisc");
        if (container.has(key, PersistentDataType.BYTE_ARRAY)) {
            event.setCancelled(true);
            ejectDisc(block);
            return;
        }

        if (isCustomMusicDisc(event) && !jukeboxContainsPersistentData(block) && !jukeboxContainsVanillaDisc(block)) {

            event.setCancelled(true);

            Component soundFileNameComponent = Objects.requireNonNull(event.getItem().getItemMeta().lore()).get(1).asComponent();
            String soundFileName = PlainTextComponentSerializer.plainText().serialize(soundFileNameComponent);

            UUID id = UUID.nameUUIDFromBytes(block.getLocation().toString().getBytes());

            Path soundFilePath = Path.of(CustomDiscs.getInstance().getDataFolder().getPath(), "musicdata", soundFileName);

            if (soundFilePath.toFile().exists()) {

                Component songNameComponent = Objects.requireNonNull(event.getItem().getItemMeta().lore()).get(0).asComponent();
                String songName = PlainTextComponentSerializer.plainText().serialize(songNameComponent);

                LocationalAudioChannel audioChannel = VoicePlugin.voicechatServerApi.createLocationalAudioChannel(id, VoicePlugin.voicechatApi.fromServerLevel(block.getLocation().getWorld()), VoicePlugin.voicechatApi.createPosition(block.getLocation().getX() + 0.5d, block.getLocation().getY() + 0.5d, block.getLocation().getZ() + 0.5d));

                //Run the audio player asynchronously to prevent lag when inserting discs.
                    Bukkit.getScheduler().runTaskAsynchronously(CustomDiscs.getInstance(), () -> {
                        AudioPlayer audioPlayer = null;
                        //Try playing the voice chat audio player.
                        try {
                            audioPlayer = VoicePlugin.voicechatServerApi.createAudioPlayer(audioChannel, VoicePlugin.voicechatApi.createEncoder(), readSoundFile(soundFilePath));
                        } catch (UnsupportedAudioFileException | IOException e) {
                            e.printStackTrace();
                            player.sendMessage(ChatColor.RED + "An error occurred while trying to play the music!");
                            return;
                        }
                        playerMap.put(id, audioPlayer);
                        assert audioPlayer != null;
                        audioPlayer.startPlaying();

                        //Run this in sync with the main thread after the disc is done loading.
                        Bukkit.getScheduler().runTask(CustomDiscs.getInstance(), () -> {
                            //set the persistent data container
                            container.set(key, PersistentDataType.BYTE_ARRAY, event.getItem().serializeAsBytes());
                            tileState.update();

                            //Send Player Action Bar
                            TextComponent customLoreSong = Component.text()
                                    .content("Now Playing: " + songName)
                                    .color(NamedTextColor.GOLD)
                                    .build();
                            player.sendActionBar(customLoreSong.asComponent());
                        });

                    });
                    //Remove the item from the player's hand in sync to prevent players from glitching the jukebox.
                    player.getInventory().setItem(Objects.requireNonNull(event.getHand()), null);

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

        UUID id = UUID.nameUUIDFromBytes(block.getLocation().toString().getBytes());

            //Spawn in item at the block position
            TileState tileState = (TileState) block.getState();
            PersistentDataContainer container = tileState.getPersistentDataContainer();
            NamespacedKey key = new NamespacedKey(CustomDiscs.getInstance(),
                    "CustomDisc");
            if (!container.has(key, PersistentDataType.BYTE_ARRAY)) return;
            container.get(key, PersistentDataType.BYTE_ARRAY);

            Location dropItemLocation = new Location(block.getWorld(), block.getLocation().getX(), block.getLocation().getY()+0.5d, block.getLocation().getZ());

            block.getWorld().dropItemNaturally(dropItemLocation, ItemStack.deserializeBytes(container.get(key, PersistentDataType.BYTE_ARRAY)));

            container.remove(key);
            tileState.update();

            player.swingMainHand();

        if (isAudioPlayerPlaying(playerMap, id)) {
            stopAudioPlayer(playerMap, id);
        }

    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onJukeboxBreak(BlockBreakEvent event) {

        Block block = event.getBlock();

        if (block.getType() != Material.JUKEBOX) return;

        UUID id = UUID.nameUUIDFromBytes(block.getLocation().toString().getBytes());

            //Spawn in item at the block position
            TileState tileState = (TileState) block.getState();
            PersistentDataContainer container = tileState.getPersistentDataContainer();
            NamespacedKey key = new NamespacedKey(CustomDiscs.getInstance(),
                    "CustomDisc");
            if (!container.has(key, PersistentDataType.BYTE_ARRAY)) return;
            container.get(key, PersistentDataType.BYTE_ARRAY);

            block.getWorld().dropItemNaturally(block.getLocation(), ItemStack.deserializeBytes(container.get(key, PersistentDataType.BYTE_ARRAY)));

            container.remove(key);
            tileState.update();

        if (isAudioPlayerPlaying(playerMap, id)) {
            stopAudioPlayer(playerMap, id);
        }

    }

    @EventHandler
    public void onJukeBoxExplode(EntityExplodeEvent event) {

        for (Block explodedBlock : event.blockList()) {
            if (explodedBlock.getType() == Material.JUKEBOX) {

                UUID id = UUID.nameUUIDFromBytes(explodedBlock.getLocation().toString().getBytes());

                    //Spawn in item at the block position
                    TileState tileState = (TileState) explodedBlock.getState();
                    PersistentDataContainer container = tileState.getPersistentDataContainer();
                    NamespacedKey key = new NamespacedKey(CustomDiscs.getInstance(),
                            "CustomDisc");
                    if (!container.has(key, PersistentDataType.BYTE_ARRAY)) return;
                    container.get(key, PersistentDataType.BYTE_ARRAY);

                    explodedBlock.getWorld().dropItemNaturally(explodedBlock.getLocation(), ItemStack.deserializeBytes(container.get(key, PersistentDataType.BYTE_ARRAY)));

                    container.remove(key);
                    tileState.update();

                if (isAudioPlayerPlaying(playerMap, id)) {
                    stopAudioPlayer(playerMap, id);
                }
            }
        }

    }

    public boolean isAudioPlayerPlaying(Map<UUID, AudioPlayer> playerMap, UUID id) {
        AudioPlayer audioPlayer = playerMap.get(id);
        if (audioPlayer == null) return false;
        return audioPlayer.isPlaying();
    }

    private boolean jukeboxContainsPersistentData(Block b) {
        TileState tileState = (TileState) b.getState();
        PersistentDataContainer container = tileState.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(CustomDiscs.getInstance(),
                "CustomDisc");
        return container.has(key, PersistentDataType.BYTE_ARRAY);
    }

    private boolean jukeboxContainsVanillaDisc(Block b) {
        Jukebox jukebox = (Jukebox) b.getLocation().getBlock().getState();
        return jukebox.getPlaying() != Material.AIR;
    }

    public static short[] readSoundFile(Path file) throws UnsupportedAudioFileException, IOException {
        return VoicePlugin.voicechatApi.getAudioConverter().bytesToShorts(convertFormat(file, FORMAT));
    }

    public static byte[] convertFormat(Path file, AudioFormat audioFormat) throws UnsupportedAudioFileException, IOException {
        AudioInputStream finalInputStream = null;

        if (getFileExtension(file.toFile().toString()).equals("wav")) {
            AudioInputStream inputStream = AudioSystem.getAudioInputStream(file.toFile());
            finalInputStream = AudioSystem.getAudioInputStream(audioFormat, inputStream);
        } else if (getFileExtension(file.toFile().toString()).equals("mp3")) {

            AudioInputStream inputStream = new MpegAudioFileReader().getAudioInputStream(file.toFile());
            AudioFormat baseFormat = inputStream.getFormat();
            AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, baseFormat.getSampleRate(), 16, baseFormat.getChannels(), baseFormat.getChannels() * 2, baseFormat.getFrameRate(), false);
            AudioInputStream convertedInputStream = new MpegFormatConversionProvider().getAudioInputStream(decodedFormat, inputStream);
            finalInputStream = AudioSystem.getAudioInputStream(audioFormat, convertedInputStream);

        }

        assert finalInputStream != null;
        return finalInputStream.readAllBytes();
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

    //Might Replace some code with this:
    private void ejectDisc(Block block) {
        UUID id = UUID.nameUUIDFromBytes(block.getLocation().toString().getBytes());

        //Spawn in item at the block position
        TileState tileState = (TileState) block.getState();
        PersistentDataContainer container = tileState.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(CustomDiscs.getInstance(),
                "CustomDisc");
        if (!container.has(key, PersistentDataType.BYTE_ARRAY)) return;
        container.get(key, PersistentDataType.BYTE_ARRAY);

        block.getWorld().dropItemNaturally(block.getLocation(), ItemStack.deserializeBytes(container.get(key, PersistentDataType.BYTE_ARRAY)));

        container.remove(key);
        tileState.update();

        if (isAudioPlayerPlaying(playerMap, id)) {
            stopAudioPlayer(playerMap, id);
        }
    }

    private void stopAudioPlayer(Map<UUID, AudioPlayer> playerMap, UUID id) {
        AudioPlayer audioPlayer = playerMap.get(id);
        if (audioPlayer != null && audioPlayer.isPlaying()) {
            audioPlayer.stopPlaying();
        }
    }

    private static String getFileExtension(String s) {
        int index = s.lastIndexOf(".");
        if (index > 0) {
            return s.substring(index + 1);
        } else {
            return "";
        }
    }

}
