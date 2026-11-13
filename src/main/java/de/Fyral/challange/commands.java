package de.Fyral.challange;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class commands implements CommandExecutor {

    private final Main plugin;
    private final GUI gui;

    public commands(Main plugin, GUI gui) {
        this.plugin = plugin;
        this.gui = gui;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;

        if (command.getName().equalsIgnoreCase("settings")) {
            if (!player.hasPermission("challange.settings")) {
                player.sendMessage(plugin.getMessage("messages.commands.no_permission"));
                return true;
            }
            if (plugin.isRunning()) {
                player.sendMessage(plugin.getMessage("messages.commands.settings_running"));
                return true;
            }
            gui.openSettings(player);
            return true;
        }

        if (command.getName().equalsIgnoreCase("pause")) {
            if (!player.hasPermission("challange.pause")) {
                player.sendMessage(plugin.getMessage("messages.commands.no_permission"));
                return true;
            }
            if (!plugin.isRunning()) {
                player.sendMessage(plugin.getMessage("messages.commands.pause_already_paused"));
                return true;
            }
            plugin.pauseTimer();
            Bukkit.broadcastMessage(plugin.getMessage("messages.commands.pause_success"));
            return true;
        }

        if (command.getName().equalsIgnoreCase("resume")) {
            if (!player.hasPermission("challange.resume")) {
                player.sendMessage(plugin.getMessage("messages.commands.no_permission"));
                return true;
            }
            if (plugin.isRunning()) {
                player.sendMessage(plugin.getMessage("messages.commands.resume_already_running"));
                return true;
            }

            if (!plugin.isAnyChallengeActive()) {
                player.sendMessage(plugin.getMessage("messages.commands.resume_none_active"));
                return true;
            }

            plugin.startTimer();
            Bukkit.broadcastMessage(plugin.getMessage("messages.commands.resume_success"));
            return true;
        }

        if (command.getName().equalsIgnoreCase("reset")) {
            if (!player.hasPermission("challange.reset")) {
                player.sendMessage(plugin.getMessage("messages.commands.no_permission"));
                return true;
            }
            gui.openResetConfirmation(player);
            return true;
        }

        if (command.getName().equalsIgnoreCase("bp")) {
            if (!plugin.getBackpack().isActive()) {
                player.sendMessage(plugin.getMessage("messages.backpack.disabled"));
                return true;
            }
            plugin.getBackpack().open(player);
            return true;
        }

        if (command.getName().equalsIgnoreCase("activechallanges")) {
            if (!sender.hasPermission("challange.activechallanges")) {
                sender.sendMessage(plugin.getMessage("messages.commands.no_permission"));
                return true;
            }

            sender.sendMessage(plugin.getMessage("messages.activechallanges.header"));
            sender.sendMessage(plugin.getMessage("messages.activechallanges.block_randomizer") + " " + (plugin.getBlockRandomizer().isActive() ? plugin.getMessage("messages.activechallanges.active") : plugin.getMessage("messages.activechallanges.inactive")));
            sender.sendMessage(plugin.getMessage("messages.activechallanges.random_effect") + " " + (plugin.getRandomEffekt().isActive() ? plugin.getMessage("messages.activechallanges.active") : plugin.getMessage("messages.activechallanges.inactive")));
            sender.sendMessage(plugin.getMessage("messages.activechallanges.entity_size") + " " + (plugin.getEntitySizeRandomizer().isActive() ? plugin.getMessage("messages.activechallanges.active") : plugin.getMessage("messages.activechallanges.inactive")));
            sender.sendMessage(plugin.getMessage("messages.activechallanges.chunk_wipe") + " " + (plugin.getChunkWipe().isActive() ? plugin.getMessage("messages.activechallanges.active") : plugin.getMessage("messages.activechallanges.inactive")));
            sender.sendMessage(plugin.getMessage("messages.activechallanges.random_teleport") + " " + (plugin.getRandomTeleport().isActive() ? plugin.getMessage("messages.activechallanges.active") : plugin.getMessage("messages.activechallanges.inactive")));
            sender.sendMessage(plugin.getMessage("messages.activechallanges.ultra_hardcore") + " " + (plugin.getUltraHardcore().isActive() ? plugin.getMessage("messages.activechallanges.active") : plugin.getMessage("messages.activechallanges.inactive")));
            sender.sendMessage(plugin.getMessage("messages.activechallanges.no_crafting") + " " + (plugin.getNoCraftingTable().isActive() ? plugin.getMessage("messages.activechallanges.active") : plugin.getMessage("messages.activechallanges.inactive")));
            sender.sendMessage(plugin.getMessage("messages.activechallanges.no_armor") + " " + (plugin.getNoArmor().isActive() ? plugin.getMessage("messages.activechallanges.active") : plugin.getMessage("messages.activechallanges.inactive")));
            sender.sendMessage(plugin.getMessage("messages.activechallanges.random_crafting") + " " + (plugin.getRandomCrafting().isActive() ? plugin.getMessage("messages.activechallanges.active") : plugin.getMessage("messages.activechallanges.inactive")));
            sender.sendMessage(plugin.getMessage("messages.activechallanges.shared_health") + " " + (plugin.getSharedHealth().isActive() ? plugin.getMessage("messages.activechallanges.active") : plugin.getMessage("messages.activechallanges.inactive")));
            sender.sendMessage(plugin.getMessage("messages.activechallanges.shared_hunger") + " " + (plugin.getSharedHunger().isActive() ? plugin.getMessage("messages.activechallanges.active") : plugin.getMessage("messages.activechallanges.inactive")));
            sender.sendMessage(plugin.getMessage("messages.activechallanges.shared_armor") + " " + (plugin.getSharedArmor().isActive() ? plugin.getMessage("messages.activechallanges.active") : plugin.getMessage("messages.activechallanges.inactive")));
            sender.sendMessage(plugin.getMessage("messages.activechallanges.backpack") + " " + (plugin.getBackpack().isActive() ? plugin.getMessage("messages.activechallanges.active") : plugin.getMessage("messages.activechallanges.inactive")));
            return true;
        }

        return false;
    }
}