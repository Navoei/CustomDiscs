package me.Navoei.customdiscsplugin.event;

import me.Navoei.customdiscsplugin.CustomDiscs;
import me.Navoei.customdiscsplugin.PlayerManager;
import me.Navoei.customdiscsplugin.VoicePlugin;
import me.Navoei.customdiscsplugin.utils.TypeChecker;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Skull;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.NotePlayEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import javax.annotation.Nullable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class HeadPlay implements Listener{

    CustomDiscs plugin = CustomDiscs.getInstance();
    PlayerManager playerManager = PlayerManager.instance();

    // Triggered on every noteblock interaction.
    // Most of the function will only be executed if a custom player head is found on top of the noteblock.
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onNotePlay(NotePlayEvent event) throws IOException {
        Block noteBlock = event.getBlock();

        if (noteBlock.getType() != Material.NOTE_BLOCK) return;

        if (PlayerManager.instance().isAudioPlayerPlaying(noteBlock.getLocation())) playerManager.stopDisc(noteBlock);

        Block headBlock = noteBlock.getRelative(BlockFace.UP);
        if (!TypeChecker.isHead(headBlock.getType())) return;

        Skull skull = (Skull) headBlock.getState();
        PersistentDataContainer persistentDataContainer = skull.getPersistentDataContainer();

        if (persistentDataContainer.has(new NamespacedKey(plugin, "customhead"), PersistentDataType.STRING)) {
            event.setCancelled(true);

            String soundFileName = persistentDataContainer.get(new NamespacedKey(plugin, "customhead"), PersistentDataType.STRING);

            float range = CustomDiscs.getInstance().customHeadDistance;
            NamespacedKey customSoundRangeKey = new NamespacedKey(plugin, "range");
            NamespacedKey headLoreKey = new NamespacedKey(plugin, "headlore");

            if(persistentDataContainer.has(customSoundRangeKey, PersistentDataType.FLOAT)) {
                float soundRange = Optional.ofNullable(persistentDataContainer.get(customSoundRangeKey, PersistentDataType.FLOAT)).orElse(0f);
                range = Math.min(soundRange, CustomDiscs.getInstance().customHeadMaxDistance);
            }

            Path soundFilePath = Path.of(plugin.getDataFolder().getPath(), "musicdata", soundFileName);

            if (soundFilePath.toFile().exists()) {
                Component songNameComponent = Optional.ofNullable(persistentDataContainer.get(headLoreKey, PersistentDataType.STRING)).map(GsonComponentSerializer.gson()::deserialize).orElse(Component.text("Unknown Song", NamedTextColor.GRAY));
                String songName = PlainTextComponentSerializer.plainText().serialize(songNameComponent);
                assert VoicePlugin.voicechatServerApi != null;
                playerManager.playAudioHead(VoicePlugin.voicechatServerApi, soundFilePath, noteBlock, songName, range);
            } else {
                event.setCancelled(true);
                throw new FileNotFoundException("Sound file is missing!");
            }
        }
    }

    // Event to delay by 1 tick after a player head had been placed
    // Delay will only be triggered on custom player head created with this plugin
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onHeadPlace(BlockPlaceEvent event) {
        ItemStack heldItem = event.getItemInHand();

        if (!TypeChecker.isHead(heldItem.getType())) return;
        if (!(heldItem.getItemMeta() instanceof SkullMeta skullMeta)) return;

        PersistentDataContainer itemPDC = skullMeta.getPersistentDataContainer();
        if (!itemPDC.has(new NamespacedKey(plugin, "customhead"), PersistentDataType.STRING)) return;

        Block block = event.getBlockPlaced();
        if (!TypeChecker.isHead(block.getType()) && !TypeChecker.isWallHead(block.getType())) return;
        plugin.getServer().getRegionScheduler().runDelayed(plugin, block.getLocation(), task -> {
            Skull skull = (Skull) block.getState();
            PersistentDataContainer blockPDC = skull.getPersistentDataContainer();

            NamespacedKey headKey = new NamespacedKey(plugin, "customhead");
            NamespacedKey loreKey = new NamespacedKey(plugin, "headlore");
            NamespacedKey rangeKey = new NamespacedKey(plugin, "range");

            if (itemPDC.has(headKey, PersistentDataType.STRING)) {
                String customHeadValue = itemPDC.get(headKey, PersistentDataType.STRING);
                blockPDC.set(headKey, PersistentDataType.STRING, customHeadValue);
            }

            if (itemPDC.has(loreKey, PersistentDataType.STRING)) {
                String headLoreValue = itemPDC.get(loreKey, PersistentDataType.STRING);
                blockPDC.set(loreKey, PersistentDataType.STRING, headLoreValue);
            }

            if (itemPDC.has(rangeKey, PersistentDataType.FLOAT)) {
                Float rangeValue = itemPDC.get(rangeKey, PersistentDataType.FLOAT);
                blockPDC.set(rangeKey, PersistentDataType.FLOAT, rangeValue);
            }

            skull.update(true, true);
        }, 1L);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHeadBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (!TypeChecker.isHead(block.getType()) && !TypeChecker.isWallHead(block.getType())) return;

        Skull headSkull = (Skull) block.getState();

        PersistentDataContainer headPDC = headSkull.getPersistentDataContainer();
        if (!headPDC.has(new NamespacedKey(plugin, "customhead"), PersistentDataType.STRING)) return;

        Block noteBlockBelow = block.getRelative(BlockFace.DOWN);

        if (noteBlockBelow.getType() != Material.NOTE_BLOCK) return;

        playerManager.stopDisc(noteBlockBelow);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHeadDrop(BlockDropItemEvent event) {
        BlockState blockState = event.getBlockState();

        if (!(blockState instanceof Skull skull)) return;

        PersistentDataContainer blockPDC = skull.getPersistentDataContainer();

        NamespacedKey headKey = new NamespacedKey(plugin, "customhead");
        NamespacedKey loreKey = new NamespacedKey(plugin, "headlore");
        NamespacedKey rangeKey = new NamespacedKey(plugin, "range");

        for (Item itemEntity : event.getItems()) {
            ItemStack droppedStack = itemEntity.getItemStack();
            if (!TypeChecker.isHead(droppedStack.getType()) && !TypeChecker.isWallHead(droppedStack.getType())) continue;

            ItemMeta droppedMeta = droppedStack.getItemMeta();
            if (droppedMeta == null) continue;

            PersistentDataContainer droppedItemPDC = droppedMeta.getPersistentDataContainer();

            String customHeadValue = blockPDC.get(headKey, PersistentDataType.STRING);
            if (customHeadValue != null) {
                droppedItemPDC.set(headKey, PersistentDataType.STRING, customHeadValue);
            }

            String headLoreValue = blockPDC.get(loreKey, PersistentDataType.STRING);
            if (headLoreValue != null) {
                droppedItemPDC.set(loreKey, PersistentDataType.STRING, headLoreValue);

                Component deserializedLore = GsonComponentSerializer.gson().deserialize(headLoreValue);
                @Nullable List<Component> itemLore = new ArrayList<>();
                itemLore.add(deserializedLore);
                droppedMeta.lore(itemLore);
            }

            Float rangeValue = blockPDC.get(rangeKey, PersistentDataType.FLOAT);
            if (rangeValue != null) {
                droppedItemPDC.set(rangeKey, PersistentDataType.FLOAT, rangeValue);
            }

            droppedStack.setItemMeta(droppedMeta);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHeadExplode(EntityExplodeEvent event) {
        onHeadExplosion(event.blockList());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHeadBlockExplode(BlockExplodeEvent event) {
        onHeadExplosion(event.blockList());
    }

    private void onHeadExplosion(List<Block> blockList) {
        NamespacedKey headKey = new NamespacedKey(plugin, "customhead");
        NamespacedKey loreKey = new NamespacedKey(plugin, "headlore");
        NamespacedKey rangeKey = new NamespacedKey(plugin, "range");

        Iterator<Block> iterator = blockList.iterator();
        while (iterator.hasNext()) {
            Block explodedBlock = iterator.next();
            if (!TypeChecker.isHead(explodedBlock.getType()) && !TypeChecker.isWallHead(explodedBlock.getType())) continue;

            Skull skull = (Skull) explodedBlock.getState();
            PersistentDataContainer persistentDataContainer = skull.getPersistentDataContainer();
            if (!persistentDataContainer.has(headKey, PersistentDataType.STRING)) continue;

            Block noteBlockBelow = explodedBlock.getRelative(BlockFace.DOWN);
            if (noteBlockBelow.getType() == Material.NOTE_BLOCK) playerManager.stopDisc(noteBlockBelow);

            iterator.remove();

            for (ItemStack dropItemStack : explodedBlock.getDrops()) {
                if (!TypeChecker.isHead(dropItemStack.getType()) && !TypeChecker.isWallHead(dropItemStack.getType())) continue;

                ItemMeta itemMeta = dropItemStack.getItemMeta();
                if (itemMeta == null) continue;

                PersistentDataContainer dropPersistentDataContainer = itemMeta.getPersistentDataContainer();

                String customHeadValue = persistentDataContainer.get(headKey, PersistentDataType.STRING);
                if (customHeadValue != null) dropPersistentDataContainer.set(headKey, PersistentDataType.STRING, customHeadValue);

                String headLoreValue = persistentDataContainer.get(loreKey, PersistentDataType.STRING);
                if (headLoreValue != null) {
                    dropPersistentDataContainer.set(loreKey, PersistentDataType.STRING, headLoreValue);
                    Component deserializedLore = GsonComponentSerializer.gson().deserialize(headLoreValue);
                    @Nullable List<Component> itemLore = new ArrayList<>();
                    itemLore.add(deserializedLore);
                    itemMeta.lore(itemLore);
                }

                Float rangeValue = persistentDataContainer.get(rangeKey, PersistentDataType.FLOAT);
                if (rangeValue != null) dropPersistentDataContainer.set(rangeKey, PersistentDataType.FLOAT, rangeValue);

                dropItemStack.setItemMeta(itemMeta);
                explodedBlock.getWorld().dropItemNaturally(explodedBlock.getLocation(), dropItemStack);
            }

            explodedBlock.setType(Material.AIR);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onNoteblockHeadBreak(BlockBreakEvent event) {
        Block block = event.getBlock();

        if (block.getType() != Material.NOTE_BLOCK) return;

        Block headBlockAbove = block.getRelative(BlockFace.UP);

        if (!TypeChecker.isHead(headBlockAbove.getType())) return;

        Skull headSkull = (Skull) headBlockAbove.getState();

        PersistentDataContainer headPDC = headSkull.getPersistentDataContainer();
        if (!headPDC.has(new NamespacedKey(plugin, "customhead"), PersistentDataType.STRING)) return;

        playerManager.stopDisc(block);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onNoteblockHeadExplode(EntityExplodeEvent event) {
        onNoteblockExplosion(event.blockList());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onNoteblockHeadBlockExplode(BlockExplodeEvent event) {
        onNoteblockExplosion(event.blockList());
    }

    private void onNoteblockExplosion(List<Block> blockList) {
        for (Block explodedBlock : blockList) {
            if (explodedBlock.getType() != Material.NOTE_BLOCK) continue;

            Block headBlockAbove = explodedBlock.getRelative(BlockFace.UP);
            if (!TypeChecker.isHead(headBlockAbove.getType())) continue;

            Skull headSkull = (Skull) headBlockAbove.getState();
            PersistentDataContainer headPersistentDataContainer = headSkull.getPersistentDataContainer();
            if (!headPersistentDataContainer.has(new NamespacedKey(plugin, "customhead"), PersistentDataType.STRING)) continue;

            playerManager.stopDisc(explodedBlock);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHeadFluidDestroy(BlockFromToEvent event) {
        Block block = event.getToBlock();
        if (!TypeChecker.isHead(block.getType()) && !TypeChecker.isWallHead(block.getType())) return;

        Skull skull = (Skull) block.getState();
        PersistentDataContainer persistentDataContainer = skull.getPersistentDataContainer();
        NamespacedKey headKey = new NamespacedKey(plugin, "customhead");
        if (!persistentDataContainer.has(headKey, PersistentDataType.STRING)) return;

        NamespacedKey loreKey = new NamespacedKey(plugin, "headlore");
        NamespacedKey rangeKey = new NamespacedKey(plugin, "range");

        Block noteBlockBelow = block.getRelative(BlockFace.DOWN);
        if (noteBlockBelow.getType() == Material.NOTE_BLOCK) playerManager.stopDisc(noteBlockBelow);

        event.setCancelled(true);

        for (ItemStack dropItemStack : block.getDrops()) {
            if (!TypeChecker.isHead(dropItemStack.getType()) && !TypeChecker.isWallHead(dropItemStack.getType())) continue;

            ItemMeta itemMeta = dropItemStack.getItemMeta();
            if (itemMeta == null) continue;

            PersistentDataContainer dropPersistentDataContainer = itemMeta.getPersistentDataContainer();

            String customHeadValue = persistentDataContainer.get(headKey, PersistentDataType.STRING);
            if (customHeadValue != null) dropPersistentDataContainer.set(headKey, PersistentDataType.STRING, customHeadValue);

            String headLoreValue = persistentDataContainer.get(loreKey, PersistentDataType.STRING);
            if (headLoreValue != null) {
                dropPersistentDataContainer.set(loreKey, PersistentDataType.STRING, headLoreValue);
                Component deserializedLore = GsonComponentSerializer.gson().deserialize(headLoreValue);
                @Nullable List<Component> itemLore = new ArrayList<>();
                itemLore.add(deserializedLore);
                itemMeta.lore(itemLore);
            }

            Float rangeValue = persistentDataContainer.get(rangeKey, PersistentDataType.FLOAT);
            if (rangeValue != null) dropPersistentDataContainer.set(rangeKey, PersistentDataType.FLOAT, rangeValue);

            dropItemStack.setItemMeta(itemMeta);
            block.getWorld().dropItemNaturally(block.getLocation(), dropItemStack);
        }

        block.setType(Material.AIR);
    }

}