package me.Navoei.customdiscsplugin.command.SubCommands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.FloatArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import me.Navoei.customdiscsplugin.CustomDiscs;
import me.Navoei.customdiscsplugin.language.Lang;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.format.NamedTextColor;
//import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
//import org.bukkit.Material;
import org.bukkit.NamespacedKey;
//import org.bukkit.inventory.ItemFlag;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Objects;
import org.bukkit.Bukkit;

public class SetRangeSubCommand extends CommandAPICommand {
	private final CustomDiscs plugin;
	
	public SetRangeSubCommand(CustomDiscs plugin) {
		super("range");
		this.plugin = plugin;
		
		this.withFullDescription(NamedTextColor.GRAY + "Set the range of a disc to the defined value (range from 1 to "+ this.plugin.musicDiscMaxDistance +").");
		this.withUsage("/cd range <range>");
		
		this.withArguments(new FloatArgument("range"));
		
		this.executesPlayer(this::onCommandPlayer);
		this.executesConsole(this::onCommandConsole);
	}
	
	private int onCommandPlayer(Player player, CommandArguments arguments) {

        ItemStack item = player.getInventory().getItemInMainHand();

        if (!isCustomDisc(item)) {
			player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.NOT_HOLDING_DISC.toString()));
			return 1;
		}

        if (!player.hasPermission("customdiscs.range")) {
			player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.NO_PERMISSION.toString()));
			return 1;
		}
		
        Float range = Objects.requireNonNull(arguments.getByClass("range", Float.class));

        if ( range < 1 || range > this.plugin.musicDiscMaxDistance) {
            player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.INVALID_RANGE.toString().replace("%range_value%", Float.toString(this.plugin.musicDiscMaxDistance))));
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

    public boolean isCustomDisc(ItemStack item) {
        if (item==null) return false;
        return item.getType().toString().contains("MUSIC_DISC") && item.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(plugin, "customdisc"));
    }

}
