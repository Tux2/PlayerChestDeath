package com.wesnc.playerchestdeath;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.inventory.ItemStack;

public class EntLis extends EntityListener
{
	public ChestDeath plugin;
	
	public EntLis(ChestDeath instance) {
		plugin = instance;
	}
	
	public void onEntityDeath(EntityDeathEvent event) {
		Entity entity = event.getEntity();
		
		if(entity instanceof Player) {
			Player player = (Player)entity;
			if(plugin.hasPermissions(player, "deadmanschest.chest")) {
				Location lastLoc = player.getLocation();
				Block block = lastLoc.getBlock();
				//See if the block we are on is a block we can safely write over...
				if(!plugin.airblocks.contains(block.getType())) {
					//Must not be, let's go a block up and see if that one is free...
					Block tempblock = block.getRelative(BlockFace.UP);
					if(plugin.airblocks.contains(tempblock.getType())) {
						block = tempblock;
					}else {
						//We can't find an open spot, so just spill the stuff on the ground...
						return;
					}
				}
				boolean doublechest = false;
				int j = 0;
				List<ItemStack> items = event.getDrops();
				LinkedList<ItemStack> addeditems = new LinkedList<ItemStack>();
				int i;
				for(i = 0; i < items.size() && j < 27; i++)	{
					ItemStack item = items.get(i);
					if(item != null && item.getType() != Material.AIR) {
						addeditems.add(item);
						items.remove(i);
						//A little hack to make sure the pointer is pointing to the right place...
						i--;
						j++;
					}
				}
				//The player is carrying too many items to fit in one chest. Let's make it a double chest (if they have permission).
				if(j == 27 && plugin.hasPermissions(player, "deadmanschest.doublechest")) {
					BlockFace[] direction = {BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH};
					boolean noroom = true;
					for(int y = 0; y < direction.length && noroom; y++) {
						Block tempblock = block.getRelative(direction[y]);
						if(plugin.airblocks.contains(tempblock.getType())) {
							//we have an adjacent empty block, let's go ahead and add those items to another chest!
							for(; i < items.size(); i++)	{
								ItemStack item = items.get(i);
								if(item != null && item.getType() != Material.AIR) {
									addeditems.add(item);
									items.remove(i);
									//A little hack to make sure the pointer is pointing to the right place...
									i--;
									j++;
								}
							}
							//Let's exit the loop.
							noroom = false;
							doublechest = true;
						}
					}
				}
				
				if(!this.plugin.drops && !plugin.hasPermissions(player, "deadmanschest.drops"))	{
					event.getDrops().clear();
				}
				
				if(this.plugin.deathMessage && plugin.hasPermissions(player, "deadmanschest.message")) {
					this.plugin.getServer().broadcastMessage(ChatColor.RED + player.getDisplayName() + ChatColor.WHITE + " " + this.plugin.deathMessageString);
				}

				this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new CreateChest(plugin, block, addeditems, player, doublechest), 1);
			}
	
		}
	}
}
