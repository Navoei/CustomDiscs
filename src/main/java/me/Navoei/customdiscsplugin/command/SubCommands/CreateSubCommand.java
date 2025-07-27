package me.Navoei.customdiscsplugin.command.SubCommands;

import me.Navoei.customdiscsplugin.CustomDiscs;
import me.Navoei.customdiscsplugin.TypeChecker;
import me.Navoei.customdiscsplugin.language.Lang;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.arguments.TextArgument;
import dev.jorel.commandapi.executors.CommandArguments;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.TooltipDisplay;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.keys.InstrumentKeys;
import io.papermc.paper.registry.keys.SoundEventKeys;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import org.bukkit.*;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
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
		this.withPermission("customdiscs.create");

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
		boolean resultIsMusicDisc = TypeChecker.isMusicDisc(item);
		boolean resultIsHorn = TypeChecker.isGoatHornPlayer(player);
		boolean resultIsHead = TypeChecker.isHeadPlayer(player);

		if (!resultIsMusicDisc && !resultIsHorn && !resultIsHead) {
			player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.NOT_HOLDING_CORRECT_ITEM.toString()));
			return 0;
		}

		String filename = Objects.requireNonNull(arguments.getByClass("filename", String.class));
		if (filename.contains("../")) {
			player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.INVALID_FILENAME.toString()));
			return 0;
		}
		
		File getDirectory = new File(this.plugin.getDataFolder(), "musicdata");
		File songFile = new File(getDirectory.getPath(), filename);
		if (songFile.exists()) {
			if (!getFileExtension(filename).equals("wav") && !getFileExtension(filename).equals("mp3") && !getFileExtension(filename).equals("flac")) {
				player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.INVALID_FORMAT.toString()));
				return 0;
			}
		} else {
			player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.FILE_NOT_FOUND.toString()));
			return 0;
		}
		
		String song_name = Objects.requireNonNull(arguments.getByClass("song_name", String.class));

		if (resultIsMusicDisc) {
			if (!CustomDiscs.isMusicDiscEnable()) { player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.CUSTOM_MUSIC_DISABLED.toString())); return 1; }
			ItemStack disc = new ItemStack(player.getInventory().getItemInMainHand());
			disc.setData(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplay.tooltipDisplay().addHiddenComponents(DataComponentTypes.JUKEBOX_PLAYABLE).build());
			ItemMeta meta = disc.getItemMeta();
			@Nullable List<Component> itemLore = new ArrayList<>();
			final TextComponent customLoreSong = Component.text().decoration(TextDecoration.ITALIC, false).content(song_name).color(NamedTextColor.GRAY).build();
			itemLore.add(customLoreSong);
			meta.lore(itemLore);

			PersistentDataContainer data = meta.getPersistentDataContainer();
			data.set(new NamespacedKey(this.plugin, "customdisc"), PersistentDataType.STRING, filename);
			player.getInventory().getItemInMainHand().setItemMeta(meta);
		} else if (resultIsHorn) {
			if (!CustomDiscs.isCustomHornEnable()) { player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.CUSTOM_HORN_DISABLED.toString())); return 1; }
			final TextComponent customLoreSong = Component.text().decoration(TextDecoration.ITALIC, false).content(song_name).color(NamedTextColor.GRAY).build();
			MusicInstrument customInstrument = MusicInstrument.create(builder -> {
				builder.copyFrom(InstrumentKeys.ADMIRE_GOAT_HORN)
						.description(customLoreSong)
						.range(256.0f)
						.duration(1.0f)
						.soundEvent(TypedKey.create(
								RegistryKey.SOUND_EVENT,
								SoundEventKeys.INTENTIONALLY_EMPTY
						));
			});
			item.setData(DataComponentTypes.INSTRUMENT, customInstrument);

			ItemStack disc = new ItemStack(player.getInventory().getItemInMainHand());
			ItemMeta meta = disc.getItemMeta();
			PersistentDataContainer data = meta.getPersistentDataContainer();
			data.set(new NamespacedKey(this.plugin, "customhorn"), PersistentDataType.STRING, filename);
			player.getInventory().getItemInMainHand().setItemMeta(meta);
		} else {
			//Must be a player head.
			if (!CustomDiscs.isCustomHeadEnable()) { player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.CUSTOM_HEAD_DISABLED.toString())); return 1; }
			final Component customLoreHead = Component.text().decoration(TextDecoration.ITALIC, false).content(song_name).color(NamedTextColor.GRAY).build();
			String serialized = GsonComponentSerializer.gson().serialize(customLoreHead);

			ItemStack disc = new ItemStack(player.getInventory().getItemInMainHand());
			ItemMeta meta = disc.getItemMeta();
			@Nullable List<Component> itemLore = new ArrayList<>();
			final TextComponent customLoreSong = Component.text().decoration(TextDecoration.ITALIC, false).content(song_name).color(NamedTextColor.GRAY).build();
			itemLore.add(customLoreSong);
			meta.lore(itemLore);

			PersistentDataContainer data = meta.getPersistentDataContainer();
			data.set(new NamespacedKey(this.plugin, "customhead"), PersistentDataType.STRING, filename);
			data.set(new NamespacedKey(this.plugin, "headlore"), PersistentDataType.STRING, serialized);
			player.getInventory().getItemInMainHand().setItemMeta(meta);
		}
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

}