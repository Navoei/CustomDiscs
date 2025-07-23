package me.Navoei.customdiscsplugin;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class TypeChecker {
    static CustomDiscs customDiscs = CustomDiscs.getInstance();

    // Commented methods are kept for possible future checks usage.

    // MUSIC DISCS

    public static boolean isMusicDisc(ItemStack item) {
        return item.getType().toString().contains("MUSIC_DISC");
    }

    /*public static boolean isMusicDiscPlayer(Player p) {
        return p.getInventory().getItemInMainHand().getType().toString().contains("MUSIC_DISC");
    }*/

    public static boolean isCustomMusicDisc(ItemStack itemStack) {
        if (itemStack == null) return false;
        if (itemStack.getItemMeta() == null) return false;
        return itemStack.getType().toString().contains("MUSIC_DISC") && itemStack.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(customDiscs, "customdisc"), PersistentDataType.STRING);
    }

    /*public static boolean isCustomMusicDiscPlayer(Player p) {
        return p.getInventory().getItemInMainHand().getType().toString().contains("MUSIC_DISC") && p.getInventory().getItemInMainHand().getItemMeta().getPersistentDataContainer().has(new NamespacedKey(customDiscs, "customdisc"), PersistentDataType.STRING);
    }*/

    // GOAT HORNS

    /*public static boolean isGoatHorn(ItemStack item) {
        return item.getType().toString().contains("GOAT_HORN");
    }*/

    public static boolean isGoatHornPlayer(Player p) {
        return p.getInventory().getItemInMainHand().getType().toString().contains("GOAT_HORN");
    }
    
    public static boolean isCustomGoatHorn(PlayerInteractEvent e) {
        if (e.getItem()==null) return false;
        return e.getItem().getType().toString().contains("GOAT_HORN") && e.getItem().getItemMeta().getPersistentDataContainer().has(new NamespacedKey(customDiscs, "customhorn"), PersistentDataType.STRING);
    }

    public static boolean isCustomGoatHornPlayer(Player p) {
        return p.getInventory().getItemInMainHand().getType().toString().contains("GOAT_HORN") && p.getInventory().getItemInMainHand().getItemMeta().getPersistentDataContainer().has(new NamespacedKey(customDiscs, "customhorn"), PersistentDataType.STRING);
    }

    // PLAYER HEADS

    /*public static boolean isHead(ItemStack item) {
        return item.getType().toString().contains("PLAYER_HEAD");
    }*/

    public static boolean isHeadPlayer(Player p) {
        return p.getInventory().getItemInMainHand().getType().toString().contains("PLAYER_HEAD");
    }

    /*public static boolean isCustomHead(ItemStack itemStack) {
        if (itemStack == null) return false;
        if (itemStack.getItemMeta() == null) return false;
        return itemStack.getType().toString().contains("PLAYER_HEAD") && itemStack.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(customDiscs, "customhead"), PersistentDataType.STRING);
    }*/

    public static boolean isCustomHeadPlayer(Player p) {
        return p.getInventory().getItemInMainHand().getType().toString().contains("PLAYER_HEAD") && p.getInventory().getItemInMainHand().getItemMeta().getPersistentDataContainer().has(new NamespacedKey(customDiscs, "customhead"), PersistentDataType.STRING);
    }

}