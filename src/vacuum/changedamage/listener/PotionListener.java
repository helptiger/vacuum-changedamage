package vacuum.changedamage.listener;

import java.util.HashMap;

import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityRegainHealthEvent;

public class PotionListener implements Listener{

	private boolean modifyMobDamage;
	private HashMap<Integer, Double> idToAmplifierMod = new HashMap<Integer, Double>();
	private HashMap<Integer, Double> idToDurationMod = new HashMap<Integer, Double>();
	private boolean verbose;

	public PotionListener(){
		//empty constructor
	}

	/*@EventHandler(priority = EventPriority.NORMAL)
	public void onPotionSplash(PotionSplashEvent evt){
		if(verbose)
			System.out.println("Splash potion detected");
		Collection<PotionEffect> effects = evt.getPotion().getEffects();
		System.out.println(effects);
		PotionEffect[] effectsAr = effects.toArray(new PotionEffect[0]);
		effects.clear();
		for (int i = 0; i < effectsAr.length; i++) {
			int id = effectsAr[i].getType().getId();
			if(verbose)
				System.out.println("Modifying potion effect " + id);
			int duration = (int) (effectsAr[i].getDuration() * idToAmplifierMod.get(id));
			int amplifier = (int) (effectsAr[i].getAmplifier() *  idToAmplifierMod.get(id));
			if(verbose){
				System.out.println("Duration: " + effectsAr[i].getDuration() + "->" + duration);
				System.out.println("Amplifier: " + effectsAr[i].getAmplifier() + "->" + amplifier);
			}
			effects.add(new PotionEffect(effectsAr[i].getType(), duration, amplifier));
		}
	}*/
	
	public void onEntityDamaged(EntityDamageEvent evt){
		if(evt.getCause().equals(DamageCause.MAGIC)){
				
		} else if (evt.getCause().equals(DamageCause.POISON)){
			
		}
	}
	
	public void onEntityHealed(EntityRegainHealthEvent evt){
		//if(evt.getRegainReason().equals(RegainReason.))
	}

	public void clear(){
		modifyMobDamage = false;
		idToAmplifierMod.clear();
		idToDurationMod.clear();
	}

	public void setPVPOnly(boolean pvponly){
		modifyMobDamage = !pvponly;
	}

	public void putAmplifier(int id, double modifier) {
		idToAmplifierMod.put(id, modifier);
	}

	public void putDuration(int id, double modifier) {
		idToDurationMod.put(id, modifier);
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}
}
