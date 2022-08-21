package me.Navoei.customdiscsplugin.command;

import me.Navoei.customdiscsplugin.command.SubCommands.ConvertCommand;
import me.Navoei.customdiscsplugin.command.SubCommands.CreateCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class CommandManager implements CommandExecutor {

    private ArrayList<SubCommand> subCommands = new ArrayList<>();

    public CommandManager() {
        subCommands.add(new CreateCommand());
        subCommands.add(new ConvertCommand());
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length > 0) {
            for (int i = 0; i < getSubCommands().size(); i++) {
                if (args[0].equalsIgnoreCase(getSubCommands().get(i).getName())) {
                    getSubCommands().get(i).perform(player, args);
                }
            }
        } else {
            player.sendMessage(ChatColor.AQUA + "---[ Custom Discs ]---");
            for (int i = 0; i < getSubCommands().size(); i++) {
                player.sendMessage(getSubCommands().get(i).getSyntax() + ChatColor.DARK_GRAY + " - " + getSubCommands().get(i).getDescription());
            }
            player.sendMessage(ChatColor.AQUA + "----------------------");
        }

        return false;
    }

    public ArrayList<SubCommand> getSubCommands() {
        return subCommands;
    }
}
