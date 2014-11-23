package com.wesnc.playerchestdeath;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.griefcraft.model.Protection;

public class CdBlockListener implements Listener {
	
	ChestDeath plugin;
	
	public CdBlockListener(ChestDeath plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockBreak(BlockBreakEvent event) {
		if(!event.isCancelled()) {
			if(plugin.nodropblocks.contains(event.getBlock())) {
				Block block = event.getBlock();
				if(!plugin.mineabledrops) {
					event.setCancelled(true);
					if(block.getType() == Material.CHEST) {
						deleteChest(block);
					}else {
						block.setType(Material.AIR);
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockPistonRetract(BlockPistonRetractEvent event) {
		if(!event.isCancelled()) {
			if(plugin.nodropblocks.contains(event.getBlock()) && !plugin.mineabledrops) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockPistonExtend(BlockPistonExtendEvent event) {
		if(!event.isCancelled()) {
			if(plugin.nodropblocks.contains(event.getBlock()) && !plugin.mineabledrops) {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockDamage (BlockDamageEvent event) {
		if(!plugin.ChestLoot) {
			return;
		}
		Player player = event.getPlayer();
		Block chestblock = event.getBlock();
		if(player.isSneaking() && chestblock.getType() == Material.CHEST) {
			if(plugin.deathchests.containsKey(event.getBlock())) {
				if(plugin.LWC_Enabled && plugin.lwc != null) {
					Protection protection = plugin.lwc.findProtection(chestblock);
					if(protection.getType() == com.griefcraft.model.Protection.Type.PRIVATE) {
						if(protection.isOwner(player) || player.hasPermission("deadmanschest.loot")) {
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

    @EventHandler
    public void onCloseInventory (InventoryCloseEvent event) {
        Inventory inventory = event.getInventory();
        if (inventory.getHolder() instanceof Chest) {
            Chest chest = (Chest) inventory.getHolder();

            if (isEmpty(inventory)) {
                deleteDeathChest(chest);
            }
        } else if (inventory.getHolder() instanceof DoubleChest) {
            DoubleChest doubleChest = (DoubleChest) inventory.getHolder();
            Chest right = (Chest) doubleChest.getRightSide();
            Chest left = (Chest) doubleChest.getLeftSide();

            if (isEmpty(inventory)) {
                deleteDeathChest(right);
                deleteDeathChest(left);
            }
        }
    }

    private void deleteDeathChest(Chest chest) {
        if(plugin.nodropblocks.contains(chest.getBlock())) {
            Block block = chest.getBlock();

            if(! plugin.mineabledrops) {
                if(block.getType() == Material.CHEST) {
                    deleteChest(block);
                }else {
                    block.setType(Material.AIR);
                }
            }
        }
    }

    private void deleteChest(Block block) {
        //This fixes the sign bug and removes the glowstone tower
        //as well.
        if(plugin.deathchests.containsKey(block)) {
            RemoveChest dcstuff = plugin.deathchests.get(block);
            dcstuff.removeTheChest();
            //cancel the removal task.
            if(dcstuff.getTaskID() != -1) {
                plugin.getServer().getScheduler().cancelTask(dcstuff.getTaskID());
            }
        } else {
            block.setType(Material.AIR);
        }

        plugin.nodropblocks.remove(block);
    }

    private Boolean isEmpty(Inventory inventory) {
        for (ItemStack stack : inventory.getContents()) {
            if (stack != null) {
                return false;
            }
        }

        return true;
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
