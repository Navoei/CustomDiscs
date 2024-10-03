package me.Navoei.customdiscsplugin.command.SubCommands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.FloatArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import me.Navoei.customdiscsplugin.CustomDiscs;
import me.Navoei.customdiscsplugin.language.Lang;
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
                if (!CustomDiscs.isGoatHorn(player)) {
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

                //Sets the lore of the item to the quotes from the command.
                ItemStack disc = new ItemStack(player.getInventory().getItemInMainHand());
                ItemMeta theItemMeta = disc.getItemMeta();

                PersistentDataContainer data = theItemMeta.getPersistentDataContainer();

                var namespaceHorn = new NamespacedKey(this.plugin, "customhorn");
                String retrieveCustomHornFile = data.get(namespaceHorn, PersistentDataType.STRING);
                if (retrieveCustomHornFile == null || retrieveCustomHornFile.compareTo("null") == 0) {
                        player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.NOT_HOLDING_MODIFIED_GOATHORN.toString()));
                        return 1;
                }

                var namespaceCustomsoundrange = new NamespacedKey(this.plugin, "customsoundrange");
                Float retrieveCustomsoundrange = data.get(namespaceCustomsoundrange, PersistentDataType.FLOAT);
                
                var namespaceCustomhorncooldown = new NamespacedKey(this.plugin, "customhorncoolodwn");
                int setCustomGoatcooldown;
                if (goatcooldown == 0) {
                    setCustomGoatcooldown = 1;
                } else {
                    setCustomGoatcooldown = Math.min(Math.round(goatcooldown * 20), CustomDiscs.getInstance().hornMaxCooldownTicks);
                }

                String command = "minecraft:item modify entity "+player.getName()+" weapon.mainhand {\"function\":\"minecraft:set_components\",\"components\":{\"minecraft:custom_data\":\"{PublicBukkitValues:{\\\""+namespaceHorn+"\\\":\\\""+retrieveCustomHornFile+"\\\",\\\""+namespaceCustomsoundrange+"\\\":"+retrieveCustomsoundrange+"f,\\\""+namespaceCustomhorncooldown+"\\\":"+setCustomGoatcooldown+"}}\"}}";
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);


                player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.CREATE_CUSTOM_GOAT_COOLDOWN.toString().replace("%custom_goat_cooldown%", Float.toString(goatcooldown))));
                
		return 1;
	}
	
	private int onCommandConsole(ConsoleCommandSender executor, CommandArguments arguments) {
		executor.sendMessage(NamedTextColor.RED + "Only players can use this command : '"+arguments+"'!");
		return 1;
	}

}
