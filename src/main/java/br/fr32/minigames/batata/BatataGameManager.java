package br.fr32.minigames.batata;

import br.fr32.minigames.GameState;
import br.fr32.minigames.arena.Arena;
import br.fr32.minigames.arena.ArenaManager;
import br.fr32.minigames.scoreboard.ScoreboardService;
import br.fr32.minigames.stats.StatsManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class BatataGameManager implements Listener {
    private final JavaPlugin plugin;
    private final ArenaManager arenaManager;
    private final BatataConfig config;
    private final ScoreboardService scoreboard;
    private final StatsManager statsManager;
    private final Random random = new Random();

    private Arena activeArena;
    private BukkitTask countdownTask;
    private BukkitTask roundTask;
    private UUID potatoHolder;
    private int roundTime;

    public BatataGameManager(JavaPlugin plugin, ArenaManager arenaManager, BatataConfig config,
                             ScoreboardService scoreboard, StatsManager statsManager) {
        this.plugin = plugin;
        this.arenaManager = arenaManager;
        this.config = config;
        this.scoreboard = scoreboard;
        this.statsManager = statsManager;
    }

    public boolean join(Player player, String arenaName) {
        Arena arena = arenaManager.getArena(arenaName);
        if (arena == null || arena.getSpawn() == null) {
            player.sendMessage("§cArena inválida ou sem spawn configurado.");
            return false;
        }

        if (isInAnyArena(player)) {
            player.sendMessage("§cVocê já está em uma arena.");
            return false;
        }

        arena.addPlayer(player.getUniqueId());
        activeArena = arena;
        player.teleport(arena.getSpawn());
        player.setHealth(Math.min(player.getMaxHealth(), config.getVidaAoComecar()));
        scoreboard.track(player, arena);
        player.sendMessage("§aVocê entrou na arena §f" + arena.getName());
        tryStartCountdown(arena);
        return true;
    }

    public void quit(Player player) {
        Arena arena = findArenaByPlayer(player.getUniqueId());
        if (arena == null) {
            player.sendMessage("§cVocê não está em arena.");
            return;
        }

        arena.removePlayer(player.getUniqueId());
        removePotato(player);
        scoreboard.untrack(player);
        player.sendMessage("§eVocê saiu da arena §f" + arena.getName());

        if (arena.getState() == GameState.CONTAGEM && arena.getPlayerCount() < arena.getMinPlayers()) {
            cancelCountdown(arena, "§cContagem cancelada: jogadores insuficientes.");
        }

        if (arena.getState() == GameState.EM_JOGO && arena.getPlayerCount() <= 1) {
            finishGame(arena);
        }
    }

    public void forceStart(String arenaName) {
        Arena arena = arenaManager.getArena(arenaName);
        if (arena != null) {
            startGame(arena);
        }
    }

    public void forceStop(String arenaName) {
        Arena arena = arenaManager.getArena(arenaName);
        if (arena != null) {
            stopGame(arena, "§cPartida encerrada por administrador.");
        }
    }

    public Arena findArenaByPlayer(UUID uuid) {
        for (Arena arena : arenaManager.getArenas()) {
            if (arena.getPlayers().contains(uuid)) {
                return arena;
            }
        }
        return null;
    }

    private boolean isInAnyArena(Player player) {
        return findArenaByPlayer(player.getUniqueId()) != null;
    }

    public void tryStartCountdown(final Arena arena) {
        if (arena.getState() != GameState.AGUARDANDO) {
            return;
        }
        if (arena.getPlayerCount() < arena.getMinPlayers()) {
            return;
        }

        arena.setState(GameState.CONTAGEM);
        final int[] seconds = new int[] {10};
        broadcast(arena, "§dContagem iniciada: " + seconds[0] + "s");
        countdownTask = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
            @Override
            public void run() {
                if (arena.getPlayerCount() < arena.getMinPlayers()) {
                    cancelCountdown(arena, "§cContagem cancelada: jogadores insuficientes.");
                    return;
                }
                seconds[0]--;
                if (seconds[0] <= 0) {
                    if (countdownTask != null) {
                        countdownTask.cancel();
                        countdownTask = null;
                    }
                    startGame(arena);
                    return;
                }
                broadcast(arena, "§dPartida iniciará em " + seconds[0] + "s");
            }
        }, 20L, 20L);
    }

    private void cancelCountdown(Arena arena, String message) {
        if (countdownTask != null) {
            countdownTask.cancel();
            countdownTask = null;
        }
        arena.setState(GameState.AGUARDANDO);
        broadcast(arena, message);
    }

    private void startGame(final Arena arena) {
        if (arena.getPlayerCount() < 2) {
            arena.setState(GameState.AGUARDANDO);
            return;
        }

        arena.setState(GameState.EM_JOGO);
        roundTime = config.getRoundDuration();
        List<Player> online = getOnlinePlayers(arena);
        potatoHolder = online.get(random.nextInt(online.size())).getUniqueId();

        for (Player player : online) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.msg("inicio", "§eO jogo começou!")));
            scoreboard.setStatus(player, hasPotato(player) ? "Com Batata" : "Sem Batata");
            scoreboard.setTime(player, roundTime);
        }
        givePotato(Bukkit.getPlayer(potatoHolder));

        roundTask = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
            @Override
            public void run() {
                if (arena.getState() != GameState.EM_JOGO) {
                    return;
                }

                roundTime--;
                for (Player player : getOnlinePlayers(arena)) {
                    scoreboard.setTime(player, roundTime);
                    scoreboard.setStatus(player, hasPotato(player) ? "Com Batata" : "Sem Batata");
                    if (config.isSomTick() && roundTime <= 5) {
                        player.playSound(player.getLocation(), Sound.NOTE_PLING, 1f, 1f);
                    }
                }

                if (roundTime <= 0) {
                    Player loser = Bukkit.getPlayer(potatoHolder);
                    if (loser != null) {
                        eliminatePlayer(loser, arena);
                    }
                    if (arena.getPlayerCount() <= 1) {
                        finishGame(arena);
                    } else {
                        selectNewPotatoHolder(arena);
                        roundTime = config.getRoundDuration();
                    }
                }
            }
        }, 20L, 20L);
    }

    private void selectNewPotatoHolder(Arena arena) {
        List<Player> players = getOnlinePlayers(arena);
        if (players.isEmpty()) {
            return;
        }
        Player chosen = players.get(random.nextInt(players.size()));
        potatoHolder = chosen.getUniqueId();
        givePotato(chosen);
    }

    private void eliminatePlayer(Player player, Arena arena) {
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.msg("perdeu", "§4Você perdeu!")));
        if (config.isExplosaoAoPerder()) {
            Location location = player.getLocation();
            location.getWorld().createExplosion(location.getX(), location.getY(), location.getZ(), 0F, false, false);
        }
        arena.removePlayer(player.getUniqueId());
        removePotato(player);
        scoreboard.setStatus(player, "Eliminado");
        player.setHealth(0D);
    }

    private void finishGame(Arena arena) {
        if (roundTask != null) {
            roundTask.cancel();
            roundTask = null;
        }

        List<Player> survivors = getOnlinePlayers(arena);
        if (!survivors.isEmpty()) {
            Player winner = survivors.get(0);
            statsManager.addWin(winner.getUniqueId(), "batata");
            broadcast(arena, "§aVencedor: §f" + winner.getName());
        }

        stopGame(arena, "§7Partida finalizada.");
    }

    private void stopGame(Arena arena, String message) {
        if (countdownTask != null) {
            countdownTask.cancel();
            countdownTask = null;
        }
        if (roundTask != null) {
            roundTask.cancel();
            roundTask = null;
        }

        arena.setState(GameState.AGUARDANDO);
        for (Player player : getOnlinePlayers(arena)) {
            removePotato(player);
            scoreboard.untrack(player);
            player.sendMessage(message);
        }
        arena.clearPlayers();
        potatoHolder = null;
        activeArena = null;
    }

    private void broadcast(Arena arena, String message) {
        for (Player player : getOnlinePlayers(arena)) {
            player.sendMessage(message);
        }
    }

    private List<Player> getOnlinePlayers(Arena arena) {
        List<Player> online = new ArrayList<Player>();
        for (UUID uuid : arena.getPlayers()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                online.add(player);
            }
        }
        return online;
    }

    public boolean hasPotato(Player player) {
        return potatoHolder != null && potatoHolder.equals(player.getUniqueId());
    }

    private void givePotato(Player player) {
        if (player == null) {
            return;
        }
        ItemStack item = new ItemStack(Material.BAKED_POTATO, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§6§lBatata Quente");
        item.setItemMeta(meta);
        int slot = Math.max(0, Math.min(8, config.getSlotBatata() - 1));
        player.getInventory().setItem(slot, item);
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.msg("recebeu", "§cVocê recebeu a BATATA QUENTE!")));
    }

    private void removePotato(Player player) {
        if (player == null) {
            return;
        }
        for (ItemStack item : player.getInventory().getContents()) {
            if (isPotato(item)) {
                player.getInventory().remove(item);
            }
        }
    }

    private boolean isPotato(ItemStack item) {
        if (item == null || item.getType() != Material.BAKED_POTATO || !item.hasItemMeta()) {
            return false;
        }
        return "§6§lBatata Quente".equals(item.getItemMeta().getDisplayName());
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        Entity target = event.getEntity();

        if (!(damager instanceof Player) || !(target instanceof Player)) {
            return;
        }

        Player attacker = (Player) damager;
        Player victim = (Player) target;

        Arena arena = findArenaByPlayer(attacker.getUniqueId());
        if (arena == null || arena != findArenaByPlayer(victim.getUniqueId()) || arena.getState() != GameState.EM_JOGO) {
            return;
        }

        if (hasPotato(attacker)) {
            potatoHolder = victim.getUniqueId();
            removePotato(attacker);
            givePotato(victim);
            scoreboard.setStatus(attacker, "Sem Batata");
            scoreboard.setStatus(victim, "Com Batata");
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        if (isPotato(event.getItemDrop().getItemStack())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (isPotato(event.getCurrentItem()) || isPotato(event.getCursor())) {
            event.setCancelled(true);
        }
    }
}
