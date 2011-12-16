package com.wesnc.playerchestdeath;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.griefcraft.model.Protection;

public class CdBlockListener extends BlockListener {
	
	ChestDeath plugin;
	
	public CdBlockListener(ChestDeath plugin) {
		this.plugin = plugin;
	}
	
	public void onBlockBreak(BlockBreakEvent event) {
		if(!event.isCancelled()) {
			if(plugin.nodropblocks.contains(event.getBlock())) {
				Block block = event.getBlock();
				if(!plugin.mineabledrops) {
					event.setCancelled(true);
					block.setType(Material.AIR);
				}
				plugin.nodropblocks.remove(block);
			}
		}
	}
	
	public void onBlockPistonRetract(BlockPistonRetractEvent event) {
		if(!event.isCancelled()) {
			if(plugin.nodropblocks.contains(event.getBlock()) && !plugin.mineabledrops) {
				event.setCancelled(true);
			}
		}
	}

	public void onBlockPistonExtend(BlockPistonExtendEvent event) {
		if(!event.isCancelled()) {
			if(plugin.nodropblocks.contains(event.getBlock()) && !plugin.mineabledrops) {
				event.setCancelled(true);
			}
		}
	}
	
	public void onBlockDamage (BlockDamageEvent event) {
		Player player = event.getPlayer();
		Block chestblock = event.getBlock();
		if(player.isSneaking() && chestblock.getType() == Material.CHEST) {
			if(plugin.deathchests.containsKey(event.getBlock())) {
				if(plugin.LWC_Enabled && plugin.lwc != null) {
					Protection protection = plugin.lwc.findProtection(chestblock);
					if(protection.getType() == com.griefcraft.model.ProtectionTypes.PRIVATE) {
						if(protection.isOwner(player) || plugin.hasPermissions(player, "deadmanschest.loot")) {
							lootChest(player, chestblock);
						}
					}else {
						lootChest(player, chestblock);
					}
				}else {
					lootChest(player, chestblock);
				}
			}
		}
	}
	
	private void lootChest(Player player, Block chestblock) {
		PlayerInventory pi = player.getInventory();
		BlockState state = chestblock.getState();
		Chest chest = (Chest)state;
		ItemStack[] chestinventory = chest.getInventory().getContents();
		for(int i = 0; i < chestinventory.length && pi.firstEmpty() != -1; i++) {
			if(chestinventory[i] != null) {
				pi.addItem(chestinventory[i]);
				chest.getInventory().removeItem(chestinventory[i]);
			}
		}
		RemoveChest rc = plugin.deathchests.get(chestblock);
		//Looting double chests requires more work...
		if(rc.chestblock2 != null) {
			state = rc.chestblock2.getState();
			chest = (Chest)state;
			chestinventory = chest.getInventory().getContents();
			for(int i = 0; i < chestinventory.length && pi.firstEmpty() != -1; i++) {
				if(chestinventory[i] != null) {
					pi.addItem(chestinventory[i]);
					chest.getInventory().removeItem(chestinventory[i]);
				}
			}
		}
		rc.removeTheChest();
		if(rc.getTaskID() != -1) {
			plugin.getServer().getScheduler().cancelTask(rc.getTaskID());
		}
	}
}
