package de.Fyral.challange.challanges;

import de.Fyral.challange.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class RandomEffekt implements Listener {

    private boolean isActive = false;
    private int secondsUntilNextEffect;
    private final Random random = new Random();
    private List<String> activeEffectsList;

    public RandomEffekt() {
        activeEffectsList = Main.getInstance().getDatabase().getEffects();
        setRandomTime();
    }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { this.isActive = active; }

    public void tick() {
        if (!isActive || !Main.getInstance().isRunning()) return;

        secondsUntilNextEffect--;

        if (secondsUntilNextEffect <= 0) {
            applyRandomEffect();
            setRandomTime();
        }
    }

    private void applyRandomEffect() {
        PotionEffectType[] allEffects = PotionEffectType.values();
        PotionEffectType picked = allEffects[random.nextInt(allEffects.length)];

        if (picked == null || picked.getName() == null) {
            applyRandomEffect();
            return;
        }

        activeEffectsList.add(picked.getName());
        Main.getInstance().getDatabase().addEffect(picked.getName());

        int count = Collections.frequency(activeEffectsList, picked.getName());
        int amplifier = count - 1;

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.addPotionEffect(new PotionEffect(picked, PotionEffect.INFINITE_DURATION, amplifier));

            String effectName = getFriendlyName(picked);
            String msg = Main.getInstance().getMessage("messages.randomeffekt.received")
                    .replace("%effect%", effectName)
                    .replace("%count%", String.valueOf(count));
            player.sendMessage(msg);
        }
    }

    public void giveAllEffectsToPlayer(Player player) {
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }

        Set<String> uniqueEffects = new HashSet<>(activeEffectsList);
        for (String effectName : uniqueEffects) {
            PotionEffectType type = PotionEffectType.getByName(effectName);
            if (type != null) {
                int count = Collections.frequency(activeEffectsList, effectName);
                player.addPotionEffect(new PotionEffect(type, PotionEffect.INFINITE_DURATION, count - 1));
            }
        }
    }

    private String getFriendlyName(PotionEffectType type) {
        String name = type.getName();
        switch (name) {
            case "FAST_DIGGING": return "Haste";
            case "SLOW_DIGGING": return "Mining Fatigue";
            case "INCREASE_DAMAGE": return "Strength";
            case "HEAL": return "Instant Health";
            case "HARM": return "Instant Damage";
            case "JUMP": return "Jump Boost";
            case "CONFUSION": return "Nausea";
            case "DAMAGE_RESISTANCE": return "Resistance";
            case "SLOW": return "Slowness";
            default:
                String raw = name.toLowerCase().replace("_", " ");
                String formatted = "";
                for(String word : raw.split(" ")) {
                    formatted += word.substring(0, 1).toUpperCase() + word.substring(1) + " ";
                }
                return formatted.trim();
        }
    }

    private void setRandomTime() {
        secondsUntilNextEffect = 120 + random.nextInt(181);
    }

    public void resetEffekte() {
        activeEffectsList.clear();
        Main.getInstance().getDatabase().clearEffects();

        for (Player player : Bukkit.getOnlinePlayers()) {
            for (PotionEffect effect : player.getActivePotionEffects()) {
                player.removePotionEffect(effect.getType());
            }
        }
        setRandomTime();
    }

    @EventHandler
    public void onPlayerConsume(PlayerItemConsumeEvent event) {
        if (isActive && Main.getInstance().isRunning()) {
            if (event.getItem().getType() == Material.MILK_BUCKET) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(Main.getInstance().getMessage("messages.randomeffekt.no_milk"));
            }
        }
    }
}