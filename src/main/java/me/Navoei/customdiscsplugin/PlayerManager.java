package me.Navoei.customdiscsplugin;

import de.maxhenkel.voicechat.api.ServerPlayer;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.audiochannel.AudioChannel;
import de.maxhenkel.voicechat.api.audiochannel.AudioPlayer;
import de.maxhenkel.voicechat.api.audiochannel.LocationalAudioChannel;
import javazoom.spi.mpeg.sampled.convert.MpegFormatConversionProvider;
import javazoom.spi.mpeg.sampled.file.MpegAudioFileReader;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jflac.sound.spi.Flac2PcmAudioInputStream;
import org.jflac.sound.spi.FlacAudioFileReader;

import javax.annotation.Nullable;
import javax.sound.sampled.*;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class PlayerManager {

    private final Map<UUID, Stoppable> playerMap;
    private final ExecutorService executorService;
    private static final AudioFormat FORMAT = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 48000F, 16, 1, 2, 48000F, false);

    public PlayerManager() {
        this.playerMap = new ConcurrentHashMap<>();
        this.executorService = Executors.newSingleThreadExecutor(r -> {
            Thread thread = new Thread(r, "AudioPlayerThread");
            thread.setDaemon(true);
            return thread;
        });
    }

    public void playLocationalAudio(VoicechatServerApi api, Path soundFilePath, Block block, Component actionbarComponent, float range) {
        UUID id = UUID.nameUUIDFromBytes(block.getLocation().toString().getBytes());

        LocationalAudioChannel audioChannel = api.createLocationalAudioChannel(id, api.fromServerLevel(block.getWorld()), api.createPosition(block.getLocation().getX() + 0.5d, block.getLocation().getY() + 0.5d, block.getLocation().getZ() + 0.5d));

        if (audioChannel == null) return;

        audioChannel.setCategory(VoicePlugin.MUSIC_DISC_CATEGORY);
        audioChannel.setDistance(range);

        AtomicBoolean stopped = new AtomicBoolean();
        AtomicReference<de.maxhenkel.voicechat.api.audiochannel.AudioPlayer> player = new AtomicReference<>();

        playerMap.put(id, () -> {
            synchronized (stopped) {
                stopped.set(true);
                de.maxhenkel.voicechat.api.audiochannel.AudioPlayer audioPlayer = player.get();
                if (audioPlayer != null) {
                    audioPlayer.stopPlaying();
                }
            }
        });

        executorService.execute(() -> {
            Collection<ServerPlayer> playersInRange = api.getPlayersInRange(api.fromServerLevel(block.getWorld()), api.createPosition(block.getLocation().getX() + 0.5d, block.getLocation().getY() + 0.5d, block.getLocation().getZ() + 0.5d), range);

            de.maxhenkel.voicechat.api.audiochannel.AudioPlayer audioPlayer = playChannel(api, audioChannel, block, soundFilePath, playersInRange);

            for (ServerPlayer serverPlayer : playersInRange) {
                Player bukkitPlayer = (Player) serverPlayer.getPlayer();
                bukkitPlayer.sendActionBar(actionbarComponent);
            }

            if (audioPlayer == null) {
                playerMap.remove(id);
                return;
            }

            audioPlayer.setOnStopped(() -> {
                //Stuff that runs once the audio player ends.

                //Stop the vanilla disc if it is playing. This ensures the hopper will pickup the disc.
                Bukkit.getScheduler().runTask(CustomDiscs.getInstance(), () -> HopperManager.instance().discToHopper(block));

                playerMap.remove(id);
            });

            synchronized (stopped) {
                if (!stopped.get()) {
                    player.set(audioPlayer);
                } else {
                    audioPlayer.stopPlaying();
                }
            }
        });
    }

    @Nullable
    private de.maxhenkel.voicechat.api.audiochannel.AudioPlayer playChannel(VoicechatServerApi api, AudioChannel audioChannel, Block block, Path soundFilePath, Collection<ServerPlayer> playersInRange) {
        try {
            short[] audio = readSoundFile(soundFilePath);
            AudioPlayer audioPlayer = api.createAudioPlayer(audioChannel, api.createEncoder(), audio);
            audioPlayer.startPlaying();
            return audioPlayer;
        } catch (Exception e) {
            e.printStackTrace();
            Bukkit.getLogger().info("Error Occurred At: " + block.getLocation());
            for (ServerPlayer serverPlayer : playersInRange) {
                Player bukkitPlayer = (Player) serverPlayer.getPlayer();
                bukkitPlayer.sendMessage(ChatColor.RED + "An error has occurred while trying to play this disc.");
            }
            return null;
        }
    }

    private static short[] readSoundFile(Path file) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        return VoicePlugin.voicechatApi.getAudioConverter().bytesToShorts(convertFormat(file, FORMAT));
    }

    private static byte[] convertFormat(Path file, AudioFormat audioFormat) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        AudioInputStream finalInputStream = null;

        if (getFileExtension(file.toFile().toString()).equals("wav")) {
            AudioInputStream inputStream = AudioSystem.getAudioInputStream(file.toFile());
            finalInputStream = AudioSystem.getAudioInputStream(audioFormat, inputStream);
        } else if (getFileExtension(file.toFile().toString()).equals("mp3")) {

            AudioInputStream inputStream = new MpegAudioFileReader().getAudioInputStream(file.toFile());
            AudioFormat baseFormat = inputStream.getFormat();
            AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, baseFormat.getSampleRate(), 16, baseFormat.getChannels(), baseFormat.getChannels() * 2, baseFormat.getFrameRate(), false);
            AudioInputStream convertedInputStream = new MpegFormatConversionProvider().getAudioInputStream(decodedFormat, inputStream);
            finalInputStream = AudioSystem.getAudioInputStream(audioFormat, convertedInputStream);

        } else if (getFileExtension(file.toFile().toString()).equals("flac")) {
            AudioInputStream inputStream = new FlacAudioFileReader().getAudioInputStream(file.toFile());
            AudioFormat baseFormat = inputStream.getFormat();
            AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, baseFormat.getSampleRate(), 16, baseFormat.getChannels(), baseFormat.getChannels() * 2, baseFormat.getFrameRate(), false);
            AudioInputStream convertedInputStream = new Flac2PcmAudioInputStream(inputStream, decodedFormat, inputStream.getFrameLength());
            finalInputStream = AudioSystem.getAudioInputStream(audioFormat, convertedInputStream);
        }

        assert finalInputStream != null;

        return adjustVolume(finalInputStream.readAllBytes(), CustomDiscs.getInstance().musicDiscVolume);
    }

    private static byte[] adjustVolume(byte[] audioSamples, double volume) {

        if (volume > 1d || volume < 0d) {
            CustomDiscs.getInstance().getServer().getLogger().info("Error: The volume must be between 0 and 1 in the config!");
            return null;
        }

        byte[] array = new byte[audioSamples.length];
        for (int i = 0; i < array.length; i+=2) {
            // convert byte pair to int
            short buf1 = audioSamples[i+1];
            short buf2 = audioSamples[i];

            buf1 = (short) ((buf1 & 0xff) << 8);
            buf2 = (short) (buf2 & 0xff);

            short res= (short) (buf1 | buf2);
            res = (short) (res * volume);

            // convert back
            array[i] = (byte) res;
            array[i+1] = (byte) (res >> 8);

        }
        return array;
    }


    public void stopLocationalAudio(Location blockLocation) {
        UUID id = UUID.nameUUIDFromBytes(blockLocation.toString().getBytes());
        Stoppable player = playerMap.get(id);
        if (player != null) {
            player.stop();
        }
        playerMap.remove(id);
    }

    public static float getLengthSeconds(Path file) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        short[] audio = readSoundFile(file);
        return (float) audio.length / FORMAT.getSampleRate();
    }

    public boolean isAudioPlayerPlaying(Location blockLocation) {
        UUID id = UUID.nameUUIDFromBytes(blockLocation.toString().getBytes());
        return playerMap.containsKey(id);
    }

    private static String getFileExtension(String s) {
        int index = s.lastIndexOf(".");
        if (index > 0) {
            return s.substring(index + 1);
        } else {
            return "";
        }
    }

    private static PlayerManager instance;

    public static PlayerManager instance() {
        if (instance == null) {
            instance = new PlayerManager();
        }
        return instance;
    }

    private interface Stoppable {
        void stop();
    }

}
