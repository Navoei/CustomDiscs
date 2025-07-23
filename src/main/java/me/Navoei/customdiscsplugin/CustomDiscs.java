package me.Navoei.customdiscsplugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import me.Navoei.customdiscsplugin.command.CustomDiscCommand;
import me.Navoei.customdiscsplugin.event.JukeBox;
import me.Navoei.customdiscsplugin.event.HeadPlay;
import me.Navoei.customdiscsplugin.event.HornPlay;
import me.Navoei.customdiscsplugin.language.Lang;

import de.maxhenkel.voicechat.api.BukkitVoicechatService;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;

import org.bukkit.NamespacedKey;
import org.bukkit.block.Jukebox;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;
import java.io.*;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class CustomDiscs extends JavaPlugin {
	static CustomDiscs instance;
	
	@Nullable
	private VoicePlugin voicechatPlugin;
	private Logger pluginLogger;
    private static boolean debugMode = false;
	public static YamlConfiguration LANG;
	public static File LANG_FILE;
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
	
	@Override
	public void onLoad() {
		CustomDiscs.instance = this;
		CommandAPI.onLoad(new CommandAPIBukkitConfig(this).verboseOutput(true));
		//To get CommandAPI working on newer MC Release - for development
		//CommandAPI.onLoad(new CommandAPIBukkitConfig(this).verboseOutput(true).beLenientForMinorVersions(true));
		new CustomDiscCommand(this).register("customdiscs");
	}
	
	@Override
	public void onEnable() {
		pluginLogger = getLogger();
		
		CommandAPI.onEnable();
		
		BukkitVoicechatService service = getServer().getServicesManager().load(BukkitVoicechatService.class);
		
		this.saveDefaultConfig();
		loadLang();

		// Config initializer section
		debugMode = getConfig().getBoolean("debugMode", false);
		musicDiscEnable = getConfig().getBoolean("music-disc-enable");
		musicDiscPlayingEnable = getConfig().getBoolean("music-disc-playing-enable");
		musicDiscDistance = getConfig().getInt("music-disc-distance");
		musicDiscMaxDistance = getConfig().getInt("music-disc-max-distance");
		musicDiscVolume = Float.parseFloat(Objects.requireNonNull(getConfig().getString("music-disc-volume")));
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

		// Checking server version and display console message in case the server is not supported
        ServerVersionChecker serverVersionChecker = new ServerVersionChecker(this);
		serverVersionChecker.checkVersion();

		File musicData = new File(this.getDataFolder(), "musicdata");
		if (!(musicData.exists())) {
			musicData.mkdirs();
		}
		
		if (service != null) {
			voicechatPlugin = new VoicePlugin();
			service.registerPlugin(voicechatPlugin);
			pluginLogger.info("Successfully registered CustomDiscs plugin");
		} else {
			pluginLogger.info("Failed to register CustomDiscs plugin");
		}

		if (isMusicDiscEnable()) {
			getServer().getPluginManager().registerEvents(new JukeBox(), this);
			getServer().getPluginManager().registerEvents(new HopperManager(), this);
		}
		if (isCustomHeadEnable()) {	getServer().getPluginManager().registerEvents(new HeadPlay(), this); }
		if (isCustomHornEnable()) {	getServer().getPluginManager().registerEvents(new HornPlay(), this); }

		// To avoid any "0" values, set it to 1.
		if (hornCooldown <= 0) {
			hornCooldown = 1;
		}
		if (hornMaxCooldown <= 0) {
			hornMaxCooldown = 1;
		}

		ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
		protocolManager.addPacketListener(new PacketAdapter(this, ListenerPriority.NORMAL, PacketType.Play.Server.WORLD_EVENT) {
			@Override
			public void onPacketSending(PacketEvent event) {
				PacketContainer packet = event.getPacket();

				if (packet.getIntegers().read(0).toString().equals("1010")) {
					if (!isMusicDiscEnable()) { return; }
					Jukebox jukebox = (Jukebox) packet.getBlockPositionModifier().read(0).toLocation(event.getPlayer().getWorld()).getBlock().getState();

					if (!jukebox.getRecord().hasItemMeta()) return;

					if (jukebox.getRecord().getItemMeta().getPersistentDataContainer().has(new NamespacedKey(this.plugin, "customdisc"), PersistentDataType.STRING)) {
						event.setCancelled(true);
					}

					//Start the jukebox state manager.
					//This keeps the jukebox powered while custom song is playing,
					//which perfectly emulates the vanilla behavior of discs.
					JukeboxStateManager.start(jukebox);
				}
			}
		});
		
	}
	
	@Override
	public void onDisable() {
		CommandAPI.onDisable();
		if (voicechatPlugin != null) {
			getServer().getServicesManager().unregister(voicechatPlugin);
			pluginLogger.info("Successfully unregistered CustomDiscs plugin");
		}
	}
	
	public static CustomDiscs getInstance() {
		return instance;
	}
        
	/**
	 * Load the lang.yml file.
	 */
	public void loadLang() {
		File lang = new File(getDataFolder(), "lang.yml");
		if (!lang.exists()) {
			try {
				getDataFolder().mkdir();
				lang.createNewFile();
				InputStream defConfigStream = this.getResource("lang.yml");
				if (defConfigStream != null) {
					copyInputStreamToFile(defConfigStream, lang);
					YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(lang);
					defConfig.save(lang);
					Lang.setFile(defConfig);
				}
			} catch (IOException e) {
				pluginLogger.severe("Failed to create lang.yml for CustomDiscs.");
				pluginLogger.severe("Now disabling...");
				this.setEnabled(false); // Without it loaded, we can't send them messages
				if (isDebugMode()) {
					pluginLogger.log(Level.SEVERE, "Exception output: ", e);
				}
			}
		}
		YamlConfiguration conf = YamlConfiguration.loadConfiguration(lang);
		for (Lang item : Lang.values()) {
			if (conf.getString(item.getPath()) == null) {
				conf.set(item.getPath(), item.getDefault());
			}
		}
		Lang.setFile(conf);
		LANG = conf;
		LANG_FILE = lang;
		try {
			conf.save(getLangFile());
		} catch (IOException e) {
			pluginLogger.warning("Failed to save lang.yml for CustomDiscs");
			pluginLogger.warning("Now disabling...");
			if (isDebugMode()) {
				pluginLogger.log(Level.SEVERE, "Exception output: ", e);
			}
		}
	}
	
	/**
	 * Gets the lang.yml config.
	 *
	 * @return The lang.yml config.
	 */
	public YamlConfiguration getLang() {
		return LANG;
	}
	
	/**
	 * Get the lang.yml file.
	 *
	 * @return The lang.yml file.
	 */
	public File getLangFile() {
		return LANG_FILE;
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
}