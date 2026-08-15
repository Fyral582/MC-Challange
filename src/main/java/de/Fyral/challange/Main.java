package de.Fyral.challange;

import de.Fyral.challange.challanges.RandomEffekt;
import de.Fyral.challange.challanges.RandomTeleport;
import de.Fyral.challange.challanges.ChunkWipe;
import de.Fyral.challange.challanges.BlockRandomizer;
import de.Fyral.challange.challanges.EntitySizeRandomizer;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.Iterator;

public class Main extends JavaPlugin implements Listener {

    private static Main instance;
    private RandomEffekt randomEffekt;
    private EntitySizeRandomizer entitySizeRandomizer;
    private RandomTeleport randomTeleport;
    private ChunkWipe chunkWipe;
    private BlockRandomizer blockRandomizer;

    private boolean isRunning = false;
    private int timeElapsed = 0;
    private BukkitTask timerTask;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        String activeWorldName = getConfig().getString("activeWorld", "world");
        if (!activeWorldName.equals("world")) {
            new WorldCreator("worlds/" + activeWorldName).createWorld();
        }

        this.randomEffekt = new RandomEffekt();
        this.randomTeleport = new RandomTeleport();
        this.entitySizeRandomizer = new EntitySizeRandomizer();
        this.chunkWipe = new ChunkWipe();
        this.blockRandomizer = new BlockRandomizer();

        randomEffekt.setActive(getConfig().getBoolean("challenges.randomeffect", false));
        randomTeleport.setActive(getConfig().getBoolean("challenges.randomteleport", false));
        entitySizeRandomizer.setActive(getConfig().getBoolean("challenges.entitysize", false));
        chunkWipe.setActive(getConfig().getBoolean("challenges.chunkwipe", false));
        blockRandomizer.setActive(getConfig().getBoolean("challenges.randomizer", false));
        timeElapsed = getConfig().getInt("timer.time", 0);

        randomEffekt.loadFromConfig();

        if (blockRandomizer.isActive()) blockRandomizer.generateMappings();

        GUI guiClass = new GUI(this);
        getServer().getPluginManager().registerEvents(guiClass, this);
        getServer().getPluginManager().registerEvents(blockRandomizer, this);
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(entitySizeRandomizer, this);

        commands cmdClass = new commands(this, guiClass);
        getCommand("settings").setExecutor(cmdClass);
        getCommand("pause").setExecutor(cmdClass);
        getCommand("resume").setExecutor(cmdClass);
        getCommand("reset").setExecutor(cmdClass);

