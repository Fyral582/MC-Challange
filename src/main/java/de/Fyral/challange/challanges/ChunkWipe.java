package de.Fyral.challange.challanges;

import de.Fyral.challange.Main;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.World;

import java.util.HashMap;
import java.util.Map;

public class ChunkWipe {

    private boolean isActive = false;
    private final Map<String, Integer> chunkTimers = new HashMap<>();
    private final Map<String, BossBar> chunkBars = new HashMap<>();

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { this.isActive = active; }

    public void tick() {
        if (!isActive || !Main.getInstance().isRunning()) return;

        for (Player player : Bukkit.getOnlinePlayers()) {
            Chunk chunk = player.getLocation().getChunk();
            String chunkKey = getChunkKey(chunk);

            if (!chunkTimers.containsKey(chunkKey)) {
                startChunkTimer(chunk, chunkKey);
            }

            updateBossBarVisibility(player, chunkKey);
        }

        updateTimers();
    }

    private void startChunkTimer(Chunk chunk, String key) {
        chunkTimers.put(key, 60);
        String title = Main.getInstance().getMessage("messages.chunkwipe.bossbar_start");
        BossBar bar = Bukkit.createBossBar(title, BarColor.RED, BarStyle.SOLID);
        chunkBars.put(key, bar);
    }

    private void updateTimers() {
        for (String key : new HashMap<>(chunkTimers).keySet()) {
            int timeLeft = chunkTimers.get(key) - 1;

            if (timeLeft <= 0) {
                deleteChunk(key);
                removeChunk(key);
            } else {
                chunkTimers.put(key, timeLeft);
                BossBar bar = chunkBars.get(key);
                if (bar != null) {
                    bar.setProgress(timeLeft / 60.0);
                    String title = Main.getInstance().getMessage("messages.chunkwipe.bossbar_timer")
                            .replace("%time%", String.valueOf(timeLeft));
                    bar.setTitle(title);
                }
            }
        }
    }

    private void updateBossBarVisibility(Player player, String currentChunkKey) {
        for (Map.Entry<String, BossBar> entry : chunkBars.entrySet()) {
            if (entry.getKey().equals(currentChunkKey)) {
                entry.getValue().addPlayer(player);
            } else {
                entry.getValue().removePlayer(player);
            }
        }
    }

    private void deleteChunk(String key) {
        String[] parts = key.split(",");
        String worldName = parts[0];
        int cx = Integer.parseInt(parts[1]);
        int cz = Integer.parseInt(parts[2]);

        World world = Bukkit.getWorld(worldName);
        if (world == null) return;
        Chunk chunk = world.getChunkAt(cx, cz);

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = world.getMinHeight(); y < world.getMaxHeight(); y++) {
                    chunk.getBlock(x, y, z).setType(Material.AIR);
                }
            }
        }
    }

    private void removeChunk(String key) {
        chunkTimers.remove(key);
        BossBar bar = chunkBars.remove(key);
        if (bar != null) bar.removeAll();
    }

    public void reset() {
        for (BossBar bar : chunkBars.values()) bar.removeAll();
        chunkBars.clear();
        chunkTimers.clear();
    }

    private String getChunkKey(Chunk chunk) {
        return chunk.getWorld().getName() + "," + chunk.getX() + "," + chunk.getZ();
    }
}