package me.Navoei.customdiscsplugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import de.maxhenkel.voicechat.api.BukkitVoicechatService;
import me.Navoei.customdiscsplugin.command.CommandManager;
import me.Navoei.customdiscsplugin.event.JukeBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Jukebox;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Objects;

public final class CustomDiscs extends JavaPlugin {

    public static final String PLUGIN_ID = "CustomDiscs";
    public static final Logger LOGGER = LogManager.getLogger(PLUGIN_ID);
    static CustomDiscs instance;

    @Nullable
    private VoicePlugin voicechatPlugin;

    public float musicDiscDistance;
    public float musicDiscVolume;

    @Override
    public void onEnable() {

        CustomDiscs.instance = this;

        BukkitVoicechatService service = getServer().getServicesManager().load(BukkitVoicechatService.class);

        this.saveDefaultConfig();

        File musicData = new File(this.getDataFolder(), "musicdata");
        if (!(musicData.exists())) {
            musicData.mkdirs();
        }

        if (service != null) {
            voicechatPlugin = new VoicePlugin();
            service.registerPlugin(voicechatPlugin);
            LOGGER.info("Successfully registered CustomDiscs plugin");
        } else {
            LOGGER.info("Failed to register CustomDiscs plugin");
        }

        getServer().getPluginManager().registerEvents(new JukeBox(), this);
        getServer().getPluginManager().registerEvents(new HopperManager(), this);
        getCommand("customdisc").setExecutor(new CommandManager());

        musicDiscDistance = getConfig().getInt("music-disc-distance");
        musicDiscVolume = Float.parseFloat(Objects.requireNonNull(getConfig().getString("music-disc-volume")));

        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();

        protocolManager.addPacketListener(new PacketAdapter(this, ListenerPriority.NORMAL, PacketType.Play.Server.WORLD_EVENT) {
            @Override
            public void onPacketSending(PacketEvent event) {
                PacketContainer packet = event.getPacket();

                if (packet.getIntegers().read(0).toString().equals("1010")) {
                    Jukebox jukebox = (Jukebox) packet.getBlockPositionModifier().read(0).toLocation(event.getPlayer().getWorld()).getBlock().getState();

                    if (!jukebox.getRecord().hasItemMeta()) return;

                    if (jukebox.getRecord().getItemMeta().getPersistentDataContainer().has(new NamespacedKey(String.valueOf(this), "customdisc"), PersistentDataType.STRING)) {
                        event.setCancelled(true);
                    }

                }
            }
        });

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
