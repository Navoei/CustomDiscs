package me.Navoei.customdiscsplugin.command.SubCommands;

import me.Navoei.customdiscsplugin.CustomDiscs;
import me.Navoei.customdiscsplugin.TypeChecker;
import me.Navoei.customdiscsplugin.language.Lang;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.FloatArgument;
import dev.jorel.commandapi.executors.CommandArguments;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Objects;

public class SetRangeSubCommand extends CommandAPICommand {
	private final CustomDiscs plugin;
	
	public SetRangeSubCommand(CustomDiscs plugin) {
		super("range");
		this.plugin = plugin;
		
		this.withFullDescription(NamedTextColor.GRAY + "Set the range of a disc to the defined value (range from 1 to "+ this.plugin.musicDiscMaxDistance +").");
		this.withUsage("/cd range <range>");
		this.withPermission("customdiscs.range");

		this.withArguments(new FloatArgument("range"));
		
		this.executesPlayer(this::onCommandPlayer);
		this.executesConsole(this::onCommandConsole);
	}
	
	private int onCommandPlayer(Player player, CommandArguments arguments) {

        ItemStack item = player.getInventory().getItemInMainHand();
		boolean resultIsCustomDisc = TypeChecker.isCustomMusicDisc(item);
		boolean resultIsCustomHorn = TypeChecker.isCustomGoatHornPlayer(player);
		boolean resultIsCustomHead = TypeChecker.isCustomHeadPlayer(player);

		if (!resultIsCustomDisc && !resultIsCustomHorn && !resultIsCustomHead) {
			player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.NOT_HOLDING_CORRECT_ITEM.toString()));
			return 1;
		}
		
        Float range = Objects.requireNonNull(arguments.getByClass("range", Float.class));

		Float configMusicDiscMaxDistance;
		if (resultIsCustomHorn) {
			if (!CustomDiscs.isCustomHornEnable()) { player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.CUSTOM_HORN_DISABLED.toString())); return 1; }
			configMusicDiscMaxDistance = this.plugin.customHornMaxDistance;
		} else if (resultIsCustomHead) {
			if (!CustomDiscs.isCustomHeadEnable()) { player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.CUSTOM_HEAD_DISABLED.toString())); return 1; }
			configMusicDiscMaxDistance = this.plugin.customHeadMaxDistance;
		} else {
			if (!CustomDiscs.isMusicDiscEnable()) { player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.CUSTOM_MUSIC_DISABLED.toString())); return 1; }
			configMusicDiscMaxDistance = this.plugin.musicDiscMaxDistance;
		}


		if ( range < 1 || range > configMusicDiscMaxDistance) {
            player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.INVALID_RANGE.toString().replace("%range_value%", Float.toString(configMusicDiscMaxDistance))));
            return 1;
        }

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer data = meta.getPersistentDataContainer();
		data.set(new NamespacedKey(this.plugin, "range"), PersistentDataType.FLOAT, range);
		player.getInventory().getItemInMainHand().setItemMeta(meta);

        player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.CREATE_CUSTOM_RANGE.toString().replace("%custom_range%", Float.toString(range))));

		return 1;
	}
	
	private int onCommandConsole(ConsoleCommandSender executor, CommandArguments arguments) {
		executor.sendMessage(NamedTextColor.RED + "Only players can use this command : '"+arguments+"'!");
		return 1;
	}

}