        if (getConfig().getBoolean("timer.running", false)) {
            startTimer();
        } else {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tick freeze");
        }
    }

    @Override
    public void onDisable() {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tick unfreeze");
        getConfig().set("challenges.randomeffect", randomEffekt.isActive());
        getConfig().set("challenges.randomteleport", randomTeleport.isActive());
        getConfig().set("challenges.entitysize", entitySizeRandomizer.isActive());
        getConfig().set("challenges.chunkwipe", chunkWipe.isActive());
        getConfig().set("challenges.randomizer", blockRandomizer.isActive());
        getConfig().set("timer.time", timeElapsed);
        getConfig().set("timer.running", isRunning);
        saveConfig();
        if (timerTask != null) timerTask.cancel();
        if (chunkWipe != null) chunkWipe.reset();
    }

    public static Main getInstance() { return instance; }
    public RandomEffekt getRandomEffekt() { return randomEffekt; }
    public EntitySizeRandomizer getEntitySizeRandomizer() { return entitySizeRandomizer; }
    public RandomTeleport getRandomTeleport() { return randomTeleport; }
    public ChunkWipe getChunkWipe() { return chunkWipe; }
    public BlockRandomizer getBlockRandomizer() { return blockRandomizer; }
    public boolean isRunning() { return isRunning; }

    private void clearAdvancements(Player player) {
        Iterator<Advancement> iterator = Bukkit.advancementIterator();
        while (iterator.hasNext()) {
            Advancement advancement = iterator.next();
            AdvancementProgress progress = player.getAdvancementProgress(advancement);
            for (String criteria : progress.getAwardedCriteria()) {
                progress.revokeCriteria(criteria);
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String activeWorldName = getConfig().getString("activeWorld", "world");
        String path = activeWorldName.equals("world") ? "world" : "worlds/" + activeWorldName;
        World activeWorld = Bukkit.getWorld(path);

        Bukkit.getScheduler().runTaskLater(this, () -> {

            if (activeWorld != null && !player.getWorld().getName().equals(activeWorld.getName())) {
                player.teleport(activeWorld.getSpawnLocation());
            }


            player.setGameMode(GameMode.SURVIVAL);


            if (randomEffekt.isActive()) {
                randomEffekt.giveAllEffectsToPlayer(player);
            }
        }, 5L);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!isRunning) return;

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.setGameMode(GameMode.SPECTATOR);
        }

        Bukkit.getScheduler().runTaskLater(this, () -> {
            if (isRunning) {
                pauseTimer();
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.sendTitle("§4Challenge failed", "§7Time: " + getFormattedTime(), 10, 100, 20);
                }
            }
        }, 60L);

        Bukkit.getScheduler().runTaskLater(this, () -> event.getEntity().spigot().respawn(), 1L);
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        String activeWorldName = getConfig().getString("activeWorld", "world");
        String path = activeWorldName.equals("world") ? "world" : "worlds/" + activeWorldName;
        World activeWorld = Bukkit.getWorld(path);
        if (activeWorld != null) {
            event.setRespawnLocation(activeWorld.getSpawnLocation());
        }
    }

    @EventHandler
    public void onPortal(PlayerPortalEvent event) {
        String activeWorldName = getConfig().getString("activeWorld", "world");
        if (activeWorldName.equals("world")) return;
        String base = "worlds/" + activeWorldName;
        World fromWorld = event.getFrom().getWorld();

        if (event.getCause() == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) {
            if (fromWorld.getName().equals(base)) {
                World nether = new WorldCreator(base + "_nether").environment(World.Environment.NETHER).createWorld();
                Location to = event.getFrom();
                to.setWorld(nether);
                to.setX(to.getX() / 8); to.setZ(to.getZ() / 8);
                event.setTo(to);
            } else if (fromWorld.getName().equals(base + "_nether")) {
                World overworld = Bukkit.getWorld(base);
                if (overworld != null) {
                    Location to = event.getFrom();
                    to.setWorld(overworld);
                    to.setX(to.getX() * 8); to.setZ(to.getZ() * 8);
                    event.setTo(to);
                }
            }
        }
    }

    public void startTimer() {
        if (isRunning) return;
        isRunning = true;
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tick unfreeze");
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.setGameMode(GameMode.SURVIVAL);
            p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.5f);
        }
        getConfig().set("timer.running", true);
        saveConfig();
        timerTask = Bukkit.getScheduler().runTaskTimer(this, () -> {
            timeElapsed++;
            for (Player p : Bukkit.getOnlinePlayers()) p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§5⏱ " + getFormattedTime()));
            randomEffekt.tick();
            entitySizeRandomizer.tick();
            randomTeleport.tick();
            chunkWipe.tick();
            if (timeElapsed % 60 == 0) { getConfig().set("timer.time", timeElapsed); saveConfig(); }
        }, 0L, 20L);
    }

    public void pauseTimer() {
        isRunning = false;
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tick freeze");
        getConfig().set("timer.running", false);
        saveConfig();
        if (timerTask != null) { timerTask.cancel(); timerTask = null; }
    }

    public void resetTimer() {
        pauseTimer();
        timeElapsed = 0;
        chunkWipe.reset();
        entitySizeRandomizer.reset();
        blockRandomizer.reset();

        for(Player p : Bukkit.getOnlinePlayers()) {
            clearAdvancements(p);
        }

        getConfig().set("timer.time", 0);
        saveConfig();
    }

    public String getFormattedTime() {
        int h = timeElapsed / 3600, m = (timeElapsed % 3600) / 60, s = timeElapsed % 60;
        return String.format("%02d:%02d:%02d", h, m, s);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (!isRunning) return;
        if (event.getEntityType() == EntityType.ENDER_DRAGON) {
            pauseTimer();
            for (Player p : Bukkit.getOnlinePlayers()) p.sendTitle("§aChallenge won", "§7Time: " + getFormattedTime(), 10, 100, 20);
        }
    }
}