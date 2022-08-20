package me.Navoei.customdiscsplugin.command.SubCommands;

import me.Navoei.customdiscsplugin.CustomDiscs;
import me.Navoei.customdiscsplugin.command.SubCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Objects;

public class ConvertCommand extends SubCommand {

    @Override
    public String getName() {
        return "convert";
    }

    @Override
    public String getDescription() {
        return ChatColor.GRAY + "Converts a disc from the old format to the new one.";
    }

    @Override
    public String getSyntax() {
        return ChatColor.GREEN + "/customdisc convert";
    }

    @Override
    public void perform(Player player, String[] args) {

        if (isOldCustomMusicDisc(player.getInventory().getItemInMainHand())) {
            ItemMeta customDiscMeta = player.getInventory().getItemInMainHand().getItemMeta();

            Component soundFileNameComponent = Objects.requireNonNull(customDiscMeta.lore()).get(1).asComponent();
            String soundFileName = PlainTextComponentSerializer.plainText().serialize(soundFileNameComponent);

            PersistentDataContainer data = customDiscMeta.getPersistentDataContainer();
            data.set(new NamespacedKey(CustomDiscs.getInstance(), "customdisc"), PersistentDataType.STRING, soundFileName);

            customDiscMeta.lore().remove(1);
            customDiscMeta.removeItemFlags(ItemFlag.values());
            customDiscMeta.removeItemFlags(ItemFlag.HIDE_ENCHANTS);

            player.getInventory().getItemInMainHand().setItemMeta(customDiscMeta);

        } else {
            player.sendMessage(ChatColor.RED + "The new format could not be applied.");
        }

    }

    private boolean isOldCustomMusicDisc (ItemStack item) {

        return item.hasItemFlag(ItemFlag.HIDE_ENCHANTS) && (
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
}
