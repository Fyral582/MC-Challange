package de.Fyral.challange.challanges;

import de.Fyral.challange.Main;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class UltraHardcore {

    private boolean isActive = false;

    public boolean isActive() { return isActive; }

    @SuppressWarnings("removal")
    public void setActive(boolean active) {
        this.isActive = active;

        for (World world : Bukkit.getWorlds()) {
            world.setGameRule(GameRule.NATURAL_REGENERATION, !active);
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            updatePlayerHearts(player);
        }
    }

    public void updatePlayerHearts(Player player) {
        try {
            player.setHealthScaled(isActive);
        } catch (Exception e) {
        }
    }

    public ItemStack getGuiIcon() {
        ItemStack item = new ItemStack(Material.GOLDEN_APPLE);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(Main.getInstance().getMessage("challenges.ultrahardcore.name"));

            String statusStr = isActive ?
                    Main.getInstance().getMessage("challenges.ultrahardcore.status_active") :
                    Main.getInstance().getMessage("challenges.ultrahardcore.status_inactive");

            List<String> rawLore = Main.getInstance().getMessageList("challenges.ultrahardcore.lore");
            List<String> finalLore = new ArrayList<>();

            for (String line : rawLore) {
                finalLore.add(line.replace("%status%", statusStr));
            }

            meta.setLore(finalLore);
            item.setItemMeta(meta);
        }

        return item;
    }

    public void reset() {
        setActive(false);
    }
}