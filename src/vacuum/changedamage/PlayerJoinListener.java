package vacuum.changedamage;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import vacuum.changedamage.hooks.RandomHook;

public class PlayerJoinListener implements Listener{
	
	public PlayerJoinListener(){
		open();
	}

	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
	public void onPlayerJoin(PlayerJoinEvent evt){
		RandomHook.applyHook(evt.getPlayer());
	}
	
	public void close(){
		for (Player p : Bukkit.getServer().getOnlinePlayers()) {
			RandomHook.removeHook(p);
		}
	}

	public void open() {
		for (Player p : Bukkit.getServer().getOnlinePlayers()) {
			RandomHook.applyHook(p);
		}
	}
}
