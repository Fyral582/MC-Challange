package de.Fyral.challange;

import de.Fyral.challange.challanges.*;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.Difficulty;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main extends JavaPlugin implements Listener {

    private static Main instance;
    private boolean isRunning = false;
    private FileConfiguration languageConfig;
    private Database database;

    private UltraHardcore ultraHardcore;
    private ChunkWipe chunkWipe;
    private BlockRandomizer blockRandomizer;
    private EntitySizeRandomizer entitySizeRandomizer;
    private RandomEffekt randomEffekt;
    private RandomTeleport randomTeleport;
    private NoCraftingTable noCraftingTable;
    private NoArmor noArmor;
    private RandomCrafting randomCrafting;
    private GUI gui;
    private int secondsElapsed = 0;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();
        secondsElapsed = getConfig().getInt("challenge-time", 0);

        isRunning = getConfig().getBoolean("is-running", false);

        setupLanguage();

        database = new Database();
        database.connect();

        ultraHardcore = new UltraHardcore();
        chunkWipe = new ChunkWipe();
        blockRandomizer = new BlockRandomizer();
        entitySizeRandomizer = new EntitySizeRandomizer();
        randomEffekt = new RandomEffekt();
        randomTeleport = new RandomTeleport();
        noCraftingTable = new NoCraftingTable();
        noArmor = new NoArmor();
        randomCrafting = new RandomCrafting();
        gui = new GUI(this);

        blockRandomizer.setActive(getConfig().getBoolean("challenges.block_randomizer", false));
        if (blockRandomizer.isActive()) blockRandomizer.generateMappings();
        chunkWipe.setActive(getConfig().getBoolean("challenges.chunk_wipe", false));
        entitySizeRandomizer.setActive(getConfig().getBoolean("challenges.entity_size", false));
        randomEffekt.setActive(getConfig().getBoolean("challenges.random_effect", false));
        randomTeleport.setActive(getConfig().getBoolean("challenges.random_teleport", false));
        noCraftingTable.setActive(getConfig().getBoolean("challenges.no_crafting_table", false));
        noArmor.setActive(getConfig().getBoolean("challenges.no_armor", false));
        randomCrafting.setActive(getConfig().getBoolean("challenges.random_crafting", false));
        if (randomCrafting.isActive()) randomCrafting.generatePossibleResults();
        ultraHardcore.setActive(getConfig().getBoolean("challenges.ultrahardcore", false));

        if (isRunning) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tick unfreeze");
        } else {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tick freeze");
        }

        loadActiveWorld();

        for (World world : Bukkit.getWorlds()) {
            applyDifficulty(world);
        }

        commands cmdExecutor = new commands(this, gui);
        getCommand("settings").setExecutor(cmdExecutor);
        getCommand("pause").setExecutor(cmdExecutor);
        getCommand("resume").setExecutor(cmdExecutor);
        getCommand("reset").setExecutor(cmdExecutor);
        getCommand("activechallanges").setExecutor(cmdExecutor);

        getServer().getPluginManager().registerEvents(gui, this);
        getServer().getPluginManager().registerEvents(blockRandomizer, this);
        getServer().getPluginManager().registerEvents(entitySizeRandomizer, this);
        getServer().getPluginManager().registerEvents(randomEffekt, this);
        getServer().getPluginManager().registerEvents(noCraftingTable, this);
        getServer().getPluginManager().registerEvents(noArmor, this);
        getServer().getPluginManager().registerEvents(randomCrafting, this);
        getServer().getPluginManager().registerEvents(this, this);

        getServer().getScheduler().runTaskTimer(this, () -> {
            if (isRunning) {
                secondsElapsed++;
                getConfig().set("challenge-time", secondsElapsed);
                saveConfig();

                chunkWipe.tick();
                entitySizeRandomizer.tick();
                randomEffekt.tick();
                randomTeleport.tick();
            }

            int minutes = secondsElapsed / 60;
            int seconds = secondsElapsed % 60;
            String timeFormatted = String.format("%02d:%02d", minutes, seconds);
            String actionBarMsg = getMessage("messages.timer.actionbar").replace("%time%", timeFormatted);

            for (Player p : Bukkit.getOnlinePlayers()) {
                p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(actionBarMsg));
            }
        }, 0L, 20L);
    }

    @Override
    public void onDisable() {
        reloadConfig();
        getConfig().set("challenge-time", secondsElapsed);
        getConfig().set("is-running", isRunning);
        getConfig().set("challenges.block_randomizer", blockRandomizer.isActive());
        getConfig().set("challenges.chunk_wipe", chunkWipe.isActive());
        getConfig().set("challenges.entity_size", entitySizeRandomizer.isActive());
        getConfig().set("challenges.random_effect", randomEffekt.isActive());
        getConfig().set("challenges.random_teleport", randomTeleport.isActive());
        getConfig().set("challenges.no_crafting_table", noCraftingTable.isActive());
        getConfig().set("challenges.no_armor", noArmor.isActive());
        getConfig().set("challenges.random_crafting", randomCrafting.isActive());
        getConfig().set("challenges.ultrahardcore", ultraHardcore.isActive());
        saveConfig();

        if (database != null) {
            database.disconnect();
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (isRunning && Bukkit.getOnlinePlayers().size() - 1 <= 0) {
            pauseTimer();
            Bukkit.broadcastMessage(getMessage("messages.timer.paused_empty"));
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!isRunning) return;
        pauseTimer();

        String titleMsg = getMessage("messages.titles.death.title");
        String subtitleMsg = getMessage("messages.titles.death.subtitle");

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendTitle(titleMsg, subtitleMsg, 10, 70, 20);
            p.setGameMode(GameMode.SPECTATOR);
            p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1f, 0.5f);
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof EnderDragon) {
            if (!isRunning) return;
            pauseTimer();

            String titleMsg = getMessage("messages.titles.win.title");
            String subtitleMsg = getMessage("messages.titles.win.subtitle");

            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendTitle(titleMsg, subtitleMsg, 10, 100, 20);
                p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
            }
        }
    }

    public void executeReset() {
        Bukkit.broadcastMessage(getMessage("messages.commands.reset_start"));

        resetTimer();
        getRandomEffekt().resetEffekte();
        getRandomTeleport().reset();
        getChunkWipe().reset();
        getEntitySizeRandomizer().reset();
        getUltraHardcore().reset();
        getNoCraftingTable().reset();
        getNoArmor().reset();
        getRandomCrafting().reset();

        String oldWorldName = getConfig().getString("activeWorld", "");
        if (!oldWorldName.isEmpty() && Bukkit.getWorld(oldWorldName) != null) {
            Bukkit.unloadWorld(oldWorldName, false);
        }

        Bukkit.getScheduler().runTaskLater(this, () -> {
            String worldName = "worlds/challenge_" + System.currentTimeMillis();

            WorldCreator creator = new WorldCreator(worldName);
            World newWorld = Bukkit.createWorld(creator);
            applyDifficulty(newWorld);

            getConfig().set("activeWorld", worldName);
            saveConfig();

            for (Player p : Bukkit.getOnlinePlayers()) {
                resetPlayerState(p, newWorld);
            }
            Bukkit.broadcastMessage(getMessage("messages.commands.reset_success"));
        }, 60L);
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        String activeWorldName = getConfig().getString("activeWorld", "world");
        World activeWorld = Bukkit.getWorld(activeWorldName);
        if (activeWorld == null) return;

        Location spawn = activeWorld.getSpawnLocation();
        int safeY = activeWorld.getHighestBlockYAt(spawn.getBlockX(), spawn.getBlockZ(), org.bukkit.HeightMap.MOTION_BLOCKING);
        spawn.setY(safeY + 1);
        event.setRespawnLocation(spawn);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        String activeWorldName = getConfig().getString("activeWorld", "world");
        World activeWorld = Bukkit.getWorld(activeWorldName);

        if (activeWorld != null && !p.getWorld().equals(activeWorld)) {
            Bukkit.getScheduler().runTaskLater(this, () -> {
                if (!p.isOnline()) return;
                resetPlayerState(p, activeWorld);
            }, 10L);
        }
    }

    private void resetPlayerState(Player p, World world) {
        p.getInventory().clear();
        p.getInventory().setArmorContents(null);
        p.setHealth(20.0);
        p.setFoodLevel(20);
        p.setSaturation(20.0f);
        p.setExp(0f);
        p.setLevel(0);
        p.setGameMode(GameMode.SURVIVAL);

        if (world != null) {
            Location spawn = world.getSpawnLocation();
            int safeY = world.getHighestBlockYAt(spawn.getBlockX(), spawn.getBlockZ(), org.bukkit.HeightMap.MOTION_BLOCKING);
            spawn.setY(safeY + 1);
            p.teleport(spawn);
        }
    }

    private void applyDifficulty(World world) {
        if (world == null) return;
        String diffString = getConfig().getString("difficulty", "hard");
        Difficulty difficulty;
        try {
            difficulty = Difficulty.valueOf(diffString.toUpperCase());
        } catch (IllegalArgumentException e) {
            difficulty = Difficulty.HARD;
        }
        world.setDifficulty(difficulty);
    }

    private void loadActiveWorld() {
        String activeWorldName = getConfig().getString("activeWorld", "world");
        if (Bukkit.getWorld(activeWorldName) == null) {
            new WorldCreator(activeWorldName).createWorld();
        }
    }

    private void setupLanguage() {
        String lang = getConfig().getString("language", "de");
        File langFolder = new File(getDataFolder(), "languages");

        if (!langFolder.exists()) {
            langFolder.mkdirs();
        }

        File langFile = new File(langFolder, lang + ".yml");
        if (!langFile.exists()) {
            String resourcePath = "languages/" + lang + ".yml";
            if (getResource(resourcePath) != null) {
                saveResource(resourcePath, false);
            } else {
                try {
                    langFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        languageConfig = YamlConfiguration.loadConfiguration(langFile);
    }

    public static Main getInstance() {
        return instance;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean running) {
        this.isRunning = running;
        getConfig().set("is-running", running);
        saveConfig();
    }

    public void startTimer() {
        this.isRunning = true;
        getConfig().set("is-running", true);
        saveConfig();
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tick unfreeze");

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f);
        }
    }

    public void pauseTimer() {
        this.isRunning = false;
        getConfig().set("is-running", false);
        saveConfig();
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tick freeze");
    }

    public void resetTimer() {
        this.isRunning = false;
        this.secondsElapsed = 0;
        getConfig().set("challenge-time", 0);
        getConfig().set("is-running", false);

        getConfig().set("challenges.block_randomizer", false);
        getConfig().set("challenges.chunk_wipe", false);
        getConfig().set("challenges.entity_size", false);
        getConfig().set("challenges.random_effect", false);
        getConfig().set("challenges.random_teleport", false);
        getConfig().set("challenges.no_crafting_table", false);
        getConfig().set("challenges.ultrahardcore", false);
        getConfig().set("challenges.no_armor", false);
        getConfig().set("challenges.random_crafting", false);
        saveConfig();

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tick freeze");
    }

    public FileConfiguration getLanguageConfig() {
        return languageConfig;
    }

    public String getMessage(String path) {
        String msg = languageConfig.getString(path, "&cMessage not found: " + path);
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    public List<String> getMessageList(String path) {
        List<String> list = languageConfig.getStringList(path);
        List<String> colored = new ArrayList<>();
        for (String s : list) {
            colored.add(ChatColor.translateAlternateColorCodes('&', s));
        }
        return colored;
    }

    public Database getDatabase() {
        return database;
    }

    public GUI getGui() { return gui; }
    public UltraHardcore getUltraHardcore() { return ultraHardcore; }
    public ChunkWipe getChunkWipe() { return chunkWipe; }
    public BlockRandomizer getBlockRandomizer() { return blockRandomizer; }
    public EntitySizeRandomizer getEntitySizeRandomizer() { return entitySizeRandomizer; }
    public RandomEffekt getRandomEffekt() { return randomEffekt; }
    public RandomTeleport getRandomTeleport() { return randomTeleport; }
    public NoCraftingTable getNoCraftingTable() { return noCraftingTable; }
    public NoArmor getNoArmor() { return noArmor; }
    public RandomCrafting getRandomCrafting() { return randomCrafting; }
}