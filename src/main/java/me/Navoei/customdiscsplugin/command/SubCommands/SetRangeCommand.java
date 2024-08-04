package me.Navoei.customdiscsplugin.command.SubCommands;

import me.Navoei.customdiscsplugin.CustomDiscs;
import me.Navoei.customdiscsplugin.command.SubCommand;
import me.Navoei.customdiscsplugin.language.Lang;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SetRangeCommand extends SubCommand {

    @Override
    public String getName() {
        return "range";
    }

    @Override
    public String getDescription() {
        return ChatColor.GRAY + "Set Range.";
    }

    @Override
    public String getSyntax() {
        return ChatColor.GREEN + "/customdisc range <range>";
    }

    @Override
    public void perform(Player player, String[] args) {
        if (CustomDiscs.isMusicDisc(player)) {
            if (args.length >= 2) {
                
                if (!player.hasPermission("customdiscs.range")) {
                    Component textComponent = LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.NO_PERMISSION.toString());
                    player.sendMessage(textComponent);
                    return;
                }

                // /cd range 0
                //      [0]  [1]
                Float range = Float.valueOf(args[1]);

                if ( range < 1 || range > CustomDiscs.getInstance().musicDiscMaxDistance) {
                    Component textComponent = LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.INVALID_RANGE.toString().replace("%range_value%", Float.toString(CustomDiscs.getInstance().musicDiscMaxDistance)));
                    player.sendMessage(textComponent);
                    return;
                }

                //Sets the lore of the item to the quotes from the command.
                ItemStack disc = new ItemStack(player.getInventory().getItemInMainHand());
                ItemMeta meta = disc.getItemMeta();

                PersistentDataContainer data = meta.getPersistentDataContainer();
                data.set(new NamespacedKey(CustomDiscs.getInstance(), "CustomSoundRange"), PersistentDataType.FLOAT, range);

                player.getInventory().getItemInMainHand().setItemMeta(meta);

                Component textComponentCustomRange = LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.CREATE_CUSTOM_RANGE.toString().replace("%custom_range%", Float.toString(range)));
                player.sendMessage(textComponentCustomRange);
            } else {
                Component textComponent = LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.INVALID_ARGUMENTS.toString().replace("%command_syntax%", getSyntax()));
                player.sendMessage(textComponent);
            }
        } else {
            Component textComponent = LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.NOT_HOLDING_DISC.toString());
            player.sendMessage(textComponent);
        }
    }
}