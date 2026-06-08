package me.Navoei.customdiscsplugin.command.SubCommands;

import me.Navoei.customdiscsplugin.CustomDiscs;
import me.Navoei.customdiscsplugin.language.Lang;
import me.Navoei.customdiscsplugin.utils.FilebinUtils;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.TextArgument;
import dev.jorel.commandapi.executors.CommandArguments;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.command.CommandSender;
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
import java.util.logging.Level;
import java.util.logging.Logger;

public class DownloadSubCommand extends CommandAPICommand {
	private final CustomDiscs plugin;

	public DownloadSubCommand(CustomDiscs plugin) {
		super("download");
		this.plugin = plugin;

		this.withFullDescription(NamedTextColor.GRAY + "Downloads a file from a given URL.");
		this.withUsage("/customdisc download <url> <filename.extension>");
		this.withPermission("customdiscs.download");

		this.withArguments(new TextArgument("url"));
		this.withArguments(new TextArgument("filename"));

		this.executesPlayer(this::onCommandPlayer);
		this.executesConsole(this::onCommandConsole);
	}

	private int onCommandPlayer(Player player, CommandArguments arguments) {
		return executeDownload(player, arguments);
	}

	private int onCommandConsole(ConsoleCommandSender console, CommandArguments arguments) {
		return executeDownload(console, arguments);
	}

