package me.Navoei.customdiscsplugin.event;

import me.Navoei.customdiscsplugin.CustomDiscs;
import me.Navoei.customdiscsplugin.HopperManager;
import me.Navoei.customdiscsplugin.PlayerManager;
import me.Navoei.customdiscsplugin.VoicePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Jukebox;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

public class JukeBox implements Listener{

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInsert(PlayerInteractEvent event) throws IOException {

        Player player = event.getPlayer();
        Block block = event.getClickedBlock();

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getClickedBlock() == null || event.getItem() == null || event.getItem().getItemMeta() == null || block == null) return;
        if (event.getClickedBlock().getType() != Material.JUKEBOX) return;

        if (isCustomMusicDisc(event) && !jukeboxContainsDisc(block)) {

            Component soundFileNameComponent = Objects.requireNonNull(event.getItem().getItemMeta().lore()).get(1).asComponent();
            String soundFileName = PlainTextComponentSerializer.plainText().serialize(soundFileNameComponent);

            Path soundFilePath = Path.of(CustomDiscs.getInstance().getDataFolder().getPath(), "musicdata", soundFileName);

            if (soundFilePath.toFile().exists()) {

                assert VoicePlugin.voicechatServerApi != null;
                PlayerManager.instance().playLocationalAudio(VoicePlugin.voicechatServerApi, soundFilePath, player, block);

                Component songNameComponent = Objects.requireNonNull(event.getItem().getItemMeta().lore()).get(0).asComponent();
                String songName = PlainTextComponentSerializer.plainText().serialize(songNameComponent);

                TextComponent customActionBarSongPlaying = Component.text()
                        .content("Now Playing: " + songName)
                        .color(NamedTextColor.GOLD)
                        .build();
                player.sendActionBar(customActionBarSongPlaying.asComponent());

            } else {
                player.sendMessage(ChatColor.RED + "Sound file not found.");
                event.setCancelled(true);
                throw new FileNotFoundException("Sound file is missing!");
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEject(PlayerInteractEvent event) {

        Player player = event.getPlayer();
        Block block = event.getClickedBlock();

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getClickedBlock() == null || block == null) return;
        if (event.getClickedBlock().getType() != Material.JUKEBOX) return;

        if (jukeboxContainsDisc(block)) {
            stopDisc(block, player);
            Bukkit.getScheduler().runTaskLater(CustomDiscs.getInstance(), () -> HopperManager.instance().getNextDiscFromHopperIntoJukebox(block), 1L);
        }

    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onJukeboxBreak(BlockBreakEvent event) {

        Block block = event.getBlock();
        Player player = event.getPlayer();

        if (block.getType() != Material.JUKEBOX) return;

        stopDisc(block, player);
    }

    @EventHandler
    public void onJukeBoxExplode(EntityExplodeEvent event) {

        for (Block explodedBlock : event.blockList()) {
            if (explodedBlock.getType() == Material.JUKEBOX) {
                stopDisc(explodedBlock, null);
            }
        }

    }

    public boolean jukeboxContainsDisc(Block b) {
        Jukebox jukebox = (Jukebox) b.getLocation().getBlock().getState();
        return jukebox.getPlaying() != Material.AIR;
    }

    public boolean isCustomMusicDisc(PlayerInteractEvent e) {

        return e.getItem().getItemMeta().hasItemFlag(ItemFlag.HIDE_ENCHANTS) &&
                (
                        e.getItem().getType().equals(Material.MUSIC_DISC_13) ||
                                e.getItem().getType().equals(Material.MUSIC_DISC_CAT) ||
                                e.getItem().getType().equals(Material.MUSIC_DISC_BLOCKS) ||
                                e.getItem().getType().equals(Material.MUSIC_DISC_CHIRP) ||
                                e.getItem().getType().equals(Material.MUSIC_DISC_FAR) ||
                                e.getItem().getType().equals(Material.MUSIC_DISC_MALL) ||
                                e.getItem().getType().equals(Material.MUSIC_DISC_MELLOHI) ||
                                e.getItem().getType().equals(Material.MUSIC_DISC_STAL) ||
                                e.getItem().getType().equals(Material.MUSIC_DISC_STRAD) ||
                                e.getItem().getType().equals(Material.MUSIC_DISC_WARD) ||
                                e.getItem().getType().equals(Material.MUSIC_DISC_11) ||
                                e.getItem().getType().equals(Material.MUSIC_DISC_WAIT) ||
                                e.getItem().getType().equals(Material.MUSIC_DISC_OTHERSIDE) ||
                                e.getItem().getType().equals(Material.MUSIC_DISC_5) ||
                                e.getItem().getType().equals(Material.MUSIC_DISC_PIGSTEP)
                );
    }

    private void stopDisc(Block block, Player player) {
        PlayerManager.instance().stopLocationalAudio(block.getLocation());
        if (player == null) return;
        if (jukeboxContainsDisc(block)) {
            player.swingMainHand();
        }
    }

}
