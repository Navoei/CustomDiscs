package me.Navoei.customdiscsplugin.command.SubCommands;

import me.Navoei.customdiscsplugin.CustomDiscs;
import me.Navoei.customdiscsplugin.utils.TypeChecker;
import me.Navoei.customdiscsplugin.language.Lang;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.executors.CommandArguments;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.TooltipDisplay;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import org.bukkit.MusicInstrument;
import org.bukkit.NamespacedKey;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import com.destroystokyo.paper.profile.PlayerProfile;

public class RevertSubCommand extends CommandAPICommand {
	private final CustomDiscs plugin;

	public RevertSubCommand(CustomDiscs plugin) {
		super("revert");
		this.plugin = plugin;

		this.withFullDescription(NamedTextColor.GRAY + "Reverts a custom item back to its original state.");
		this.withUsage("/customdisc revert");
		this.withPermission("customdiscs.revert");

		this.executesPlayer(this::onCommandPlayer);
		this.executesConsole(this::onCommandConsole);
	}

	private int onCommandPlayer(Player player, CommandArguments arguments) {
		ItemStack heldItem = player.getInventory().getItemInMainHand();

		if (TypeChecker.isCustomMusicDisc(heldItem)) {
			revertDisc(player, heldItem);
			return 1;
		}

		if (TypeChecker.isCustomGoatHornPlayer(player)) {
			revertHorn(player, heldItem);
			return 1;
		}

		if (TypeChecker.isCustomHeadPlayer(player)) {
			revertHead(player, heldItem);
			return 1;
		}

		player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX.forPlayer(player) + Lang.REVERT_NOT_CUSTOM.forPlayer(player)));
		return 0;
	}

	private void revertDisc(Player player, ItemStack heldItem) {
		ItemStack freshItem = new ItemStack(heldItem.getType(), heldItem.getAmount());
		freshItem.setData(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplay.tooltipDisplay().build());
		player.getInventory().setItemInMainHand(freshItem);

		player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX.forPlayer(player) + Lang.REVERT_SUCCESS.forPlayer(player)));
	}

	private void revertHorn(Player player, ItemStack heldItem) {
		ItemMeta itemMeta = heldItem.getItemMeta();
		PersistentDataContainer persistentDataContainer = itemMeta.getPersistentDataContainer();
		String originalInstrumentKey = persistentDataContainer.get(new NamespacedKey(plugin, "originalinstrument"), PersistentDataType.STRING);
		if (originalInstrumentKey == null || !isValidInstrumentKey(originalInstrumentKey)) {
			originalInstrumentKey = plugin.defaultHornInstrument;
		}

		ItemStack freshItem = new ItemStack(heldItem.getType(), heldItem.getAmount());
		MusicInstrument originalInstrument = RegistryAccess.registryAccess().getRegistry(RegistryKey.INSTRUMENT).get(NamespacedKey.fromString(originalInstrumentKey));
		if (originalInstrument != null) {
			freshItem.setData(DataComponentTypes.INSTRUMENT, originalInstrument);
		}
		player.getInventory().setItemInMainHand(freshItem);

		player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX.forPlayer(player) + Lang.REVERT_SUCCESS.forPlayer(player)));
	}

	private void revertHead(Player player, ItemStack heldItem) {
		ItemStack freshItem = new ItemStack(heldItem.getType(), heldItem.getAmount());
		if (heldItem.getItemMeta() instanceof SkullMeta originalSkullMeta) {
			SkullMeta freshSkullMeta = (SkullMeta) freshItem.getItemMeta();
			PlayerProfile playerProfile = originalSkullMeta.getPlayerProfile();
			if (playerProfile != null) {
				freshSkullMeta.setPlayerProfile(playerProfile);
			}
			freshItem.setItemMeta(freshSkullMeta);
		}
		player.getInventory().setItemInMainHand(freshItem);

		player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX.forPlayer(player) + Lang.REVERT_SUCCESS.forPlayer(player)));
	}

	private boolean isValidInstrumentKey(String instrumentKey) {
		String instrumentName = instrumentKey.startsWith("minecraft:") ? instrumentKey.substring("minecraft:".length()) : instrumentKey;
		return CustomDiscs.VALID_HORN_INSTRUMENTS.contains(instrumentName);
	}

	private int onCommandConsole(ConsoleCommandSender executor, CommandArguments arguments) {
		executor.sendMessage(NamedTextColor.RED + "Only players can use this command : '" + arguments + "'!");
		return 1;
	}
}
