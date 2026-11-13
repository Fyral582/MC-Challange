package de.Fyral.challange.challanges;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import de.Fyral.challange.Main;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class SharedArmor implements Listener {

    private boolean isActive = false;

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { this.isActive = active; }

    @EventHandler
    public void onArmorChange(PlayerArmorChangeEvent event) {
        if (!isActive || !Main.getInstance().isRunning()) return;
        Player player = event.getPlayer();
        if (!isAlive(player)) return;
        Bukkit.getScheduler().runTask(Main.getInstance(), () -> syncFrom(player));
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!isActive) return;
        Player joined = event.getPlayer();
        Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
            if (!joined.isOnline()) return;
            for (Player other : Bukkit.getOnlinePlayers()) {
                if (other != joined && isAlive(other)) {
                    copy(other, joined);
                    return;
                }
            }
        }, 20L);
    }

    private void syncFrom(Player source) {
        if (!source.isOnline() || source.isDead()) return;
        for (Player other : Bukkit.getOnlinePlayers()) {
            if (other == source || !isAlive(other)) continue;
            copy(source, other);
        }
    }

    private void copy(Player from, Player to) {
        ItemStack[] armor = from.getInventory().getArmorContents();
        // Nur setzen, wenn sich etwas geändert hat, sonst würden sich die Spieler gegenseitig endlos updaten
        if (Arrays.equals(armor, to.getInventory().getArmorContents())) return;
        ItemStack[] copy = new ItemStack[armor.length];
        for (int i = 0; i < armor.length; i++) {
            copy[i] = armor[i] == null ? null : armor[i].clone();
        }
        to.getInventory().setArmorContents(copy);
    }

    private boolean isAlive(Player player) {
        return !player.isDead() && (player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE);
    }

    public void reset() {
    }
}
