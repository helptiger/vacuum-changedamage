package vacuum.changedamage.listener;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

import net.minecraft.server.v1_5_R2.EnchantmentManager;
import net.minecraft.server.v1_5_R2.EntityLiving;
import net.minecraft.server.v1_5_R2.EntityPlayer;
import net.minecraft.server.v1_5_R2.MobEffectList;

import org.bukkit.World;
import org.bukkit.craftbukkit.v1_5_R2.entity.CraftArrow;
import org.bukkit.craftbukkit.v1_5_R2.entity.CraftEgg;
import org.bukkit.craftbukkit.v1_5_R2.entity.CraftEnderPearl;
import org.bukkit.craftbukkit.v1_5_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_5_R2.entity.CraftFireball;
import org.bukkit.craftbukkit.v1_5_R2.entity.CraftFish;
import org.bukkit.craftbukkit.v1_5_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_5_R2.entity.CraftSmallFireball;
import org.bukkit.craftbukkit.v1_5_R2.entity.CraftSnowball;
import org.bukkit.craftbukkit.v1_5_R2.entity.CraftThrownExpBottle;
import org.bukkit.craftbukkit.v1_5_R2.entity.CraftThrownPotion;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import vacuum.changedamage.equations.PostfixNotation;
import vacuum.changedamage.equations.element.number.Variable;
import vacuum.changedamage.equations.element.number.VariablePool;

public class DamageListener implements Listener{

	public static final String[] PROJECTILE_TYPES = {
		"flying_arrow",
		"thrown_egg",
		"thrown_ender_pearl",
		"thrown_small_fireball",
		"thrown_fireball",
		"thrown_exp_bottle",
		"thrown_potion",
		"thrown_snowball",
		"flying_fish"
	};

	static {
		Arrays.sort(PROJECTILE_TYPES);
	}

	private enum ProjectileType {
		FLYING_ARROW(CraftArrow.class),
		THROWN_EGG(CraftEgg.class),
		THROWN_ENDER_PEARL(CraftEnderPearl.class),
		THROWN_SMALL_FIREBALL(CraftSmallFireball.class),
		THROWN_FIREBALL(CraftFireball.class),
		THROWN_EXP_BOTTLE(CraftThrownExpBottle.class),
		THROWN_POTION(CraftThrownPotion.class),
		THROWN_SNOWBALL(CraftSnowball.class),
		FLYING_FISH(CraftFish.class);

		private Class<? extends Projectile> clazz;

		private ProjectileType(Class<? extends Projectile> clazz){
			this.clazz = clazz;
		}

