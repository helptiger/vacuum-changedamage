package vacuum.changedamage;

import java.util.HashMap;

import net.minecraft.server.EntityDamageSource;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class DamageListener implements Listener{

	private HashMap<Integer, Integer> damageMap = new HashMap<Integer, Integer>();
	private boolean pvpOnly = true;
	boolean verbose;
	private double arrowDamge;
	private int defaultDamage = -1;

	public void setPVPOnly(boolean val){
		pvpOnly = val;
	}

	public void setVerbose(boolean verbose){
		this.verbose = verbose;
	}

	@EventHandler(priority=EventPriority.NORMAL)
	public void entityDamaged(EntityDamageByEntityEvent evt){
		Player p;
		if(evt.getDamager() instanceof Player){
			if(pvpOnly && !(evt.getEntity() instanceof Player))
				return;
			p = (Player) evt.getDamager();
			int item = p.getItemInHand().getTypeId();
			Integer ret = damageMap.get(item);
			System.out.println("Damage for type " + item + " was " + ret);
			if(ret == null){
				if(defaultDamage != -1){
					ret = defaultDamage;
					if(verbose)
						System.out.println("Ret was null. Changing ret to " + ret);
				}
			}
			if(ret != null){
				evt.setDamage(ret);
				if(verbose)
					System.out.println("Set damage to " + ret);
			}
		} else if (evt.getDamager() instanceof Arrow){
			if(!(((Arrow)evt.getDamager()).getShooter() instanceof Player))
				return;
			double raw = evt.getDamage() * arrowDamge;
			if(verbose)
				System.out.println("Raw arrow damage: " + raw);
			evt.setDamage(round(raw));
			if(verbose)
				System.out.println("Set arrow damage to " + evt.getDamage());
		}
	}

	private int round(double d) {
		return ((int)d) + (((d - (int)d) > Math.random()) ? 1 : 0) ;
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

	public void setArrowDamage(double arrowDamage) {
		this.arrowDamge = arrowDamage;
	}
	
	public void setDefaultDamage(int damage){
		this.defaultDamage = damage;
	}
}
