package de.Fyral.challange.challanges;

import de.Fyral.challange.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;

public class NoCraftingTable implements Listener {

    private boolean isActive = false;

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { this.isActive = active; }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!isActive || !Main.getInstance().isRunning()) return;
        if (event.getInventory().getType() != InventoryType.WORKBENCH) return;
        if (!(event.getPlayer() instanceof Player)) return;

        event.setCancelled(true);
        Player player = (Player) event.getPlayer();
        player.sendMessage(Main.getInstance().getMessage("messages.nocrafting.blocked"));
    }

    public void reset() {
    }
}