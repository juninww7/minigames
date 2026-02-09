package br.fr32.minigames.scoreboard;

import br.fr32.minigames.arena.Arena;
import br.fr32.minigames.stats.StatsManager;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public class ScoreboardService {
    private final JavaPlugin plugin;
    private final StatsManager statsManager;
    private final Map<UUID, Arena> activeArena = new HashMap<UUID, Arena>();
    private final Map<UUID, String> status = new HashMap<UUID, String>();
    private final Map<UUID, Integer> time = new HashMap<UUID, Integer>();

    public ScoreboardService(JavaPlugin plugin, StatsManager statsManager) {
        this.plugin = plugin;
        this.statsManager = statsManager;
        Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
            @Override
            public void run() {
                updateAll();
            }
        }, 20L, 20L);
    }

    public void track(Player player, Arena arena) {
        activeArena.put(player.getUniqueId(), arena);
        status.put(player.getUniqueId(), "Aguardando");
        time.put(player.getUniqueId(), 0);
        update(player);
    }

    public void untrack(Player player) {
        activeArena.remove(player.getUniqueId());
        status.remove(player.getUniqueId());
        time.remove(player.getUniqueId());
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }

    public void setStatus(Player player, String value) {
        status.put(player.getUniqueId(), value);
        update(player);
    }

    public void setTime(Player player, int seconds) {
        time.put(player.getUniqueId(), seconds);
        update(player);
    }

    private void updateAll() {
        for (UUID uuid : activeArena.keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                update(player);
            }
        }
    }

    public void update(Player player) {
        Arena arena = activeArena.get(player.getUniqueId());
        if (arena == null) {
            return;
        }

        Scoreboard sb = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = sb.registerNewObjective("fr32", "dummy");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.setDisplayName("§5§lBATATA QUENTE");

        String nickLine = trim("§dJogador: §f" + player.getName());
        String timeLine = trim("§dTempo: §f" + time.get(player.getUniqueId()));
        String rankLine = trim("§dRank: §f" + arena.getPlayerCount());
        String winsLine = trim("§dVitórias: §f" + statsManager.getWins(player.getUniqueId(), "batata"));
        String statusLine = trim("§7Status: §f" + status.get(player.getUniqueId()));

        objective.getScore("§8§m----------------").setScore(7);
        objective.getScore(nickLine).setScore(6);
        objective.getScore(timeLine).setScore(5);
        objective.getScore("§0").setScore(4);
        objective.getScore(rankLine).setScore(3);
        objective.getScore(winsLine).setScore(2);
        objective.getScore(statusLine).setScore(1);

        player.setScoreboard(sb);
    }

    private String trim(String value) {
        return ChatColor.translateAlternateColorCodes('&', value.length() > 40 ? value.substring(0, 40) : value);
    }
}
