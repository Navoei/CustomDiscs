package me.Navoei.customdiscsplugin.command.SubCommands;

import me.Navoei.customdiscsplugin.CustomDiscs;
import me.Navoei.customdiscsplugin.command.SubCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
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
                    player.sendMessage(ChatColor.RED + "You do not have permission to execute this command!");
                    return;
                }

                // /cd range 0
                //      [0]  [1]
                //Find file, if file not there then say "file not there"
                Float range = Float.valueOf(args[1]);

                if ( range < 1 || range > CustomDiscs.getInstance().musicDiscMaxDistance) {
                    player.sendMessage(ChatColor.RED + "You need to select a ranger between 1 and " + CustomDiscs.getInstance().musicDiscMaxDistance);
                    return;
                }

                //Sets the lore of the item to the quotes from the command.
                ItemStack disc = new ItemStack(player.getInventory().getItemInMainHand());
                ItemMeta meta = disc.getItemMeta();

                PersistentDataContainer data = meta.getPersistentDataContainer();
                data.set(new NamespacedKey(CustomDiscs.getInstance(), "CustomSoundRange"), PersistentDataType.FLOAT, range);

                player.getInventory().getItemInMainHand().setItemMeta(meta);

                player.sendMessage("Your range is set to: " + ChatColor.GRAY + range);

            } else {
                player.sendMessage(ChatColor.RED + "Insufficient arguments! ( /customdisc range <range>");
            }
        } else {
            player.sendMessage(ChatColor.RED + "You are not holding a music disc in your main hand!");
        }
    }
}
