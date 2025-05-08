package me.Navoei.customdiscsplugin.command.SubCommands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.arguments.TextArgument;
import dev.jorel.commandapi.executors.CommandArguments;
//import io.papermc.paper.datacomponent.DataComponentTypes;
//import io.papermc.paper.datacomponent.item.JukeboxPlayable;
//import io.papermc.paper.datacomponent.item.ShownInTooltip;
import me.Navoei.customdiscsplugin.CustomDiscs;
import me.Navoei.customdiscsplugin.language.Lang;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.*;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
//import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
//import org.bukkit.inventory.meta.MusicInstrumentMeta;
//import org.bukkit.inventory.meta.components.JukeboxPlayableComponent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class CreateSubCommand extends CommandAPICommand {
	private final CustomDiscs plugin;
	
	public CreateSubCommand(CustomDiscs plugin) {
		super("create");
		this.plugin = plugin;
		
		this.withFullDescription(NamedTextColor.GRAY + "Creates a custom music disc.");
		this.withUsage("/customdisc create <filename> \"Custom Lore\"");
		
		this.withArguments(new StringArgument("filename").replaceSuggestions(ArgumentSuggestions.stringCollection((sender) -> {
			File musicDataFolder = new File(this.plugin.getDataFolder(), "musicdata");
			if (!musicDataFolder.isDirectory()) {
				return List.of();
			}
			
			File[] files = musicDataFolder.listFiles();
			if (files == null) {
				return List.of();
			}
			
			return Arrays.stream(files).filter(file -> !file.isDirectory()).map(File::getName).toList();
		})));
		
		this.withArguments(new TextArgument("song_name"));
		
		this.executesPlayer(this::onCommandPlayer);
		this.executesConsole(this::onCommandConsole);
	}
	
	private int onCommandPlayer(Player player, CommandArguments arguments) {

		ItemStack item = player.getInventory().getItemInMainHand();

		if (!isMusicDisc(item)) {
			player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.NOT_HOLDING_DISC.toString()));
			return 1;
		}
		
		if (!player.hasPermission("customdiscs.create")) {
			player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.NO_PERMISSION.toString()));
			return 1;
		}
		
		// Find file, if file not there then say "file not there"
		String filename = Objects.requireNonNull(arguments.getByClass("filename", String.class));
		if (filename.contains("../")) {
			player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.INVALID_FILENAME.toString()));
			return 1;
		}
		
		File getDirectory = new File(this.plugin.getDataFolder(), "musicdata");
		File songFile = new File(getDirectory.getPath(), filename);
		if (songFile.exists()) {
			if (!getFileExtension(filename).equals("wav") && !getFileExtension(filename).equals("mp3") && !getFileExtension(filename).equals("flac")) {
				player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.INVALID_FORMAT.toString()));
				return 1;
			}
		} else {
			player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.FILE_NOT_FOUND.toString()));
			return 1;
		}
		
		String song_name = Objects.requireNonNull(arguments.getByClass("song_name", String.class));

		ItemStack disc = new ItemStack(player.getInventory().getItemInMainHand());
		ItemMeta meta = disc.getItemMeta();
		@Nullable List<Component> itemLore = new ArrayList<>();
		final TextComponent customLoreSong = Component.text().decoration(TextDecoration.ITALIC, false).content(song_name).color(NamedTextColor.GRAY).build();
		itemLore.add(customLoreSong);
		meta.lore(itemLore);

		/*JukeboxPlayableComponent jpc = meta.getJukeboxPlayable();
		jpc.setShowInTooltip(false); //DEPRECATED
		meta.setJukeboxPlayable(jpc);*/

		PersistentDataContainer data = meta.getPersistentDataContainer();
		data.set(new NamespacedKey(this.plugin, "customdisc"), PersistentDataType.STRING, filename);
		player.getInventory().getItemInMainHand().setItemMeta(meta);
		player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.CREATE_FILENAME.toString().replace("%filename%", filename)));
		player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.CREATE_CUSTOM_NAME.toString().replace("%custom_name%", song_name)));
		return 1;
	}
	
	private int onCommandConsole(ConsoleCommandSender executor, CommandArguments arguments) {
		executor.sendMessage(NamedTextColor.RED + "Only players can use this command : '"+arguments+"'!");
		return 1;
	}
	
	private String getFileExtension(String s) {
		int index = s.lastIndexOf(".");
		if (index > 0) {
			return s.substring(index + 1);
		} else {
			return "";
		}
	}

	public static boolean isMusicDisc(ItemStack item) {
		return item.getType().toString().contains("MUSIC_DISC");
	}

}
