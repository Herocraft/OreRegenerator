package me.steffansk1997.OreRegenerator;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class OreRegenerator extends JavaPlugin{
	public final SQLManager sql = new SQLManager(this);
	public final EventListener el = new EventListener(this);
	private Flags flags;
	@Override
	public void onEnable(){
		this.saveDefaultConfig();
		this.getServer().getPluginManager().registerEvents(el, this);
		sql.initDatabase();
		startCheck();
		if(Bukkit.getPluginManager().getPlugin("WGCustomFlags") != null &&  Bukkit.getPluginManager().getPlugin("WGCustomFlags").isEnabled()) {
			this.flags = new Flags(this);
		}
	}
	@Override
	public void onDisable(){
		sql.closeConnection();
	}
	
	private void startCheck(){
		new BukkitRunnable() {
			@Override
			public void run() {
				sql.check();
			}			
		}.runTaskTimerAsynchronously(this, 0L, getConfig().getInt("interval")*20L);
	}
	public Flags getFlags() {
		return flags;
	}
}
