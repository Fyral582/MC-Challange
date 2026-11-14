package de.Fyral.challange.challanges;

import de.Fyral.challange.Main;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockRandomizer implements Listener {

    private boolean isActive = false;
    private final Map<Material, Material> blockMap = new HashMap<>();
    private final File file = new File(Main.getInstance().getDataFolder(), "block_randomizer.yml");

    public BlockRandomizer() {
        load();
    }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { this.isActive = active; }

    public void generateMappings() {
        blockMap.clear();

        List<Material> breakableBlocks = new ArrayList<>();
        List<Material> dropableItems = new ArrayList<>();

        for (Material mat : Material.values()) {
            if (mat == Material.AIR || mat.name().contains("LEGACY") || mat.name().endsWith("_AIR")) {
                continue;
            }

            if (mat.isBlock()) {
                breakableBlocks.add(mat);
            }

            if (mat.isItem()) {
                dropableItems.add(mat);
            }
        }

        Collections.shuffle(dropableItems);

        for (int i = 0; i < breakableBlocks.size(); i++) {
            Material block = breakableBlocks.get(i);
            Material randomDrop = dropableItems.get(i % dropableItems.size());
            blockMap.put(block, randomDrop);
        }
        save();
    }

    // Behaelt die Zuordnung bis zum naechsten Reset, auch ueber Neustarts und An/Aus in den Settings
    public void ensureMappings() {
        if (blockMap.isEmpty()) generateMappings();
    }

    private void load() {
        if (!file.exists()) return;
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        for (String key : config.getKeys(false)) {
            Material block = Material.matchMaterial(key);
            Material drop = Material.matchMaterial(config.getString(key, ""));
            if (block != null && drop != null) blockMap.put(block, drop);
        }
    }

    private void save() {
        YamlConfiguration config = new YamlConfiguration();
        for (Map.Entry<Material, Material> entry : blockMap.entrySet()) {
            config.set(entry.getKey().name(), entry.getValue().name());
        }
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!isActive) return;
        if (!Main.getInstance().isRunning()) return;

        Material original = event.getBlock().getType();

        if (blockMap.containsKey(original)) {
            event.setDropItems(false);
            Material dropMaterial = blockMap.get(original);

            event.getBlock().getWorld().dropItemNaturally(
                    event.getBlock().getLocation(),
                    new ItemStack(dropMaterial)
            );
        }
    }

    public void reset() {
        blockMap.clear();
        save();
        if (isActive) generateMappings();
    }
}