package de.Fyral.challange.challanges;

import de.Fyral.challange.Main;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDispenseArmorEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class NoArmor implements Listener {

    private boolean isActive = false;

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { this.isActive = active; }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!isActive || !Main.getInstance().isRunning()) return;
        if (event.getSlotType() == InventoryType.SlotType.ARMOR) {
            event.setCancelled(true);
            return;
        }

        if (event.isShiftClick() && event.getClickedInventory() != null && event.getClickedInventory().getType() == InventoryType.PLAYER) {
            ItemStack clicked = event.getCurrentItem();
            if (clicked != null && isArmorPiece(clicked.getType())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!isActive || !Main.getInstance().isRunning()) return;

        for (int slot : event.getRawSlots()) {
            if (slot >= 5 && slot <= 8) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!isActive || !Main.getInstance().isRunning()) return;
        ItemStack inHand = event.getItem();
        if (inHand == null || !isArmorPiece(inHand.getType())) return;

        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDispenseArmor(BlockDispenseArmorEvent event) {
        if (!isActive || !Main.getInstance().isRunning()) return;
        event.setCancelled(true);
    }

    private boolean isArmorPiece(org.bukkit.Material material) {
        String name = material.name();
        return name.contains("HELMET") || name.contains("CHESTPLATE") ||
                name.contains("LEGGINGS") || name.contains("BOOTS");
    }

    public void reset() {
    }
}