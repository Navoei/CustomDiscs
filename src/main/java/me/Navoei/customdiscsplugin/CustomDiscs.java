package me.Navoei.customdiscsplugin;

import me.Navoei.customdiscsplugin.command.CustomDiscCommand;
import me.Navoei.customdiscsplugin.event.JukeBox;
import me.Navoei.customdiscsplugin.event.HeadPlay;
import me.Navoei.customdiscsplugin.event.HornPlay;
import me.Navoei.customdiscsplugin.language.Lang;
import me.Navoei.customdiscsplugin.utils.ServerVersionChecker;
import me.Navoei.customdiscsplugin.utils.UpdateChecker;

import de.maxhenkel.voicechat.api.BukkitVoicechatService;

import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEffect;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIPaperConfig;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import org.bukkit.block.Jukebox;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class CustomDiscs extends JavaPlugin {
	static CustomDiscs instance;
	private static final int CONFIG_VERSION = 2;

	// For my own memo (Athar) - Implemented during development cycle where keys changed, isn't needed for now by could be in the future, so keeping it just in case.
	// Mapping Keys/Values for when a default value need to be overridden upon update.
	// If a key format changed at any given config-version, we must declare it there.
	// Example: if "default-horn-instrument" changed format at version 2, we need a line like this one :
	// 2, List.of("default-horn-instrument")
	// The "Integer" is the targeted config-version, and the List is the key to overwrite the value with the new default from the config.yml file (so we didn't keep the user value, by safety due to major change).
	private static final Map<Integer, List<String>> MIGRATION_EXCLUDED_KEYS = Map.of(
			//2, List.of("default-horn-instrument")
	);

	@Nullable
	private VoicePlugin voicechatPlugin;
	@Nullable
	private UpdateChecker updateChecker;
	private Logger pluginLogger;
    private static boolean debugMode = false;
    private static Component[] helpMessage;
    private static final LegacyComponentSerializer LegacyComponentAmpersand = LegacyComponentSerializer.legacyAmpersand();
    private static final List<String> BUNDLED_LANGS = List.of("en", "fr", "de", "ru", "es", "pt", "zh", "pl", "nl", "it", "tr", "cs", "hu", "ko", "tt");
	public static final List<String> VALID_HORN_INSTRUMENTS = List.of("ponder_goat_horn", "sing_goat_horn", "seek_goat_horn", "feel_goat_horn", "admire_goat_horn", "call_goat_horn", "yearn_goat_horn", "dream_goat_horn");
	public static boolean musicDiscEnable = true;
	public static boolean musicDiscPlayingEnable = true;
	public float musicDiscDistance;
	public float musicDiscMaxDistance;
	public float musicDiscVolume;
	public static boolean customHornEnable = true;
	public static boolean customHornPlayingEnable = true;
	public float customHornDistance;
	public float customHornMaxDistance;
	public int hornCooldown;
	public int hornMaxCooldown;
	public static boolean customHeadEnable = true;
	public static boolean customHeadPlayingEnable = true;
	public float customHeadDistance;
	public float customHeadMaxDistance;
    public int filenameMaximumLength;
	public String subdirectoryDepth;
	public String defaultHornInstrument;

	@Override
	public void onLoad() {
		CustomDiscs.instance = this;

        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().load();

        CommandAPI.onLoad(new CommandAPIPaperConfig(this).verboseOutput(true).fallbackToLatestNMS(true));
	}
	
	@Override
	public void onEnable() {
		pluginLogger = getLogger();

		PacketEvents.getAPI().init();

		CommandAPI.onEnable();

        new CustomDiscCommand(this).register("customdiscs");

		BukkitVoicechatService service = getServer().getServicesManager().load(BukkitVoicechatService.class);
		
		this.saveDefaultConfig();
		migrateConfig();
		loadLangs();
		loadConfigValues();

		// Checking server version and display console message in case the server is not officially supported by us
        ServerVersionChecker serverVersionChecker = new ServerVersionChecker(this);
		serverVersionChecker.checkVersion();

		File musicDataDirectory = new File(this.getDataFolder(), "musicdata");
		if (!(musicDataDirectory.exists())) {
			musicDataDirectory.mkdirs();
		}

        helpMessage = new Component[]{
                LegacyComponentAmpersand.deserialize("&8-[&6CustomDiscs v"+ this.getPluginMeta().getVersion() +" - Help Page&8]-"),
                LegacyComponentAmpersand.deserialize("&aAuthor&7: ")
                        .append(Component.text("Navoei")
                        .color(TextColor.color(0xd9a334))
                        .decorate(TextDecoration.UNDERLINED)
                        .clickEvent(ClickEvent.openUrl("https://github.com/Navoei"))
                        .hoverEvent(HoverEvent.showText(Component.text("Click to open Navoei's GitHub page")))),
                LegacyComponentAmpersand.deserialize("&aContributors&7: ")
                        .append(Component.text("Athar42")
                        .color(TextColor.color(0xd9a334))
                        .decorate(TextDecoration.UNDERLINED)
                        .clickEvent(ClickEvent.openUrl("https://github.com/Athar42"))
                        .hoverEvent(HoverEvent.showText(Component.text("Click to open Athar42's GitHub page"))))
                        .append(Component.text(" / "))
                        .append(Component.text("alfw")
                        .color(TextColor.color(0xd9a334))
                        .decorate(TextDecoration.UNDERLINED)
                        .clickEvent(ClickEvent.openUrl("https://github.com/alfw"))
                        .hoverEvent(HoverEvent.showText(Component.text("Click to open alfw's GitHub page")))),
                LegacyComponentAmpersand.deserialize("&fGit&0Hub&7: ")
                        .append(Component.text("https://github.com/Navoei/CustomDiscs")
                        .color(NamedTextColor.BLUE)
                        .decorate(TextDecoration.UNDERLINED)
                        .clickEvent(ClickEvent.openUrl("https://github.com/Navoei/CustomDiscs"))
                        .hoverEvent(HoverEvent.showText(Component.text("Click to open CustomDiscs' GitHub page")))),
                LegacyComponentAmpersand.deserialize("&aDiscord&7: ")
                        .append(Component.text("https://discord.gg/rJtBRmRFCr")
                        .color(NamedTextColor.BLUE)
                        .decorate(TextDecoration.UNDERLINED)
                        .clickEvent(ClickEvent.openUrl("https://discord.gg/rJtBRmRFCr"))
                        .hoverEvent(HoverEvent.showText(Component.text("Click to join our Discord !"))))
        };
		
		if (service != null) {
			voicechatPlugin = new VoicePlugin();
			service.registerPlugin(voicechatPlugin);
			pluginLogger.info("Successfully registered CustomDiscs plugin");
		} else {
			pluginLogger.info("Failed to register CustomDiscs plugin");
		}

		if (getConfig().getBoolean("update-checker.enabled", true)) {
			String updateChannel = getConfig().getString("update-checker.channel", "release");
			updateChecker = new UpdateChecker(this, updateChannel);
			updateChecker.start();
		}

		registerListeners();

		PacketEvents.getAPI().getEventManager().registerListener(new PacketListenerAbstract(PacketListenerPriority.NORMAL) {
			@Override
			public void onPacketSend(PacketSendEvent event) {
				if (event.getPacketType() != PacketType.Play.Server.EFFECT) return;
				try {
					WrapperPlayServerEffect wrapper = new WrapperPlayServerEffect(event);
					if (wrapper.getType() != 1010) return;
					if (!isMusicDiscEnable()) return;
					if (!(event.getPlayer() instanceof Player player)) return;

					Vector3i jukeboxPosition = wrapper.getPosition();
					Location jukeboxLocation = new Location(player.getWorld(), jukeboxPosition.x, jukeboxPosition.y, jukeboxPosition.z);

					if (JukeboxStateManager.isCustomDiscLocation(jukeboxLocation)) {
						event.setCancelled(true);

						getServer().getRegionScheduler().run(CustomDiscs.this, jukeboxLocation, task ->
								JukeboxStateManager.start((Jukebox) jukeboxLocation.getBlock().getState())
						);
					}
				} catch (Exception e) {
					if (isDebugMode()) {
						pluginLogger.warning("Unexpected packet content detected (" + e.getClass().getSimpleName() + ": " + e.getMessage() + ")");
					}
				}
			}
		});
		
	}
	
	/**
	 * Load configuration values from config.yml.
	 * Called from onEnable() and reloadPlugin().
	 */
	private void loadConfigValues() {
		debugMode = getConfig().getBoolean("debugMode", false);
		musicDiscEnable = getConfig().getBoolean("music-disc-enable");
		musicDiscPlayingEnable = getConfig().getBoolean("music-disc-playing-enable");
		musicDiscDistance = getConfig().getInt("music-disc-distance");
		musicDiscMaxDistance = getConfig().getInt("music-disc-max-distance");
		try {
			musicDiscVolume = Float.parseFloat(Objects.requireNonNull(getConfig().getString("music-disc-volume")));
		} catch (NumberFormatException | NullPointerException e) {
			musicDiscVolume = 1.0f;
			pluginLogger.warning("Invalid music-disc-volume in config.yml, defaulting to 1.0");
		}
		customHornEnable = getConfig().getBoolean("custom-horn-enable");
		customHornPlayingEnable = getConfig().getBoolean("custom-horn-playing-enable");
		customHornDistance = getConfig().getInt("custom-horn-distance");
		customHornMaxDistance = getConfig().getInt("custom-horn-max-distance");
		hornCooldown = getConfig().getInt("horn-cooldown");
		hornMaxCooldown = getConfig().getInt("horn-max-cooldown");
		customHeadEnable = getConfig().getBoolean("custom-head-enable");
		customHeadPlayingEnable = getConfig().getBoolean("custom-head-playing-enable");
		customHeadDistance = getConfig().getInt("custom-head-distance");
		customHeadMaxDistance = getConfig().getInt("custom-head-max-distance");
		filenameMaximumLength = getConfig().getInt("filename-maximum-length");
		subdirectoryDepth = getConfig().getString("subdirectory-depth", "none");

		String hornInstrumentConfig = getConfig().getString("default-horn-instrument", "ponder_goat_horn");
		if (!VALID_HORN_INSTRUMENTS.contains(hornInstrumentConfig)) {
			pluginLogger.warning("Invalid default-horn-instrument '" + hornInstrumentConfig + "' in config.yml. Using a random valid instrument.");
			hornInstrumentConfig = VALID_HORN_INSTRUMENTS.get(ThreadLocalRandom.current().nextInt(VALID_HORN_INSTRUMENTS.size()));
		}
		defaultHornInstrument = "minecraft:" + hornInstrumentConfig;

		if (hornCooldown <= 0) { hornCooldown = 1; }
		if (hornMaxCooldown <= 0) { hornMaxCooldown = 1; }
	}

	/**
	 * Migrate config.yml if its version is outdated.
	 * Regenerates the file from the default one and re-applies user values as needed.
	 * Called only once at startup.
	 */
	private void migrateConfig() {
		int currentConfigVersion = getConfig().getInt("config-version", 0);
		if (currentConfigVersion == CONFIG_VERSION) return;

		pluginLogger.info("Updating config.yml from version " + currentConfigVersion + " to " + CONFIG_VERSION + "...");

		Map<String, Object> savedValues = new HashMap<>();
		for (String key : getConfig().getKeys(true)) {
			if (!getConfig().isConfigurationSection(key)) {
				savedValues.put(key, getConfig().get(key));
			}
		}

		File configFile = new File(getDataFolder(), "config.yml");
		File backupFile = new File(getDataFolder(), "config.yml.bak");
		if (backupFile.exists()) backupFile.delete();
		configFile.renameTo(backupFile);

		saveResource("config.yml", false);
		reloadConfig();

		List<String> excludedKeys = new ArrayList<>();
		for (int version = currentConfigVersion + 1; version <= CONFIG_VERSION; version++) {
			excludedKeys.addAll(MIGRATION_EXCLUDED_KEYS.getOrDefault(version, List.of()));
		}

		for (Map.Entry<String, Object> configEntry : savedValues.entrySet()) {
			String key = configEntry.getKey();
			if (key.equals("config-version")) continue;
			if (excludedKeys.contains(key)) continue;
			if (getConfig().contains(key)) {
				getConfig().set(key, configEntry.getValue());
			}
		}

		saveConfig();
		pluginLogger.info("Config migration complete. Backup saved as config.yml.bak");
	}

	/**
	 * Register the event listeners based on current configuration.
	 * Called from onEnable() and reloadPlugin().
	 */
	private void registerListeners() {
		if (isMusicDiscEnable()) {
			getServer().getPluginManager().registerEvents(new JukeBox(), this);
			getServer().getPluginManager().registerEvents(new HopperManager(), this);
		}
		if (isCustomHeadEnable()) {
			getServer().getPluginManager().registerEvents(new HeadPlay(), this);
		}
		if (isCustomHornEnable()) {
			getServer().getPluginManager().registerEvents(new HornPlay(), this);
		}

		getServer().getPluginManager().registerEvents(new Listener() {
			@EventHandler
			public void onJoin(PlayerJoinEvent event) {
				if (updateChecker == null) return;
				String latestVersion = updateChecker.getLatestVersion();
				if (latestVersion == null) return;
				Player player = event.getPlayer();
				if (!player.isOp() && !player.hasPermission("customdiscs.update")) return;

				String currentVersion = getPluginMeta().getVersion();
				Component playerUpdateMessage = LegacyComponentAmpersand.deserialize(Lang.PREFIX.forPlayer(player) + Lang.UPDATE_AVAILABLE.forPlayer(player).replace("%latest_version%", latestVersion).replace("%current_version%", currentVersion));
				Component linkUpdateMessage = LegacyComponentAmpersand.deserialize("&8[&6CustomDiscs&8]&r &7➜ ").append(Component.text(UpdateChecker.MODRINTH_PAGE_URL).color(NamedTextColor.AQUA).decorate(TextDecoration.UNDERLINED).clickEvent(ClickEvent.openUrl(UpdateChecker.MODRINTH_PAGE_URL)).hoverEvent(HoverEvent.showText(Component.text("Click to open the Modrinth page"))));
				player.sendMessage(playerUpdateMessage);
				player.sendMessage(linkUpdateMessage);
			}
		}, this);
	}

	/**
	 * Reload configuration, language, and re-register listeners.
	 * Called by the reload subcommand.
	 */
	public void reloadPlugin() {
		reloadConfig();
		loadConfigValues();
		loadLangs();
		HandlerList.unregisterAll(this);
		registerListeners();
	}

	@Override
	public void onDisable() {
		CommandAPI.onDisable();
		PacketEvents.getAPI().terminate();
		if (updateChecker != null) {
			updateChecker.stop();
		}
		if (voicechatPlugin != null) {
			getServer().getServicesManager().unregister(voicechatPlugin);
			pluginLogger.info("Successfully unregistered CustomDiscs plugin");
		}
	}
	
	public static CustomDiscs getInstance() {
		return instance;
	}
        
	/**
	 * Load all language files from the langs/ folder.
	 * Also migrates the old original lang.yml into langs/en.yml if present and needed (from older release).
	 * Also copies bundled lang files from the JAR for any missing languages and update (rebuild) existing ones (if needed).
	 */
	public void loadLangs() {
		String defaultLang = getConfig().getString("default-lang", "en");

		File langsDir = new File(getDataFolder(), "langs");
		langsDir.mkdirs();

		// Migrate old lang.yml file if existing first
		File oldOriginalLangFile = new File(getDataFolder(), "lang.yml");
		File enNewLangFile = new File(langsDir, "en.yml");
		if (!enNewLangFile.exists() && oldOriginalLangFile.exists()) {
			try {
				Files.move(oldOriginalLangFile.toPath(), enNewLangFile.toPath());
				pluginLogger.info("Migrated lang.yml into langs/en.yml");
			} catch (IOException e) {
				pluginLogger.warning("Could not migrate lang.yml into langs/en.yml: " + e.getMessage());
			}
		}

		// Create all lang files if they don't exist
		for (String lang : BUNDLED_LANGS) {
			if (!new File(langsDir, lang + ".yml").exists()) {
				try {
					saveResource("langs/" + lang + ".yml", false);
				} catch (Exception e) {
					pluginLogger.warning("Could not extract bundled lang file: " + lang + ".yml");
				}
			}
		}

		// Load and rebuild/update all *.yml files in langs/
		Map<String, YamlConfiguration> loadedLangs = new HashMap<>();
		File[] langFiles = langsDir.listFiles((dir, name) -> name.endsWith(".yml"));
		if (langFiles != null) {
			for (File langFile : langFiles) {
				// Load existing file from disk
				String langCode = langFile.getName().replace(".yml", "");
				YamlConfiguration existingConfig = YamlConfiguration.loadConfiguration(langFile);

				// Load bundled reference from JAR (fallback for missing keys)
				YamlConfiguration referenceConfig = null;
				if (BUNDLED_LANGS.contains(langCode)) {
					InputStream resourceStream = getResource("langs/" + langCode + ".yml");
					if (resourceStream != null) {
						referenceConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(resourceStream));
					}
				}

				// Rebuild order: on disk/admin value > plugin bundled reference file > hardcoded default (en)
				YamlConfiguration rebuilt = new YamlConfiguration();
				for (Lang entry : Lang.values()) {
					String message = existingConfig.getString(entry.getPath());
					if (message == null && referenceConfig != null) {
						message = referenceConfig.getString(entry.getPath());
					}
					rebuilt.set(entry.getPath(), message != null ? message : entry.getDefault());
				}
				try {
					rebuilt.save(langFile);
				} catch (IOException e) {
					pluginLogger.warning("Failed to save lang file: " + langFile.getName());
				}
				loadedLangs.put(langCode, rebuilt);
			}
		}

		// Validate default language (failsafe to avoid any NPE)
		if (loadedLangs.isEmpty()) {
			pluginLogger.severe("No language files could be loaded from langs/ folder. Messages will use default English values.");
			defaultLang = null;
		} else if (!loadedLangs.containsKey(defaultLang)) {
			pluginLogger.warning("default-lang '" + defaultLang + "' not found in langs/. Falling back to hardcoded English defaults.");
			defaultLang = null;
		}

		Lang.setLangs(loadedLangs, defaultLang);
	}
	
	public static void copyInputStreamToFile(InputStream input, File file) {
		
		try (OutputStream output = new FileOutputStream(file)) {
			input.transferTo(output);
		} catch (IOException ioException) {
			if (isDebugMode()) {
				CustomDiscs.getInstance().getLogger().log(Level.SEVERE, "Exception output: ", ioException);
			}
		}
		
	}

	/**
	 * Get the debugMode configuration.
	 *
	 * @return The boolean value of debugMode.
	 */
	public static boolean isDebugMode() { return debugMode; }

	/**
	 * Get the musicDiscPlayingEnable configuration.
	 *
	 * @return The boolean value of musicDiscPlayingEnable.
	 */
	public static boolean isMusicDiscEnable() { return musicDiscEnable; }

	/**
	 * Get the customHornPlayingEnable configuration.
	 *
	 * @return The boolean value of customHornPlayingEnable.
	 */
	public static boolean isCustomHornEnable() { return customHornEnable; }

	/**
	 * Get the customHeadPlayingEnable configuration.
	 *
	 * @return The boolean value of customHeadPlayingEnable.
	 */
	public static boolean isCustomHeadEnable() { return customHeadEnable; }

	/**
	 * Get the musicDiscPlayingEnable configuration.
	 *
	 * @return The boolean value of musicDiscPlayingEnable.
	 */
	public static boolean isMusicDiscPlayingEnable() { return musicDiscPlayingEnable; }

	/**
	 * Get the customHornPlayingEnable configuration.
	 *
	 * @return The boolean value of customHornPlayingEnable.
	 */
	public static boolean isCustomHornPlayingEnable() { return customHornPlayingEnable; }

	/**
	 * Get the customHeadPlayingEnable configuration.
	 *
	 * @return The boolean value of customHeadPlayingEnable.
	 */
	public static boolean isCustomHeadPlayingEnable() { return customHeadPlayingEnable; }

    /**
     * Get the help message.
     *
     * @return The text component for the help message.
     */
    public static Component[] getHelpMessage() { return helpMessage; }

	/**
	 * Checks that a filename (which may include subdirectory path) resolves within the musicdata directory and cannot escape it via traversal.
	 */
	public boolean isMusicdataPathSafe(String filename) {
		Path musicDataDir = Path.of(getDataFolder().getPath(), "musicdata").toAbsolutePath().normalize();
		Path resolvedPath = musicDataDir.resolve(filename).toAbsolutePath().normalize();
		return resolvedPath.startsWith(musicDataDir);
	}

	/**
	 * Checks that the subdirectory depth of a filename is allowed by the configured subdirectory-depth setting.
	 */
	public boolean isMusicdataDepthAllowed(String filename) {
		Path musicDataDir = Path.of(getDataFolder().getPath(), "musicdata").toAbsolutePath().normalize();
		Path resolvedPath = musicDataDir.resolve(filename).toAbsolutePath().normalize();
		Path relativePath = musicDataDir.relativize(resolvedPath);
		int depth = relativePath.getNameCount() - 1;
		return switch (subdirectoryDepth) {
			case "none" -> depth == 0;
			case "single" -> depth <= 1;
			default -> true;
		};
	}
}