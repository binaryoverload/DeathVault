package com.wishiwashi.deathvault;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;



public class MenuManager implements Listener{
	
	private Inventory inv;
	
	public MenuManager(Plugin p) {

		Bukkit.getServer().getPluginManager().registerEvents(this, DeathVault.getplugin());
	}
	
	public void show(Player p, ItemStack[] items) {
		inv = Bukkit.getServer().createInventory(null, 45, "DeathVault: " + p.getName());
		
		int i = 0;
		for(ItemStack item : items){
			inv.setItem(i, item);
			i++;
		}
		
		p.openInventory(inv);
	}
}
