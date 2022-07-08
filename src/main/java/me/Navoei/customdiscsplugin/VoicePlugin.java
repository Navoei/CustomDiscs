package me.Navoei.customdiscsplugin;

import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.VoicechatServerStartedEvent;

import javax.annotation.Nullable;

public class VoicePlugin implements VoicechatPlugin {

    public static VoicechatApi voicechatApi;

    @Nullable
    public static VoicechatServerApi voicechatServerApi;

    /**
     * @return the unique ID for this voice chat plugin
     */
    @Override
    public String getPluginId() {
        return CustomDiscs.PLUGIN_ID;
    }

    /**
     * Called when the voice chat initializes the plugin.
     *
     * @param api the voice chat API
     */
    @Override
    public void initialize(final VoicechatApi api) {
        VoicePlugin.voicechatApi = api;
    }

    /**
     * Called once by the voice chat to register all events.
     *
     * @param registration the event registration
     */
    @Override
    public void registerEvents(final EventRegistration registration) {
        registration.registerEvent(VoicechatServerStartedEvent.class, this::onServerStarted);
    }

    public void onServerStarted(final VoicechatServerStartedEvent event) {
        VoicePlugin.voicechatServerApi = event.getVoicechat();
    }

    public void playAudio() {

    }
}
