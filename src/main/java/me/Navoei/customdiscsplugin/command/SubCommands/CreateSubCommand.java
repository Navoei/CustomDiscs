package me.Navoei.customdiscsplugin.command.SubCommands;

import me.Navoei.customdiscsplugin.CustomDiscs;
import me.Navoei.customdiscsplugin.utils.TypeChecker;
import me.Navoei.customdiscsplugin.language.Lang;

import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import dev.jorel.commandapi.CommandAPICommand;
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
import java.nio.file.Path;
import java.util.ArrayList;
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

		this.withArguments(new TextArgument("filename").replaceSuggestions((info, builder) -> {
			File musicDataFolder = new File(this.plugin.getDataFolder(), "musicdata");
			if (!musicDataFolder.isDirectory()) return builder.buildFuture();

			int maxDepth = switch (this.plugin.subdirectoryDepth) {
				case "none" -> 0;
				case "single" -> 1;
				default -> Integer.MAX_VALUE;
			};

			String remaining = builder.getRemaining();

			if (remaining.startsWith("\"")) {
				SuggestionsBuilder quotedSuggestionBuilder = builder.createOffset(builder.getStart() + 1);
				String typedInput = quotedSuggestionBuilder.getRemaining();

				String directoryPrefix = "";
				int lastSlashIndex = typedInput.lastIndexOf('/');
				if (lastSlashIndex >= 0) {
					directoryPrefix = typedInput.substring(0, lastSlashIndex + 1);
				}

				int currentDepth = directoryPrefix.isEmpty() ? 0 : (int) directoryPrefix.chars().filter(character -> character == '/').count();
				File currentDirectory = directoryPrefix.isEmpty() ? musicDataFolder : new File(musicDataFolder, directoryPrefix);

				suggestCurrentDirectory(currentDirectory, directoryPrefix, currentDepth, maxDepth, quotedSuggestionBuilder);
				return quotedSuggestionBuilder.buildFuture();
			} else {
				String typedInput = remaining.toLowerCase();
				File[] files = musicDataFolder.listFiles();
				if (files != null) {
					for (File file : files) {
						if (!file.isFile()) continue;
						String fileExtension = getFileExtension(file.getName());
						if (fileExtension.equals("wav") || fileExtension.equals("mp3") || fileExtension.equals("flac")) {
							if (file.getName().toLowerCase().startsWith(typedInput)) {
								builder.suggest(file.getName());
							}
						}
					}
				}
				return builder.buildFuture();
			}
		}));
		
		this.withArguments(new TextArgument("songName"));
		
		this.executesPlayer(this::onCommandPlayer);
		this.executesConsole(this::onCommandConsole);
	}
	
	private int onCommandPlayer(Player player, CommandArguments arguments) {

		ItemStack heldItem = player.getInventory().getItemInMainHand();
		boolean resultIsMusicDisc = TypeChecker.isMusicDisc(heldItem);
		boolean resultIsHorn = TypeChecker.isGoatHornPlayer(player);
		boolean resultIsHead = TypeChecker.isHeadPlayer(player);

		if (!resultIsMusicDisc && !resultIsHorn && !resultIsHead) {
			player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX.forPlayer(player) + Lang.NOT_HOLDING_CORRECT_ITEM.forPlayer(player)));
			return 0;
		}

		String filename = Objects.requireNonNull(arguments.getByClass("filename", String.class));
		if (!this.plugin.isMusicdataPathSafe(filename)) {
			player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX.forPlayer(player) + Lang.INVALID_FILENAME.forPlayer(player)));
			return 0;
		}
		if (!this.plugin.isMusicdataDepthAllowed(filename)) {
			Lang depthMessage = "none".equals(this.plugin.subdirectoryDepth) ? Lang.SUBDIRECTORY_NOT_ALLOWED : Lang.SUBDIRECTORY_DEPTH_EXCEEDED;
			player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX.forPlayer(player) + depthMessage.forPlayer(player)));
			return 0;
		}
		
		File musicDataDirectory = new File(this.plugin.getDataFolder(), "musicdata");
		File songFile = new File(musicDataDirectory.getPath(), filename);
		if (songFile.exists()) {
			if (!getFileExtension(filename).equals("wav") && !getFileExtension(filename).equals("mp3") && !getFileExtension(filename).equals("flac")) {
				player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX.forPlayer(player) + Lang.INVALID_FORMAT.forPlayer(player)));
				return 0;
			}
		} else {
			player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX.forPlayer(player) + Lang.FILE_NOT_FOUND.forPlayer(player)));
			return 0;
		}
		
		String songName = Objects.requireNonNull(arguments.getByClass("songName", String.class));

		if (resultIsMusicDisc) {
			if (!CustomDiscs.isMusicDiscEnable()) { player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX.forPlayer(player) + Lang.CUSTOM_MUSIC_DISABLED.forPlayer(player))); return 1; }
			ItemStack discCopy = new ItemStack(player.getInventory().getItemInMainHand());
			discCopy.setData(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplay.tooltipDisplay().addHiddenComponents(DataComponentTypes.JUKEBOX_PLAYABLE).build());
			ItemMeta itemMeta = discCopy.getItemMeta();
			@Nullable List<Component> itemLore = new ArrayList<>();
			final TextComponent customLoreSong = Component.text().decoration(TextDecoration.ITALIC, false).content(songName).color(NamedTextColor.GRAY).build();
			itemLore.add(customLoreSong);
			itemMeta.lore(itemLore);

			PersistentDataContainer persistentDataContainer = itemMeta.getPersistentDataContainer();
			persistentDataContainer.set(new NamespacedKey(this.plugin, "customdisc"), PersistentDataType.STRING, filename);
			player.getInventory().getItemInMainHand().setItemMeta(itemMeta);
		} else if (resultIsHorn) {
			if (!CustomDiscs.isCustomHornEnable()) { player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX.forPlayer(player) + Lang.CUSTOM_HORN_DISABLED.forPlayer(player))); return 1; }

			String originalInstrumentKey = null;
			MusicInstrument currentInstrument = heldItem.getData(DataComponentTypes.INSTRUMENT);
			if (currentInstrument instanceof org.bukkit.Keyed keyed) {
				originalInstrumentKey = keyed.getKey().toString();
			}

			final TextComponent customLoreSong = Component.text().decoration(TextDecoration.ITALIC, false).content(songName).color(NamedTextColor.GRAY).build();
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
			heldItem.setData(DataComponentTypes.INSTRUMENT, customInstrument);

			ItemStack hornCopy = new ItemStack(player.getInventory().getItemInMainHand());
			ItemMeta itemMeta = hornCopy.getItemMeta();
			PersistentDataContainer persistentDataContainer = itemMeta.getPersistentDataContainer();
			persistentDataContainer.set(new NamespacedKey(this.plugin, "customhorn"), PersistentDataType.STRING, filename);
			if (originalInstrumentKey != null && !persistentDataContainer.has(new NamespacedKey(this.plugin, "originalinstrument"), PersistentDataType.STRING)) {
				persistentDataContainer.set(new NamespacedKey(this.plugin, "originalinstrument"), PersistentDataType.STRING, originalInstrumentKey);
			}
			player.getInventory().getItemInMainHand().setItemMeta(itemMeta);
		} else {
			//Must be a player head.
			if (!CustomDiscs.isCustomHeadEnable()) { player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX.forPlayer(player) + Lang.CUSTOM_HEAD_DISABLED.forPlayer(player))); return 1; }
			final Component customLoreHead = Component.text().decoration(TextDecoration.ITALIC, false).content(songName).color(NamedTextColor.GRAY).build();
			String serializedLore = GsonComponentSerializer.gson().serialize(customLoreHead);

			ItemStack headCopy = new ItemStack(player.getInventory().getItemInMainHand());
			ItemMeta itemMeta = headCopy.getItemMeta();
			@Nullable List<Component> itemLore = new ArrayList<>();
			final TextComponent customLoreSong = Component.text().decoration(TextDecoration.ITALIC, false).content(songName).color(NamedTextColor.GRAY).build();
			itemLore.add(customLoreSong);
			itemMeta.lore(itemLore);

			PersistentDataContainer persistentDataContainer = itemMeta.getPersistentDataContainer();
			persistentDataContainer.set(new NamespacedKey(this.plugin, "customhead"), PersistentDataType.STRING, filename);
			persistentDataContainer.set(new NamespacedKey(this.plugin, "headlore"), PersistentDataType.STRING, serializedLore);
			player.getInventory().getItemInMainHand().setItemMeta(itemMeta);
		}
		player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX.forPlayer(player) + Lang.CREATE_FILENAME.forPlayer(player).replace("%filename%", filename)));
		player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX.forPlayer(player) + Lang.CREATE_CUSTOM_NAME.forPlayer(player).replace("%custom_name%", songName)));
		return 1;
	}
	
	private int onCommandConsole(ConsoleCommandSender executor, CommandArguments arguments) {
		executor.sendMessage(NamedTextColor.RED + "Only players can use this command : '"+arguments+"'!");
		return 1;
	}
	
	private void suggestCurrentDirectory(File directory, String pathPrefix, int currentDepth, int maxDepth, SuggestionsBuilder builder) {
		if (!directory.isDirectory()) return;

		Path musicDataPath = new File(this.plugin.getDataFolder(), "musicdata").toPath().toAbsolutePath().normalize();
		Path directoryPath = directory.toPath().toAbsolutePath().normalize();
		if (!directoryPath.startsWith(musicDataPath)) return;

		String typedInput = builder.getRemaining().toLowerCase();
		File[] files = directory.listFiles();
		if (files == null) return;
		for (File file : files) {
			if (file.isFile()) {
				String fileExtension = getFileExtension(file.getName());
				if (fileExtension.equals("wav") || fileExtension.equals("mp3") || fileExtension.equals("flac")) {
					String suggestion = pathPrefix + file.getName() + "\"";
					if (suggestion.toLowerCase().startsWith(typedInput)) {
						builder.suggest(suggestion);
					}
				}
			} else if (file.isDirectory() && currentDepth < maxDepth) {
				String suggestion = pathPrefix + file.getName();
				if (suggestion.toLowerCase().startsWith(typedInput)) {
					builder.suggest(suggestion);
				}
			}
		}
	}

	private String getFileExtension(String filename) {
		int dotIndex = filename.lastIndexOf(".");
		if (dotIndex > 0) {
			return filename.substring(dotIndex + 1);
		} else {
			return "";
		}
	}

}