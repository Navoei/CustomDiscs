package me.Navoei.customdiscsplugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Jukebox;
import org.bukkit.block.data.type.Hopper;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;

public class HopperManager implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHopperPickupFromOtherSource(InventoryMoveItemEvent event) {

        if (event.getDestination().getLocation() == null) return;
        if (!event.getDestination().getLocation().getChunk().isLoaded()) return;
        if (!event.getDestination().getLocation().getBlock().getType().equals(Material.HOPPER)) return;
        if (!isCustomMusicDisc(event.getItem())) return;

        Block hopperBlock = event.getDestination().getLocation().getBlock();
        Hopper hopperData = (Hopper) hopperBlock.getBlockData();
        org.bukkit.block.Hopper hopper = (org.bukkit.block.Hopper) hopperBlock.getState();

        if (!hopperBlock.getRelative(hopperData.getFacing()).getType().equals(Material.JUKEBOX)) return;
        if (hopperBlock.getRelative(BlockFace.DOWN).getType().equals(Material.HOPPER)) return;
        if (!hopperData.isEnabled()) return;

        Jukebox jukebox = (Jukebox) hopperBlock.getRelative(hopperData.getFacing()).getState();

        if (!jukebox.getRecord().getType().equals(Material.AIR)) return;

        jukebox.setRecord(event.getItem());
        jukebox.update();

        Bukkit.getScheduler().runTaskLater(CustomDiscs.getInstance(), () -> {
            hopper.getInventory().removeItem(event.getItem());
        }, 1L);

        String soundFileName = event.getItem().getItemMeta().getPersistentDataContainer().get(new NamespacedKey(CustomDiscs.getInstance(), "customdisc"), PersistentDataType.STRING);

        Path soundFilePath = Path.of(CustomDiscs.getInstance().getDataFolder().getPath(), "musicdata", soundFileName);

