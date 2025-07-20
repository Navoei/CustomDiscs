package me.Navoei.customdiscsplugin.command.SubCommands;

import me.Navoei.customdiscsplugin.CustomDiscs;
import me.Navoei.customdiscsplugin.TypeChecker;
import me.Navoei.customdiscsplugin.language.Lang;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.executors.CommandArguments;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import org.bukkit.NamespacedKey;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Objects;

public class SetHornCooldownSubCommand extends CommandAPICommand {
	private final CustomDiscs plugin;
	
	public SetHornCooldownSubCommand(CustomDiscs plugin) {
		super("goatcooldown");
		this.plugin = plugin;
		
		this.withFullDescription(NamedTextColor.GRAY + "Set the cooldown for a modified goat horn (range from 1 to "+ this.plugin.hornMaxCooldown +" in ticks).");
		this.withUsage("/cd goatcooldown <range>");
		this.withPermission("customdiscs.horncooldown");

		this.withArguments(new IntegerArgument("goatcooldown"));
		
		this.executesPlayer(this::onCommandPlayer);
		this.executesConsole(this::onCommandConsole);
	}
	
	private int onCommandPlayer(Player player, CommandArguments arguments) {
        if (!TypeChecker.isCustomGoatHornPlayer(player)) {
			player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.NOT_HOLDING_MODIFIED_GOATHORN.toString()));
			return 1;
		}

		if (TypeChecker.isCustomGoatHornPlayer(player)) {
			if (!CustomDiscs.isMusicDiscEnable()) { player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.CUSTOM_HORN_DISABLED.toString())); return 1; }
		}

        int goatcooldown = Objects.requireNonNull(arguments.getByClass("goatcooldown", Integer.class));

        if (goatcooldown <= 0 || goatcooldown > this.plugin.hornMaxCooldown) {
                player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.INVALID_COOLDOWN.toString().replace("%cooldown_value%", Integer.toString(this.plugin.hornMaxCooldown))));
                return 1;
        }

        ItemStack disc = new ItemStack(player.getInventory().getItemInMainHand());
        ItemMeta meta = disc.getItemMeta();
        PersistentDataContainer data = meta.getPersistentDataContainer();

        data.set(new NamespacedKey(this.plugin, "customhorncoolodwn"), PersistentDataType.INTEGER, Math.min(goatcooldown, CustomDiscs.getInstance().hornMaxCooldown));
        player.getInventory().getItemInMainHand().setItemMeta(meta);

        player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.CREATE_CUSTOM_GOAT_COOLDOWN.toString().replace("%custom_goat_cooldown%", Integer.toString(goatcooldown))));
                
		return 1;
	}
	
	private int onCommandConsole(ConsoleCommandSender executor, CommandArguments arguments) {
		executor.sendMessage(NamedTextColor.RED + "Only players can use this command : '"+arguments+"'!");
		return 1;
	}

}