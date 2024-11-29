package me.Navoei.customdiscsplugin;

import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.VolumeCategory;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.VoicechatServerStartedEvent;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.Enumeration;

public class VoicePlugin implements VoicechatPlugin {

    public static String MUSIC_DISC_CATEGORY = "music_discs";
    
    public static String GOAT_HORN_CATEGORY = "goat_horns";
    
    public static String PLAYER_HEAD_CATEGORY = "player_heads";

    public static VoicechatApi voicechatApi;
    @Nullable
    public static VoicechatServerApi voicechatServerApi;
    @Nullable
    public static VolumeCategory musicDiscs;
    @Nullable
    public static VolumeCategory goatHorns;
    @Nullable
    public static VolumeCategory playerHeads;

    /**
     * @return the unique ID for this voice chat plugin
     */
    @Override
    public String getPluginId() {
        return null;
    }

    /**
     * Called when the voice chat initializes the plugin.
     *
     * @param api the voice chat API
     */
    @Override
    public void initialize(VoicechatApi api) {
        voicechatApi = api;
    }

    /**
     * Called once by the voice chat to register all events.
     *
     * @param registration the event registration
     */
    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(VoicechatServerStartedEvent.class, this::onServerStarted);
    }

    public void onServerStarted(VoicechatServerStartedEvent event) {
        voicechatServerApi = event.getVoicechat();

        musicDiscs = voicechatServerApi.volumeCategoryBuilder()
                .setId(MUSIC_DISC_CATEGORY)
                .setName("Music Discs")
                .setDescription("The volume of music discs")
                .setIcon(getMusicDiscIcon())
                .build();
        voicechatServerApi.registerVolumeCategory(musicDiscs);
        
        goatHorns = voicechatServerApi.volumeCategoryBuilder()
                .setId(GOAT_HORN_CATEGORY)
                .setName("Goat Horns")
                .setDescription("The volume of goat horns")
                .setIcon(getGoatHornsIcon())
                .build();
        voicechatServerApi.registerVolumeCategory(goatHorns);
        
        /*
        playerHeads = voicechatServerApi.volumeCategoryBuilder()
                .setId(PLAYER_HEAD_CATEGORY)
                .setName("Player Heads")
                .setDescription("The volume of player heads (not enabled)")
                .setIcon(getPlayerHeadsIcon())
                .build();
        voicechatServerApi.registerVolumeCategory(playerHeads);
        */

    }

    private int[][] getMusicDiscIcon() {
        try {
            Enumeration<URL> resources = CustomDiscs.getInstance().getClass().getClassLoader().getResources("music_disc_category.png");

            while (resources.hasMoreElements()) {
                BufferedImage bufferedImage = ImageIO.read(resources.nextElement().openStream());
                if (bufferedImage.getWidth() != 16) {
                    continue;
                }
                if (bufferedImage.getHeight() != 16) {
                    continue;
                }
                int[][] image = new int[16][16];
                for (int x = 0; x < bufferedImage.getWidth(); x++) {
                    for (int y = 0; y < bufferedImage.getHeight(); y++) {
                        image[x][y] = bufferedImage.getRGB(x, y);
                    }
                }
                return image;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    private int[][] getGoatHornsIcon() {
        try {
            Enumeration<URL> resources = CustomDiscs.getInstance().getClass().getClassLoader().getResources("goat_horn_category.png");

            while (resources.hasMoreElements()) {
                BufferedImage bufferedImage = ImageIO.read(resources.nextElement().openStream());
                if (bufferedImage.getWidth() != 16) {
                    continue;
                }
                if (bufferedImage.getHeight() != 16) {
                    continue;
                }
                int[][] image = new int[16][16];
                for (int x = 0; x < bufferedImage.getWidth(); x++) {
                    for (int y = 0; y < bufferedImage.getHeight(); y++) {
                        image[x][y] = bufferedImage.getRGB(x, y);
                    }
                }
                return image;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /*
    private int[][] getPlayerHeadsIcon() {
        try {
            Enumeration<URL> resources = CustomDiscs.getInstance().getClass().getClassLoader().getResources("player_head_category.png");

            while (resources.hasMoreElements()) {
                BufferedImage bufferedImage = ImageIO.read(resources.nextElement().openStream());
                if (bufferedImage.getWidth() != 16) {
                    continue;
                }
                if (bufferedImage.getHeight() != 16) {
                    continue;
                }
                int[][] image = new int[16][16];
                for (int x = 0; x < bufferedImage.getWidth(); x++) {
                    for (int y = 0; y < bufferedImage.getHeight(); y++) {
                        image[x][y] = bufferedImage.getRGB(x, y);
                    }
                }
                return image;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    */


}