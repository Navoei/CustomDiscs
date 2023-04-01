package me.Navoei.customdiscsplugin.command.SubCommands;

import me.Navoei.customdiscsplugin.CustomDiscs;
import me.Navoei.customdiscsplugin.command.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.codehaus.plexus.util.FileUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Objects;

public class DownloadCommand extends SubCommand {

    CustomDiscs customDiscs = CustomDiscs.getInstance();

    @Override
    public String getName() {
        return "download";
    }

    @Override
    public String getDescription() {
        return ChatColor.GRAY + "Downloads a file from a given URL.";
    }

    @Override
    public String getSyntax() {
        return ChatColor.GREEN + "/customdisc download <url> <filename.extension>";
    }

    @Override
    public void perform(Player player, String[] args) {
        // /cd   download    url   filename
        //         [0]       [1]     [2]

        if (!player.hasPermission("customdiscs.download")) {
            player.sendMessage(ChatColor.RED + "You do not have permission to execute this command!");
            return;
        }
        
        if (args.length!=3) {
            player.sendMessage(ChatColor.RED + "Invalid arguments! ( /customdisc download <url> <filename.extension> )");
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(customDiscs, () -> {
            try {
                URL fileURL = new URL(args[1]);
                String filename = args[2];
                if (filename.contains("../")) {
                    player.sendMessage(ChatColor.RED + "This is an invalid filename!");
                    return;
                }

                System.out.println(filename);

                if (!getFileExtension(filename).equals("wav") && !getFileExtension(filename).equals("mp3") && !getFileExtension(filename).equals("flac")) {
                    player.sendMessage(ChatColor.RED + "The file must have an extension of wav, flac, or mp3!");
                    return;
                }

                player.sendMessage(ChatColor.GRAY + "Downloading file...");
                Path downloadPath = Path.of(customDiscs.getDataFolder().getPath(), "musicdata", filename);
                File downloadFile = new File(downloadPath.toUri());
                FileUtils.copyURLToFile(fileURL, downloadFile);

                player.sendMessage(ChatColor.GREEN + "File successfully downloaded to " + ChatColor.GRAY + downloadPath.toUri() + ChatColor.GREEN + " .");
                player.sendMessage(ChatColor.GREEN + "Create a disc by doing " + ChatColor.GRAY + "/cd create "+filename+" \"Custom Lore\" " + ChatColor.GREEN + ".");
            } catch (IOException e) {
                player.sendMessage(ChatColor.RED + "An error has occurred while downloading.");
                e.printStackTrace();
            }
        });
    }

    private String getFileExtension(String s) {
        int index = s.lastIndexOf(".");
        if (index > 0) {
            return s.substring(index + 1);
        } else {
            return "";
        }
    }

}
