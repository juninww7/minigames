package br.fr32.minigames.command;

import br.fr32.minigames.arena.Arena;
import br.fr32.minigames.arena.ArenaManager;
import br.fr32.minigames.batata.BatataConfig;
import br.fr32.minigames.batata.BatataGameManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class BatataCommand implements CommandExecutor, TabCompleter {
    private final ArenaManager arenaManager;
    private final BatataGameManager gameManager;
    private final BatataConfig batataConfig;

    public BatataCommand(ArenaManager arenaManager, BatataGameManager gameManager, BatataConfig batataConfig) {
        this.arenaManager = arenaManager;
        this.gameManager = gameManager;
        this.batataConfig = batataConfig;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Apenas jogadores.");
            return true;
        }

        Player player = (Player) sender;
        if (args.length == 0) {
            player.sendMessage("§eUse: /batata <entrar|sair|info|admin>");
            return true;
        }

        if ("entrar".equalsIgnoreCase(args[0])) {
            if (!player.hasPermission("batata.play")) {
                player.sendMessage("§cSem permissão.");
                return true;
            }
            if (args.length < 2) {
                player.sendMessage("§cUse: /batata entrar <arena>");
                return true;
            }
            gameManager.join(player, args[1]);
            return true;
        }

        if ("sair".equalsIgnoreCase(args[0])) {
            gameManager.quit(player);
            return true;
        }

        if ("info".equalsIgnoreCase(args[0])) {
            player.sendMessage("§dBatata Quente: passe a batata e sobreviva!");
            return true;
        }

        if (!"admin".equalsIgnoreCase(args[0])) {
            return true;
        }

        if (!player.hasPermission("batata.admin")) {
            player.sendMessage("§cSem permissão de administrador.");
            return true;
        }

        if (args.length < 2) {
            player.sendMessage("§cUse: /batata admin <criar|setspawn|start|stop|reload>");
            return true;
        }

        String action = args[1].toLowerCase();
        if ("criar".equals(action)) {
            if (args.length < 3) {
                player.sendMessage("§cUse: /batata admin criar <arena>");
                return true;
            }
            Arena arena = arenaManager.createArena(args[2], 2);
            player.sendMessage("§aArena criada: §f" + arena.getName());
            return true;
        }

        if ("setspawn".equals(action)) {
            if (args.length < 3) {
                player.sendMessage("§cUse: /batata admin setspawn <arena>");
                return true;
            }
            Arena arena = arenaManager.getArena(args[2]);
            if (arena == null) {
                player.sendMessage("§cArena inexistente.");
                return true;
            }
            arena.setSpawn(player.getLocation());
            arenaManager.save();
            player.sendMessage("§aSpawn da arena atualizado.");
            return true;
        }

        if ("start".equals(action)) {
            if (args.length < 3) {
                player.sendMessage("§cUse: /batata admin start <arena>");
                return true;
            }
            gameManager.forceStart(args[2]);
            player.sendMessage("§aStart enviado.");
            return true;
        }

        if ("stop".equals(action)) {
            if (args.length < 3) {
                player.sendMessage("§cUse: /batata admin stop <arena>");
                return true;
            }
            gameManager.forceStop(args[2]);
            player.sendMessage("§aStop enviado.");
            return true;
        }

        if ("reload".equals(action)) {
            batataConfig.load();
            arenaManager.load();
            player.sendMessage("§aConfiguração recarregada.");
            return true;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return filter(Arrays.asList("entrar", "sair", "info", "admin"), args[0]);
        }
        if (args.length == 2 && "admin".equalsIgnoreCase(args[0])) {
            if (sender.hasPermission("batata.admin")) {
                return filter(Arrays.asList("criar", "setspawn", "start", "stop", "reload"), args[1]);
            }
            return Collections.emptyList();
        }
        if (args.length == 3 && "entrar".equalsIgnoreCase(args[0])) {
            return filter(arenaNames(), args[2]);
        }
        if (args.length == 3 && "admin".equalsIgnoreCase(args[0])
            && Arrays.asList("setspawn", "start", "stop").contains(args[1].toLowerCase())) {
            return filter(arenaNames(), args[2]);
        }
        return Collections.emptyList();
    }

    private List<String> arenaNames() {
        List<String> names = new ArrayList<String>();
        for (Arena arena : arenaManager.getArenas()) {
            names.add(arena.getName());
        }
        return names;
    }

    private List<String> filter(List<String> source, String token) {
        List<String> output = new ArrayList<String>();
        for (String entry : source) {
            if (entry.toLowerCase().startsWith(token.toLowerCase())) {
                output.add(entry);
            }
        }
        return output;
    }
}
