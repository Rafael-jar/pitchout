package be.raffon.pitchout.scoreboards.games;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import be.raffon.pitchout.pitchout;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class GameManager {
	
	private ArrayList<Game> games;
	public Integer minimum = 2;
	public Integer maximum = 10;
	public HashMap<Integer, Integer> attentes;
	
	
	public GameManager() {
		games = new ArrayList<Game>();
		attentes = new HashMap<Integer, Integer>();
		attentes.put(2, 120);
		attentes.put(4, 90);
		attentes.put(6, 60);
		attentes.put(8, 30);
		attentes.put(10, 10);

	}
	
	public void registerGame(Game game) {
		games.add(game);
	}
	
	public ArrayList<Game> getGames() {
		return games;
	}

	public Game getPopularGame() {
		Game higher = null;
		for(int i=0; i<games.size(); i++) {
			Game game = games.get(i);
			if(game.Status == status.WAITING || game.Status == status.STARTING) {
				if(higher == null) {
					higher = game;
				}
				if(higher.players.size() < game.players.size()) {
					higher = game;
				}
			}
		}
		
		if(higher == null) {
			System.out.println("v");
			higher = generateGame();
		}
		return higher;
	}

	private Game generateGame() {
		ArrayList<ConfigurationSection> locs = pitchout.getLocation();
		for(int i=0; i<locs.size(); i++) {
			ConfigurationSection obj = locs.get(i);
			Boolean used = false;
			for(int k=0; k<games.size(); k++) {
				Game game = games.get(k);
				
				if(game.usedLoc == i) {
					used = true;
				}
			}
			
			if(!used) {
				Location loc = new Location(obj);
				Game game =  new Game(loc, i);
				games.add(game);
				return game;
			} else {
				Location loc = new Location(obj);


			}
		}
		return null;
		
	}
	
	public Game getPlayerGame(Player pl) {
		for(int i=0; i<games.size(); i++) {
			Game game = games.get(i);
			
			for(int k=0; k<game.players.size(); k++) {
				UUID uuid = game.players.get(k);
				Player player = Bukkit.getPlayer(uuid);
				
				if(pl.getUniqueId().equals(player.getUniqueId())) {
					return game;
				}
			}
		}
		return null;
	}


	public void startGame(Game gam) {
		ArrayList<org.bukkit.Location> arr = gam.loc.locations;

		gam.Status = status.INPROGRESS;
		for(int i=0; i<gam.players.size(); i++) {


			UUID uuid = gam.players.get(i);
			Player pl = Bukkit.getPlayer(uuid);
			org.bukkit.Location location = arr.get(i);
			pl.teleport(location);
			pl.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 999999, 255));
			pl.setBedSpawnLocation(location);
			pl.sendMessage(pitchout.text + " Good luck !");

			ItemStack stick = new ItemStack(Material.STICK, 1);
			stick.addUnsafeEnchantment(Enchantment.KNOCKBACK, 5);

			ItemStack bow = new ItemStack(Material.BOW, 1);
			bow.addUnsafeEnchantment(Enchantment.ARROW_KNOCKBACK, 4);
			bow.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 1);

			pl.getInventory().setItem(0, stick);
			pl.getInventory().setItem(1, bow);
			pl.getInventory().setItem(8, new ItemStack(Material.ARROW, 1));
		}
	}

	public void endGame(Game gam, Player winner) {
		ArrayList<org.bukkit.Location> arr = gam.loc.locations;

		gam.Status = status.INPROGRESS;
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "gmysterybox give " + winner.getName() + " 1 3");
		for(int i=0; i<gam.players.size(); i++) {
			UUID uuid = gam.players.get(i);
			Player pl = Bukkit.getPlayer(uuid);
			winner.sendMessage(pitchout.text + " Well done you won one mystery box level 3.");
			Bukkit.dispatchCommand(pl, "hub");
		}
	}

}