		public static ProjectileType valueOf(Projectile p){
			for(ProjectileType t : ProjectileType.values())
				if(p.getClass().isAssignableFrom(t.clazz)){
					return t;
				}
			System.out.println("[ChangeDamage] Unrecognized projectile: " + p.getClass());
			return null;
		}
	}

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
	private HashMap<World, HashMap<String, Double>> worldToProjectileDamageMap = new HashMap<World, HashMap<String, Double>>();
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
			Integer ret;
			if(damageMap == null)
				ret = null;
			else
				ret = damageMap.get(item);

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
					System.out.println("[ChangeDamage] Set damage to " + ret);
			}
		} else if (evt.getDamager() instanceof Projectile){
			if(!(((Projectile)evt.getDamager()).getShooter() instanceof Player) || !((Player)((Projectile)evt.getDamager()).getShooter()).hasPermission("vacuum.changedamage.damage"))
				return;


			Double damage = null;
			try{
				String type = ProjectileType.valueOf((Projectile)evt.getDamager()).toString().toLowerCase();
				if(verbose)
					System.out.println("[ChangeDamage] Identified projectile of type " + evt.getDamager().getClass() + " as " + type);
				HashMap<String, Double> map = worldToProjectileDamageMap.get(evt
						.getDamager()
						.getWorld());
				if(map == null)
					map = worldToProjectileDamageMap.get(null);
				if(map == null)
					return;
				damage = map.get(type);
			} catch (Exception ex){
				if(!(ex instanceof NullPointerException))
					ex.printStackTrace();
			}
			if(damage == null)
				return;
			double raw;
			if(evt.getDamage() != 0)
				raw = evt.getDamage() * damage;
			else
				raw = damage;
			if(verbose)
				System.out.println("Raw projectile damage: " + raw);
			evt.setDamage(round(raw));
			if(verbose)
				System.out.println("Set projectile damage to " + evt.getDamage());
		}
	}

	private int applyEffects(int ret, Player p, Entity entity) {
		EntityPlayer mcPlayer = ((CraftPlayer)p).getHandle();

		Variable damage = pool.getVariable("i");
		damage.setValue(ret);

		if(verbose)
			System.out.println("Initial: " + damage.getValue());

		/* apply weakness */
		if(mcPlayer.hasEffect(MobEffectList.WEAKNESS)) {
			if(equations.containsKey("weakness")){
				pool.getVariable("w").setValue(mcPlayer.getEffect(MobEffectList.WEAKNESS).getAmplifier());
				damage.setValue(equations.get("weakness").evaluate());
			} else {
				damage.setValue(damage.getValue() - (2 << mcPlayer.getEffect(MobEffectList.INCREASE_DAMAGE).getAmplifier()));
			}

			if(verbose)
				System.out.println("Weakness result: " + damage.getValue());
		}

		/* apply strength */
		if(mcPlayer.hasEffect(MobEffectList.INCREASE_DAMAGE))
		{
			if(equations.containsKey("strength")){
				pool.getVariable("s").setValue(mcPlayer.getEffect(MobEffectList.INCREASE_DAMAGE).getAmplifier());
				damage.setValue(equations.get("strength").evaluate());
			} else {
				damage.setValue(damage.getValue() +(3 << mcPlayer.getEffect(MobEffectList.INCREASE_DAMAGE).getAmplifier()));
			}

			if(verbose)
				System.out.println("Strength result: " + damage.getValue());
		}

		/* apply enchantment */
		damage.setValue(damage.getValue() + EnchantmentManager.a(mcPlayer, (EntityLiving)((CraftEntity)entity).getHandle()));

		if(verbose)
			System.out.println("Enchantment result: " + damage.getValue());

		if(verbose) {
			System.out.printf("fallen: %b, not on ground: %b, !g_: %b, !G(): %b, not blind: %b, no vehicle: %b",
					(mcPlayer.fallDistance > 0.0F),
				(!mcPlayer.onGround),
				(!mcPlayer.g_()),
				!mcPlayer.G(),
				(!mcPlayer.hasEffect(MobEffectList.BLINDNESS)),
				(mcPlayer.vehicle == null));
		}
		
		/* apply critical */
		if(		(mcPlayer.fallDistance > 0.0F)
				&&(!mcPlayer.onGround)
				&& (!mcPlayer.g_())
				&& (!mcPlayer.G())
				&& (!mcPlayer.hasEffect(MobEffectList.BLINDNESS))
				&& (mcPlayer.vehicle == null)
				&& entity instanceof LivingEntity
				){
			if(equations.containsKey("critical")){
				pool.getVariable("d").setValue(mcPlayer.fallDistance);
				damage.setValue(equations.get("critical").evaluate());
			} else {
				damage.setValue(damage.getValue() + r.nextInt((int)Math.floor(damage.getValue()) / 2 + 2));
			}

			if(verbose)
				System.out.println("Critical result: " + damage.getValue());
		}

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
		worldToProjectileDamageMap.clear();
		worldToDefaultDamageMap.clear();
		equations.clear();
	}

	public void remove(World w, int k){
		worldDamageMapMap.get(w).remove(k);
	}

	public void setProjectileDamage(World w, String type, double damage) {
		if(!worldToProjectileDamageMap.containsKey(w))
			worldToProjectileDamageMap.put(w, new HashMap<String, Double>());
		worldToProjectileDamageMap.get(w).put(type.toLowerCase(), damage);
	}

	public void setDefaultDamage(World w, int damage){
		worldToDefaultDamageMap.put(w, damage);
	}

	public void setEquation(String s, PostfixNotation eq){
		equations.put(s, eq);
	}
}