        assert VoicePlugin.voicechatServerApi != null;
        PlayerManager.instance().playLocationalAudio(VoicePlugin.voicechatServerApi, soundFilePath, null, jukebox.getBlock());

    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHopperPickupItem(InventoryPickupItemEvent event) {

        if (!Objects.requireNonNull(event.getInventory().getLocation()).getChunk().isLoaded()) return;
        if (!event.getInventory().getLocation().getBlock().getType().equals(Material.HOPPER)) return;
        if (!isCustomMusicDisc(event.getItem().getItemStack())) return;

        Block hopperBlock = event.getInventory().getLocation().getBlock();
        Hopper hopperData = (Hopper) hopperBlock.getBlockData();
        org.bukkit.block.Hopper hopper = (org.bukkit.block.Hopper) hopperBlock.getState();

        if (!hopperBlock.getRelative(hopperData.getFacing()).getType().equals(Material.JUKEBOX)) return;
        if (hopperBlock.getRelative(BlockFace.DOWN).getType().equals(Material.HOPPER)) return;
        if (!hopperData.isEnabled()) return;

        Jukebox jukebox = (Jukebox) hopperBlock.getRelative(hopperData.getFacing()).getState();

        if (!jukebox.getRecord().getType().equals(Material.AIR)) return;

        jukebox.setRecord(event.getItem().getItemStack());
        jukebox.update();

        Bukkit.getScheduler().runTaskLater(CustomDiscs.getInstance(), () -> {
            hopper.getInventory().removeItem(event.getItem().getItemStack());
        }, 1L);

        Component soundFileNameComponent = Objects.requireNonNull(event.getItem().getItemStack().getItemMeta().lore()).get(1).asComponent();
        String soundFileName = PlainTextComponentSerializer.plainText().serialize(soundFileNameComponent);

        Path soundFilePath = Path.of(CustomDiscs.getInstance().getDataFolder().getPath(), "musicdata", soundFileName);

        assert VoicePlugin.voicechatServerApi != null;
        PlayerManager.instance().playLocationalAudio(VoicePlugin.voicechatServerApi, soundFilePath, null, jukebox.getBlock());

    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemPlayerToHopper(InventoryClickEvent event) {

        if (event.getClickedInventory() == null) return;
        if (Objects.requireNonNull(event.getClickedInventory()).getLocation() == null) return;
        if (event.getInventory().getHolder() instanceof HopperMinecart) return;

        if (event.getAction().equals(InventoryAction.PLACE_ALL)) {

            if (!event.getClickedInventory().getLocation().getBlock().getType().equals(Material.HOPPER)) return;

            Bukkit.getScheduler().runTaskLater(CustomDiscs.getInstance(), () -> {

                Hopper hopperData = (Hopper) event.getInventory().getLocation().getBlock().getBlockData();
                Block jukeboxBlock = event.getInventory().getLocation().getBlock().getRelative(hopperData.getFacing());

                getNextDiscFromHopperIntoJukebox(jukeboxBlock);

            }, 1L);
            return;
        }

        if (event.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY)) {

            if (event.getClickedInventory().getLocation().getBlock().getType().equals(Material.HOPPER)) return;

            if (!event.getInventory().getType().equals(InventoryType.HOPPER)) return;

            Bukkit.getScheduler().runTaskLater(CustomDiscs.getInstance(), () -> {

                Hopper hopperData = (Hopper) event.getInventory().getLocation().getBlock().getBlockData();
                Block jukeboxBlock = event.getInventory().getLocation().getBlock().getRelative(hopperData.getFacing());

                getNextDiscFromHopperIntoJukebox(jukeboxBlock);

            }, 1L);
            return;
        }

        if (event.getAction().equals(InventoryAction.SWAP_WITH_CURSOR)) {

            if (!event.getClickedInventory().getLocation().getBlock().getType().equals(Material.HOPPER)) return;

            Bukkit.getScheduler().runTaskLater(CustomDiscs.getInstance(), () -> {

                Hopper hopperData = (Hopper) event.getInventory().getLocation().getBlock().getBlockData();
                Block jukeboxBlock = event.getInventory().getLocation().getBlock().getRelative(hopperData.getFacing());

                getNextDiscFromHopperIntoJukebox(jukeboxBlock);

            }, 1L);
            return;
        }

        if (event.getClick().isRightClick()) {

            if (!event.getClickedInventory().getLocation().getBlock().getType().equals(Material.HOPPER)) return;

            Bukkit.getScheduler().runTaskLater(CustomDiscs.getInstance(), () -> {

                Hopper hopperData = (Hopper) event.getInventory().getLocation().getBlock().getBlockData();
                Block jukeboxBlock = event.getInventory().getLocation().getBlock().getRelative(hopperData.getFacing());

                getNextDiscFromHopperIntoJukebox(jukeboxBlock);

            }, 1L);
            return;
        }

        if (event.getClick() == ClickType.NUMBER_KEY || event.getClick() == ClickType.SWAP_OFFHAND) {

            if (!event.getInventory().getType().equals(InventoryType.HOPPER)) return;

            Bukkit.getScheduler().runTaskLater(CustomDiscs.getInstance(), () -> {

                Hopper hopperData = (Hopper) event.getInventory().getLocation().getBlock().getBlockData();
                Block jukeboxBlock = event.getInventory().getLocation().getBlock().getRelative(hopperData.getFacing());

                getNextDiscFromHopperIntoJukebox(jukeboxBlock);

            }, 1L);
        }

    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {

        if (Objects.requireNonNull(event.getInventory()).getLocation() == null) return;

        if (!event.getInventory().getLocation().getBlock().getType().equals(Material.HOPPER)) return;

        if (!event.getInventory().getType().equals(InventoryType.HOPPER)) return;

        if (event.getInventory().getHolder() instanceof HopperMinecart) return;

        Bukkit.getScheduler().runTaskLater(CustomDiscs.getInstance(), () -> {

            Hopper hopperData = (Hopper) event.getInventory().getLocation().getBlock().getBlockData();
            Block jukeboxBlock = event.getInventory().getLocation().getBlock().getRelative(hopperData.getFacing());

            getNextDiscFromHopperIntoJukebox(jukeboxBlock);

        }, 1L);

    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChunkLoad(ChunkLoadEvent event) {
        for (BlockState blockState : event.getChunk().getTileEntities()) {
            if (blockState instanceof Jukebox) {
                if (!PlayerManager.instance().isAudioPlayerPlaying(blockState.getLocation())) {
                    itemJukeboxToHopper(blockState.getBlock());
                    getNextDiscFromHopperIntoJukebox(blockState.getBlock());
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onJukeboxPlace(BlockPlaceEvent event) {
        if (!event.getBlock().getType().equals(Material.JUKEBOX)) return;

        getNextDiscFromHopperIntoJukebox(event.getBlock());

    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHopperPlace(BlockPlaceEvent event) {
        if (!event.getBlock().getType().equals(Material.HOPPER)) return;

        Block hopperBlock = event.getBlock();

        if (!PlayerManager.instance().isAudioPlayerPlaying(hopperBlock.getRelative(BlockFace.UP).getLocation())) {
            itemJukeboxToHopper(hopperBlock.getRelative(BlockFace.UP));
        }

    }

    public void itemJukeboxToHopper (Block block) {

        if (block == null) return;
        if (!block.getLocation().getChunk().isLoaded()) return;
        if (!block.getType().equals(Material.JUKEBOX)) return;
        if (!block.getRelative(BlockFace.DOWN).getType().equals(Material.HOPPER)) return;

        Block hopperBlock = block.getRelative(BlockFace.DOWN);
        org.bukkit.block.Hopper hopper = (org.bukkit.block.Hopper) hopperBlock.getState();

        Jukebox jukebox = (Jukebox) block.getState();

        if (!Arrays.toString(hopper.getInventory().getContents()).contains("null")) return;

        hopper.getInventory().setItem(hopper.getInventory().firstEmpty(), jukebox.getRecord());

        block.setType(Material.AIR);
        block.setType(Material.JUKEBOX);

        getNextDiscFromHopperIntoJukebox(block);

    }

    public void getNextDiscFromHopperIntoJukebox(Block block) {

        if (!block.getType().equals(Material.JUKEBOX)) return;
        Jukebox jukebox = (Jukebox) block.getState();
        if (!jukebox.getRecord().getType().equals(Material.AIR)) return;

        lookForItemInHoppersThenPlayMusic(block, BlockFace.UP, jukebox);

        lookForItemInHoppersThenPlayMusic(block, BlockFace.SOUTH, jukebox);

        lookForItemInHoppersThenPlayMusic(block, BlockFace.WEST, jukebox);

        lookForItemInHoppersThenPlayMusic(block, BlockFace.NORTH, jukebox);

        lookForItemInHoppersThenPlayMusic(block, BlockFace.EAST, jukebox);

    }

    private void lookForItemInHoppersThenPlayMusic (Block block, BlockFace blockFace, Jukebox jukebox) {

        if (block.getRelative(blockFace).getType().equals(Material.HOPPER)) {
            org.bukkit.block.Hopper hopper = (org.bukkit.block.Hopper) block.getRelative(blockFace).getState();
            if (!hopper.getInventory().isEmpty()) {
                for (int i = 0; i < hopper.getInventory().getSize(); i++) {
                    if (hopper.getInventory().getItem(i) != null) {
                        if (isCustomMusicDisc(hopper.getInventory().getItem(i))) {

                            jukebox.setRecord(hopper.getInventory().getItem(i));
                            jukebox.update();

                            String soundFileName = jukebox.getRecord().getItemMeta().getPersistentDataContainer().get(new NamespacedKey(CustomDiscs.getInstance(), "customdisc"), PersistentDataType.STRING);

                            Path soundFilePath = Path.of(CustomDiscs.getInstance().getDataFolder().getPath(), "musicdata", soundFileName);

                            assert VoicePlugin.voicechatServerApi != null;
                            PlayerManager.instance().playLocationalAudio(VoicePlugin.voicechatServerApi, soundFilePath, null, jukebox.getBlock());

                            hopper.getInventory().setItem(i, new ItemStack(Material.AIR));
                            return;
                        }
                    }
                }
            }
        }

    }

    private boolean isCustomMusicDisc (ItemStack item) {

        return item.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(CustomDiscs.getInstance(), "customdisc"), PersistentDataType.STRING) && (
                        item.getType().equals(Material.MUSIC_DISC_13) ||
                        item.getType().equals(Material.MUSIC_DISC_CAT) ||
                        item.getType().equals(Material.MUSIC_DISC_BLOCKS) ||
                        item.getType().equals(Material.MUSIC_DISC_CHIRP) ||
                        item.getType().equals(Material.MUSIC_DISC_FAR) ||
                        item.getType().equals(Material.MUSIC_DISC_MALL) ||
                        item.getType().equals(Material.MUSIC_DISC_MELLOHI) ||
                        item.getType().equals(Material.MUSIC_DISC_STAL) ||
                        item.getType().equals(Material.MUSIC_DISC_STRAD) ||
                        item.getType().equals(Material.MUSIC_DISC_WARD) ||
                        item.getType().equals(Material.MUSIC_DISC_11) ||
                        item.getType().equals(Material.MUSIC_DISC_WAIT) ||
                        item.getType().equals(Material.MUSIC_DISC_OTHERSIDE) ||
                        item.getType().equals(Material.MUSIC_DISC_5) ||
                        item.getType().equals(Material.MUSIC_DISC_PIGSTEP)
                );
    }

    private static HopperManager instance;

    public static HopperManager instance() {
        if (instance == null) {
            instance = new HopperManager();
        }
        return instance;
    }

}
