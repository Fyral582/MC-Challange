package de.Fyral.challange;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

public class GUI implements Listener {

    private final Main plugin;
    private final String noPerm = "§cYou do not have permission for this!";

    public GUI(Main plugin) {
        this.plugin = plugin;
    }

    public void openSettings(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, "§8Challenge Settings");
        renderItems(gui);
        player.openInventory(gui);
    }

    private void renderItems(Inventory inv) {
        inv.setItem(10, createItem(Material.CRAFTING_TABLE, plugin.getBlockRandomizer().isActive() ? "§aBlock Randomizer: ON" : "§cBlock Randomizer: OFF"));
        ItemStack potion = new ItemStack(Material.POTION);
        PotionMeta pm = (PotionMeta) potion.getItemMeta();
        if (pm != null) {
            pm.setBasePotionType(PotionType.STRENGTH);
            pm.setDisplayName(plugin.getRandomEffekt().isActive() ? "§aRandom Effect: ON" : "§cRandom Effect: OFF");
            potion.setItemMeta(pm);
        }
        inv.setItem(11, potion);
        inv.setItem(12, createItem(Material.ARMOR_STAND, plugin.getEntitySizeRandomizer().isActive() ? "§aRandom Size: ON" : "§cRandom Size: OFF"));
        inv.setItem(13, createItem(Material.BARRIER, plugin.getChunkWipe().isActive() ? "§aChunk Wipe: ON" : "§cChunk Wipe: OFF"));
        inv.setItem(15, createItem(Material.ENDER_PEARL, plugin.getRandomTeleport().isActive() ? "§aRandom Teleport: ON" : "§cRandom Teleport: OFF"));
        inv.setItem(22, createItem(Material.NETHER_STAR, "§a§lStart Challenge"));
    }

    private ItemStack createItem(Material mat, String name) {
        ItemStack is = new ItemStack(mat);
        ItemMeta im = is.getItemMeta();
        if (im != null) { im.setDisplayName(name); is.setItemMeta(im); }
        return is;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("§8Challenge Settings")) return;
        event.setCancelled(true);
        if (event.getCurrentItem() == null) return;
        Player player = (Player) event.getWhoClicked();
        if (!player.hasPermission("challange.settings")) { player.sendMessage(noPerm); player.closeInventory(); return; }

        int slot = event.getSlot();
        boolean update = false;

        if (slot == 10) {
            plugin.getBlockRandomizer().setActive(!plugin.getBlockRandomizer().isActive());
            if (plugin.getBlockRandomizer().isActive()) plugin.getBlockRandomizer().generateMappings();
            update = true;
        } else if (slot == 11) {
            plugin.getRandomEffekt().setActive(!plugin.getRandomEffekt().isActive());
            update = true;
        } else if (slot == 12) {
            plugin.getEntitySizeRandomizer().setActive(!plugin.getEntitySizeRandomizer().isActive());
            update = true;
        } else if (slot == 13) {
            plugin.getChunkWipe().setActive(!plugin.getChunkWipe().isActive());
            update = true;
        } else if (slot == 15) {
            plugin.getRandomTeleport().setActive(!plugin.getRandomTeleport().isActive());
            update = true;
        } else if (slot == 22) {
            if (plugin.isRunning()) { player.sendMessage("§cAlready running!"); return; }
            if (!plugin.getRandomEffekt().isActive() && !plugin.getRandomTeleport().isActive() && !plugin.getChunkWipe().isActive() && !plugin.getBlockRandomizer().isActive() && !plugin.getEntitySizeRandomizer().isActive()) {
                player.sendMessage("§cNo challenge active!"); return;
            }
            player.closeInventory();
            plugin.startTimer();
            return;
        }

        if (update) {
            plugin.saveConfig();
            renderItems(event.getInventory());
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
        }
    }
}