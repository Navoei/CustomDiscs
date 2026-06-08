package me.Navoei.customdiscsplugin.command.SubCommands;

import me.Navoei.customdiscsplugin.CustomDiscs;
import me.Navoei.customdiscsplugin.utils.TypeChecker;
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

import java.util.Optional;

public class SetRangeSubCommand extends CommandAPICommand {
	private final CustomDiscs plugin;
	
	public SetRangeSubCommand(CustomDiscs plugin) {
		super("range");
		this.plugin = plugin;
		
		this.withFullDescription(NamedTextColor.GRAY + "Set the range of the custom item in hand (disc: 1–" + (int) this.plugin.musicDiscMaxDistance + ", horn: 1–" + (int) this.plugin.customHornMaxDistance + ", head: 1–" + (int) this.plugin.customHeadMaxDistance + ").");
		this.withUsage("/cd range <range>");
		this.withPermission("customdiscs.range");

		this.withArguments(new FloatArgument("range"));
		
		this.executesPlayer(this::onCommandPlayer);
		this.executesConsole(this::onCommandConsole);
	}
	
	private int onCommandPlayer(Player player, CommandArguments arguments) {

        ItemStack heldItem = player.getInventory().getItemInMainHand();
		boolean resultIsCustomDisc = TypeChecker.isCustomMusicDisc(heldItem);
		boolean resultIsCustomHorn = TypeChecker.isCustomGoatHornPlayer(player);
		boolean resultIsCustomHead = TypeChecker.isCustomHeadPlayer(player);

		if (!resultIsCustomDisc && !resultIsCustomHorn && !resultIsCustomHead) {
			player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX.forPlayer(player) + Lang.NOT_HOLDING_CORRECT_ITEM.forPlayer(player)));
			return 1;
		}

        float range = Optional.ofNullable(arguments.getByClass("range", Float.class)).orElse(0f);

		float itemMaxRange;
		if (resultIsCustomHorn) {
			if (!CustomDiscs.isCustomHornEnable()) { player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX.forPlayer(player) + Lang.CUSTOM_HORN_DISABLED.forPlayer(player))); return 1; }
			itemMaxRange = this.plugin.customHornMaxDistance;
		} else if (resultIsCustomHead) {
			if (!CustomDiscs.isCustomHeadEnable()) { player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX.forPlayer(player) + Lang.CUSTOM_HEAD_DISABLED.forPlayer(player))); return 1; }
			itemMaxRange = this.plugin.customHeadMaxDistance;
		} else {
			if (!CustomDiscs.isMusicDiscEnable()) { player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX.forPlayer(player) + Lang.CUSTOM_MUSIC_DISABLED.forPlayer(player))); return 1; }
			itemMaxRange = this.plugin.musicDiscMaxDistance;
		}


		if ( range < 1 || range > itemMaxRange) {
            player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX.forPlayer(player) + Lang.INVALID_RANGE.forPlayer(player).replace("%range_value%", Float.toString(itemMaxRange))));
            return 1;
        }

        ItemMeta itemMeta = heldItem.getItemMeta();
        PersistentDataContainer persistentDataContainer = itemMeta.getPersistentDataContainer();
		persistentDataContainer.set(new NamespacedKey(this.plugin, "range"), PersistentDataType.FLOAT, range);
		player.getInventory().getItemInMainHand().setItemMeta(itemMeta);

        player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX.forPlayer(player) + Lang.CREATE_CUSTOM_RANGE.forPlayer(player).replace("%custom_range%", Float.toString(range))));

		return 1;
	}
	
	private int onCommandConsole(ConsoleCommandSender executor, CommandArguments arguments) {
		executor.sendMessage(NamedTextColor.RED + "Only players can use this command : '"+arguments+"'!");
		return 1;
	}

}