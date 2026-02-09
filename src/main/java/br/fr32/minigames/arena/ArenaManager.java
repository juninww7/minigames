package br.fr32.minigames.arena;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ArenaManager {
    private final JavaPlugin plugin;
    private final Map<String, Arena> arenas = new HashMap<String, Arena>();
    private final File file;
    private YamlConfiguration config;

    public ArenaManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "arenas.yml");
        load();
    }

    public void load() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().warning("Não foi possível criar arenas.yml");
            }
        }
        this.config = YamlConfiguration.loadConfiguration(file);
        arenas.clear();

        ConfigurationSection section = config.getConfigurationSection("arenas");
        if (section == null) {
            return;
        }

        for (String arenaName : section.getKeys(false)) {
            String path = "arenas." + arenaName;
            int minPlayers = config.getInt(path + ".min-jogadores", 2);
            Arena arena = new Arena(arenaName, minPlayers);

            String worldName = config.getString(path + ".spawn.world");
            if (worldName != null) {
                World world = Bukkit.getWorld(worldName);
                if (world != null) {
                    double x = config.getDouble(path + ".spawn.x");
                    double y = config.getDouble(path + ".spawn.y");
                    double z = config.getDouble(path + ".spawn.z");
                    float yaw = (float) config.getDouble(path + ".spawn.yaw");
                    float pitch = (float) config.getDouble(path + ".spawn.pitch");
                    arena.setSpawn(new Location(world, x, y, z, yaw, pitch));
                }
            }

            arenas.put(arenaName.toLowerCase(), arena);
        }
    }

    public void save() {
        config.set("arenas", null);
        for (Arena arena : arenas.values()) {
            String path = "arenas." + arena.getName();
            config.set(path + ".min-jogadores", arena.getMinPlayers());
            Location spawn = arena.getSpawn();
            if (spawn != null && spawn.getWorld() != null) {
                config.set(path + ".spawn.world", spawn.getWorld().getName());
                config.set(path + ".spawn.x", spawn.getX());
                config.set(path + ".spawn.y", spawn.getY());
                config.set(path + ".spawn.z", spawn.getZ());
                config.set(path + ".spawn.yaw", spawn.getYaw());
                config.set(path + ".spawn.pitch", spawn.getPitch());
            }
        }

        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Não foi possível salvar arenas.yml");
        }
    }

    public Arena createArena(String name, int minPlayers) {
        Arena arena = new Arena(name, minPlayers);
        arenas.put(name.toLowerCase(), arena);
        save();
        return arena;
    }

    public boolean removeArena(String name) {
        Arena removed = arenas.remove(name.toLowerCase());
        save();
        return removed != null;
    }

    public Arena getArena(String name) {
        return arenas.get(name.toLowerCase());
    }

    public Collection<Arena> getArenas() {
        return Collections.unmodifiableCollection(arenas.values());
    }
}
