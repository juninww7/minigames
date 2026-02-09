package br.fr32.minigames.command;

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

public class MinigameCommand implements CommandExecutor, TabCompleter {
    private final BatataGameManager batata;

    public MinigameCommand(BatataGameManager batata) {
        this.batata = batata;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Apenas jogadores.");
            return true;
        }

        Player player = (Player) sender;
        if (args.length == 0) {
            player.sendMessage("§eUse: /minigame <entrar|sair|info|status>");
            return true;
        }

        if ("entrar".equalsIgnoreCase(args[0])) {
            if (!player.hasPermission("batata.play")) {
                player.sendMessage("§cSem permissão.");
                return true;
            }
            if (args.length < 2) {
                player.sendMessage("§cUse: /minigame entrar <arena>");
                return true;
            }
            batata.join(player, args[1]);
            return true;
        }

        if ("sair".equalsIgnoreCase(args[0])) {
            batata.quit(player);
            return true;
        }

        if ("info".equalsIgnoreCase(args[0])) {
            player.sendMessage("§dFR32Minigames - Batata Quente disponível.");
            return true;
        }

        if ("status".equalsIgnoreCase(args[0])) {
            player.sendMessage("§7Sistema ativo.");
            return true;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return filter(Arrays.asList("entrar", "sair", "info", "status"), args[0]);
        }
        return Collections.emptyList();
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
