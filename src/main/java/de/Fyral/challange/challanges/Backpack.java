package de.Fyral.challange.challanges;

import de.Fyral.challange.Main;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Backpack implements Listener, InventoryHolder {

    private static final int SIZE = 27;

    private boolean isActive = false;
    private Inventory inventory;
    private final File file;

    public Backpack() {
        this.file = new File(Main.getInstance().getDataFolder(), "backpack.yml");
        this.inventory = Bukkit.createInventory(this, SIZE, Main.getInstance().getMessage("messages.backpack.title"));
        load();
    }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { this.isActive = active; }

    @Override
    public Inventory getInventory() { return inventory; }

    public void open(Player player) {
        player.openInventory(inventory);
    }

    public void load() {
        if (!file.exists()) return;
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        for (int i = 0; i < SIZE; i++) {
            inventory.setItem(i, config.getItemStack("slot." + i));
        }
    }

    public void save() {
        YamlConfiguration config = new YamlConfiguration();
        ItemStack[] contents = inventory.getContents();
        for (int i = 0; i < contents.length; i++) {
            if (contents[i] != null) config.set("slot." + i, contents[i]);
        }
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reset() {
        for (HumanEntity viewer : new ArrayList<>(inventory.getViewers())) {
            viewer.closeInventory();
        }
        inventory.clear();
        save();
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() == this) save();
    }
}
