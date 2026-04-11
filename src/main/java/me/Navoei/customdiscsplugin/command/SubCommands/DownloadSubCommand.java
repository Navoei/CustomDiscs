package me.Navoei.customdiscsplugin.command.SubCommands;

import me.Navoei.customdiscsplugin.CustomDiscs;
import me.Navoei.customdiscsplugin.language.Lang;
import me.Navoei.customdiscsplugin.utils.FilebinUtils;
import me.Navoei.customdiscsplugin.utils.YouTubeToolManager;

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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DownloadSubCommand extends CommandAPICommand {
	private final CustomDiscs plugin;

	public DownloadSubCommand(CustomDiscs plugin) {
		super("download");
		this.plugin = plugin;

		this.withFullDescription(NamedTextColor.GRAY + "Downloads a file from a given URL.");
		this.withUsage("/customdisc download <url> [filename.extension]");
		this.withPermission("customdiscs.download");

		this.withArguments(new TextArgument("url"));
		this.withOptionalArguments(new TextArgument("filename"));

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
                        sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.INVALID_PROTOCOL.toString()));
                        return;
                    }
					String filename = arguments.getByClass("filename", String.class);

					if(CustomDiscs.isDebugMode()) {
						pluginLogger.info("DEBUG - Download File URL: " + uri.toURL());
						pluginLogger.info("DEBUG - File name: " + filename);
					}

					if (isYouTubeUrl(uri)) {
						handleYouTubeDownload(sender, urlString, filename);
						return;
					}
					handleDirectDownload(sender, urlString, uri, filename);
				} catch (URISyntaxException | MalformedURLException e) {
					sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.DOWNLOAD_ERROR.toString()));
					pluginLogger.warning("A download error occurred.");
					if(CustomDiscs.isDebugMode()) {
						pluginLogger.log(Level.SEVERE, "Exception output: ", e);
					}
				}
			} catch (IOException e) {
				sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.DOWNLOAD_ERROR.toString()));
				pluginLogger.warning("A download error occurred.");
				if(CustomDiscs.isDebugMode()) {
					pluginLogger.log(Level.SEVERE, "Exception output: ", e);
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.DOWNLOAD_ERROR.toString()));
				pluginLogger.warning("A download task was interrupted.");
				if (CustomDiscs.isDebugMode()) {
					pluginLogger.log(Level.SEVERE, "Exception output: ", e);
				}
			}
		});

		return 1;
	}

	private void handleDirectDownload(CommandSender sender, String urlString, URI uri, String filename) throws IOException {
		if (filename == null || filename.isBlank()) {
			sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.DOWNLOAD_FILENAME_REQUIRED.toString()));
			return;
		}

		if (filename.length() > this.plugin.filenameMaximumLength) {
			sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.INVALID_FILENAME_LENGTH.toString().replace("%filename_length_value%", Integer.toString(this.plugin.filenameMaximumLength))));
			return;
		}
		if (!plugin.isMusicdataPathSafe(filename)) {
			sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.INVALID_FILENAME.toString()));
			return;
		}
		if (!plugin.isMusicdataDepthAllowed(filename)) {
			Lang depthMessage = "none".equals(plugin.subdirectoryDepth) ? Lang.SUBDIRECTORY_NOT_ALLOWED : Lang.SUBDIRECTORY_DEPTH_EXCEEDED;
			sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + depthMessage.toString()));
			return;
		}

		String fileExtension = getFileExtension(filename);
		if (!fileExtension.equals("wav") && !fileExtension.equals("mp3") && !fileExtension.equals("flac")) {
			sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.INVALID_FORMAT.toString()));
			return;
		}

		URL fileURL = uri.toURL();

		String finalFilename = filename;
		File downloadFileLocation = Path.of(this.plugin.getDataFolder().getPath(), "musicdata", finalFilename).toFile();
		if (downloadFileLocation.exists()) {
			finalFilename = getAvailableFilename(filename);
			downloadFileLocation = Path.of(this.plugin.getDataFolder().getPath(), "musicdata", finalFilename).toFile();
			sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.FILE_ALREADY_EXISTS.toString().replace("%filename%", filename).replace("%new_filename%", finalFilename)));
		}

		File parentDirectory = downloadFileLocation.getParentFile();
		if (parentDirectory != null && !parentDirectory.exists()) {
			parentDirectory.mkdirs();
		}

		sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.DOWNLOADING_FILE.toString()));

		int maxDownloadSize = this.plugin.getConfig().getInt("max-download-size", 50);

		if (FilebinUtils.isFilebinUrl(urlString)) {
			try {
				if (FilebinUtils.isFilebinDirectUrl(urlString)) {
					if (CustomDiscs.isDebugMode()) {
						plugin.getLogger().info("DEBUG - Detected Filebin direct file URL, downloading...");
					}
					FilebinUtils.downloadFilebinFile(fileURL, downloadFileLocation, maxDownloadSize);
				} else {
					if (CustomDiscs.isDebugMode()) {
						plugin.getLogger().info("DEBUG - Detected Filebin bin URL, querying API...");
					}
					FilebinUtils.FilebinFileInfo fileInfo = FilebinUtils.getFirstAudioFile(urlString, maxDownloadSize);
					if (CustomDiscs.isDebugMode()) {
						plugin.getLogger().info("DEBUG - Filebin file found: " + fileInfo.filename() + " (" + fileInfo.bytes() + " bytes)");
					}
					FilebinUtils.downloadFilebinFile(fileInfo.downloadUrl(), downloadFileLocation, maxDownloadSize);
				}
			} catch (FilebinUtils.FileTooLargeException e) {
				sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.FILE_TOO_LARGE.toString().replace("%max_download_size%", String.valueOf(maxDownloadSize))));
				return;
			} catch (FilebinUtils.NoAudioFilesException e) {
				sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.FILEBIN_NO_AUDIO.toString()));
				plugin.getLogger().warning("No valid audio file found in Filebin bin: " + urlString);
				if (CustomDiscs.isDebugMode()) {
					plugin.getLogger().severe("Exception output: " + e);
				}
				return;
			} catch (IOException e) {
				sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.FILEBIN_API_ERROR.toString()));
				plugin.getLogger().warning("Filebin download failed for: " + urlString);
				if (CustomDiscs.isDebugMode()) {
					plugin.getLogger().severe("Exception output: " + e);
				}
				return;
			}
		} else {
			URLConnection connection = fileURL.openConnection();
			if (connection != null) {
				long fileSizeInMB = connection.getContentLengthLong() / 1048576;
				if (fileSizeInMB > maxDownloadSize) {
					sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.FILE_TOO_LARGE.toString().replace("%max_download_size%", String.valueOf(maxDownloadSize))));
					return;
				}
			}
			FileUtils.copyURLToFile(fileURL, downloadFileLocation);
		}

		sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.SUCCESSFUL_DOWNLOAD.toString().replace("%file_path%", "plugins/CustomDiscs/musicdata/" + finalFilename)));
		sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.CREATE_DISC.toString().replace("%filename%", finalFilename)));
	}

	private void handleYouTubeDownload(CommandSender sender, String urlString, String requestedFilename) throws IOException, InterruptedException {
		if (!this.plugin.getConfig().getBoolean("youtube-download-enable", true)) {
			sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.YOUTUBE_DOWNLOAD_DISABLED.toString()));
			return;
		}

		String ytDlpPath = Objects.requireNonNullElse(this.plugin.getConfig().getString("yt-dlp-path"), "yt-dlp");
		String ffmpegPath = Objects.requireNonNullElse(this.plugin.getConfig().getString("ffmpeg-path"), "ffmpeg");
		int timeoutSeconds = Math.max(30, this.plugin.getConfig().getInt("youtube-process-timeout-seconds", 300));
		int maxDownloadSize = this.plugin.getConfig().getInt("max-download-size", 50);

		if (!isExecutableAvailable(ytDlpPath, "--version", timeoutSeconds) || !isExecutableAvailable(ffmpegPath, "-version", timeoutSeconds)) {
			if (this.plugin.getConfig().getBoolean("youtube-auto-install-tools", true)) {
				sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.YOUTUBE_TOOLS_INSTALLING.toString()));
				try {
					YouTubeToolManager.ToolPaths installedTools = YouTubeToolManager.ensureTools(this.plugin);
					if (installedTools != null) {
						ytDlpPath = installedTools.ytDlpPath();
						ffmpegPath = installedTools.ffmpegPath();
						sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(
								Lang.PREFIX + Lang.YOUTUBE_TOOLS_INSTALLED.toString()
										.replace("%yt_dlp_path%", ytDlpPath)
										.replace("%ffmpeg_path%", ffmpegPath)
						));
					} else if (!YouTubeToolManager.isAutoInstallSupported()) {
						sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.YOUTUBE_TOOLS_UNSUPPORTED_PLATFORM.toString()));
					} else {
						sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.YOUTUBE_TOOLS_INSTALL_FAILED.toString()));
					}
				} catch (IOException e) {
					sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.YOUTUBE_TOOLS_INSTALL_FAILED.toString()));
					if (CustomDiscs.isDebugMode()) {
						this.plugin.getLogger().log(Level.SEVERE, "Failed to auto-install youtube tools", e);
					}
				}
			}
		}

		if (!isExecutableAvailable(ytDlpPath, "--version", timeoutSeconds) || !isExecutableAvailable(ffmpegPath, "-version", timeoutSeconds)) {
			sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(
					Lang.PREFIX + Lang.YOUTUBE_TOOLS_MISSING.toString()
							.replace("%yt_dlp_path%", ytDlpPath)
							.replace("%ffmpeg_path%", ffmpegPath)
			));
			return;
		}

		String finalFilename = resolveYouTubeFilename(urlString, requestedFilename, ytDlpPath, timeoutSeconds);
		if (finalFilename.length() > this.plugin.filenameMaximumLength) {
			sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.INVALID_FILENAME_LENGTH.toString().replace("%filename_length_value%", Integer.toString(this.plugin.filenameMaximumLength))));
			return;
		}
		if (!plugin.isMusicdataPathSafe(finalFilename)) {
			sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.INVALID_FILENAME.toString()));
			return;
		}
		if (!plugin.isMusicdataDepthAllowed(finalFilename)) {
			Lang depthMessage = "none".equals(plugin.subdirectoryDepth) ? Lang.SUBDIRECTORY_NOT_ALLOWED : Lang.SUBDIRECTORY_DEPTH_EXCEEDED;
			sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + depthMessage.toString()));
			return;
		}

		File downloadFileLocation = Path.of(this.plugin.getDataFolder().getPath(), "musicdata", finalFilename).toFile();
		if (downloadFileLocation.exists()) {
			String uniqueFilename = getAvailableFilename(finalFilename);
			sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(
					Lang.PREFIX + Lang.FILE_ALREADY_EXISTS.toString()
							.replace("%filename%", finalFilename)
							.replace("%new_filename%", uniqueFilename)
			));
			finalFilename = uniqueFilename;
			downloadFileLocation = Path.of(this.plugin.getDataFolder().getPath(), "musicdata", finalFilename).toFile();
		}

		File parentDirectory = downloadFileLocation.getParentFile();
		if (parentDirectory != null && !parentDirectory.exists()) {
			parentDirectory.mkdirs();
		}

		sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.DOWNLOADING_FILE.toString()));

		String outputTemplate;
		String absolutePath = downloadFileLocation.getAbsolutePath();
		if (absolutePath.endsWith(".mp3")) {
			outputTemplate = absolutePath.substring(0, absolutePath.length() - 4) + ".%(ext)s";
		} else {
			outputTemplate = absolutePath + ".%(ext)s";
		}
		List<String> command = new ArrayList<>();
		command.add(ytDlpPath);
		command.add("--no-playlist");
		command.add("--no-warnings");
		command.add("--no-progress");
		command.add("--extract-audio");
		command.add("--audio-format");
		command.add("mp3");
		command.add("--audio-quality");
		command.add("0");
		command.add("--ffmpeg-location");
		command.add(ffmpegPath);
		if (maxDownloadSize > 0) {
			command.add("--max-filesize");
			command.add(maxDownloadSize + "M");
		}
		command.add("-o");
		command.add(outputTemplate);
		command.add(urlString);

		ProcessResult result = runProcess(command, timeoutSeconds);
		if (result.exitCode() != 0) {
			String outputLower = result.output().toLowerCase(Locale.ROOT);
			if (outputLower.contains("max-filesize")) {
				sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.FILE_TOO_LARGE.toString().replace("%max_download_size%", String.valueOf(maxDownloadSize))));
				return;
			}
			sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.DOWNLOAD_ERROR.toString()));
			if (CustomDiscs.isDebugMode()) {
				plugin.getLogger().warning("DEBUG - yt-dlp command failed (" + result.exitCode() + "):\n" + result.output());
			}
			return;
		}
		if (!downloadFileLocation.exists()) {
			sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.DOWNLOAD_ERROR.toString()));
			plugin.getLogger().warning("YouTube download process succeeded but resulting file was not found: " + downloadFileLocation.getAbsolutePath());
			return;
		}

		sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.SUCCESSFUL_DOWNLOAD.toString().replace("%file_path%", "plugins/CustomDiscs/musicdata/" + finalFilename)));
		sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.CREATE_DISC.toString().replace("%filename%", finalFilename)));
	}

	private boolean isYouTubeUrl(URI uri) {
		String host = uri.getHost();
		if (host == null) return false;
		String normalizedHost = host.toLowerCase(Locale.ROOT);
		return normalizedHost.equals("youtube.com")
				|| normalizedHost.equals("www.youtube.com")
				|| normalizedHost.equals("m.youtube.com")
				|| normalizedHost.equals("music.youtube.com")
				|| normalizedHost.equals("youtu.be")
				|| normalizedHost.equals("www.youtu.be");
	}

	private String resolveYouTubeFilename(String urlString, String requestedFilename, String ytDlpPath, int timeoutSeconds) throws IOException, InterruptedException {
		String baseName;
		if (requestedFilename != null && !requestedFilename.isBlank()) {
			baseName = stripExtension(requestedFilename);
		} else {
			String title = fetchYouTubeTitle(urlString, ytDlpPath, timeoutSeconds);
			if (title == null || title.isBlank()) {
				baseName = "youtube_audio_" + System.currentTimeMillis();
			} else {
				baseName = title;
			}
		}

		int maxBaseLength = Math.max(1, this.plugin.filenameMaximumLength - 4);
		return sanitizeFilename(baseName, maxBaseLength) + ".mp3";
	}

	private String fetchYouTubeTitle(String urlString, String ytDlpPath, int timeoutSeconds) throws IOException, InterruptedException {
		List<String> command = List.of(
				ytDlpPath,
				"--no-playlist",
				"--no-warnings",
				"--get-title",
				urlString
		);
		ProcessResult result = runProcess(command, timeoutSeconds);
		if (result.exitCode() != 0) {
			return null;
		}
		String output = result.output().trim();
		if (output.isBlank()) {
			return null;
		}
		String[] lines = output.split("\\R");
		return lines.length == 0 ? null : lines[0];
	}

	private boolean isExecutableAvailable(String executable, String versionArgument, int timeoutSeconds) {
		try {
			ProcessResult result = runProcess(List.of(executable, versionArgument), timeoutSeconds);
			return result.exitCode() == 0;
		} catch (IOException | InterruptedException e) {
			return false;
		}
	}

	private ProcessResult runProcess(List<String> command, int timeoutSeconds) throws IOException, InterruptedException {
		ProcessBuilder processBuilder = new ProcessBuilder(command);
		processBuilder.redirectErrorStream(true);
		Path outputLog = Files.createTempFile("customdiscs-download-", ".log");
		try {
			processBuilder.redirectOutput(outputLog.toFile());
			Process process = processBuilder.start();

			boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
			if (!finished) {
				process.destroyForcibly();
				String timeoutOutput = Files.readString(outputLog, StandardCharsets.UTF_8);
				return new ProcessResult(-1, "Process timed out\n" + timeoutOutput);
			}

			String output = Files.readString(outputLog, StandardCharsets.UTF_8);
			return new ProcessResult(process.exitValue(), output);
		} finally {
			Files.deleteIfExists(outputLog);
		}
	}

	private String stripExtension(String filename) {
		int lastDotIndex = filename.lastIndexOf('.');
		return lastDotIndex > 0 ? filename.substring(0, lastDotIndex) : filename;
	}

	private String sanitizeFilename(String filename, int maxLength) {
		String sanitized = filename
				.replaceAll("[\\x00-\\x1F<>:\"/\\\\|?*]", "_")
				.replaceAll("\\s+", "_")
				.replaceAll("_+", "_")
				.replaceAll("^[._ ]+", "")
				.replaceAll("[._ ]+$", "");

		if (sanitized.isBlank()) {
			sanitized = "youtube_audio_" + System.currentTimeMillis();
		}
		if (sanitized.length() > maxLength) {
			sanitized = sanitized.substring(0, maxLength);
		}
		return sanitized;
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

	private record ProcessResult(int exitCode, String output) {}

}
