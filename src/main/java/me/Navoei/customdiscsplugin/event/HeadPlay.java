package me.Navoei.customdiscsplugin.event;

import me.Navoei.customdiscsplugin.CustomDiscs;
import me.Navoei.customdiscsplugin.PlayerManager;
import me.Navoei.customdiscsplugin.VoicePlugin;
import me.Navoei.customdiscsplugin.language.Lang;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Skull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.NotePlayEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

public class HeadPlay implements Listener{

    CustomDiscs customDiscs = CustomDiscs.getInstance();
    PlayerManager playerManager = PlayerManager.instance();

    // Triggered on every noteblock interaction.
    // Most of the function will only be executed if a custom player head is found on top of the noteblock.
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onNotePlay(NotePlayEvent event) throws IOException {
        Block noteBlock = event.getBlock();

        if (noteBlock.getType() != Material.NOTE_BLOCK) return;

        if (PlayerManager.instance().isAudioPlayerPlaying(noteBlock.getLocation())) return;

        Block headBlock = noteBlock.getRelative(BlockFace.UP);
        if (headBlock.getType() != Material.PLAYER_HEAD) return;

        Skull skull = (Skull) headBlock.getState();
        PersistentDataContainer persistentDataContainer = skull.getPersistentDataContainer();

        if (persistentDataContainer.has(new NamespacedKey(customDiscs, "customhead"), PersistentDataType.STRING)) {

            String soundFileName = persistentDataContainer.get(new NamespacedKey(customDiscs, "customhead"), PersistentDataType.STRING);

            float range = CustomDiscs.getInstance().customHeadDistance;
            NamespacedKey customSoundRangeKey = new NamespacedKey(customDiscs, "range");
            NamespacedKey customLore = new NamespacedKey(customDiscs, "headlore");

            if(persistentDataContainer.has(customSoundRangeKey, PersistentDataType.FLOAT)) {
                float soundRange = Optional.ofNullable(persistentDataContainer.get(customSoundRangeKey, PersistentDataType.FLOAT)).orElse(0f);
                range = Math.min(soundRange, CustomDiscs.getInstance().customHeadMaxDistance);
            }

            Path soundFilePath = Path.of(customDiscs.getDataFolder().getPath(), "musicdata", soundFileName);

            if (soundFilePath.toFile().exists()) {
                Component songNameComponent = Optional.ofNullable(persistentDataContainer.get(customLore, PersistentDataType.STRING)).map(GsonComponentSerializer.gson()::deserialize).orElse(Component.text("Unknown Song", NamedTextColor.GRAY));
                String songName = PlainTextComponentSerializer.plainText().serialize(songNameComponent);
                Component customActionBarSongPlaying = LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.NOW_PLAYING.toString().replace("%song_name%", songName));

                assert VoicePlugin.voicechatServerApi != null;
                playerManager.playAudioHead(VoicePlugin.voicechatServerApi, soundFilePath, noteBlock, customActionBarSongPlaying, range);
            } else {
                event.setCancelled(true);
                throw new FileNotFoundException("Sound file is missing!");
            }
        }
    }

    // Event to delay by 1 tick after a player head had been placed
    // Delay will only be triggered on custom player head created with this plugin
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onHeadPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();

        if (item.getType() != Material.PLAYER_HEAD) return;
        if (!(item.getItemMeta() instanceof SkullMeta meta)) return;

        PersistentDataContainer itemPDC = meta.getPersistentDataContainer();
        if (!itemPDC.has(new NamespacedKey(customDiscs, "customhead"), PersistentDataType.STRING)) return;

        Block block = event.getBlockPlaced();
        Bukkit.getRegionScheduler().runDelayed(customDiscs, block.getLocation(), task -> {
            if (block.getType() != Material.PLAYER_HEAD && block.getType() != Material.PLAYER_WALL_HEAD) return;

            Skull skull = (Skull) block.getState();
            PersistentDataContainer blockPDC = skull.getPersistentDataContainer();

            NamespacedKey headKey = new NamespacedKey(customDiscs, "customhead");
            NamespacedKey loreKey = new NamespacedKey(customDiscs, "headlore");
            NamespacedKey rangeKey = new NamespacedKey(customDiscs, "range");

            if (itemPDC.has(headKey, PersistentDataType.STRING)) {
                String customheadValue = itemPDC.get(headKey, PersistentDataType.STRING);
                blockPDC.set(headKey, PersistentDataType.STRING, customheadValue);
            }

            if (itemPDC.has(loreKey, PersistentDataType.STRING)) {
                String headloreValue = itemPDC.get(loreKey, PersistentDataType.STRING);
                blockPDC.set(loreKey, PersistentDataType.STRING, headloreValue);
            }

            if (itemPDC.has(rangeKey, PersistentDataType.FLOAT)) {
                Float rangeValue = itemPDC.get(rangeKey, PersistentDataType.FLOAT);
                blockPDC.set(rangeKey, PersistentDataType.FLOAT, rangeValue);
            }

            skull.update(true, true);
        }, 1L);
    }

}