package br.fr32.minigames.config;

import java.io.File;
import java.io.IOException;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class MessageManager {
    private final JavaPlugin plugin;
    private final File file;
    private YamlConfiguration config;

    public MessageManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "messages.yml");
        load();
    }

    public void load() {
        if (!file.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    public void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Não foi possível salvar messages.yml");
        }
    }

    public String get(String path) {
        String raw = config.getString(path, "§cMensagem não encontrada: " + path);
        return ChatColor.translateAlternateColorCodes('&', raw);
    }
}
