package me.Navoei.customdiscsplugin;

import me.Navoei.customdiscsplugin.utils.ServerVersionChecker;
import me.Navoei.customdiscsplugin.utils.TypeChecker;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.TooltipDisplay;

import net.kyori.adventure.text.Component;
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
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class HopperManager implements Listener {

    CustomDiscs plugin = CustomDiscs.getInstance();
    PlayerManager playerManager = PlayerManager.instance();
    private final Logger pluginLogger = plugin.getLogger();

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onJukeboxInsertFromHopper(InventoryMoveItemEvent event) {
        if (CustomDiscs.isDebugMode()) {
            pluginLogger.info("DEBUG - HopperManager -> Enter : onJukeboxInsertFromHopper");
        }

        Inventory destinationInventory = event.getDestination();

        if (destinationInventory.getLocation() == null) return;
        if (!destinationInventory.getType().equals(InventoryType.JUKEBOX)) return;

        ItemStack eventItemStack = event.getItem();

        if (!TypeChecker.isCustomMusicDisc(eventItemStack)) return;

        ItemMeta discMeta = eventItemStack.getItemMeta();

        List<Component> discLore = discMeta.lore();
        if (discLore == null || discLore.isEmpty()) return;
        Component songNameComponent = discLore.getFirst().asComponent();
        String songName = PlainTextComponentSerializer.plainText().serialize(songNameComponent);
        String soundFileName = discMeta.getPersistentDataContainer().get(new NamespacedKey(plugin, "customdisc"), PersistentDataType.STRING);

        PersistentDataContainer persistentDataContainer = discMeta.getPersistentDataContainer();
        float range = CustomDiscs.getInstance().musicDiscDistance;
        NamespacedKey customSoundRangeKey = new NamespacedKey(plugin, "range");

        if(persistentDataContainer.has(customSoundRangeKey, PersistentDataType.FLOAT)) {
            float soundRange = Optional.ofNullable(persistentDataContainer.get(customSoundRangeKey, PersistentDataType.FLOAT)).orElse(0f);
            range = Math.min(soundRange, CustomDiscs.getInstance().musicDiscMaxDistance);
        }

        if (!eventItemStack.hasData(DataComponentTypes.TOOLTIP_DISPLAY) || !eventItemStack.getData(DataComponentTypes.TOOLTIP_DISPLAY).hiddenComponents().contains(DataComponentTypes.JUKEBOX_PLAYABLE)) {
            eventItemStack.setData(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplay.tooltipDisplay().addHiddenComponents(DataComponentTypes.JUKEBOX_PLAYABLE).build());
        }

        Path soundFilePath = Path.of(plugin.getDataFolder().getPath(), "musicdata", soundFileName);
        JukeboxStateManager.markJukeboxPending(destinationInventory.getLocation().getBlock().getLocation());
        assert VoicePlugin.voicechatServerApi != null;
        playerManager.playAudio(VoicePlugin.voicechatServerApi, soundFilePath, destinationInventory.getLocation().getBlock(), songName, range);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onJukeboxEjectToHopperMinecart(InventoryMoveItemEvent event) {
        if (CustomDiscs.isDebugMode()) {
            pluginLogger.info("DEBUG - HopperManager -> Enter : onJukeboxEjectToHopper");
        }

        Inventory sourceInventory = event.getSource();

        if (sourceInventory.getLocation() == null) return;
        if (!sourceInventory.getType().equals(InventoryType.JUKEBOX)) return;
        if (!TypeChecker.isCustomMusicDisc(event.getItem())) return;

        if (ServerVersionChecker.isPaperAPI()) {
            if (!(event.getDestination().getHolder(false) instanceof HopperMinecart)) return;

            InventoryHolder holderSource = sourceInventory.getHolder(false);
            if (holderSource instanceof BlockState) {
                playerManager.stopDisc(((BlockState) holderSource).getBlock());
            }
        } else {
            if (!(event.getDestination().getHolder() instanceof HopperMinecart)) return;

            InventoryHolder holderSource = sourceInventory.getHolder();
            if (holderSource instanceof BlockState) {
                playerManager.stopDisc(((BlockState) holderSource).getBlock());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChunkLoad(ChunkLoadEvent event) {
        if (CustomDiscs.isDebugMode()) {
            pluginLogger.info("DEBUG - HopperManager -> Enter : onChunkLoad");
        }
        for (BlockState blockState : event.getChunk().getTileEntities(false)) {
            if (blockState instanceof Jukebox jukebox) {
                if (!jukebox.hasRecord()) continue;
                if (!PlayerManager.instance().isAudioPlayerPlaying(blockState.getLocation()) && TypeChecker.isCustomMusicDisc(jukebox.getRecord())) {
                    jukebox.stopPlaying();
                }
            }
        }
    }

}