package br.fr32.minigames.stats;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class StatsManager {
    private final JavaPlugin plugin;
    private final File file;
    private YamlConfiguration config;

    public StatsManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "stats.yml");
        load();
    }

    public void load() {
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().warning("Não foi possível criar stats.yml");
            }
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    public void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Não foi possível salvar stats.yml");
        }
    }

    public int getWins(UUID uuid, String minigame) {
        return config.getInt("players." + uuid + "." + minigame + ".wins", 0);
    }

    public void addWin(UUID uuid, String minigame) {
        String path = "players." + uuid + "." + minigame + ".wins";
        config.set(path, config.getInt(path, 0) + 1);
        save();
    }
}
