package vacuum.changedamage.listener;

import java.util.HashMap;
import java.util.Random;

import net.minecraft.server.EnchantmentManager;
import net.minecraft.server.EntityLiving;
import net.minecraft.server.EntityPlayer;
import net.minecraft.server.MobEffectList;

import org.bukkit.World;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import vacuum.changedamage.equations.PostfixNotation;
import vacuum.changedamage.equations.element.number.Variable;
import vacuum.changedamage.equations.element.number.VariablePool;

public class DamageListener implements Listener{

	private static Random r = new Random();

	public final VariablePool pool = new VariablePool(false);

	{
		char[] variables = {'i', 's', 'w', 'd'};
		for(char c : variables)
			pool.register(String.valueOf(c), 0);
	}

	private HashMap<World, HashMap<Integer, Integer>> worldDamageMapMap = new HashMap<World, HashMap<Integer,Integer>>();
	private boolean pvpOnly = true;
	boolean verbose;
	private HashMap<World, Double> worldToArrowDamageMap = new HashMap<World, Double>();
	private HashMap<World, Integer> worldToDefaultDamageMap = new HashMap<World, Integer>();
	private EventPriority priority = EventPriority.NORMAL;
	private HashMap<String, PostfixNotation> equations = new HashMap<String, PostfixNotation>();

	public void setPVPOnly(boolean val){
		pvpOnly = val;
	}

	public void setVerbose(boolean verbose){
		this.verbose = verbose;
	}

	public void setEventPriority(String priority){
		try{
			this.priority = EventPriority.valueOf(priority.toUpperCase());
		} catch (Exception ex){
			this.priority = EventPriority.NORMAL;
		}
	}

	@EventHandler(priority=EventPriority.LOWEST)
	public void entityDamagedLOWEST(EntityDamageByEntityEvent evt){
		if(priority.equals(EventPriority.LOWEST))
			entityDamaged(evt);
	}

	@EventHandler(priority=EventPriority.LOW)
	public void entityDamagedLOW(EntityDamageByEntityEvent evt){
		if(priority.equals(EventPriority.LOW))
			entityDamaged(evt);
	}

	@EventHandler(priority=EventPriority.NORMAL)
	public void entityDamagedNORMAL(EntityDamageByEntityEvent evt){
		if(priority.equals(EventPriority.NORMAL))
			entityDamaged(evt);
	}

	@EventHandler(priority=EventPriority.HIGH)
	public void entityDamagedHIGH(EntityDamageByEntityEvent evt){
		if(priority.equals(EventPriority.HIGH))
			entityDamaged(evt);
	}

	@EventHandler(priority=EventPriority.HIGHEST)
	public void entityDamagedHIGHEST(EntityDamageByEntityEvent evt){
		if(priority.equals(EventPriority.HIGHEST))
			entityDamaged(evt);
	}

