package me.Navoei.customdiscsplugin.command.SubCommands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.FloatArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.JukeboxPlayable;
import io.papermc.paper.datacomponent.item.UseCooldown;
import me.Navoei.customdiscsplugin.CustomDiscs;
import me.Navoei.customdiscsplugin.language.Lang;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.NamespacedKey;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.UseCooldownComponent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Objects;
import org.bukkit.Bukkit;

public class SetHornCooldownSubCommand extends CommandAPICommand {
	private final CustomDiscs plugin;
	
	public SetHornCooldownSubCommand(CustomDiscs plugin) {
		super("goatcooldown");
		this.plugin = plugin;
		
		this.withFullDescription(NamedTextColor.GRAY + "Set the cooldown for a modified goat horn (range from 0 to "+ this.plugin.hornMaxCooldown +" in seconds).");
		this.withUsage("/cd goatcooldown <range>");
		
		this.withArguments(new FloatArgument("goatcooldown"));
		
		this.executesPlayer(this::onCommandPlayer);
		this.executesConsole(this::onCommandConsole);
	}
	
	private int onCommandPlayer(Player player, CommandArguments arguments) {

        ItemStack item = player.getInventory().getItemInMainHand();

        if (!isCustomGoatHorn(item)) {
			player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.NOT_HOLDING_GOATHORN.toString()));
			return 1;
		}

        if (!player.hasPermission("customdiscs.horncooldown")) {
			player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.NO_PERMISSION.toString()));
			return 1;
		}
		
        Float goatcooldown = Objects.requireNonNull(arguments.getByClass("goatcooldown", Float.class));

        if ( goatcooldown < 0 || goatcooldown > this.plugin.hornMaxCooldown) {
            player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.INVALID_COOLDOWN.toString().replace("%cooldown_value%", Float.toString(this.plugin.hornMaxCooldown))));
            return 1;
        }

		ItemMeta meta = item.getItemMeta();
		meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "horncooldown"), PersistentDataType.INTEGER, goatcooldown.intValue());
		item.setItemMeta(meta);

        player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.CREATE_CUSTOM_GOAT_COOLDOWN.toString().replace("%custom_goat_cooldown%", Float.toString(goatcooldown))));
                
		return 1;
	}
	
	private int onCommandConsole(ConsoleCommandSender executor, CommandArguments arguments) {
		executor.sendMessage(NamedTextColor.RED + "Only players can use this command : '"+arguments+"'!");
		return 1;
	}

    public boolean isCustomGoatHorn(ItemStack item) {
        if (item==null) return false;
        return item.getType().toString().contains("GOAT_HORN") && item.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(plugin, "customhorn"));
    }

}
