package com.wishiwashi.deathvault;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import net.minecraft.server.v1_8_R3.Item;
import net.minecraft.server.v1_8_R3.MinecraftKey;

public class DeathVault extends JavaPlugin implements Listener{
	

	//Declare logger to use in other methods
	private Logger logger = getLogger();
	
	//Declare PlayerDatabase dir
	String userdata = this.getDataFolder() + File.separator + "PlayerDatabase";

	public void onEnable() {
		
		//Register Method Listener
		Bukkit.getServer().getPluginManager().registerEvents(this, this);
		//Setup config files
		saveDefaultConfig();
		//Log out that plugin is enabled
		logger.info("DeathVaults Enabled!");
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		//Check if the command is the correct one ("DeathRestore")
		if(command.getName().equalsIgnoreCase("deathrestore")){
			if(args.length == 0){
				if(sender instanceof Player) {
					Player p = (Player) sender;
					sender.sendMessage("Restoring Items...");
					deathcheck(p);
					return true;
				} else {
					sender.sendMessage(ChatColor.RED + "You cannot do this from the console with no arguments!");
					sender.sendMessage(ChatColor.DARK_RED + "Usage: " + command.getName() + " <Player>");
					return true;
				}
			} else if (args.length == 1){
				Player p = Bukkit.getPlayer(args[0]);
				if(p == null){
					sender.sendMessage(ChatColor.RED + "Argument supplied is not a valid player!");
					sender.sendMessage(ChatColor.DARK_RED + "Usage: " + command.getName() + " <Player>");
					return true;
				} else {
					deathcheck(p);
					return true;
				}
			}
			
		}
		return true;
	}
	
	//Testing Methods
		public void saveInv(Player p) throws IOException {
	        YamlConfiguration c = new YamlConfiguration();
	        String folder = userdata;
	        File f = new File(folder, File.separator + p.getUniqueId() + ".yml");
	        c.set("inventory.armor", p.getInventory().getArmorContents());
	        c.set("inventory.content", p.getInventory().getContents());
	        c.save(f);
	    }
		
		@SuppressWarnings("unchecked")
		public void restoreInv(Player p) throws IOException {
			String folder = userdata;
	        File f = new File(folder, File.separator + p.getUniqueId() + ".yml");
	        YamlConfiguration c = YamlConfiguration.loadConfiguration(f);
	        
	        ItemStack[] content = ((List<ItemStack>) c.get("inventory.armor")).toArray(new ItemStack[0]);
	        p.getInventory().setArmorContents(content);
	        content = ((List<ItemStack>) c.get("inventory.content")).toArray(new ItemStack[0]);
	        p.getInventory().setContents(content);
	    }
		//END Testing Methods
		
		private void deathSave(PlayerDeathEvent e) throws IOException {
			YamlConfiguration c = new YamlConfiguration();
			String folder = userdata;
	        File f = new File(folder, File.separator + e.getEntity().getUniqueId() + ".yml");
	        c.set("inventory", e.getDrops().toArray(new ItemStack[0]));
	        c.set("xpcost", e.getDroppedExp() * 1.25);
	        c.save(f);
		}
		
		@SuppressWarnings("unchecked")
		public void deathRestore(Player p) {
			String folder = this.userdata;
	        File f = new File(folder, File.separator + p.getUniqueId() + ".yml");
	        YamlConfiguration c = YamlConfiguration.loadConfiguration(f);
	        int xpcost = c.getInt("xpcost");
	        ItemStack[] content = ((List<ItemStack>) c.get("inventory")).toArray(new ItemStack[0]);
	        int xp = p.getTotalExperience();
	        
	        if(xp >= xpcost){
	        	MenuManager menu = new MenuManager(Bukkit.getServer().getPluginManager().getPlugin("DeathVault"));
	        	menu.show(p, content);
	        } else {
	        	p.sendMessage(ChatColor.RED + "You do not have the required EXP to restore your inventory! (" + xp + "/" + xpcost + ")");
	        }
		}
		
		public void deathcheck(Player p){
			String folder = this.userdata;
	        File f = new File(folder, File.separator + p.getUniqueId() + ".yml");
			if(f != null){
				YamlConfiguration c = YamlConfiguration.loadConfiguration(f);
				String itemID = getConfig().getString("cost-item");
				int costamount = getConfig().getInt("cost-amount");
				
				MinecraftKey mk = new MinecraftKey(itemID);
				ItemStack item = CraftItemStack.asNewCraftStack(Item.REGISTRY.get(mk));
				item.setAmount(costamount);
				
				boolean hasdied = c.isSet("inventory");
				if(hasdied) {
					
					if(p.getInventory().contains(item)){
						p.sendMessage("Hey! 2");
						p.getInventory().remove(item);
						deathRestore(p);
						f.delete();
					} else {
						p.sendMessage(ChatColor.RED + "You do not have the require items to restore your items!");
						p.sendMessage(ChatColor.DARK_RED + "Requirements: " + itemID + "*" + costamount);
					}
					
				}
			}
			
		}
		
		//Listener for player death using PlayerDeathEvent
		@EventHandler
		public void onPlayerDeath(PlayerDeathEvent e) {
				try {
					deathSave(e);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
		}
		
		public static Plugin getplugin() {
			return Bukkit.getServer().getPluginManager().getPlugin("DeathVaults");
		}
}
