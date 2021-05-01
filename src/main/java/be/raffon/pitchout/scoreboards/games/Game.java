package be.raffon.pitchout.scoreboards.games;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import be.raffon.pitchout.pitchout;

public class Game {
	
	public ArrayList<UUID> players;
	public org.bukkit.Location waitingroom;
	public status Status;
	public HashMap<UUID, Integer> scores;
	public HashMap<UUID, Integer> lives;
	public Integer usedLoc;
	public HashMap<UUID, UUID> damagers;
	public Location loc;
	public ArrayList<UUID> spectators;
	
	public Game(Location loc, Integer usedLoc) {
		this.waitingroom = new org.bukkit.Location(Bukkit.getWorld(loc.world), loc.waiting_x, loc.waiting_y, loc.waiting_z);
		this.players = new ArrayList<UUID>();
		this.usedLoc = usedLoc;
		this.loc = loc;
		this.scores = new HashMap<UUID, Integer>();
		this.lives = new HashMap<UUID, Integer>();
		this.damagers = new HashMap<UUID, UUID>();
		this.spectators = new ArrayList<UUID>();
		this.Status = status.WAITING;
	}
	
	public void addPlayer(Player player) {
		players.add(player.getUniqueId());
		scores.put(player.getUniqueId(), 0);
		lives.put(player.getUniqueId(), 3);
		player.teleport(waitingroom);
	}

	public void start(pitchout plugin) {
		this.Status = status.STARTING;
		
		Game gam = this;
		
		plugin.tasks.put(usedLoc, Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
			Integer seconds = 1000000000;
            @Override
            public void run() {
            	Integer players = gam.players.size();

	            for(Map.Entry<Integer, Integer> entry : plugin.games.attentes.entrySet()) {
	            	Integer key = entry.getKey();
	            	Integer value = entry.getValue();
	            	if(key == players || key == players + 1) {
	            		if(value <= seconds) {
	            			seconds = value;
	            		}
	            	}
	            }
            
	            for(UUID uuid : gam.players) {
	            	Player pl = Bukkit.getPlayer(uuid);
	            	Game game = plugin.games.getPlayerGame(pl);
	            	if(gam.equals(game) && (seconds == 120 || seconds == 60 || seconds == 90|| seconds == 30 || seconds == 10)) {
	            		pl.playSound(pl.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 10, 1);
	            		pl.sendMessage(plugin.text + " The Game will start in " + seconds + " seconds");
	            	}
	            	if(game.Status == status.CANCELLED_START) {
	            		pl.sendMessage(plugin.text + " There is not enough players to start.");
	            		Bukkit.getServer().getScheduler().cancelTask(plugin.tasks.get(usedLoc));
	            	}
	            	if(seconds < 10 && gam.equals(game)) {
	            		pl.sendMessage(plugin.text + " The Game will start in " + seconds);
	            		pl.playSound(pl.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 10, 1);
	            	}
	            	if(seconds == 1&& gam.equals(game)) {
	            		pl.sendTitle(ChatColor.GOLD + "Game starting...", "", 1, 20, 1);
	            	}
	            }

	            
	            if(gam.Status == status.CANCELLED_START) {
	            	gam.Status = status.WAITING;
	            }
	            
	            if(seconds == 0) {
	            	plugin.games.startGame(gam);
	            	Bukkit.getServer().getScheduler().cancelTask(plugin.tasks.get(usedLoc));
	            }

	            seconds--;
	            
            }
		}, 0L, 20L));
	}
	
	public void cancelStarting() {
		if(this.Status == status.STARTING) {
			this.Status = status.CANCELLED_START;
		}
		
	}
	
	
	public Boolean equals(Game game) {
		if(game == null) {
			return false;
		}
		
		if(game.players != this.players) {
			return false;
		}
		
		return true;
	}

	public void removeLive(Player pl) {
		
		Integer lives = this.lives.get(pl.getUniqueId());
		lives = lives -1;
		if(lives < 0) return;
		this.lives.remove(pl.getUniqueId());
		this.lives.put(pl.getUniqueId(), lives);
		pl.sendMessage(pitchout.text + " You now have " + lives + " lives left.");
		if(lives < 1) {
			spectators.add(pl.getUniqueId());
			pl.setGameMode(GameMode.SPECTATOR);
			this.broadCast(pitchout.text + " " +pl.getDisplayName() + " was eliminated");
		}
		
	}
	
	public void broadCast(String txt) {
        for(UUID id : this.players) {
			Player pl = Bukkit.getPlayer(id);
        	pl.sendMessage(txt);
        }
	}

	public void end() {
		/* TP tout le monde au server hub*/

	}
	
	

}
