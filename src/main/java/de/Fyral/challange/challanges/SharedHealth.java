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
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Objects;

public class SharedHealth implements Listener {

    private boolean isActive = false;
    // Verhindert, dass unsere eigenen Effekt-Aenderungen wieder einen Sync ausloesen
    private boolean syncing = false;

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

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPotionEffect(EntityPotionEffectEvent event) {
        if (syncing || event.getModifiedType() != PotionEffectType.ABSORPTION) return;
        if (!(event.getEntity() instanceof Player player) || !shouldSync(player)) return;
        // Die Absorptions-Herzen werden erst nach dem Event gesetzt, deshalb einen Tick spaeter syncen
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
                    syncing = true;
                    try {
                        copy(other, joined, false);
                    } finally {
                        syncing = false;
                    }
                    return;
                }
            }
        }, 20L);
    }

    private void syncFrom(Player source, boolean damaged) {
        if (!source.isOnline() || source.isDead() || source.getHealth() <= 0) return;
        syncing = true;
        try {
            for (Player other : Bukkit.getOnlinePlayers()) {
                if (other == source || !isAlive(other)) continue;
                copy(source, other, damaged);
            }
        } finally {
            syncing = false;
        }
    }

    private void copy(Player from, Player to, boolean damaged) {
        boolean changed = false;

        // Ohne den Effekt begrenzt Minecraft die Absorption auf 0, deshalb zuerst den Effekt angleichen
        PotionEffect effect = from.getPotionEffect(PotionEffectType.ABSORPTION);
        PotionEffect current = to.getPotionEffect(PotionEffectType.ABSORPTION);
        if (effect == null && current != null) {
            to.removePotionEffect(PotionEffectType.ABSORPTION);
        } else if (effect != null && !Objects.equals(effect, current)) {
            to.addPotionEffect(effect);
        }

        if (to.getAbsorptionAmount() != from.getAbsorptionAmount()) {
            to.setAbsorptionAmount(from.getAbsorptionAmount());
            changed = true;
        }

        if (to.getHealth() != from.getHealth()) {
            applyHealth(to, from.getHealth());
            changed = true;
        }

        if (damaged && changed) to.playHurtAnimation(0);
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
