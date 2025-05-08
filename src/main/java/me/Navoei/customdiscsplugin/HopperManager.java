package me.Navoei.customdiscsplugin;

import me.Navoei.customdiscsplugin.language.Lang;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
//import org.bukkit.block.Container;
import org.bukkit.block.Jukebox;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.inventory.meta.ItemMeta;
//import org.bukkit.inventory.meta.components.JukeboxPlayableComponent;
import org.bukkit.persistence.PersistentDataType;

import java.nio.file.Path;
import java.util.Objects;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.inventory.InventoryHolder;

// Used only if logger is needed
//import java.util.logging.Logger;
//import org.bukkit.Bukkit;

public class HopperManager implements Listener {

    CustomDiscs customDiscs = CustomDiscs.getInstance();

    PlayerManager playerManager = PlayerManager.instance();
    
    //private static final Logger logger = Bukkit.getLogger(); // or Logger.getLogger("Minecraft");

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onJukeboxInsertFromHopper(InventoryMoveItemEvent event) {
        //logger.warning("Enter : onJukeboxInsertFromHopper");
        if (event.getDestination().getLocation() == null) return;
        if (!event.getDestination().getType().equals(InventoryType.JUKEBOX)) return;
        if (!isCustomMusicDisc(event.getItem())) return;

        Component songNameComponent = Objects.requireNonNull(event.getItem().getItemMeta().lore()).get(0).asComponent();
        String songName = PlainTextComponentSerializer.plainText().serialize(songNameComponent);
        Component customActionBarSongPlaying = LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.NOW_PLAYING.toString().replace("%song_name%", songName));

        ItemMeta discMeta = event.getItem().getItemMeta();
        String soundFileName = discMeta.getPersistentDataContainer().get(new NamespacedKey(customDiscs, "customdisc"), PersistentDataType.STRING);
        
        PersistentDataContainer persistentDataContainer = event.getItem().getItemMeta().getPersistentDataContainer();
        float range = CustomDiscs.getInstance().musicDiscDistance;
        NamespacedKey customSoundRangeKey = new NamespacedKey(customDiscs, "range");

        if(persistentDataContainer.has(customSoundRangeKey, PersistentDataType.FLOAT)) {
            range = Math.min(persistentDataContainer.get(customSoundRangeKey, PersistentDataType.FLOAT), CustomDiscs.getInstance().musicDiscMaxDistance);
        }
        
        /*if (discMeta.getJukeboxPlayable().isShowInTooltip()) { //DEPRECATED
            JukeboxPlayableComponent jpc = discMeta.getJukeboxPlayable();
            jpc.setShowInTooltip(false); //DEPRECATED
            discMeta.setJukeboxPlayable(jpc);
            event.getItem().setItemMeta(discMeta);
        }*/

        Path soundFilePath = Path.of(customDiscs.getDataFolder().getPath(), "musicdata", soundFileName);
        assert VoicePlugin.voicechatServerApi != null;
        playerManager.playAudio(VoicePlugin.voicechatServerApi, soundFilePath, event.getDestination().getLocation().getBlock(), customActionBarSongPlaying, range);

    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onJukeboxEjectToHopperMinecart(InventoryMoveItemEvent event) {
        //logger.warning("Enter : onJukeboxEjectToHopper");

        InventoryHolder holderSource = event.getSource().getHolder();
        InventoryHolder holderDestination = event.getDestination().getHolder();

        if (event.getSource().getLocation() == null) return;
        if (!event.getSource().getType().equals(InventoryType.JUKEBOX)) return;
        if (event.getItem().getItemMeta() == null) return;
        if (!isCustomMusicDisc(event.getItem())) return;

        if (holderDestination instanceof HopperMinecart) {
            stopDisc(((BlockState) holderSource).getBlock());
        }

    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChunkLoad(ChunkLoadEvent event) {
        //logger.warning("Enter : onChunkLoad");
        for (BlockState blockState : event.getChunk().getTileEntities()) {
            if (blockState instanceof Jukebox jukebox) {
                if (!jukebox.hasRecord()) return;
                if (!PlayerManager.instance().isAudioPlayerPlaying(blockState.getLocation()) && isCustomMusicDisc(jukebox.getRecord())) {
                    //Set the block type to force an update.
                    jukebox.stopPlaying();
                }
            }
        }
    }

    public void discToHopper(Block block) {
        if (block == null) return;
        if (!block.getLocation().getChunk().isLoaded()) return;
        if (!block.getType().equals(Material.JUKEBOX)) return;

        Jukebox jukebox = (Jukebox) block.getState();
        jukebox.stopPlaying();
    }

    private boolean isCustomMusicDisc(ItemStack item) {
        //logger.warning("Enter : isCustomMusicDisc");
        return item.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(customDiscs, "customdisc"), PersistentDataType.STRING);
    }

    private void stopDisc(Block block) {
        playerManager.stopLocationalAudio(block.getLocation());
    }

    private static HopperManager instance;

    public static HopperManager instance() {
        //logger.warning("Enter : HopperManager Instance");
        if (instance == null) {
            instance = new HopperManager();
        }
        return instance;
    }


}
