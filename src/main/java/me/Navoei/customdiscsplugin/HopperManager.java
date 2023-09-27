package me.Navoei.customdiscsplugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Jukebox;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;

public class HopperManager implements Listener {

    CustomDiscs customDiscs = CustomDiscs.getInstance();

    PlayerManager playerManager = PlayerManager.instance();

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onJukeboxInsertFromHopper(InventoryMoveItemEvent event) {

        if (event.getDestination().getLocation() == null) return;
        if (!event.getDestination().getType().equals(InventoryType.JUKEBOX)) return;
        if (!isCustomMusicDisc(event.getItem())) return;

        Component songNameComponent = Objects.requireNonNull(event.getItem().getItemMeta().lore()).get(0).asComponent();
        String songName = PlainTextComponentSerializer.plainText().serialize(songNameComponent);

        TextComponent customActionBarSongPlaying = Component.text()
                .content("Now Playing: " + songName)
                .color(NamedTextColor.GOLD)
                .build();

        String soundFileName = event.getItem().getItemMeta().getPersistentDataContainer().get(new NamespacedKey(customDiscs, "customdisc"), PersistentDataType.STRING);


        PersistentDataContainer persistentDataContainer = event.getItem().getItemMeta().getPersistentDataContainer();
        float range = CustomDiscs.getInstance().musicDiscDistance;
        NamespacedKey customSoundRangeKey = new NamespacedKey(customDiscs, "CustomSoundRange");

        if(persistentDataContainer.has(customSoundRangeKey, PersistentDataType.FLOAT)) {
            range = Math.min(persistentDataContainer.get(customSoundRangeKey, PersistentDataType.FLOAT), CustomDiscs.getInstance().musicDiscMaxDistance);
        }


        Path soundFilePath = Path.of(customDiscs.getDataFolder().getPath(), "musicdata", soundFileName);
        assert VoicePlugin.voicechatServerApi != null;
        playerManager.playLocationalAudio(VoicePlugin.voicechatServerApi, soundFilePath, event.getDestination().getLocation().getBlock(), customActionBarSongPlaying.asComponent(), range);

    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onJukeboxEjectToHopper(InventoryMoveItemEvent event) {

        if (event.getSource().getLocation() == null) return;
        if (!event.getSource().getType().equals(InventoryType.JUKEBOX)) return;
        if (event.getItem().getItemMeta() == null) return;
        if (!isCustomMusicDisc(event.getItem())) return;

        event.setCancelled(playerManager.isAudioPlayerPlaying(event.getSource().getLocation()));

    }

    public void discToHopper(Block block) {

        if (block == null) return;
        if (!block.getLocation().getChunk().isLoaded()) return;
        if (!block.getType().equals(Material.JUKEBOX)) return;
        if (!block.getRelative(BlockFace.DOWN).getType().equals(Material.HOPPER)) return;

        Block hopperBlock = block.getRelative(BlockFace.DOWN);
        org.bukkit.block.Hopper hopper = (org.bukkit.block.Hopper) hopperBlock.getState();

        Jukebox jukebox = (Jukebox) block.getState();

        InventoryMoveItemEvent event = new InventoryMoveItemEvent(jukebox.getInventory(), jukebox.getRecord(), hopper.getInventory(), false);
        Bukkit.getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            if (!Arrays.toString(hopper.getInventory().getContents()).contains("null")) return;

            jukebox.setRecord(new ItemStack(Material.AIR));
            block.setBlockData(jukebox.getBlockData());
        }

    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChunkLoad(ChunkLoadEvent event) {
        for (BlockState blockState : event.getChunk().getTileEntities()) {
            if (blockState instanceof Jukebox jukebox) {
                if (!PlayerManager.instance().isAudioPlayerPlaying(blockState.getLocation()) && !jukebox.isPlaying()) {
                    discToHopper(blockState.getBlock());
                }
            }
        }
    }

    private boolean isCustomMusicDisc (ItemStack item) {

        return item.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(customDiscs, "customdisc"), PersistentDataType.STRING) && (
                        item.getType().equals(Material.MUSIC_DISC_13) ||
                        item.getType().equals(Material.MUSIC_DISC_CAT) ||
                        item.getType().equals(Material.MUSIC_DISC_BLOCKS) ||
                        item.getType().equals(Material.MUSIC_DISC_CHIRP) ||
                        item.getType().equals(Material.MUSIC_DISC_FAR) ||
                        item.getType().equals(Material.MUSIC_DISC_MALL) ||
                        item.getType().equals(Material.MUSIC_DISC_MELLOHI) ||
                        item.getType().equals(Material.MUSIC_DISC_STAL) ||
                        item.getType().equals(Material.MUSIC_DISC_STRAD) ||
                        item.getType().equals(Material.MUSIC_DISC_WARD) ||
                        item.getType().equals(Material.MUSIC_DISC_11) ||
                        item.getType().equals(Material.MUSIC_DISC_WAIT) ||
                        item.getType().equals(Material.MUSIC_DISC_OTHERSIDE) ||
                        item.getType().equals(Material.MUSIC_DISC_5) ||
                        item.getType().equals(Material.MUSIC_DISC_PIGSTEP)
                );
    }

    private static HopperManager instance;

    public static HopperManager instance() {
        if (instance == null) {
            instance = new HopperManager();
        }
        return instance;
    }

}
