package me.Navoei.customdiscsplugin.command.SubCommands;

import me.Navoei.customdiscsplugin.CustomDiscs;
import me.Navoei.customdiscsplugin.language.Lang;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.executors.CommandArguments;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class ReloadSubCommand extends CommandAPICommand {
	private final CustomDiscs plugin;

	public ReloadSubCommand(CustomDiscs plugin) {
		super("reload");
		this.plugin = plugin;

		this.withFullDescription(NamedTextColor.GRAY + "Reloads the plugin configuration and language files.");
		this.withUsage("/customdisc reload");
		this.withPermission("customdiscs.reload");

		this.executesPlayer(this::onCommandPlayer);
		this.executesConsole(this::onCommandConsole);
	}

	private int onCommandPlayer(Player player, CommandArguments arguments) {
		return executeReload(player);
	}

	private int onCommandConsole(ConsoleCommandSender console, CommandArguments arguments) {
		return executeReload(console);
	}

	private int executeReload(CommandSender sender) {
		plugin.reloadPlugin();
		sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX.forSender(sender) + Lang.RELOAD_SUCCESS.forSender(sender)));
		plugin.getLogger().info("Configuration reloaded by " + sender.getName());
		return 1;
	}
}
