package de.Fyral.challange;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class commands implements CommandExecutor {

    private final Main plugin;
    private final GUI gui;
    private final String noPerm = "§cYou do not have permission for this!";

    public commands(Main plugin, GUI gui) {
        this.plugin = plugin;
        this.gui = gui;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;

        if (command.getName().equalsIgnoreCase("settings")) {
            if (!player.hasPermission("challange.settings")) { player.sendMessage(noPerm); return true; }
            if (plugin.isRunning()) {
                player.sendMessage("§cYou cannot open the settings while a challenge is running!");
                return true;
            }
            gui.openSettings(player);
            return true;
        }

        if (command.getName().equalsIgnoreCase("pause")) {
            if (!player.hasPermission("challange.pause")) { player.sendMessage(noPerm); return true; }
            if (!plugin.isRunning()) {
                player.sendMessage("§cThe challenge is already paused!");
                return true;
            }
            plugin.pauseTimer();
            Bukkit.broadcastMessage("§eThe challenge has been paused!");
            return true;
        }

        if (command.getName().equalsIgnoreCase("resume")) {
            if (!player.hasPermission("challange.resume")) { player.sendMessage(noPerm); return true; }
            if (plugin.isRunning()) {
                player.sendMessage("§cThe challenge is already running!");
                return true;
            }

            boolean anyActive = plugin.getRandomEffekt().isActive() || plugin.getRandomTeleport().isActive() ||
                    plugin.getChunkWipe().isActive() || plugin.getBlockRandomizer().isActive() ||
                    plugin.getEntitySizeRandomizer().isActive();

            if (!anyActive) {
                player.sendMessage("§cNo challenge is activated! Open /settings first.");
                return true;
            }

            plugin.startTimer();
            Bukkit.broadcastMessage("§aThe challenge has been started/resumed!");
            return true;
        }

        if (command.getName().equalsIgnoreCase("reset")) {
            if (!player.hasPermission("challange.reset")) { player.sendMessage(noPerm); return true; }
            Bukkit.broadcastMessage("§4RESET §7Creating new world...");

            plugin.resetTimer();
            plugin.getRandomEffekt().resetEffekte();
            plugin.getRandomTeleport().reset();
            plugin.getChunkWipe().reset();
            plugin.getEntitySizeRandomizer().reset();

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                String rawName = "challenge_" + System.currentTimeMillis();
                String worldPath = "worlds/" + rawName;

                WorldCreator creator = new WorldCreator(worldPath);
                World newWorld = Bukkit.createWorld(creator);

                plugin.getConfig().set("activeWorld", rawName);
                plugin.saveConfig();

                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.getInventory().clear();
                    p.getInventory().setArmorContents(null);
                    p.setHealth(20.0);
                    p.setFoodLevel(20);
                    p.setSaturation(20.0f);
                    p.setExp(0f);
                    p.setLevel(0);
                    p.setGameMode(GameMode.SURVIVAL);
                    if (newWorld != null) p.teleport(newWorld.getSpawnLocation());
                }
                Bukkit.broadcastMessage("§aThe new world has been successfully generated!");
            }, 60L);
            return true;
        }

        return false;
    }
}