	private int executeDownload(CommandSender sender, CommandArguments arguments) {
		final Logger pluginLogger = plugin.getLogger();

        plugin.getServer().getAsyncScheduler().runNow(this.plugin, scheduledTask ->  {
			try {
				try {
					String urlString = Objects.requireNonNull(arguments.getByClass("url", String.class));
					URI uri = new URI(urlString);
					if (uri.getScheme() == null) return;
                    if (!uri.getScheme().equalsIgnoreCase("http") && !uri.getScheme().equalsIgnoreCase("https")) {
                        sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX.forSender(sender) + Lang.INVALID_PROTOCOL.forSender(sender)));
                        return;
                    }
					URL fileURL = uri.toURL();

                    String filename = Objects.requireNonNull(arguments.getByClass("filename", String.class));
                    if (filename.length() > this.plugin.filenameMaximumLength) {
                        sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX.forSender(sender) + Lang.INVALID_FILENAME_LENGTH.forSender(sender).replace("%filename_length_value%", Integer.toString(this.plugin.filenameMaximumLength))));
                        return;
                    }

					if(CustomDiscs.isDebugMode()) {
						pluginLogger.info("DEBUG - Download File URL: " + fileURL);
						pluginLogger.info("DEBUG - File name: " + filename);
					}

					if (!plugin.isMusicdataPathSafe(filename)) {
						sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX.forSender(sender) + Lang.INVALID_FILENAME.forSender(sender)));
						return;
					}
					if (!plugin.isMusicdataDepthAllowed(filename)) {
						Lang depthMessage = "none".equals(plugin.subdirectoryDepth) ? Lang.SUBDIRECTORY_NOT_ALLOWED : Lang.SUBDIRECTORY_DEPTH_EXCEEDED;
						sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX.forSender(sender) + depthMessage.forSender(sender)));
						return;
					}

					String fileExtension = getFileExtension(filename);
					if (!fileExtension.equals("wav") && !fileExtension.equals("mp3") && !fileExtension.equals("flac")) {
						sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX.forSender(sender) + Lang.INVALID_FORMAT.forSender(sender)));
						return;
					}

					String finalFilename = filename;
					File downloadFileLocation = Path.of(this.plugin.getDataFolder().getPath(), "musicdata", finalFilename).toFile();
					if (downloadFileLocation.exists()) {
						finalFilename = getAvailableFilename(filename);
						downloadFileLocation = Path.of(this.plugin.getDataFolder().getPath(), "musicdata", finalFilename).toFile();
						sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX.forSender(sender) + Lang.FILE_ALREADY_EXISTS.forSender(sender).replace("%filename%", filename).replace("%new_filename%", finalFilename)));
					}

					File parentDirectory = downloadFileLocation.getParentFile();
					if (parentDirectory != null && !parentDirectory.exists()) {
						parentDirectory.mkdirs();
					}

					sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX.forSender(sender) + Lang.DOWNLOADING_FILE.forSender(sender)));

					int maxDownloadSize = this.plugin.getConfig().getInt("max-download-size", 50);

					if (FilebinUtils.isFilebinUrl(urlString)) {
						try {
							if (FilebinUtils.isFilebinDirectUrl(urlString)) {
								if (CustomDiscs.isDebugMode()) {
									pluginLogger.info("DEBUG - Detected Filebin direct file URL, downloading...");
								}
								FilebinUtils.downloadFilebinFile(fileURL, downloadFileLocation, maxDownloadSize);
							} else {
								if (CustomDiscs.isDebugMode()) {
									pluginLogger.info("DEBUG - Detected Filebin bin URL, querying API...");
								}
								FilebinUtils.FilebinFileInfo fileInfo = FilebinUtils.getFirstAudioFile(urlString, maxDownloadSize);
								if (CustomDiscs.isDebugMode()) {
									pluginLogger.info("DEBUG - Filebin file found: " + fileInfo.filename() + " (" + fileInfo.bytes() + " bytes)");
								}
								FilebinUtils.downloadFilebinFile(fileInfo.downloadUrl(), downloadFileLocation, maxDownloadSize);
							}
						} catch (FilebinUtils.FileTooLargeException e) {
							sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX.forSender(sender) + Lang.FILE_TOO_LARGE.forSender(sender).replace("%max_download_size%", String.valueOf(maxDownloadSize))));
							return;
						} catch (FilebinUtils.NoAudioFilesException e) {
							sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX.forSender(sender) + Lang.FILEBIN_NO_AUDIO.forSender(sender)));
							pluginLogger.warning("No valid audio file found in Filebin bin: " + urlString);
							if (CustomDiscs.isDebugMode()) {
								pluginLogger.severe("Exception output: " + e);
							}
							return;
						} catch (IOException e) {
							sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX.forSender(sender) + Lang.FILEBIN_API_ERROR.forSender(sender)));
							pluginLogger.warning("Filebin download failed for: " + urlString);
							if (CustomDiscs.isDebugMode()) {
								pluginLogger.severe("Exception output: " + e);
							}
							return;
						}
					} else {
						URLConnection connection = fileURL.openConnection();
						if (connection != null) {
							long fileSizeInMB = connection.getContentLengthLong() / 1048576;
							if (fileSizeInMB > maxDownloadSize) {
								sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX.forSender(sender) + Lang.FILE_TOO_LARGE.forSender(sender).replace("%max_download_size%", String.valueOf(maxDownloadSize))));
								return;
							}
						}
						FileUtils.copyURLToFile(fileURL, downloadFileLocation);
					}

					sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX.forSender(sender) + Lang.SUCCESSFUL_DOWNLOAD.forSender(sender).replace("%file_path%", "plugins/CustomDiscs/musicdata/" + finalFilename)));
					sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX.forSender(sender) + Lang.CREATE_DISC.forSender(sender).replace("%filename%", finalFilename)));
				} catch (URISyntaxException | MalformedURLException e) {
					sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX.forSender(sender) + Lang.DOWNLOAD_ERROR.forSender(sender)));
					pluginLogger.warning("A download error occurred.");
					if(CustomDiscs.isDebugMode()) {
						pluginLogger.log(Level.SEVERE, "Exception output: ", e);
					}
				}
			} catch (IOException e) {
				sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX.forSender(sender) + Lang.DOWNLOAD_ERROR.forSender(sender)));
				pluginLogger.warning("A download error occurred.");
				if(CustomDiscs.isDebugMode()) {
					pluginLogger.log(Level.SEVERE, "Exception output: ", e);
				}
			}
		});

		return 1;
	}

	private String getAvailableFilename(String filename) {
		int lastDotIndex = filename.lastIndexOf('.');
		String baseName = lastDotIndex > 0 ? filename.substring(0, lastDotIndex) : filename;
		String fileExtension  = lastDotIndex > 0 ? filename.substring(lastDotIndex) : "";
		int counter = 1;
		String availableFilename;
		do {
			availableFilename = baseName + "_" + counter + fileExtension;
			counter++;
		} while (Path.of(this.plugin.getDataFolder().getPath(), "musicdata", availableFilename).toFile().exists());
		return availableFilename;
	}

	private String getFileExtension(String filename) {
		int dotIndex = filename.lastIndexOf(".");
		if (dotIndex > 0) {
			return filename.substring(dotIndex + 1);
		} else {
			return "";
		}
	}

}