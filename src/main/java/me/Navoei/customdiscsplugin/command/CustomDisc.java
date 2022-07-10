package me.Navoei.customdiscsplugin.command;

import me.Navoei.customdiscsplugin.CustomDiscs;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CustomDisc implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command!");
            return true;
        }

        Player p = (Player) sender;

        // /customdisc wewontbealone "We wont be alone"
        if (command.getName().equalsIgnoreCase("customdisc")) {
            if (isMusicDisc(p)) {
                if (args.length >= 2) {

                    //Find file, if file not there then say "file not there"
                    String songname = "";
                    String filename = args[0];

                    File getDirectory = new File(CustomDiscs.getInstance().getDataFolder(), "musicdata");
                    File songFile = new File(getDirectory.getPath(), filename);
                    if (songFile.exists()) {
                        if (getFileExtension(filename).equals("wav")) {
                            songname = args[0];
                        } else {
                            p.sendMessage(ChatColor.RED + "File is not in wav format!");
                            return true;
                        }
                    } else {
                        p.sendMessage(ChatColor.RED + "File not found!");
                        return true;
                    }

                    //Reads the command for quotations.
                    ArrayList<String> quotes = new ArrayList<>();
                    String temp = "";
                    boolean inQuotes = false;

                    for (String s : args) {
                        if (s.startsWith("\"") && s.endsWith("\"")) {
                            temp += s.substring(1, s.length()-1);
                            quotes.add(temp);
                        } else if (s.startsWith("\"")) {
                            temp += s.substring(1);
                            quotes.add(temp);
                            inQuotes = true;
                        } else if (s.endsWith("\"")) {
                            temp += s.substring(0, s.length()-1);
                            quotes.add(temp);
                            inQuotes = false;
                        } else if (inQuotes) {
                            temp += s;
                            quotes.add(temp);
                        }
                        temp = "";
                    }

                    //Sets the lore of the item to the quotes from the command.
                    ItemStack disc = new ItemStack(p.getInventory().getItemInMainHand());
                    ItemMeta meta = disc.getItemMeta();
                    @Nullable List<Component> itemLore = new ArrayList<>();
                        final TextComponent customLoreSong = Component.text()
                                .decoration(TextDecoration.ITALIC, false)
                                .content(customName(quotes))
                                .color(NamedTextColor.GRAY)
                                .build();
                        itemLore.add(customLoreSong);
                        final TextComponent customLoreFile = Component.text()
                                .content(filename)
                                .color(NamedTextColor.DARK_GRAY)
                                .build();
                        itemLore.add(customLoreFile);
                    meta.lore(itemLore);
                    meta.addItemFlags(ItemFlag.values());
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    p.getInventory().getItemInMainHand().setItemMeta(meta);


                    p.sendMessage("Your filename is: " + songname);
                    p.sendMessage("Your custom name is: " + customName(quotes));

                    return true;

                } else {
                    p.sendMessage(ChatColor.RED + "Insufficient arguments! ( /customdisc <filename> [\"customname\"] )");
                    return true;
                }
            } else {
                p.sendMessage(ChatColor.RED + "You are not holding a music disc in your main hand!");
            }
        }

        return false;
    }

    private String getFileExtension(String s) {
        int index = s.lastIndexOf(".");
        if (index > 0) {
            return s.substring(index + 1);
        } else {
            return "";
        }
    }

    private String customName(ArrayList<String> q) {

        StringBuffer sb = new StringBuffer();

        for (String s : q) {
            sb.append(s);
            sb.append(" ");
        }

        if (sb.isEmpty()) {
            return sb.toString();
        } else {
            return sb.toString().substring(0, sb.length()-1);
        }
    }

    private boolean isMusicDisc(Player p) {

        if (    p.getInventory().getItemInMainHand().getType().equals(Material.MUSIC_DISC_13) ||
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
        {
            return true;
        }

        return false;
    }

}
