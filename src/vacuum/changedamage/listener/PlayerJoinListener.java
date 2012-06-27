package vacuum.changedamage.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import vacuum.changedamage.equations.PostfixNotation;
import vacuum.changedamage.equations.element.number.Variable;
import vacuum.changedamage.hooks.RandomHook;

public class PlayerJoinListener implements Listener{
	
	private PostfixNotation expression;
	private Variable n;
	
	public PlayerJoinListener(PostfixNotation expression, Variable n){
		open(expression, n);
	}

	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
	public void onPlayerJoin(PlayerJoinEvent evt){
		RandomHook.applyHook(evt.getPlayer(), expression, n);
	}
	
	public void close(){
		for (Player p : Bukkit.getServer().getOnlinePlayers()) {
			RandomHook.removeHook(p);
		}
	}

	public void open(PostfixNotation expression, Variable n) {
		this.expression = expression;
		this.n = n;
		for (Player p : Bukkit.getServer().getOnlinePlayers()) {
			RandomHook.applyHook(p, expression, n);
		}
	}
}
