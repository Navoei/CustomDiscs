package na.Navoei.customdiscsplugin.command;

import
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import org.jetbrains.annotations.NotNull;

public class CustomDisc implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command!");
            return true;
        }

        Player p = (Player) sender;

        // /customdisc wewontbealone "We wont be alone"
        if (command.getName().equalsIgnoreCase("customdisc")) {
            if (args.length == 2) {



                p.sendMessage("Your filename is:" + args[0]);
                p.sendMessage("Your custom name is " + args[1]);
            } else {
                p.sendMessage(ChatColor.RED + "Incorrect arguments! ( /customdisc <filename> [customname] )");
            }
        }

        return false;
    }
}
