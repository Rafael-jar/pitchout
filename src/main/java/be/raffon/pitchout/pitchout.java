package be.raffon.pitchout;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import be.raffon.pitchout.scoreboards.FastBoard;
import be.raffon.pitchout.scoreboards.games.Game;
import be.raffon.pitchout.scoreboards.games.GameManager;
import be.raffon.pitchout.scoreboards.games.status;

public class pitchout extends JavaPlugin implements Listener {
	
	String host;
	int port;
	String database;
	String username;
	String password;
	public static YamlConfiguration config;
	public static File fileconfig;
	public static String text = "["+ChatColor.RED+"Pichout"+ChatColor.WHITE+"]";
	public GameManager games;
	public HashMap<Integer, Integer> tasks = new HashMap<Integer, Integer>();
	public static int task = 0;
	private final Map<UUID, FastBoard> boards = new HashMap<UUID, FastBoard>();
	SQLManager sqlmanager;
	
	@SuppressWarnings("static-access")
	@Override
	public void onEnable() {
		System.out.println("Pitchout succeffuly loaded !");
		
		System.setProperty("file.encoding", "UTF-8");
		getServer().getPluginManager().registerEvents(this, this);
		this.fileconfig = new File(this.getDataFolder().getPath(), "config.yml");
		YamlConfiguration config = YamlConfiguration.loadConfiguration(fileconfig);
		this.config = config;
		games = new GameManager();
		host = "localhost";
		port = 3306;
		database = "sf2021";
		username = "sf2021";
		password = "Lq%n9aajZS7CtU";
		sqlmanager = new SQLManager(host, port, database, username, password);
        
		this.tasks.put(-1, Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            public void run() {
	            for(Player player : Bukkit.getOnlinePlayers()) {
	            	FastBoard board = boards.get(player.getUniqueId());
	            	Game game = games.getPlayerGame(player);
	            	if(game == null) return;
	            	if(game.Status == status.INPROGRESS) {
	            		Integer survivors = 0;
	            		for(UUID id : game.players) {
							Player pl = Bukkit.getPlayer(id);
	            			if(pl.getGameMode() == GameMode.SURVIVAL) {
	            				survivors = survivors + 1;
	            			}
	            		}
	            		board.updateLines(
		                        "",
		                        ChatColor.GOLD + "Players: " + ChatColor.WHITE + survivors,
		                        ""
		                );
	            		
	            	}
	            	
	            	
	            	
	                /**/
	            }
            }
		}, 0L, 20L));
	}
	
	 /*@EventHandler
	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
       if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Console can not use this plugin!");
            return true;
        }
        Player player = (Player) sender;
        if(args[0] == "addLoc") {
        	sender.sendMessage(text+"");
        }
        if(args[0] == "game" && args.length >= 2) {
        	if(args[1] == "waiting") {
        		try {
					Region rg = worldeditpl.getSession(player).getSelection((World) player.getWorld());
					
				} catch (IncompleteRegionException e) {
					// TODO Auto-generated catch block
					player.sendMessage(text + " The region is not complete, please use worldedit to do the region of the waitingroom.");
				}
        	} if(args[1] == "limit") {
				try {
				    Integer n = Integer.parseInt(args[2]);
				    
				    
				} catch (NumberFormatException e) {
				    // str is not a number
				}
        	}
        }
		return true;
		
	}*/
    

	
    @EventHandler
    public void onJoin(PlayerJoinEvent evt) {
    	
    	evt.getPlayer().setGameMode(GameMode.SURVIVAL);

    	if(getStaff(evt.getPlayer())) return;
    	
    	Player p = evt.getPlayer();

    	Game game = games.getPopularGame();
    	
    	game.addPlayer(evt.getPlayer());   
    	
        Player player = evt.getPlayer();

        FastBoard board = new FastBoard(player);

        board.updateTitle(ChatColor.RED + "Pitchout");

        boards.put(player.getUniqueId(), board);
    	
    	if(game.players.size() >= games.minimum && game.Status == status.WAITING) {
    		game.broadCast(text + ChatColor.RED + evt.getPlayer().getDisplayName() + ChatColor.WHITE + " joined the waiting room (" + ChatColor.BLUE + game.players.size() + "/" + games.maximum + ChatColor.WHITE + ")" + ChatColor.WHITE + "." + " The game will start soon.");
    		game.start(this);
    	} else if(game.Status == status.STARTING){
    		game.broadCast(text + ChatColor.RED + evt.getPlayer().getDisplayName() + ChatColor.WHITE + " joined the waiting room (" + ChatColor.BLUE + game.players.size() + "/" + games.maximum + ")" + ChatColor.WHITE + "." + ChatColor.BLUE + (games.minimum - game.players.size()) + ChatColor.WHITE + " players left to start the game.");
    	}
    	
    	p.getInventory().clear();
    	
    	
    }
    
	public Boolean getStaff(Player pl) {
		AtomicReference<Boolean> bool = new AtomicReference<Boolean>();
		System.out.println(pl.hasPermission("staff.staff"));
		if(!pl.hasPermission("staff.staff")) return false;
		bool.set(false);
		sqlmanager.getInstance().query("SELECT * FROM staff_players WHERE username = '" + pl.getUniqueId() + "';", rs -> {
			try {
				if(rs.next()) {
					Boolean staff = rs.getBoolean("staff");
					String world = rs.getString("world");
					System.out.println(staff);
					if(staff) {
						bool.set(true);
						return;
					}
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		return bool.get();
		
	}
    
    /*@EventHandler
    public void onPlayerDamage(EntityDamageEvent e){
    	if (!(e.getEntity() instanceof  Player)) return;
    	Player pl = (Player) e.getEntity();
    	
    	
    	if (DamageCause.VOID == e.getCause()){
    		e.setCancelled(true);
    		Game game = games.getPlayerGame(pl);
    		
    		Random rand = new Random();
    		Integer in = game.loc.locations.size();
    		int random = rand.nextInt(in);
    		
    		Location loc = game.loc.locations.get(random);
    		pl.setFallDistance(0.0f);
    		pl.teleport(loc);
    		pl.setFoodLevel(15);
    		
    		game.removeLive(pl);
    		
    		Integer survivors = 0;
    		Player sur = null;
    		for(Player player : game.players) {
    			if(player.getGameMode() == GameMode.SURVIVAL) {
    				survivors = survivors + 1;
    				sur = player;
    			}
    		}
    		if(survivors == 1) {
    			game.broadCast(text + " "+sur.getDisplayName() + " is the winner !");
    			game.end();
    		}
    		
    		if(game.damagers.containsKey(pl.getUniqueId())) {
    			return;
    		}
    		UUID killer = game.damagers.get(pl.getUniqueId());

    		Integer kills = game.scores.get(killer);
    		game.scores.remove(killer);
    		game.scores.put(killer, kills);
    		
    		Bukkit.getPlayer(killer).sendMessage(text + "You now have " + kills + " kills.");

    	}
    	
    }*/
    
    @EventHandler
    public void onMove(PlayerMoveEvent e) {
    	Player pl = e.getPlayer();
    	
		Integer y = pl.getLocation().getBlockY();
		Game game = games.getPlayerGame(pl);



		if(game == null) return;
		pl.setHealth(20);
		pl.setFoodLevel(20);
		if(game.Status != status.INPROGRESS) return;


		if(game.loc.ground-2 >= y) {
			Random rand = new Random();
			Integer in = game.loc.locations.size();
			int random = rand.nextInt(in);
			
			Location loc = game.loc.locations.get(random);
			pl.setFallDistance(0.0f);
			pl.teleport(loc);
			pl.setFoodLevel(15);
			
			game.removeLive(pl);
			
			Integer survivors = 0;
			Player sur = null;
			for(UUID id : game.players) {
				Player player = Bukkit.getPlayer(id);
				if(player.getGameMode() == GameMode.SURVIVAL) {
					survivors = survivors + 1;
					sur = player;
				}
			}
			if(survivors == 1) {
				game.broadCast(text + " "+sur.getDisplayName() + " is the winner !");
				games.endGame(game, sur);
			}
			
			if(game.damagers.get(pl.getUniqueId()) == null) {
				return;
			}
			UUID killer = game.damagers.get(pl.getUniqueId());

			Integer kills = game.scores.get(killer);
			game.scores.remove(killer);
			game.scores.put(killer, kills);
			
			Bukkit.getPlayer(killer).sendMessage(text + " You now have " + kills + " kills.");
		}


    }
    
    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event)
    {
        event.setCancelled(true);
    }
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
    	System.out.println("break" + " " + getStaff(event.getPlayer()));
    	if(!getStaff(event.getPlayer())) {
    		
    		event.setCancelled(true);
    	
    	}
    }
    
    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
    	if (!(e.getEntity() instanceof  Player)) return;
    	Player pl = (Player) e.getEntity();

    	/*if (e.getDamager() instanceof Player) {
    		Player damager = (Player) e.getDamager();
    		
    		Game game = this.games.getPlayerGame(pl);
    		Game game2 = this.games.getPlayerGame(damager);
    		
    		if(game.equals(game2)) {

    			pl.setHealth(20);
    			pl.setFoodLevel(20);

    			if(game.Status == status.WAITING) {e.setCancelled(true); return;}
    			
    			if(game.damagers.get(pl.getUniqueId()) == null) {
    				game.damagers.put(pl.getUniqueId(), damager.getUniqueId());
    			} else {
    				game.damagers.remove(pl.getUniqueId());
    				game.damagers.put(pl.getUniqueId(), damager.getUniqueId());
    			}
    		}
    		
    		
    	}*/
    	
    }
    
    
    @EventHandler
    public void onLeave(PlayerQuitEvent evt) {
    	Game game = null;
		try {
			game = this.games.getPlayerGame(evt.getPlayer());
		} catch(NullPointerException e1) {
			return;
		}
		if(game == null) return;
		game.players.remove(evt.getPlayer().getUniqueId());
    	if(game.Status == status.STARTING && game.players.size()-1 < games.minimum) {
    		game.Status = status.CANCELLED_START;
    	}
    }
    
    public static void logToFile(String key, Object value){
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(fileconfig);
        yaml.set(key, value);
        try{
            yaml.save(fileconfig);
            config = YamlConfiguration.loadConfiguration(fileconfig);
        }catch(Exception e){
             e.printStackTrace();
        }
    }
    
    
    public static Object getObj(String key){
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(fileconfig);
        return yaml.get(key);
    }

	public static ArrayList<ConfigurationSection> getLocation() {
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(fileconfig);
        ArrayList<ConfigurationSection> arr = new ArrayList<ConfigurationSection>();
        
        ConfigurationSection command = yaml.getConfigurationSection("locations");
        for (String key : command.getKeys(false)) {
        	ConfigurationSection sect = yaml.getConfigurationSection("locations."+key);
        	arr.add(sect);
        }
        
        return arr;
	}
	
	@EventHandler
	public void dropEvent(PlayerDropItemEvent event){
    	if(getStaff(event.getPlayer())) return;
		event.setCancelled(true);
	}

}
