package na.Navoei.customdiscsplugin;

import de.maxhenkel.voicechat.api.BukkitVoicechatService;
import na.Navoei.customdiscsplugin.command.CustomDisc;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;
import java.io.File;
import java.net.URI;

public final class CustomDiscs extends JavaPlugin {

    public static final String PLUGIN_ID = "CustomDiscs";
    public static final Logger LOGGER = LogManager.getLogger(PLUGIN_ID);
    static CustomDiscs instance;

    @Nullable
    private PlayMusic voicechatPlugin;

    @Override
    public void onEnable() {

        CustomDiscs.instance = this;

        BukkitVoicechatService service = getServer().getServicesManager().load(BukkitVoicechatService.class);

        CustomDisc command = new CustomDisc();

        if (!new File(this.getDataFolder(), "config.yml").exists()) {
            this.getConfig().options().copyDefaults(true);
        }
        this.saveConfig();

        File musicData = new File(this.getDataFolder() + "\\musicdata\\");
        if (!(musicData.exists())) {
            musicData.mkdirs();
        }

        if (service != null) {
            voicechatPlugin = new PlayMusic();
            service.registerPlugin(voicechatPlugin);
            LOGGER.info("Successfully registered CustomDiscs plugin");
        } else {
            LOGGER.info("Failed to register CustomDiscs plugin");
        }


        getCommand("customdisc").setExecutor(command);

    }

    @Override
    public void onDisable() {
        if (voicechatPlugin != null) {
            getServer().getServicesManager().unregister(voicechatPlugin);
            LOGGER.info("Successfully unregistered CustomDiscs plugin");
        }
    }

    public static CustomDiscs getInstance() {
        return instance;
    }
}
