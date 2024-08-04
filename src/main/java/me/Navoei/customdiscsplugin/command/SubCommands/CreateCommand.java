package me.Navoei.customdiscsplugin.command.SubCommands;

import me.Navoei.customdiscsplugin.CustomDiscs;
import me.Navoei.customdiscsplugin.command.SubCommand;
import me.Navoei.customdiscsplugin.language.Lang;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.JukeboxPlayableComponent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CreateCommand extends SubCommand {

    @Override
    public String getName() {
        return "create";
    }

    @Override
    public String getDescription() {
        return ChatColor.GRAY + "Creates a custom music disc.";
    }

    @Override
    public String getSyntax() {
        return "/customdisc create <filename> \"Custom Lore\"";
    }

    @Override
    public void perform(Player player, String[] args) {
        if (CustomDiscs.isMusicDisc(player)) {
            if (args.length >= 3) {

                if (!player.hasPermission("customdiscs.create")) {
                    Component textComponent = LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.NO_PERMISSION.toString());
                    player.sendMessage(textComponent);
                    return;
                }

                // /cd create test.mp3 "test"
                //      [0]     [1]     [2]
                //Find file, if file not there then say "file not there"
                String song_name = "";
                String filename = args[1];
                if (filename.contains("../")) {
                    Component textComponent = LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.INVALID_FILENAME.toString());
                    player.sendMessage(textComponent);
                    return;
                }

                if (customName(readQuotes(args)).equalsIgnoreCase("")) {
                    Component textComponent = LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.NO_DISC_NAME_PROVIDED.toString());
                    player.sendMessage(textComponent);
                    return;
                }

                File getDirectory = new File(CustomDiscs.getInstance().getDataFolder(), "musicdata");
                File songFile = new File(getDirectory.getPath(), filename);
                if (songFile.exists()) {
                    if (getFileExtension(filename).equals("wav") || getFileExtension(filename).equals("mp3") || getFileExtension(filename).equals("flac")) {
                        song_name = args[1];
                    } else {
                        Component textComponent = LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.INVALID_FORMAT.toString());
                        player.sendMessage(textComponent);
                        return;
                    }
                } else {
                    Component textComponent = LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.FILE_NOT_FOUND.toString());
                    player.sendMessage(textComponent);
                    return;
                }

                //Sets the lore of the item to the quotes from the command.
                ItemStack disc = new ItemStack(player.getInventory().getItemInMainHand());
                ItemMeta meta = disc.getItemMeta();
                @Nullable List<Component> itemLore = new ArrayList<>();
                final TextComponent customLoreSong = Component.text()
                        .decoration(TextDecoration.ITALIC, false)
                        .content(customName(readQuotes(args)))
                        .color(NamedTextColor.GRAY)
                        .build();
                itemLore.add(customLoreSong);
                meta.lore(itemLore);
                JukeboxPlayableComponent jpc = meta.getJukeboxPlayable();
                jpc.setShowInTooltip(false);
                meta.setJukeboxPlayable(jpc);

                PersistentDataContainer data = meta.getPersistentDataContainer();
                data.set(new NamespacedKey(CustomDiscs.getInstance(), "customdisc"), PersistentDataType.STRING, filename);

                player.getInventory().getItemInMainHand().setItemMeta(meta);

                Component textComponentFileName = LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.CREATE_FILENAME.toString().replace("%filename%", song_name));
                Component textComponentCustomName = LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.CREATE_CUSTOM_NAME.toString().replace("%custom_name%", customName(readQuotes(args))));
                player.sendMessage(textComponentFileName);
                player.sendMessage(textComponentCustomName);
            } else {
                Component textComponent = LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.INVALID_ARGUMENTS.toString().replace("%command_syntax%", getSyntax()));
                player.sendMessage(textComponent);
            }
        } else {
            Component textComponent = LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.NOT_HOLDING_DISC.toString());
            player.sendMessage(textComponent);
        }
    }

    private String getFileExtension(String s) {
        int index = s.lastIndexOf(".");
        if (index > 0) {
            return s.substring(index + 1);
        } else {
            return "";
        }
    }

    private ArrayList<String> readQuotes(String[] args) {
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

        return quotes;
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

}
