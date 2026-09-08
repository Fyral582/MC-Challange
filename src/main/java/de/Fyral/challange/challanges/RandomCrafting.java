package de.Fyral.challange.challanges;

import de.Fyral.challange.Main;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomCrafting implements Listener {

    private boolean isActive = false;
    private final Random random = new Random();
    private final List<Material> possibleResults = new ArrayList<>();

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { this.isActive = active; }

    public void generatePossibleResults() {
        possibleResults.clear();

        for (Material mat : Material.values()) {
            if (mat == Material.AIR || mat.isLegacy() || mat.name().endsWith("_AIR")) continue;
            if (!mat.isItem()) continue;
            possibleResults.add(mat);
        }
    }

    @EventHandler
    public void onCraftItem(CraftItemEvent event) {
        if (!isActive || !Main.getInstance().isRunning()) return;
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (possibleResults.isEmpty()) generatePossibleResults();

        ItemStack currentResult = event.getCurrentItem();
        if (currentResult == null || currentResult.getType() == Material.AIR) return;

        int amount = currentResult.getAmount();
        Player player = (Player) event.getWhoClicked();

        Material firstMaterial = possibleResults.get(random.nextInt(possibleResults.size()));
        event.setCurrentItem(new ItemStack(firstMaterial, 1));

        for (int i = 1; i < amount; i++) {
            Material randomMaterial = possibleResults.get(random.nextInt(possibleResults.size()));
            player.getInventory().addItem(new ItemStack(randomMaterial, 1));
        }
    }

    public void reset() {
        possibleResults.clear();
        if (isActive) generatePossibleResults();
    }
}