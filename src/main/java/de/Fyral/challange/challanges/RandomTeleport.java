package de.Fyral.challange.challanges;

import de.Fyral.challange.Main;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.Random;

public class RandomTeleport {

    private boolean isActive = false;
    private int secondsUntilNextTeleport;
    private final Random random = new Random();

    public RandomTeleport() {
        secondsUntilNextTeleport = Main.getInstance().getConfig().getInt("state.random_teleport.seconds", 0);
        if (secondsUntilNextTeleport <= 0) setRandomTime();
    }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { this.isActive = active; }

    public void tick() {
        if (!isActive || !Main.getInstance().isRunning()) return;

        secondsUntilNextTeleport--;

        if (secondsUntilNextTeleport <= 0) {
            teleportAllPlayersRandomly();
            setRandomTime();
        }
        saveState();
    }

    private void saveState() {
        Main.getInstance().getConfig().set("state.random_teleport.seconds", secondsUntilNextTeleport);
    }

    private void teleportAllPlayersRandomly() {
        String worldName = Main.getInstance().getConfig().getString("activeWorld", "world");
        World overworld = Bukkit.getWorld(worldName);
        World nether = Bukkit.getWorld(worldName + "_nether");
        if (nether == null) nether = Bukkit.getWorld("world_nether");
        World end = Bukkit.getWorld(worldName + "_the_end");
        if (end == null) end = Bukkit.getWorld("world_the_end");

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getGameMode() == GameMode.SPECTATOR) continue;

            Location safeLocation = null;

            while (safeLocation == null) {
                World targetWorld = overworld;
                int chance = random.nextInt(100);

                if (chance < 65) {
                    targetWorld = overworld;
                } else if (chance < 95) {
                    targetWorld = (nether != null) ? nether : overworld;
                } else {
                    targetWorld = (end != null) ? end : overworld;
                }

                if (targetWorld == null) targetWorld = overworld;

                int newX, newZ;
                if (targetWorld.getEnvironment() == World.Environment.THE_END) {
                    newX = random.nextInt(301) - 150;
                    newZ = random.nextInt(301) - 150;
                } else {
                    int offsetX = (1000 + random.nextInt(9001)) * (random.nextBoolean() ? 1 : -1);
                    int offsetZ = (1000 + random.nextInt(9001)) * (random.nextBoolean() ? 1 : -1);
                    newX = player.getLocation().getBlockX() + offsetX;
                    newZ = player.getLocation().getBlockZ() + offsetZ;
                }

                int groundY = -1;
                if (targetWorld.getEnvironment() == World.Environment.NETHER) {
                    groundY = findSafeNetherY(targetWorld, newX, newZ);
                } else {
                    groundY = targetWorld.getHighestBlockYAt(newX, newZ);
                    if (targetWorld.getEnvironment() == World.Environment.THE_END && groundY <= 0) groundY = -1;
                }

                if (groundY > 0) {
                    Block groundBlock = targetWorld.getBlockAt(newX, groundY, newZ);
                    if (isSafeBlock(groundBlock.getType())) {
                        safeLocation = new Location(targetWorld, newX + 0.5, groundY + 1, newZ + 0.5);
                    }
                }
            }

            player.teleport(safeLocation);

            String dimDisplay = Main.getInstance().getMessage("messages.randomteleport.world_overworld");
            if(safeLocation.getWorld().getEnvironment() == World.Environment.NETHER) dimDisplay = Main.getInstance().getMessage("messages.randomteleport.world_nether");
            if(safeLocation.getWorld().getEnvironment() == World.Environment.THE_END) dimDisplay = Main.getInstance().getMessage("messages.randomteleport.world_end");

            String msg = Main.getInstance().getMessage("messages.randomteleport.teleported")
                    .replace("%x%", String.valueOf(safeLocation.getBlockX()))
                    .replace("%y%", String.valueOf(safeLocation.getBlockY()))
                    .replace("%z%", String.valueOf(safeLocation.getBlockZ()))
                    .replace("%world%", dimDisplay);
            player.sendMessage(msg);
        }
    }

    private void setRandomTime() {
        secondsUntilNextTeleport = 300 + random.nextInt(301);
    }

    public void reset() {
        setRandomTime();
        saveState();
    }

    private boolean isSafeBlock(Material material) {
        if (!material.isSolid()) return false;
        if (material == Material.LAVA || material == Material.WATER) return false;
        if (material == Material.MAGMA_BLOCK || material == Material.CACTUS) return false;
        return true;
    }

    private int findSafeNetherY(World world, int x, int z) {
        for (int y = 115; y > 32; y--) {
            Block feet = world.getBlockAt(x, y, z);
            Block head = world.getBlockAt(x, y + 1, z);
            Block ground = world.getBlockAt(x, y - 1, z);
            if (feet.getType().isAir() && head.getType().isAir() && isSafeBlock(ground.getType())) {
                return y - 1;
            }
        }
        return -1;
    }
}