package be.raffon.pitchout.scoreboards.games;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

public class Location {
	public Integer waiting_x;
	public Integer waiting_y;
	public Integer waiting_z;
	public String world;
	public ArrayList<org.bukkit.Location> locations;
	public Integer ground;
	
	public Location(ConfigurationSection obj) {
		this.waiting_x = obj.getInt("waiting.x");
		this.waiting_y = obj.getInt("waiting.y");
		this.waiting_z = obj.getInt("waiting.z");
		this.ground = obj.getInt("ground");
		
		this.world = obj.getString("world");
		
		this.locations = new ArrayList<org.bukkit.Location>();
        ConfigurationSection command = obj.getConfigurationSection("locations");
        for (String key : command.getKeys(false)) {
        	ConfigurationSection sect = obj.getConfigurationSection("locations."+key);
        	
        	
        	Integer x = sect.getInt("x");
        	Integer y = sect.getInt("y");
        	Integer z = sect.getInt("z");
        	org.bukkit.Location loc = new org.bukkit.Location(Bukkit.getWorld(this.world), x, y, z);
        	this.locations.add(loc);
        }
	}
}
