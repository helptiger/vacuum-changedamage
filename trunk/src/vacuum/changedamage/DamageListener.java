package vacuum.changedamage;

import java.util.HashMap;

import org.bukkit.World;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class DamageListener implements Listener{

	private HashMap<World, HashMap<Integer, Integer>> worldDamageMapMap = new HashMap<World, HashMap<Integer,Integer>>();
	private boolean pvpOnly = true;
	boolean verbose;
	private HashMap<World, Double> worldToArrowDamageMap = new HashMap<World, Double>();
	private HashMap<World, Integer> worldToDefaultDamageMap = new HashMap<World, Integer>();

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
			HashMap<Integer, Integer> damageMap = worldDamageMapMap.get(evt.getDamager().getWorld());
			if(damageMap == null || !damageMap.containsKey(item))
				damageMap = worldDamageMapMap.get(null);
			Integer ret = damageMap.get(item);
			if(verbose)
				System.out.println("Damage for type " + item + " was " + ret);
			if(ret == null){
				int defaultDamage;
				if(worldToDefaultDamageMap.containsKey(evt.getDamager().getWorld()))
					defaultDamage = worldToDefaultDamageMap.get(evt.getDamager().getWorld());
				else{
					if(worldToDefaultDamageMap.containsKey(null))
						defaultDamage = worldToDefaultDamageMap.get(null);
					else
						return; //no default damage
				}
				if(defaultDamage != -1){
					ret = defaultDamage;
					if(verbose)
						System.out.println("Ret was null. Changing ret to " + ret);
				}
			}
			if(ret != null){
				if(ret == -1){
					return;
				}
				evt.setDamage(ret);
				if(verbose)
					System.out.println("Set damage to " + ret);
			}
		} else if (evt.getDamager() instanceof Arrow){
			if(!(((Arrow)evt.getDamager()).getShooter() instanceof Player))
				return;
			double arrowDamage = worldToArrowDamageMap.get(evt.getDamager().getWorld());
			double raw = evt.getDamage() * arrowDamage;
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

	public void put(World w, int k, int v){
		if(!worldDamageMapMap.containsKey(w))
			worldDamageMapMap.put(w, new HashMap<Integer, Integer>());
		worldDamageMapMap.get(w).put(k, v);
	}

	public void clear(){
		worldDamageMapMap.clear();
	}

	public void remove(World w, int k){
		worldDamageMapMap.get(w).remove(k);
	}

	public void setArrowDamage(World w, double arrowDamage) {
		worldToArrowDamageMap.put(w, arrowDamage);
	}

	public void setDefaultDamage(World w, int damage){
		worldToDefaultDamageMap.put(w, damage);
	}
}
