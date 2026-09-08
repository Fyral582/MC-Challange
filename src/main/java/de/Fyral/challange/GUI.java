package de.Fyral.challange;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;
import org.bukkit.NamespacedKey;

public class GUI implements Listener {

    private final Main plugin;

    public GUI(Main plugin) {
        this.plugin = plugin;
    }

    public void openSettings(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, plugin.getMessage("messages.gui.settings_title"));
        renderItems(gui);
        player.openInventory(gui);
    }

    public void openResetConfirmation(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, plugin.getMessage("messages.gui.reset_title"));

        ItemStack confirm = new ItemStack(Material.LIME_WOOL);
        ItemMeta confirmMeta = confirm.getItemMeta();
        confirmMeta.setDisplayName(plugin.getMessage("messages.gui.reset_confirm"));
        confirm.setItemMeta(confirmMeta);

        ItemStack cancel = new ItemStack(Material.RED_WOOL);
        ItemMeta cancelMeta = cancel.getItemMeta();
        cancelMeta.setDisplayName(plugin.getMessage("messages.gui.reset_cancel"));
        cancel.setItemMeta(cancelMeta);

        gui.setItem(11, confirm);
        gui.setItem(15, cancel);
        player.openInventory(gui);
    }

    private void renderItems(Inventory inv) {
        inv.setItem(9, createItem(Material.HOPPER, plugin.getRandomCrafting().isActive() ? plugin.getMessage("messages.gui.random_crafting_active") : plugin.getMessage("messages.gui.random_crafting_inactive")));
        inv.setItem(10, createCustomItem(Material.DIRT, "block_randomizer", plugin.getBlockRandomizer().isActive() ? plugin.getMessage("messages.gui.block_randomizer_active") : plugin.getMessage("messages.gui.block_randomizer_inactive")));
        ItemStack potion = new ItemStack(Material.POTION);
        PotionMeta pm = (PotionMeta) potion.getItemMeta();
        if (pm != null) {
            pm.setBasePotionType(PotionType.STRENGTH);
            pm.setDisplayName(plugin.getRandomEffekt().isActive() ? plugin.getMessage("messages.gui.random_effect_active") : plugin.getMessage("messages.gui.random_effect_inactive"));
            potion.setItemMeta(pm);
        }
        inv.setItem(11, potion);

        inv.setItem(12, createItem(Material.ARMOR_STAND, plugin.getEntitySizeRandomizer().isActive() ? plugin.getMessage("messages.gui.random_size_active") : plugin.getMessage("messages.gui.random_size_inactive")));
        inv.setItem(13, createItem(Material.BARRIER, plugin.getChunkWipe().isActive() ? plugin.getMessage("messages.gui.chunk_wipe_active") : plugin.getMessage("messages.gui.chunk_wipe_inactive")));
        inv.setItem(14, createItem(Material.GOLDEN_APPLE, plugin.getUltraHardcore().isActive() ? plugin.getMessage("messages.gui.ultrahardcore_active") : plugin.getMessage("messages.gui.ultrahardcore_inactive")));
        inv.setItem(15, createItem(Material.ENDER_PEARL, plugin.getRandomTeleport().isActive() ? plugin.getMessage("messages.gui.random_teleport_active") : plugin.getMessage("messages.gui.random_teleport_inactive")));
        inv.setItem(16, createCustomItem(Material.CRAFTING_TABLE, "no_crafting", plugin.getNoCraftingTable().isActive() ? plugin.getMessage("messages.gui.no_crafting_active") : plugin.getMessage("messages.gui.no_crafting_inactive")));
        inv.setItem(17, createCustomItem(Material.IRON_CHESTPLATE, "no_armor", plugin.getNoArmor().isActive() ? plugin.getMessage("messages.gui.no_armor_active") : plugin.getMessage("messages.gui.no_armor_inactive")));
        inv.setItem(22, createItem(Material.NETHER_STAR, plugin.getMessage("messages.gui.challenge_start")));
    }

    private ItemStack createItem(Material mat, String name) {
        ItemStack is = new ItemStack(mat);
        ItemMeta im = is.getItemMeta();
        if (im != null) { im.setDisplayName(name); is.setItemMeta(im); }
        return is;
    }

    private ItemStack createCustomItem(Material baseMaterial, String modelId, String displayName) {
        ItemStack item = new ItemStack(baseMaterial);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(displayName);
            meta.setItemModel(new NamespacedKey("challange", modelId));
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        String settingsTitle = plugin.getMessage("messages.gui.settings_title");
        String resetTitle = plugin.getMessage("messages.gui.reset_title");

        if (!title.equals(settingsTitle) && !title.equals(resetTitle)) return;

        event.setCancelled(true);
        if (event.getCurrentItem() == null) return;
        Player player = (Player) event.getWhoClicked();

        if (title.equals(resetTitle)) {
            if (event.getSlot() == 11) {
                player.closeInventory();
                plugin.executeReset();
            } else if (event.getSlot() == 15) {
                player.closeInventory();
                player.sendMessage(plugin.getMessage("messages.commands.reset_cancelled"));
            }
            return;
        }

        if (!player.hasPermission("challange.settings")) {
            player.sendMessage(plugin.getMessage("messages.commands.no_permission"));
            player.closeInventory();
            return;
        }

        int slot = event.getSlot();
        boolean update = false;
        if (slot == 9) {
            boolean newState = !plugin.getRandomCrafting().isActive();
            plugin.getRandomCrafting().setActive(newState);
            if (newState) plugin.getRandomCrafting().generatePossibleResults();
            plugin.getConfig().set("challenges.random_crafting", newState);
            update = true;
        } else if (slot == 10) {
            boolean newState = !plugin.getBlockRandomizer().isActive();
            plugin.getBlockRandomizer().setActive(newState);
            if (newState) plugin.getBlockRandomizer().generateMappings();
            plugin.getConfig().set("challenges.block_randomizer", newState);
            update = true;
        } else if (slot == 11) {
            boolean newState = !plugin.getRandomEffekt().isActive();
            plugin.getRandomEffekt().setActive(newState);
            plugin.getConfig().set("challenges.random_effect", newState);
            update = true;
        } else if (slot == 12) {
            boolean newState = !plugin.getEntitySizeRandomizer().isActive();
            plugin.getEntitySizeRandomizer().setActive(newState);
            plugin.getConfig().set("challenges.entity_size", newState);
            update = true;
        } else if (slot == 13) {
            boolean newState = !plugin.getChunkWipe().isActive();
            plugin.getChunkWipe().setActive(newState);
            plugin.getConfig().set("challenges.chunk_wipe", newState);
            update = true;
        } else if (slot == 14) {
            boolean newState = !plugin.getUltraHardcore().isActive();
            plugin.getUltraHardcore().setActive(newState);
            plugin.getConfig().set("challenges.ultrahardcore", newState);
            update = true;
        } else if (slot == 15) {
            boolean newState = !plugin.getRandomTeleport().isActive();
            plugin.getRandomTeleport().setActive(newState);
            plugin.getConfig().set("challenges.random_teleport", newState);
            update = true;
        } else if (slot == 16) {
            boolean newState = !plugin.getNoCraftingTable().isActive();
            plugin.getNoCraftingTable().setActive(newState);
            plugin.getConfig().set("challenges.no_crafting_table", newState);
            update = true;
        } else if (slot == 17) {
            boolean newState = !plugin.getNoArmor().isActive();
            plugin.getNoArmor().setActive(newState);
            plugin.getConfig().set("challenges.no_armor", newState);
            update = true;
        } else if (slot == 22) {
            if (plugin.isRunning()) {
                player.sendMessage(plugin.getMessage("messages.commands.already_running"));
                return;
            }
            if (!plugin.getRandomEffekt().isActive() && !plugin.getRandomTeleport().isActive() && !plugin.getChunkWipe().isActive() && !plugin.getBlockRandomizer().isActive() && !plugin.getEntitySizeRandomizer().isActive() && !plugin.getUltraHardcore().isActive() && !plugin.getNoCraftingTable().isActive()) {
                player.sendMessage(plugin.getMessage("messages.commands.resume_none_active"));
                return;
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