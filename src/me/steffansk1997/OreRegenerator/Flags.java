package me.steffansk1997.OreRegenerator;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;

import com.mewin.WGCustomFlags.WGCustomFlagsPlugin;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.StateFlag;

public class Flags {
	private OreRegenerator plugin;
	public Flags(OreRegenerator plugin) {
		this.plugin = plugin;
		if(plugin.getConfig().getString("mode").equalsIgnoreCase("flag")){
			this.pluginWGCustomFlags = this.setWGCustomFlags();
			this.pluginWorldGuard = this.setWG();
			this.pluginWGCustomFlags.addCustomFlag(FLAG_REGENORES);
		}
	}
	public WGCustomFlagsPlugin setWGCustomFlags(){
		Plugin wgcf = Bukkit.getPluginManager().getPlugin("WGCustomFlags");
		if((wgcf == null) || (!(wgcf instanceof WGCustomFlagsPlugin))){
			return null;
		}
		return (WGCustomFlagsPlugin) wgcf;
	}
	public WorldGuardPlugin setWG(){
		Plugin wg = Bukkit.getPluginManager().getPlugin("WorldGuard");
		if((wg == null) || (!(wg instanceof WorldGuardPlugin))){
			return null;
		}
		return (WorldGuardPlugin) wg;
	}
	public WorldGuardPlugin getWG(){
		return this.pluginWorldGuard;
	}
	public WGCustomFlagsPlugin getWGCF(){
		return this.pluginWGCustomFlags;
	}
	private WGCustomFlagsPlugin pluginWGCustomFlags;
	private WorldGuardPlugin pluginWorldGuard;
	private static StateFlag FLAG_REGENORES = new StateFlag("regen-ores", false);
	public void handle(String i, Block bl) {
		WorldGuardPlugin wgp = this.plugin.getFlags().getWG();
		StateFlag.State state = (StateFlag.State)wgp.getRegionManager(bl.getWorld()).getApplicableRegions(bl.getLocation()).getFlag(FLAG_REGENORES);
		if(state == StateFlag.State.ALLOW && state != null){
			int delay = plugin.getConfig().getInt("delays."+i+".delay");
			plugin.sql.insertBlock(i, (int) bl.getData(), bl.getX(), bl.getY(), bl.getZ(), bl.getWorld().getName(), delay);
			if(plugin.getConfig().contains("delays."+bl.getType().name()+".empty")){
				Material type = bl.getType();
				plugin.el.setBlock(bl, Material.valueOf(plugin.getConfig().getString("delays."+type.name()+".empty").toUpperCase()));
			}else{
				plugin.el.setBlock(bl, Material.valueOf(plugin.getConfig().getString("empty").toUpperCase()));
			}
		}
	}
}
