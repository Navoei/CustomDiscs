package me.Navoei.customdiscsplugin.command.SubCommands;

import me.Navoei.customdiscsplugin.CustomDiscs;
import me.Navoei.customdiscsplugin.command.SubCommand;
import me.Navoei.customdiscsplugin.language.Lang;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;

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
        return "/customdisc download <url> <filename.extension>";
    }

    @Override
    public void perform(Player player, String[] args) {
        // /cd   download    url   filename
        //         [0]       [1]     [2]

        if (!player.hasPermission("customdiscs.download")) {
            Component textComponent = LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.NO_PERMISSION.toString());
            player.sendMessage(textComponent);
            return;
        }

        if (args.length!=3) {
            Component textComponent = LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.INVALID_ARGUMENTS.toString().replace("%command_syntax", getSyntax()));
            player.sendMessage(textComponent);
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(customDiscs, () -> {
            try {
                URL fileURL = new URL(args[1]);
                String filename = args[2];
                if (filename.contains("../")) {
                    Component textComponent = LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.INVALID_FILENAME.toString());
                    player.sendMessage(textComponent);
                    return;
                }

                System.out.println(filename);

                if (!getFileExtension(filename).equals("wav") && !getFileExtension(filename).equals("mp3") && !getFileExtension(filename).equals("flac")) {
                    Component textComponent = LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.INVALID_FORMAT.toString());
                    player.sendMessage(textComponent);
                    return;
                }

                Component textComponent = LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.DOWNLOADING_FILE.toString());
                player.sendMessage(textComponent);
                Path downloadPath = Path.of(customDiscs.getDataFolder().getPath(), "musicdata", filename);
                File downloadFile = new File(downloadPath.toUri());

                URLConnection connection = fileURL.openConnection();

                if (connection != null) {
                    long size = connection.getContentLengthLong() / 1048576;
                    if (size > customDiscs.getConfig().getInt("max-download-size", 50)) {
                        Component textComponent2 = LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.FILE_TOO_LARGE.toString().replace("%max_download_size%", String.valueOf(customDiscs.getConfig().getInt("max-download-size", 50))));
                        player.sendMessage(textComponent2);
                        return;
                    }
                }

                FileUtils.copyURLToFile(fileURL, downloadFile);

                Component fileDownloaded = LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.SUCCESSFUL_DOWNLOAD.toString().replace("%file_path%", "plugins/CustomDiscs/musicdata/" + filename));
                Component createDisc = LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.CREATE_DISC.toString().replace("%filename%", filename));

                player.sendMessage(fileDownloaded);
                player.sendMessage(createDisc);
            } catch (IOException e) {
                Component textComponent = LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.DOWNLOAD_ERROR.toString());
                player.sendMessage(textComponent);
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
