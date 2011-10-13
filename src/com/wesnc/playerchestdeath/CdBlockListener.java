package com.wesnc.playerchestdeath;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;

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
}