	public void entityDamaged(EntityDamageByEntityEvent evt){
		Player p;
		if(evt.getDamager() instanceof Player){
			if(!((Player)evt.getDamager()).hasPermission("vacuum.changedamage.damage"))
				return;
			if(pvpOnly && !(evt.getEntity() instanceof Player))
				return;
			p = (Player) evt.getDamager();
			int item = p.getItemInHand().getTypeId();

			HashMap<Integer, Integer> damageMap = worldDamageMapMap.get(evt.getDamager().getWorld());

			/* check if a damageMap is registered for this world */
			if(damageMap == null || !damageMap.containsKey(item))
				damageMap = worldDamageMapMap.get(null);

			/* get a value for the item */
			Integer ret = damageMap.get(item);

			if(verbose)
				System.out.println("Damage for type " + item + " was " + ret);

			/* if there value is unspecified... */
			if(ret == null){
				int defaultDamage;
				if(worldToDefaultDamageMap.containsKey(evt.getDamager().getWorld()))
					defaultDamage = worldToDefaultDamageMap.get(evt.getDamager().getWorld());
				else{
					if(worldToDefaultDamageMap.containsKey(null))
						defaultDamage = worldToDefaultDamageMap.get(null);
					else
						defaultDamage = -1;
				}
				ret = defaultDamage;
				if(defaultDamage != -1){
					if(verbose)
						System.out.println("[ChangeDamage] Found default damage. Changing damage to " + ret);
				} else if (verbose)
					System.out.println("[ChangeDamage] Found no default damage. Using MC default");
			}
			if(ret != null){
				if(ret == -1){
					ret = ((CraftPlayer)p).getHandle().inventory.a(((CraftEntity)evt.getEntity()).getHandle());
					if(verbose)
						System.out.println("[ChangeDamage] Identified MC default damage as " + ret);
				}

				if(verbose)
					System.out.println("Applying effects");

				/* Enchantments, MobEffects, Critical */
				ret = applyEffects(ret, p, evt.getEntity());

				evt.setDamage(ret);
				if(verbose)
					System.out.println("Set damage to " + ret);
			}
		} else if (evt.getDamager() instanceof Arrow){
			if(!(((Arrow)evt.getDamager()).getShooter() instanceof Player) || ((Player)(((Arrow)evt.getDamager()).getShooter())).hasPermission("vacuum.changedamage.damage"))
				return;

			Double arrowDamage = 
					worldToArrowDamageMap
					.get(
							evt
							.getDamager()
							.getWorld()
							);
			if(arrowDamage == null)
				return;
			double raw = evt.getDamage() * arrowDamage;
			if(verbose)
				System.out.println("Raw arrow damage: " + raw);
			evt.setDamage(round(raw));
			if(verbose)
				System.out.println("Set arrow damage to " + evt.getDamage());
		}
	}

	private int applyEffects(int ret, Player p, Entity entity) {
		EntityPlayer mcPlayer = ((CraftPlayer)p).getHandle();

		Variable damage = pool.getVariable("i");
		damage.setValue(ret);

		if(verbose)
			System.out.println("Initial: " + damage.getValue());

		/* apply weakness */
		if(mcPlayer.hasEffect(MobEffectList.WEAKNESS))
			if(equations.containsKey("weakness")){
				pool.getVariable("w").setValue(mcPlayer.getEffect(MobEffectList.WEAKNESS).getAmplifier());
				damage.setValue(equations.get("weakness").evaluate());
			} else {
				damage.setValue(damage.getValue() - (2 << mcPlayer.getEffect(MobEffectList.INCREASE_DAMAGE).getAmplifier()));
			}

		if(verbose)
			System.out.println("Weakness result: " + damage.getValue());

		/* apply strength */
		if(mcPlayer.hasEffect(MobEffectList.INCREASE_DAMAGE))
			if(equations.containsKey("strength")){
				pool.getVariable("s").setValue(mcPlayer.getEffect(MobEffectList.INCREASE_DAMAGE).getAmplifier());
				damage.setValue(equations.get("strength").evaluate());
			} else {
				damage.setValue(damage.getValue() +(3 << mcPlayer.getEffect(MobEffectList.INCREASE_DAMAGE).getAmplifier()));
			}

		if(verbose)
			System.out.println("Strength result: " + damage.getValue());

		/* apply enchantment */
		damage.setValue(damage.getValue() + EnchantmentManager.a(mcPlayer.inventory, (EntityLiving)((CraftEntity)entity).getHandle()));

		if(verbose)
			System.out.println("Enchantment result: " + damage.getValue());

		/* apply critical */
		if((mcPlayer.fallDistance > 0.0F) && (!mcPlayer.onGround) && (!mcPlayer.t()) && (!mcPlayer.aU()) && (!mcPlayer.hasEffect(MobEffectList.BLINDNESS)) && (mcPlayer.vehicle == null) && ((CraftEntity)entity).getHandle() instanceof EntityLiving){
			if(equations.containsKey("critical")){
				pool.getVariable("d").setValue(mcPlayer.fallDistance);
				damage.setValue(equations.get("critical").evaluate());
			} else {
				damage.setValue(damage.getValue() + r.nextInt((int)Math.floor(damage.getValue()) / 2 + 2));
			}
		}

		if(verbose)
			System.out.println("Critical result: " + damage.getValue());

		return (int)Math.floor(damage.getValue());
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
		worldToArrowDamageMap.clear();
		worldToDefaultDamageMap.clear();
		equations.clear();
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

	public void setEquation(String s, PostfixNotation eq){
		equations.put(s, eq);
	}
}
