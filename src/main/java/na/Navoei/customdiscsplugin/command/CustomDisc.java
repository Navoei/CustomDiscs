package na.Navoei.customdiscsplugin.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CustomDisc implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }

        Player p = (Player) sender;

        if (command.getName().equalsIgnoreCase("customdisc")) {
            if (args.length == 2) {

            } else {
                sender.sendMessage("§cIncorrect arguments! ( /customdiscs <filename> [customname] )");
            }
        }

        return false;
    }
}
