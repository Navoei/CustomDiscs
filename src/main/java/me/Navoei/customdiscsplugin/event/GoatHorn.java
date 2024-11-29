package me.Navoei.customdiscsplugin.event;

import me.Navoei.customdiscsplugin.CustomDiscs;
import me.Navoei.customdiscsplugin.PlayerManager;
import me.Navoei.customdiscsplugin.VoicePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.block.data.Powerable;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

public class GoatHorn implements Listener {

    static CustomDiscs customDiscs = CustomDiscs.getInstance();
    PlayerManager playerManager = PlayerManager.instance();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) throws IOException {
        ItemStack item = event.getItem();
        Player player = event.getPlayer();

        if (item == null) return;
        if (item.getType() != Material.GOAT_HORN) return;

        Block block = event.getClickedBlock();
        if (player.hasCooldown(Material.GOAT_HORN)) return;

        if (isCustomGoatHorn(event) && ((event.getAction() == Action.RIGHT_CLICK_BLOCK) || (event.getAction() == Action.RIGHT_CLICK_AIR))) {

            if (event.getAction() == Action.RIGHT_CLICK_BLOCK && !player.isSneaking()) {
                if ((block.getBlockData() instanceof Powerable) || (block instanceof TileState) && event.useInteractedBlock().equals(Event.Result.ALLOW)) {
                    return;
                }
            }

            String soundFileName = event.getItem().getItemMeta().getPersistentDataContainer().get(new NamespacedKey(customDiscs, "customhorn"), PersistentDataType.STRING);

            @NotNull PersistentDataContainer persistentDataContainer = event.getItem().getItemMeta().getPersistentDataContainer();

            float range;
            NamespacedKey customSoundRangeKey = new NamespacedKey(customDiscs, "range");
            if(persistentDataContainer.has(customSoundRangeKey, PersistentDataType.FLOAT)) {
                range = Math.min(persistentDataContainer.get(customSoundRangeKey, PersistentDataType.FLOAT), CustomDiscs.getInstance().musicDiscMaxDistance);
            } else {
                range = Math.min(CustomDiscs.getInstance().musicDiscDistance, CustomDiscs.getInstance().musicDiscMaxDistance);
            }

            int hornCooldown;
            NamespacedKey hornCooldownKey = new NamespacedKey(customDiscs, "horncooldown");
            if(persistentDataContainer.has(hornCooldownKey, PersistentDataType.INTEGER)) {
                hornCooldown = Math.min(persistentDataContainer.get(hornCooldownKey, PersistentDataType.INTEGER), CustomDiscs.getInstance().hornMaxCooldown) * 20;
            } else {
                hornCooldown = Math.min(Math.round(CustomDiscs.getInstance().hornCooldown) * 20, CustomDiscs.getInstance().hornMaxCooldown) * 20;
            }

            Path soundFilePath = Path.of(customDiscs.getDataFolder().getPath(), "musicdata", soundFileName);

            if (soundFilePath.toFile().exists()) {

                Component songNameComponent = Objects.requireNonNull(event.getItem().getItemMeta().lore()).get(0).asComponent();
                String songName = PlainTextComponentSerializer.plainText().serialize(songNameComponent);

                TextComponent customActionBarSongPlaying = Component.text()
                        .content("Now Horn Playing: " + songName)
                        .color(NamedTextColor.GOLD)
                        .build();

                assert VoicePlugin.voicechatServerApi != null;
                playerManager.playLocationalAudioHorn(VoicePlugin.voicechatServerApi, soundFilePath, player, customActionBarSongPlaying.asComponent(), range);
                player.setCooldown(Material.GOAT_HORN, hornCooldown);
            } else {
                player.sendMessage(NamedTextColor.RED + "Sound file not found.");
                event.setCancelled(true);
                throw new FileNotFoundException("Sound file is missing!");
            }
        }
    }

    public static boolean isCustomGoatHorn(PlayerInteractEvent e) {
        if (e.getItem()==null) return false;
        return e.getItem().getItemMeta().getPersistentDataContainer().has(new NamespacedKey(customDiscs, "customhorn"));
    }

}