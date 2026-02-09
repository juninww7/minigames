package br.fr32.minigames.batata;

import java.io.File;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class BatataConfig {
    private final JavaPlugin plugin;
    private final File file;
    private YamlConfiguration config;

    public BatataConfig(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "batataquente.yml");
        load();
    }

    public void load() {
        if (!file.exists()) {
            plugin.saveResource("batataquente.yml", false);
        }
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    public int getRoundDuration() {
        return config.getInt("duracao-round", 30);
    }

    public int getSlotBatata() {
        return config.getInt("slot-batata", 4);
    }

    public double getVidaAoComecar() {
        return config.getDouble("vida-ao-comecar", 20D);
    }

    public boolean isExplosaoAoPerder() {
        return config.getBoolean("explosao-ao-perder", true);
    }

    public boolean isSomTick() {
        return config.getBoolean("som-tick", true);
    }

    public String msg(String key, String def) {
        return config.getString("mensagens." + key, def);
    }
}
