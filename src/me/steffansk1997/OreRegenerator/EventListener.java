package me.steffansk1997.OreRegenerator;

import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.scheduler.BukkitRunnable;

public class EventListener implements Listener{
	private OreRegenerator plugin;
	public EventListener(OreRegenerator plugin){
		this.plugin = plugin;
	}
	@SuppressWarnings("deprecation")
	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onBlockBreak(BlockBreakEvent e){
		if(!plugin.getConfig().getBoolean("creative") && e.getPlayer().getGameMode() == GameMode.CREATIVE){
			return;
		}
		Material mat = e.getBlock().getType();
		Block bl = e.getBlock();
		Set<String> delays = plugin.getConfig().getConfigurationSection("delays").getKeys(false);
		for(String i:delays){
			if(Material.valueOf(i.toUpperCase()) == mat){
				if(plugin.getFlags() != null && plugin.getConfig().getString("mode").equalsIgnoreCase("flag")){
					plugin.getFlags().handle(i, bl);
				}else {
					int delay = plugin.getConfig().getInt("delays."+i+".delay");
					plugin.sql.insertBlock(i, (int) bl.getData(), bl.getX(), bl.getY(), bl.getZ(), bl.getWorld().getName(), delay);
					if(plugin.getConfig().contains("delays."+bl.getType().name()+".empty")){
						Material type = bl.getType();
						setBlock(bl, Material.valueOf(plugin.getConfig().getString("delays."+type.name()+".empty").toUpperCase()));
					} else{
						setBlock(bl, Material.valueOf(plugin.getConfig().getString("empty").toUpperCase()));
					}
				}
			}
		}
	}
	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		// TODO
	}
	@EventHandler
	public void onRightClick(final PlayerInteractEvent e) {
		if(e.getClickedBlock() == null) return;
		if(e.getHand() == EquipmentSlot.HAND && e.getAction().equals(Action.RIGHT_CLICK_BLOCK)){
			if(plugin.getConfig().getBoolean("right-click-message")) {
				boolean found = false;
				for(String block: plugin.getConfig().getConfigurationSection("delays").getKeys(false)) {
					String empty = plugin.getConfig().getString("delays." + block + ".empty");
					if(empty == null) continue;
					try {
						Material m = Material.valueOf(empty);
						if(e.getClickedBlock().getType() == m) {
							found = true;
							break;
						}
					} catch(Exception ex) {
						ex.printStackTrace();
					}
				}
				if(!found) return;
				new BukkitRunnable() {
					@Override 
					public void run() {
						if(plugin.sql.getBlockData("id", e.getClickedBlock().getWorld().getName(), e.getClickedBlock().getX(), e.getClickedBlock().getY(), e.getClickedBlock().getZ()) != null){
							String blocktype = plugin.getConfig().getString("delays."+ plugin.sql.getBlockData("material", e.getClickedBlock().getWorld().getName(), e.getClickedBlock().getX(), e.getClickedBlock().getY(), e.getClickedBlock().getZ()) + ".name");
							int secs = Integer.parseInt(plugin.sql.getBlockData("respawntime", e.getClickedBlock().getWorld().getName(), e.getClickedBlock().getX(), e.getClickedBlock().getY(), e.getClickedBlock().getZ()));
							e.getPlayer().sendMessage(ChatColor.RED + "[" + ChatColor.AQUA + "OreRegen" + ChatColor.RED + "] " + ChatColor.GREEN + "This " + ChatColor.RED + blocktype + ChatColor.GREEN + " will respawn in "+ secToHMS(secs));
						}
					}
				}.runTaskAsynchronously(plugin);
			}
		}
	}
	public String secToHMS (int secs){
		int hr = (int) Math.floor(secs/3600);
	    int rem = (int)(secs%3600);
	    int mn = (int) Math.floor(rem/60);
	    int sec = rem%60;
	    String hrs = (hr == 0 ? "" : ChatColor.RED +""+ hr + ChatColor.GREEN +" hour" +(hr == 1 ? "" : "s") + (mn == 0 ? (sec == 0 ? "" : " and ") : (sec == 0 ? " and " : ", ")));
	    String mns = (mn == 0 ? "" : ChatColor.RED +""+ mn + ChatColor.GREEN + " minute"+ (mn == 1 ?  "" : "s") + (sec == 0 ? "" : " and "));
	    String seco = (sec == 0 ? "" : ChatColor.RED+""+sec+ChatColor.GREEN + " second" + (sec == 1 ? "" : "s"));
		return hrs + mns + seco;
	}
	public void setBlock(final Block bl, final Material m){
        new BukkitRunnable() {
            @Override
            public void run() {
                bl.setType(m);
            }
        }.runTask(plugin);
    }
}
