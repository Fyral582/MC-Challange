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
import org.bukkit.inventory.meta.components.CustomModelDataComponent;

import java.util.List;

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

        gui.setItem(11, createCustomItem(Material.LIME_WOOL, "reset_confirm", plugin.getMessage("messages.gui.reset_confirm")));
        gui.setItem(15, createCustomItem(Material.RED_WOOL, "reset_cancel", plugin.getMessage("messages.gui.reset_cancel")));
        player.openInventory(gui);
    }

    private void renderItems(Inventory inv) {
        inv.setItem(9, createCustomItem(Material.HOPPER, "random_crafting", plugin.getRandomCrafting().isActive() ? plugin.getMessage("messages.gui.random_crafting_active") : plugin.getMessage("messages.gui.random_crafting_inactive")));
        inv.setItem(10, createCustomItem(Material.DIRT, "block_randomizer", plugin.getBlockRandomizer().isActive() ? plugin.getMessage("messages.gui.block_randomizer_active") : plugin.getMessage("messages.gui.block_randomizer_inactive")));
        ItemStack potion = new ItemStack(Material.POTION);
        PotionMeta pm = (PotionMeta) potion.getItemMeta();
        if (pm != null) {
            pm.setBasePotionType(PotionType.STRENGTH);
            pm.setDisplayName(plugin.getRandomEffekt().isActive() ? plugin.getMessage("messages.gui.random_effect_active") : plugin.getMessage("messages.gui.random_effect_inactive"));
            setModel(pm, "random_effect");
            potion.setItemMeta(pm);
        }
        inv.setItem(11, potion);

        inv.setItem(12, createCustomItem(Material.ARMOR_STAND, "random_size", plugin.getEntitySizeRandomizer().isActive() ? plugin.getMessage("messages.gui.random_size_active") : plugin.getMessage("messages.gui.random_size_inactive")));
        inv.setItem(13, createCustomItem(Material.BARRIER, "chunk_wipe", plugin.getChunkWipe().isActive() ? plugin.getMessage("messages.gui.chunk_wipe_active") : plugin.getMessage("messages.gui.chunk_wipe_inactive")));
        inv.setItem(14, createCustomItem(Material.GOLDEN_APPLE, "ultrahardcore", plugin.getUltraHardcore().isActive() ? plugin.getMessage("messages.gui.ultrahardcore_active") : plugin.getMessage("messages.gui.ultrahardcore_inactive")));
        inv.setItem(15, createCustomItem(Material.ENDER_PEARL, "random_teleport", plugin.getRandomTeleport().isActive() ? plugin.getMessage("messages.gui.random_teleport_active") : plugin.getMessage("messages.gui.random_teleport_inactive")));
        inv.setItem(16, createCustomItem(Material.CRAFTING_TABLE, "no_crafting", plugin.getNoCraftingTable().isActive() ? plugin.getMessage("messages.gui.no_crafting_active") : plugin.getMessage("messages.gui.no_crafting_inactive")));
        inv.setItem(17, createCustomItem(Material.IRON_CHESTPLATE, "no_armor", plugin.getNoArmor().isActive() ? plugin.getMessage("messages.gui.no_armor_active") : plugin.getMessage("messages.gui.no_armor_inactive")));
        inv.setItem(4, createCustomItem(Material.BUNDLE, "backpack", plugin.getBackpack().isActive() ? plugin.getMessage("messages.gui.backpack_active") : plugin.getMessage("messages.gui.backpack_inactive")));
        inv.setItem(18, createCustomItem(Material.GLISTERING_MELON_SLICE, "shared_health", plugin.getSharedHealth().isActive() ? plugin.getMessage("messages.gui.shared_health_active") : plugin.getMessage("messages.gui.shared_health_inactive")));
        inv.setItem(19, createCustomItem(Material.COOKED_BEEF, "shared_hunger", plugin.getSharedHunger().isActive() ? plugin.getMessage("messages.gui.shared_hunger_active") : plugin.getMessage("messages.gui.shared_hunger_inactive")));
        inv.setItem(20, createCustomItem(Material.DIAMOND_CHESTPLATE, "shared_armor", plugin.getSharedArmor().isActive() ? plugin.getMessage("messages.gui.shared_armor_active") : plugin.getMessage("messages.gui.shared_armor_inactive")));
        inv.setItem(22, createCustomItem(Material.NETHER_STAR, "challenge_start", plugin.getMessage("messages.gui.challenge_start")));
    }

    private ItemStack createCustomItem(Material baseMaterial, String modelId, String displayName) {
        ItemStack item = new ItemStack(baseMaterial);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(displayName);
            setModel(meta, modelId);
            item.setItemMeta(meta);
        }
        return item;
    }

    // Das Resourcepack waehlt ueber diesen String die eigene Textur, ohne Pack bleibt das Vanilla-Item sichtbar
    private void setModel(ItemMeta meta, String modelId) {
        CustomModelDataComponent cmd = meta.getCustomModelDataComponent();
        cmd.setStrings(List.of("challange:" + modelId));
        meta.setCustomModelDataComponent(cmd);
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
        if (slot == 4) {
            boolean newState = !plugin.getBackpack().isActive();
            plugin.getBackpack().setActive(newState);
            plugin.getConfig().set("settings.backpack", newState);
            update = true;
        } else if (slot == 9) {
            boolean newState = !plugin.getRandomCrafting().isActive();
            plugin.getRandomCrafting().setActive(newState);
            if (newState) plugin.getRandomCrafting().generatePossibleResults();
            plugin.getConfig().set("challenges.random_crafting", newState);
            update = true;
        } else if (slot == 10) {
            boolean newState = !plugin.getBlockRandomizer().isActive();
            plugin.getBlockRandomizer().setActive(newState);
            if (newState) plugin.getBlockRandomizer().ensureMappings();
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
        } else if (slot == 18) {
            boolean newState = !plugin.getSharedHealth().isActive();
            plugin.getSharedHealth().setActive(newState);
            plugin.getConfig().set("challenges.shared_health", newState);
            update = true;
        } else if (slot == 19) {
            boolean newState = !plugin.getSharedHunger().isActive();
            plugin.getSharedHunger().setActive(newState);
            plugin.getConfig().set("challenges.shared_hunger", newState);
            update = true;
        } else if (slot == 20) {
            boolean newState = !plugin.getSharedArmor().isActive();
            plugin.getSharedArmor().setActive(newState);
            plugin.getConfig().set("challenges.shared_armor", newState);
            update = true;
        } else if (slot == 22) {
            if (plugin.isRunning()) {
                player.sendMessage(plugin.getMessage("messages.commands.already_running"));
                return;
            }
            if (!plugin.isAnyChallengeActive()) {
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