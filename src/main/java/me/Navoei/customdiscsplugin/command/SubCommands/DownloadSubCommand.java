package me.Navoei.customdiscsplugin.command.SubCommands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.arguments.TextArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import me.Navoei.customdiscsplugin.CustomDiscs;
import me.Navoei.customdiscsplugin.language.Lang;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.Objects;

public class DownloadSubCommand extends CommandAPICommand {
	private final CustomDiscs plugin;
	
	public DownloadSubCommand(CustomDiscs plugin) {
		super("download");
		this.plugin = plugin;
		
		this.withFullDescription(NamedTextColor.GRAY + "Downloads a file from a given URL.");
		this.withUsage("/customdisc download <url> <filename.extension>");
		
		this.withArguments(new TextArgument("url"));
		this.withArguments(new StringArgument("filename"));
		
		this.executesPlayer(this::onCommandPlayer);
		this.executesConsole(this::onCommandConsole);
	}
	
	private int onCommandPlayer(Player player, CommandArguments arguments) {
		if (!player.hasPermission("customdiscs.download")) {
			player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.NO_PERMISSION.toString()));
			return 1;
		}
		
		Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
			try {
				try {
					URI uri = new URI(Objects.requireNonNull(arguments.getByClass("url", String.class)));
					URL fileURL = uri.toURL();
					String filename = Objects.requireNonNull(arguments.getByClass("filename", String.class));
					if (filename.contains("../")) {
						player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.INVALID_FILENAME.toString()));
						return;
					}

					System.out.println(filename);

					if (!getFileExtension(filename).equals("wav") && !getFileExtension(filename).equals("mp3") && !getFileExtension(filename).equals("flac")) {
						player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.INVALID_FORMAT.toString()));
						return;
					}

					player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.DOWNLOADING_FILE.toString()));

					URLConnection connection = fileURL.openConnection();
					if (connection != null) {
						long size = connection.getContentLengthLong() / 1048576;
						if (size > this.plugin.getConfig().getInt("max-download-size", 50)) {
							player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.FILE_TOO_LARGE.toString().replace("%max_download_size%", String.valueOf(this.plugin.getConfig().getInt("max-download-size", 50)))));
							return;
						}
					}

					Path downloadPath = Path.of(this.plugin.getDataFolder().getPath(), "musicdata", filename);
					File downloadFile = new File(downloadPath.toUri());
					FileUtils.copyURLToFile(fileURL, downloadFile);

					player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.SUCCESSFUL_DOWNLOAD.toString().replace("%file_path%", "plugins/CustomDiscs/musicdata/" + filename)));
					player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.CREATE_DISC.toString().replace("%filename%", filename)));
				} catch (URISyntaxException | MalformedURLException e) {
					player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.DOWNLOAD_ERROR.toString()));
					e.printStackTrace();
				}
			} catch (IOException e) {
				player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.DOWNLOAD_ERROR.toString()));
				e.printStackTrace();
			}
		});
		
		return 1;
	}
	
	private int onCommandConsole(ConsoleCommandSender executor, CommandArguments arguments) {
		executor.sendMessage(NamedTextColor.RED + "Only players can use this command : '"+arguments+"'!");
		return 1;
	}
	
	private String getFileExtension(String s) {
		int index = s.lastIndexOf(".");
		if (index > 0) {
			return s.substring(index + 1);
		} else {
			return "";
		}
	}
}
