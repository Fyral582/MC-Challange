package de.Fyral.challange.challanges;

import de.Fyral.challange.Main;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.Random;

public class EntitySizeRandomizer implements Listener {

    private static final double MIN_SCALE = 0.1;
    private static final double MAX_SCALE = 10.0;

    private boolean isActive = false;
    private int secondsUntilNextResize;
    private final Random random = new Random();

    private double currentScale = 1.0;

    public EntitySizeRandomizer() {
        setRandomTime();
    }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { this.isActive = active; }

    public void tick() {
        if (!isActive || !Main.getInstance().isRunning()) return;

        secondsUntilNextResize--;

        if (secondsUntilNextResize <= 0) {
            applyRandomSizes();
            setRandomTime();
        }
    }

    private void applyRandomSizes() {
        currentScale = MIN_SCALE + (random.nextDouble() * (MAX_SCALE - MIN_SCALE));

        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (!(entity instanceof LivingEntity) || entity instanceof Player) continue;

                LivingEntity living = (LivingEntity) entity;
                AttributeInstance scaleAttribute = living.getAttribute(Attribute.SCALE);
                if (scaleAttribute == null) continue;

                scaleAttribute.setBaseValue(currentScale);
            }
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage("§b§lCHALLENGE §7-> §fAll creatures have been randomly scaled!");
        }
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (!isActive || !Main.getInstance().isRunning()) return;

        LivingEntity living = event.getEntity();
        if (living instanceof Player) return;

        AttributeInstance scaleAttribute = living.getAttribute(Attribute.SCALE);
        if (scaleAttribute != null) scaleAttribute.setBaseValue(currentScale);
    }

    private void setRandomTime() {
        secondsUntilNextResize = 180 + random.nextInt(121);
    }

    public void reset() {
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (!(entity instanceof LivingEntity) || entity instanceof Player) continue;

                LivingEntity living = (LivingEntity) entity;
                AttributeInstance scaleAttribute = living.getAttribute(Attribute.SCALE);
                if (scaleAttribute != null) scaleAttribute.setBaseValue(1.0);
            }
        }
        currentScale = 1.0;
        setRandomTime();
    }
}