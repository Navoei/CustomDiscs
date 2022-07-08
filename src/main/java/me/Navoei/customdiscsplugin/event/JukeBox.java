package me.Navoei.customdiscsplugin.event;

import me.Navoei.customdiscsplugin.CustomDiscs;
import me.Navoei.customdiscsplugin.VoicePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

public class JukeBox implements Listener {

    @EventHandler
    public void onInsert(PlayerInteractEvent event) throws UnsupportedAudioFileException, IOException {

        if (event.getAction().isRightClick() && isCustomMusicDisc(event.getPlayer()) && Objects.requireNonNull(event.getClickedBlock()).getType().equals(Material.JUKEBOX)) {

            Component soundFileComponent = Objects.requireNonNull(event.getPlayer().getInventory().getItemInMainHand().getItemMeta().lore()).get(1).asComponent();
            String soundFileName = PlainTextComponentSerializer.plainText().serialize(soundFileComponent);

            Path soundFilePath = Path.of(CustomDiscs.getInstance().getDataFolder() + "\\musicdata\\" + soundFileName);
            if (soundFilePath.toFile().exists()) {
                Component songNameComponent = Objects.requireNonNull(event.getPlayer().getInventory().getItemInMainHand().getItemMeta().lore()).get(0).asComponent();
                String songName = PlainTextComponentSerializer.plainText().serialize(songNameComponent);
                event.getPlayer().sendMessage(ChatColor.GOLD + "Now playing: " + songName);

                //VoicePlugin.voicechatServerApi.createAudioPlayer();

            } else {
                event.getPlayer().sendMessage(ChatColor.RED + "Sound file not found.");
                event.setCancelled(true);
                throw new FileNotFoundException("ERROR: Sound file is missing!");
            }
        }

    }

    public boolean isCustomMusicDisc(Player p) {

        if (
                p.getInventory().getItemInMainHand().hasItemFlag(ItemFlag.HIDE_ENCHANTS) &&
                (
                p.getInventory().getItemInMainHand().getType().equals(Material.MUSIC_DISC_13) ||
                p.getInventory().getItemInMainHand().getType().equals(Material.MUSIC_DISC_CAT) ||
                p.getInventory().getItemInMainHand().getType().equals(Material.MUSIC_DISC_BLOCKS) ||
                p.getInventory().getItemInMainHand().getType().equals(Material.MUSIC_DISC_CHIRP) ||
                p.getInventory().getItemInMainHand().getType().equals(Material.MUSIC_DISC_FAR) ||
                p.getInventory().getItemInMainHand().getType().equals(Material.MUSIC_DISC_MALL) ||
                p.getInventory().getItemInMainHand().getType().equals(Material.MUSIC_DISC_MELLOHI) ||
                p.getInventory().getItemInMainHand().getType().equals(Material.MUSIC_DISC_STAL) ||
                p.getInventory().getItemInMainHand().getType().equals(Material.MUSIC_DISC_STRAD) ||
                p.getInventory().getItemInMainHand().getType().equals(Material.MUSIC_DISC_WARD) ||
                p.getInventory().getItemInMainHand().getType().equals(Material.MUSIC_DISC_11) ||
                p.getInventory().getItemInMainHand().getType().equals(Material.MUSIC_DISC_WAIT) ||
                p.getInventory().getItemInMainHand().getType().equals(Material.MUSIC_DISC_OTHERSIDE) ||
                p.getInventory().getItemInMainHand().getType().equals(Material.MUSIC_DISC_5) ||
                p.getInventory().getItemInMainHand().getType().equals(Material.MUSIC_DISC_PIGSTEP)
                )
        ) {
            return true;
        }
        return false;
    }

}
