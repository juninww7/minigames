package br.fr32.minigames;

import br.fr32.minigames.arena.ArenaManager;
import br.fr32.minigames.batata.BatataConfig;
import br.fr32.minigames.batata.BatataGameManager;
import br.fr32.minigames.command.BatataCommand;
import br.fr32.minigames.command.MinigameCommand;
import br.fr32.minigames.config.MessageManager;
import br.fr32.minigames.scoreboard.ScoreboardService;
import br.fr32.minigames.stats.StatsManager;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class FR32MinigamesPlugin extends JavaPlugin {
    private ArenaManager arenaManager;
    private BatataConfig batataConfig;
    private MessageManager messageManager;
    private StatsManager statsManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        arenaManager = new ArenaManager(this);
        batataConfig = new BatataConfig(this);
        messageManager = new MessageManager(this);
        statsManager = new StatsManager(this);

        ScoreboardService scoreboardService = new ScoreboardService(this, statsManager);
        BatataGameManager batataGameManager = new BatataGameManager(this, arenaManager, batataConfig, scoreboardService, statsManager);

        PluginCommand minigame = getCommand("minigame");
        if (minigame != null) {
            MinigameCommand minigameCommand = new MinigameCommand(batataGameManager);
            minigame.setExecutor(minigameCommand);
            minigame.setTabCompleter(minigameCommand);
        }

        PluginCommand batata = getCommand("batata");
        if (batata != null) {
            BatataCommand batataCommand = new BatataCommand(arenaManager, batataGameManager, batataConfig);
            batata.setExecutor(batataCommand);
            batata.setTabCompleter(batataCommand);
        }

        getServer().getPluginManager().registerEvents(batataGameManager, this);
        getLogger().info("FR32Minigames habilitado.");
    }

    @Override
    public void onDisable() {
        arenaManager.save();
        statsManager.save();
        messageManager.save();
        getLogger().info("FR32Minigames desabilitado.");
    }
}
