package me.Navoei.customdiscsplugin.command;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.executors.CommandArguments;
import me.Navoei.customdiscsplugin.CustomDiscs;
import me.Navoei.customdiscsplugin.command.SubCommands.CreateSubCommand;
import me.Navoei.customdiscsplugin.command.SubCommands.DownloadSubCommand;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class CustomDiscCommand extends CommandAPICommand {
	private final CustomDiscs plugin;
	
	public CustomDiscCommand(CustomDiscs plugin) {
		super("customdisc");
		this.plugin = plugin;
		
		this.withAliases("cd");
		this.withFullDescription("The custom discs command.");
		
		this.withSubcommand(new CreateSubCommand(plugin));
		this.withSubcommand(new DownloadSubCommand(plugin));
		
		this.executesPlayer(this::onCommandPlayer);
		this.executesConsole(this::onCommandConsole);
	}
	
	private int onCommandPlayer(Player player, CommandArguments arguments) {
		FileConfiguration config = this.plugin.getConfig();
		for (String message : config.getStringList("help")) {
			player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(message));
		}
		
		return 1;
	}
	
	private int onCommandConsole(ConsoleCommandSender executor, CommandArguments arguments) {
		executor.sendMessage(ChatColor.RED + "Only players can use this command!");
		return 1;
	}
}
