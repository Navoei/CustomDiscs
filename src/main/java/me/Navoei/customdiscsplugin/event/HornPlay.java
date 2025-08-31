package me.Navoei.customdiscsplugin.event;

import me.Navoei.customdiscsplugin.CustomDiscs;
import me.Navoei.customdiscsplugin.PlayerManager;
import me.Navoei.customdiscsplugin.utils.TypeChecker;
import me.Navoei.customdiscsplugin.VoicePlugin;

import io.papermc.paper.datacomponent.DataComponentTypes;

import me.Navoei.customdiscsplugin.language.Lang;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import org.bukkit.Material;
import org.bukkit.MusicInstrument;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import javax.annotation.Nonnull;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class HornPlay implements Listener{

    CustomDiscs customDiscs = CustomDiscs.getInstance();
    PlayerManager playerManager = PlayerManager.instance();

    private final List<Material> goatHornNotInteractable = Arrays.asList(
            Material.ANVIL,
            Material.ARMOR_STAND,
            Material.BARREL,
            Material.BEACON,
            Material.BLAST_FURNACE,
            Material.BREWING_STAND,
            Material.CARTOGRAPHY_TABLE,
            Material.CHIPPED_ANVIL,
            Material.CHISELED_BOOKSHELF,
            Material.COMMAND_BLOCK,
            Material.COMPARATOR,
            Material.CRAFTER,
            Material.CRAFTING_TABLE,
            Material.DAMAGED_ANVIL,
            Material.DAYLIGHT_DETECTOR,
            Material.DECORATED_POT,
            Material.DISPENSER,
            Material.DROPPER,
            Material.ENCHANTING_TABLE,
            Material.FLOWER_POT,
            Material.FURNACE,
            Material.GRINDSTONE,
            Material.HOPPER,
            Material.ITEM_FRAME,
            Material.LECTERN,
            Material.LEVER,
            Material.LOOM,
            Material.NOTE_BLOCK,
            Material.REPEATER,
            Material.SMITHING_TABLE,
            Material.SMOKER,
            Material.STONECUTTER,
            Material.STRUCTURE_BLOCK
    );

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) throws IOException {
        ItemStack item = event.getItem();
        Player player = event.getPlayer();

        if (item == null) return;
        if (item.getType() != Material.GOAT_HORN) return;

        Block block = event.getClickedBlock();
        if (player.hasCooldown(Material.GOAT_HORN)) return;

        if (TypeChecker.isCustomGoatHorn(event) && ((event.getAction() == Action.RIGHT_CLICK_BLOCK) || (event.getAction() == Action.RIGHT_CLICK_AIR))) {

            if (!player.isSneaking() && block != null) {
                Material targetBlockType = block.getType();
                if (goatHornNotInteractable.contains(targetBlockType)) return;
                if (targetBlockType.name().contains("_BED") || targetBlockType.name().contains("_BOAT") || targetBlockType.name().contains("_BUTTON") || targetBlockType.name().contains("CHEST") || targetBlockType.name().contains("_DOOR") /*|| targetBlockType.name().contains("_FENCE_GATE") */|| targetBlockType.name().contains("_GATE") || targetBlockType.name().contains("MINECART") || targetBlockType.name().contains("POTTED_") || targetBlockType.name().contains("_SIGN") || targetBlockType.name().contains("_TRAPDOOR")) return;
            }

            String soundFileName = event.getItem().getItemMeta().getPersistentDataContainer().get(new NamespacedKey(customDiscs, "customhorn"), PersistentDataType.STRING);

            @Nonnull PersistentDataContainer persistentDataContainer = event.getItem().getItemMeta().getPersistentDataContainer();

            float range = CustomDiscs.getInstance().customHornDistance;
            NamespacedKey customSoundRangeKey = new NamespacedKey(customDiscs, "range");

            if(persistentDataContainer.has(customSoundRangeKey, PersistentDataType.FLOAT)) {
                float soundRange = Optional.ofNullable(persistentDataContainer.get(customSoundRangeKey, PersistentDataType.FLOAT)).orElse(0f);
                range = Math.min(soundRange, CustomDiscs.getInstance().customHornMaxDistance);
            }
            
            int hornCooldown;
            NamespacedKey hornCooldownKey = new NamespacedKey(customDiscs, "goat_horn_cooldown");
            if(persistentDataContainer.has(hornCooldownKey, PersistentDataType.INTEGER)) {
                hornCooldown = Math.min(Optional.ofNullable(persistentDataContainer.get(hornCooldownKey, PersistentDataType.INTEGER)).orElse(0), CustomDiscs.getInstance().hornMaxCooldown);
            } else {
                hornCooldown = Math.min(CustomDiscs.getInstance().hornCooldown, CustomDiscs.getInstance().hornMaxCooldown);
            }

            Path soundFilePath = Path.of(customDiscs.getDataFolder().getPath(), "musicdata", soundFileName);

            if (soundFilePath.toFile().exists()) {

                String songName = "Unknown sound";

                MusicInstrument instrument = item.getDataOrDefault(DataComponentTypes.INSTRUMENT, null);
                if (instrument != null) {
                    Component songNameComponent = instrument.description(); // This is the one you're asking for
                    songName = PlainTextComponentSerializer.plainText().serialize(songNameComponent);
                } else {
                    songName = "Unknown sound";
                }

                Component customActionBarSongPlaying = LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.NOW_PLAYING.toString().replace("%song_name%", songName));

                assert VoicePlugin.voicechatServerApi != null;
                playerManager.playAudioHorn(VoicePlugin.voicechatServerApi, soundFilePath, player, customActionBarSongPlaying.asComponent(), range);
                player.setCooldown(Material.GOAT_HORN, hornCooldown);
            } else {
                player.sendMessage(NamedTextColor.RED + "Sound file not found.");
                event.setCancelled(true);
                throw new FileNotFoundException("Sound file is missing!");
            }
        }
    }

}