package com.wesnc.playerchestdeath;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.inventory.ItemStack;

import com.griefcraft.model.Protection;

public class EntLis extends EntityListener
{
	public ChestDeath plugin;
	
	public EntLis(ChestDeath instance)
	{
		plugin = instance;
	}
	
	public void onEntityDeath(EntityDeathEvent event)
	{
		Entity entity = event.getEntity();
		
		if(entity instanceof Player)
		{
			Player player = (Player)entity;
			Location lastLoc = player.getLocation();
			Block block = lastLoc.getBlock();

			LinkedList<Block> changedblocks = new LinkedList<Block>();
			Block tsignblock = null;
			//chest related
			block.setType(Material.CHEST);
			changedblocks.add(block);
			plugin.nodropblocks.add(block);
			BlockState state = block.getState();
			Chest chest = (Chest)state;
			Block protectionblock = null;
			
			Location chestLocation = chest.getBlock().getLocation();
			
			int j = 0;
			List<ItemStack> items = event.getDrops();
			for(int i = 0; i < items.size() && j < 27; i++)
			{
				ItemStack item = items.get(i);
				if(item != null && item.getType() != Material.AIR) {
					System.out.println("Adding item " + item.getType().toString() + " to chest.");
					chest.getInventory().addItem(item);
					items.remove(i);
					//A little hack to make sure the pointer is pointing to the right place...
					i--;
					j++;
				}
			}
			
			if(!this.plugin.drops)
			{
				event.getDrops().clear();
			}
			
			if(this.plugin.deathMessage)
			{
				this.plugin.getServer().broadcastMessage(ChatColor.RED + player.getDisplayName() + ChatColor.WHITE + " " + this.plugin.deathMessageString);
			}
			
			if(this.plugin.LWC_Enabled && plugin.lwc != null)
			{
				int blockId = chest.getTypeId();
				int type = 0;
				String world = chest.getWorld().getName();
				String owner = player.getName();
				String password = "";
				int x = chest.getX();
				int y = chest.getY();
				int z = chest.getZ();
				
				if(this.plugin.LWC_PrivateDefault)
				{
					type = com.griefcraft.model.ProtectionTypes.PRIVATE;
				}
				else
				{
					type = com.griefcraft.model.ProtectionTypes.PUBLIC;
				}
				plugin.lwc.getPhysicalDatabase().registerProtection(blockId, type, world, owner, password, x, y, z);
				protectionblock = block;
			}
			
			if(this.plugin.SignOnChest)
			{
				boolean foundair = false;
				BlockFace[] directions = {BlockFace.EAST,BlockFace.WEST,BlockFace.NORTH,BlockFace.SOUTH};
				byte[] signbyte = {0x2,0x3,0x4,0x5};
				int signdirection = 1;
				for(int i = 0; i < directions.length && !foundair; i++) {
					if(plugin.LiquidReplace) {
						//If we can replace water, let's do it with the sign too!
						Block tempblock = block.getRelative(directions[i]);
						if(tempblock.getType() == Material.AIR|| tempblock.getType() == Material.WATER 
								|| tempblock.getType() == Material.STATIONARY_WATER
								|| tempblock.getType() == Material.LAVA 
								|| tempblock.getType() == Material.STATIONARY_LAVA) {
							signdirection = i;
							foundair = true;
						}
					}else {
						if(block.getRelative(directions[i]).getType() == Material.AIR) {
							signdirection = i;
							foundair = true;
						}
					}
				}
				
				if(foundair) {
					//-----------------------------------------------------------
					Block signBlock = block.getRelative(directions[signdirection]);
					signBlock.setTypeIdAndData(68, signbyte[signdirection], false);
					BlockState signState = signBlock.getState();
					Sign sign = (Sign)signState;
					//-----------------------------------------------------------
					
					sign.setLine(0, player.getDisplayName()+"'s");
					sign.setLine(1, "Deathpile");
					sign.update();
					changedblocks.add(signBlock);
					plugin.nodropblocks.add(signBlock);
					plugin.signblocks.put(block, signBlock);
				}else {
					// If we didn't find a free spot, let's put the sign above the chest...
					// Will probably look very ugly though and pop off anyways...
					//-----------------------------------------------------------
					Block signBlock = block.getRelative(BlockFace.UP);
					signBlock.setTypeIdAndData(63, signbyte[signdirection], false);
					BlockState signState = signBlock.getState();
					Sign sign = (Sign)signState;
					//-----------------------------------------------------------
					
					sign.setLine(0, player.getDisplayName()+"'s");
					sign.setLine(1, "Deathpile");
					sign.update();
					changedblocks.add(signBlock);
					plugin.nodropblocks.add(signBlock);
					plugin.signblocks.put(block, signBlock);
				}
			}
			
			if(this.plugin.Sign_BeaconEnabled)
			{
				int height = this.plugin.Sign_BeaconHeight;
				Location chestLocation1 = block.getLocation();
				
				Location firstlocation = chestLocation1.add(0.0, 2.0, 0.0);
				Block nextblock = firstlocation.getBlock();
				
				for(int i = 0; i < height; i++) {
					if(plugin.LiquidReplace) {
						if(nextblock.getType() == Material.AIR || nextblock.getType() == Material.WATER 
								|| nextblock.getType() == Material.STATIONARY_WATER
								|| nextblock.getType() == Material.LAVA 
								|| nextblock.getType() == Material.STATIONARY_LAVA ) {
							nextblock.setType(Material.GLOWSTONE);
							plugin.nodropblocks.add(nextblock);
							changedblocks.add(nextblock);
						}
					}else {
						if(nextblock.getType() == Material.AIR) {
							nextblock.setType(Material.GLOWSTONE);
							plugin.nodropblocks.add(nextblock);
							changedblocks.add(nextblock);
						}
					}
					nextblock = nextblock.getRelative(BlockFace.UP);
				}
			}
			
			if(this.plugin.ChestDeleteIntervalEnabled)
			{
				int delay = this.plugin.ChestDeleteInterval*20;
				this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new RemoveChest(plugin, changedblocks, protectionblock), delay);
				
			}
	
		}
	}
}
