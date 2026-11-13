package de.Fyral.challange.challanges;

import de.Fyral.challange.Main;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class SharedHealth implements Listener {

    private boolean isActive = false;

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { this.isActive = active; }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player) || !shouldSync(player)) return;
        Bukkit.getScheduler().runTask(Main.getInstance(), () -> syncFrom(player, true));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onRegain(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof Player player) || !shouldSync(player)) return;
        Bukkit.getScheduler().runTask(Main.getInstance(), () -> syncFrom(player, false));
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!isActive) return;
        Player joined = event.getPlayer();
        Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
            if (!joined.isOnline()) return;
            for (Player other : Bukkit.getOnlinePlayers()) {
                if (other != joined && isAlive(other)) {
                    applyHealth(joined, other.getHealth());
                    return;
                }
            }
        }, 20L);
    }

    private void syncFrom(Player source, boolean damaged) {
        if (!source.isOnline() || source.isDead()) return;
        double health = source.getHealth();
        if (health <= 0) return;
        for (Player other : Bukkit.getOnlinePlayers()) {
            if (other == source || !isAlive(other)) continue;
            if (other.getHealth() == health) continue;
            applyHealth(other, health);
            if (damaged) other.playHurtAnimation(0);
        }
    }

    private void applyHealth(Player player, double health) {
        AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
        double max = maxHealth != null ? maxHealth.getValue() : 20.0;
        player.setHealth(Math.max(0.5, Math.min(health, max)));
    }

    private boolean shouldSync(Player player) {
        return isActive && Main.getInstance().isRunning() && isAlive(player);
    }

    private boolean isAlive(Player player) {
        return !player.isDead() && (player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE);
    }

    public void reset() {
    }
}
