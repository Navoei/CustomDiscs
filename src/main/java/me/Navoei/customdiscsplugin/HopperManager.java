package me.Navoei.customdiscsplugin;

import me.Navoei.customdiscsplugin.language.Lang;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.TooltipDisplay;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import org.bukkit.block.BlockState;
import org.bukkit.block.Jukebox;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;

import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

public class HopperManager implements Listener {

    CustomDiscs customDiscs = CustomDiscs.getInstance();
    PlayerManager playerManager = PlayerManager.instance();
    private final Logger pluginLogger = customDiscs.getLogger();
    private final boolean debugModeResult = CustomDiscs.isDebugMode();

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onJukeboxInsertFromHopper(InventoryMoveItemEvent event) {
        if (debugModeResult) {
            pluginLogger.info("DEBUG - HopperManager -> Enter : onJukeboxInsertFromHopper");
        }
        if (event.getDestination().getLocation() == null) return;
        if (!event.getDestination().getType().equals(InventoryType.JUKEBOX)) return;
        if (!TypeChecker.isCustomMusicDisc(event.getItem())) return;

        Component songNameComponent = Objects.requireNonNull(event.getItem().getItemMeta().lore()).get(0).asComponent();
        String songName = PlainTextComponentSerializer.plainText().serialize(songNameComponent);
        Component customActionBarSongPlaying = LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.NOW_PLAYING.toString().replace("%song_name%", songName));

        ItemMeta discMeta = event.getItem().getItemMeta();
        String soundFileName = discMeta.getPersistentDataContainer().get(new NamespacedKey(customDiscs, "customdisc"), PersistentDataType.STRING);
        
        PersistentDataContainer persistentDataContainer = event.getItem().getItemMeta().getPersistentDataContainer();
        float range = CustomDiscs.getInstance().musicDiscDistance;
        NamespacedKey customSoundRangeKey = new NamespacedKey(customDiscs, "range");

        if(persistentDataContainer.has(customSoundRangeKey, PersistentDataType.FLOAT)) {
            float soundRange = Optional.ofNullable(persistentDataContainer.get(customSoundRangeKey, PersistentDataType.FLOAT)).orElse(0f);
            range = Math.min(soundRange, CustomDiscs.getInstance().musicDiscMaxDistance);
        }

        if (!event.getItem().hasData(DataComponentTypes.TOOLTIP_DISPLAY) || !event.getItem().getData(DataComponentTypes.TOOLTIP_DISPLAY).hiddenComponents().contains(DataComponentTypes.JUKEBOX_PLAYABLE)) {
            event.getItem().setData(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplay.tooltipDisplay().addHiddenComponents(DataComponentTypes.JUKEBOX_PLAYABLE).build());
        }

        Path soundFilePath = Path.of(customDiscs.getDataFolder().getPath(), "musicdata", soundFileName);
        assert VoicePlugin.voicechatServerApi != null;
        playerManager.playAudio(VoicePlugin.voicechatServerApi, soundFilePath, event.getDestination().getLocation().getBlock(), customActionBarSongPlaying, range);

    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onJukeboxEjectToHopperMinecart(InventoryMoveItemEvent event) {
        if (debugModeResult) {
            pluginLogger.info("DEBUG - HopperManager -> Enter : onJukeboxEjectToHopper");
        }

        if (!TypeChecker.isCustomMusicDisc(event.getItem())) return;
        if (event.getSource().getLocation() == null) return;
        if (!event.getSource().getType().equals(InventoryType.JUKEBOX)) return;

        if (ServerVersionChecker.isPaperAPI()) {
            if (!(event.getDestination().getHolder(false) instanceof HopperMinecart)) return;

            InventoryHolder holderSource = event.getSource().getHolder(false);
            if (holderSource instanceof BlockState) {
                playerManager.stopDisc(((BlockState) holderSource).getBlock());
            }
        } else {
            if (!(event.getDestination().getHolder() instanceof HopperMinecart)) return;

            InventoryHolder holderSource = event.getSource().getHolder();
            if (holderSource instanceof BlockState) {
                playerManager.stopDisc(((BlockState) holderSource).getBlock());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChunkLoad(ChunkLoadEvent event) {
        if (debugModeResult) {
            pluginLogger.info("DEBUG - HopperManager -> Enter : onChunkLoad");
        }
        for (BlockState blockState : event.getChunk().getTileEntities()) {
            if (blockState instanceof Jukebox jukebox) {
                if (!jukebox.hasRecord()) return;
                if (!PlayerManager.instance().isAudioPlayerPlaying(blockState.getLocation()) && TypeChecker.isCustomMusicDisc(jukebox.getRecord())) {
                    jukebox.stopPlaying();
                }
            }
        }
    }

    private static HopperManager instance;

    public static HopperManager instance() {
        if (CustomDiscs.isDebugMode()) {
            CustomDiscs.getInstance().getLogger().info("DEBUG - HopperManager -> Enter : HopperManager Instance");
        }
        if (instance == null) {
            instance = new HopperManager();
        }
        return instance;
    }

}