package br.fr32.minigames.arena;

import br.fr32.minigames.GameState;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Location;

public class Arena {
    private final String name;
    private int minPlayers;
    private Location spawn;
    private final Set<UUID> players = new HashSet<UUID>();
    private GameState state = GameState.AGUARDANDO;

    public Arena(String name, int minPlayers) {
        this.name = name;
        this.minPlayers = minPlayers;
    }

    public String getName() {
        return name;
    }

    public int getMinPlayers() {
        return minPlayers;
    }

    public void setMinPlayers(int minPlayers) {
        this.minPlayers = minPlayers;
    }

    public Location getSpawn() {
        return spawn;
    }

    public void setSpawn(Location spawn) {
        this.spawn = spawn;
    }

    public GameState getState() {
        return state;
    }

    public void setState(GameState state) {
        this.state = state;
    }

    public Set<UUID> getPlayers() {
        return Collections.unmodifiableSet(players);
    }

    public void addPlayer(UUID uuid) {
        players.add(uuid);
    }

    public void removePlayer(UUID uuid) {
        players.remove(uuid);
    }

    public int getPlayerCount() {
        return players.size();
    }

    public void clearPlayers() {
        players.clear();
    }
}
