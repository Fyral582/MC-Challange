package de.Fyral.challange.challanges;

import de.Fyral.challange.Main;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class SharedHunger implements Listener {

    private boolean isActive = false;

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { this.isActive = active; }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFoodChange(FoodLevelChangeEvent event) {
        if (!isActive || !Main.getInstance().isRunning()) return;
        if (!(event.getEntity() instanceof Player player) || !isAlive(player)) return;
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
        to.setFoodLevel(from.getFoodLevel());
        to.setSaturation(from.getSaturation());
        to.setExhaustion(from.getExhaustion());
    }

    private boolean isAlive(Player player) {
        return !player.isDead() && (player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE);
    }

    public void reset() {
    }
}
