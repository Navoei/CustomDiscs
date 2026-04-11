package me.Navoei.customdiscsplugin.utils;

import me.Navoei.customdiscsplugin.CustomDiscs;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.Locale;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class YouTubeToolManager {
	private static final Object INSTALL_LOCK = new Object();
	private static final String YT_DLP_LINUX_URL = "https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp_linux";
	private static final String YT_DLP_WINDOWS_URL = "https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp.exe";
	private static final String FFMPEG_LINUX_URL = "https://github.com/yt-dlp/FFmpeg-Builds/releases/download/latest/ffmpeg-master-latest-linux64-gpl.tar.xz";
	private static final String FFMPEG_WINDOWS_URL = "https://github.com/yt-dlp/FFmpeg-Builds/releases/download/latest/ffmpeg-master-latest-win64-gpl.zip";

	private YouTubeToolManager() {
	}

	public static ToolPaths ensureTools(CustomDiscs plugin) throws IOException, InterruptedException {
		if (!plugin.getConfig().getBoolean("youtube-auto-install-tools", true)) {
			return null;
		}

		Platform platform = detectPlatform();
		if (platform == Platform.UNSUPPORTED) {
			return null;
		}

		synchronized (INSTALL_LOCK) {
			Logger logger = plugin.getLogger();
			Path toolsDir = plugin.getDataFolder().toPath().resolve("tools");
			Files.createDirectories(toolsDir);

			Path ytDlpPath = toolsDir.resolve(platform == Platform.WINDOWS ? "yt-dlp.exe" : "yt-dlp");
			Path ffmpegPath = toolsDir.resolve(platform == Platform.WINDOWS ? "ffmpeg.exe" : "ffmpeg");

			if (!Files.exists(ytDlpPath)) {
				downloadFile(platform == Platform.WINDOWS ? YT_DLP_WINDOWS_URL : YT_DLP_LINUX_URL, ytDlpPath);
			}
			makeExecutableIfNeeded(ytDlpPath, platform);

			if (!Files.exists(ffmpegPath)) {
				Path archive = toolsDir.resolve(platform == Platform.WINDOWS ? "ffmpeg.zip" : "ffmpeg.tar.xz");
				downloadFile(platform == Platform.WINDOWS ? FFMPEG_WINDOWS_URL : FFMPEG_LINUX_URL, archive);
				if (platform == Platform.WINDOWS) {
					extractFfmpegFromZip(archive, ffmpegPath);
				} else {
					extractFfmpegFromTarXz(archive, ffmpegPath);
				}
				Files.deleteIfExists(archive);
			}
			makeExecutableIfNeeded(ffmpegPath, platform);

			if (Files.exists(ytDlpPath) && Files.exists(ffmpegPath)) {
				return new ToolPaths(ytDlpPath.toAbsolutePath().toString(), ffmpegPath.toAbsolutePath().toString());
			}

			logger.warning("Failed to ensure yt-dlp/ffmpeg binaries in plugin tools directory.");
			return null;
		}
	}

	public static boolean isAutoInstallSupported() {
		return detectPlatform() != Platform.UNSUPPORTED;
	}

	private static Platform detectPlatform() {
		String osName = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
		String osArch = System.getProperty("os.arch", "").toLowerCase(Locale.ROOT);
		boolean is64Bit = osArch.contains("amd64") || osArch.contains("x86_64");

		if (!is64Bit) {
			return Platform.UNSUPPORTED;
		}
		if (osName.contains("win")) {
			return Platform.WINDOWS;
		}
		if (osName.contains("linux")) {
			return Platform.LINUX;
		}
		return Platform.UNSUPPORTED;
	}

	private static void downloadFile(String url, Path destination) throws IOException, InterruptedException {
		Path temporaryFile = Files.createTempFile(destination.getParent(), destination.getFileName().toString(), ".download");
		try {
			HttpClient httpClient = HttpClient.newBuilder()
					.connectTimeout(Duration.ofSeconds(30))
					.followRedirects(HttpClient.Redirect.NORMAL)
					.build();
			HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create(url))
					.timeout(Duration.ofMinutes(10))
					.header("User-Agent", "CustomDiscs")
					.GET()
					.build();
			HttpResponse<Path> response = httpClient.send(request, HttpResponse.BodyHandlers.ofFile(temporaryFile));
			int statusCode = response.statusCode();
			if (statusCode < 200 || statusCode >= 300) {
				throw new IOException("Unexpected status code while downloading tool: " + statusCode);
			}
			Files.move(temporaryFile, destination, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
		} finally {
			Files.deleteIfExists(temporaryFile);
		}
	}

	private static void extractFfmpegFromZip(Path archivePath, Path targetBinary) throws IOException {
		try (InputStream inputStream = Files.newInputStream(archivePath);
			 ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {
			ZipEntry zipEntry;
			while ((zipEntry = zipInputStream.getNextEntry()) != null) {
				if (zipEntry.isDirectory()) {
					continue;
				}
				String entryName = zipEntry.getName().replace('\\', '/').toLowerCase(Locale.ROOT);
				if (!entryName.endsWith("/ffmpeg.exe")) {
					continue;
				}
				try (OutputStream outputStream = Files.newOutputStream(targetBinary)) {
					zipInputStream.transferTo(outputStream);
				}
				return;
			}
		}
		throw new IOException("Could not find ffmpeg.exe in downloaded archive.");
	}

	private static void extractFfmpegFromTarXz(Path archivePath, Path targetBinary) throws IOException {
		try (InputStream inputStream = Files.newInputStream(archivePath);
			 XZCompressorInputStream xzInputStream = new XZCompressorInputStream(inputStream);
			 TarArchiveInputStream tarInputStream = new TarArchiveInputStream(xzInputStream)) {
			TarArchiveEntry tarEntry;
			while ((tarEntry = tarInputStream.getNextEntry()) != null) {
				if (!tarEntry.isFile()) {
					continue;
				}
				String entryName = tarEntry.getName().replace('\\', '/').toLowerCase(Locale.ROOT);
				if (!entryName.endsWith("/ffmpeg")) {
					continue;
				}
				try (OutputStream outputStream = Files.newOutputStream(targetBinary)) {
					tarInputStream.transferTo(outputStream);
				}
				return;
			}
		}
		throw new IOException("Could not find ffmpeg binary in downloaded archive.");
	}

	private static void makeExecutableIfNeeded(Path path, Platform platform) throws IOException {
		if (platform == Platform.WINDOWS) {
			return;
		}
		path.toFile().setExecutable(true, false);
		if (!Files.isExecutable(path)) {
			throw new IOException("Could not set executable bit on " + path);
		}
	}

	private enum Platform {
		WINDOWS,
		LINUX,
		UNSUPPORTED
	}

	public record ToolPaths(String ytDlpPath, String ffmpegPath) {
	}
}
