package vacuum.changedamage;

import java.util.HashMap;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class DamageListener implements Listener{
	
	private HashMap<Integer, Integer> damageMap = new HashMap<Integer, Integer>();
	private boolean pvpOnly = true;
	
	public void setPVPOnly(boolean val){
		pvpOnly = val;
	}
	
	@EventHandler(priority=EventPriority.NORMAL)
	public void entityDamaged(EntityDamageByEntityEvent evt){
		if(pvpOnly && !(evt.getEntity() instanceof Player))
			return;
		Player p;
		if(evt.getDamager() instanceof Player){
			p = (Player) evt.getDamager();
			int item = p.getItemInHand().getTypeId();
			Integer ret = damageMap.get(item);
			if(ret != null)
				evt.setDamage(ret);
		}
	}
	
	public void put(int k, int v){
		damageMap.put(k, v);
	}
	
	public void clear(){
		damageMap.clear();
	}
	
	public void remove(int k){
		damageMap.remove(k);
	}
}
