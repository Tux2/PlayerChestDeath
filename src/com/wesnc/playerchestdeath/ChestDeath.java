package com.wesnc.playerchestdeath;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.griefcraft.lwc.*;
import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

public class ChestDeath extends JavaPlugin
{
	
	Logger logger = Logger.getLogger("Minecraft");
	
	public String mainDir = "plugins/DeadMansChest/";
	public File configFile = new File(mainDir+"Config.cfg");
	public Properties prop = new Properties();
	public LinkedList<Block> nodropblocks = new LinkedList<Block>();
	public ConcurrentHashMap<Block, Block> signblocks = new ConcurrentHashMap<Block, Block>();
	
	EntLis entityListener = new EntLis(this);

	public LWC lwc = null;
	
	public String configFileHeader = 
		"Edit this file as needed.\n" +
		"Death Message must be true for the death message String to work!" +
		"ChestDeleteInterval is in seconds.\n";
	
	public boolean drops = true;
	public boolean mineabledrops = false;
	public boolean deathMessage = true;
	public String deathMessageString = "died and left a chest where he died.";
	public boolean SignOnChest = true;
	public boolean LWC_Enabled = true;
	public boolean LWC_PrivateDefault =true;
	public boolean Sign_BeaconEnabled = true;
	public int Sign_BeaconHeight = 10;
	public boolean LiquidReplace = true;
	public int ChestDeleteInterval = 80;
	public boolean ChestDeleteIntervalEnabled = true;

	private static PermissionHandler Permissions;
	
	@Override
	public void onDisable()
	{
		logger.log(Level.INFO, "[DeadMansChest] unloaded.");
		
	}

	@Override
	public void onEnable()
	{
		setupPermissions();
		Plugin lwcPlugin = getServer().getPluginManager().getPlugin("LWC");
		if(lwcPlugin != null) {
			System.out.println("[DeadMansChest] LWC plugin found!");
		    lwc = ((LWCPlugin) lwcPlugin).getLWC();
		}
		logger.log(Level.INFO, "[DeadMansChest] loaded.");
		new File(mainDir).mkdir();
		
		if(!configFile.exists())
		{
			try
			{
				configFile.createNewFile();
				FileOutputStream out = new FileOutputStream(configFile);
				prop.put("DropsEnabled", "true");
				prop.put("DeathMessage", "true");
				prop.put("DeathMessageString", "died and left a chest where he died.");
				prop.put("SignOnChest", "true");
				prop.put("LWCEnabled", "true");
				prop.put("LWCPrivateDefault", "true");
				prop.put("BeaconEnabled", "true");
				prop.put("BeaconHeight", "10");
				prop.put("BeaconReplacesLiquid", "true");
				prop.put("MineableDrops", "false");
				prop.put("ChestDeleteIntervalEnabled", "true");
				prop.put("ChestDeleteInterval", "80");
				prop.store(out, configFileHeader);
				out.flush();
				out.close();
			}
			catch(IOException ex) {}
		}
		else
		{
			loadConfig();
		}

		registerEvents();
	}

	private void loadConfig()
	{
		try
		{
			FileInputStream in = new FileInputStream(configFile);
			prop.load(in);
			
			drops = Boolean.parseBoolean(prop.getProperty("DropsEnabled"));
			deathMessage = Boolean.parseBoolean(prop.getProperty("DeathMessage"));
			deathMessageString = prop.getProperty("DeathMessageString");
			SignOnChest = Boolean.parseBoolean(prop.getProperty("SignOnChest"));
			LWC_Enabled = Boolean.parseBoolean(prop.getProperty("LWCEnabled"));
			LWC_PrivateDefault = Boolean.parseBoolean(prop.getProperty("LWCPrivateDefault"));
			Sign_BeaconEnabled = Boolean.parseBoolean(prop.getProperty("BeaconEnabled"));
			Sign_BeaconHeight = Integer.parseInt(prop.getProperty("BeaconHeight"));
			LiquidReplace = Boolean.parseBoolean(prop.getProperty("BeaconReplacesLiquid"));
			mineabledrops = Boolean.parseBoolean(prop.getProperty("MineableDrops"));
			ChestDeleteInterval = Integer.parseInt(prop.getProperty("ChestDeleteInterval"));
			ChestDeleteIntervalEnabled = Boolean.parseBoolean(prop.getProperty("ChestDeleteIntervalEnabled"));
		}
		catch(IOException ex) { }
		
	}

	private void registerEvents()
	{
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvent(Event.Type.ENTITY_DEATH, entityListener, Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.BLOCK_BREAK, new CdBlockListener(this), Event.Priority.Monitor, this);
		pm.registerEvent(Event.Type.BLOCK_PISTON_EXTEND, new CdBlockListener(this), Event.Priority.Monitor, this);
		pm.registerEvent(Event.Type.BLOCK_PISTON_RETRACT, new CdBlockListener(this), Event.Priority.Monitor, this);
	}
    
    private void setupPermissions() {
        Plugin permissions = this.getServer().getPluginManager().getPlugin("Permissions");

        if (Permissions == null) {
            if (permissions != null) {
                Permissions = ((Permissions)permissions).getHandler();
            } else {
            }
        }
    }
    
    public boolean hasPermissions(Player player, String node) {
        if (Permissions != null) {
            return Permissions.has(player, node);
        } else {
            return player.hasPermission(node);
        }
    }

}
