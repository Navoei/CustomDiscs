package me.Navoei.customdiscsplugin.command.SubCommands;

import me.Navoei.customdiscsplugin.CustomDiscs;
import me.Navoei.customdiscsplugin.command.SubCommand;
import me.Navoei.customdiscsplugin.language.Lang;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ReloadCommand extends SubCommand {

    CustomDiscs customDiscs = CustomDiscs.getInstance();

    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public String getDescription() {
        return ChatColor.GRAY + "Reloads plugin configuration.";
    }

    @Override
    public String getSyntax() {
        return "/customdisc reload";
    }

    @Override
    public void perform(Player player, String[] args) {

        if (!player.hasPermission("customdiscs.reload")) {
            Component textComponent = LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.NO_PERMISSION.toString());
            player.sendMessage(textComponent);
            return;
        }

        customDiscs.reload();
        Component textComponent = LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.RELOAD.toString());
        player.sendMessage(textComponent);

    }
}