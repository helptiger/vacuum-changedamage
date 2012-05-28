package vacuum.changedamage.hooks;

import java.lang.reflect.Field;
import java.util.Random;

import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import vacuum.changedamage.ChangeDamage;

public class RandomHook extends Random{

	public static boolean applyHook(Player p){
		try {
			System.out.println("[ChangeDamage] Applying Rnadom hook to " + p.getName());
			new RandomHook(p);
			System.out.println("[ChangeDamage] Random hook successfully applied!");
			return true;
		} catch (SecurityException ex) {
			ex.printStackTrace();
		} catch (NoSuchFieldException ex) {
			ex.printStackTrace();
		} catch (IllegalAccessException ex) {
			ex.printStackTrace();
		}
		return false;
	}

	public static boolean removeHook(Player p){
		try {
			Field random = net.minecraft.server.Entity.class.getDeclaredField("random");
			random.setAccessible(true);
			random.set(((CraftPlayer)p).getHandle(), new Random());
			return true;
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return false;
	}

	private RandomHook(Entity entity) throws SecurityException, NoSuchFieldException, IllegalAccessException{
		super();
		Field random = net.minecraft.server.Entity.class.getDeclaredField("random");
		random.setAccessible(true);
		random.set(((CraftEntity)entity).getHandle(), this);
	}

	public int nextInt(int n){
		StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[2];
		if(stackTraceElement.getMethodName() != "aA" && ChangeDamage.research)
			System.out.println("[ChangeDamage] [Research] Please alert the developer!" + stackTraceElement.getClassName() + ", " + stackTraceElement.getMethodName());
		if(stackTraceElement.getClassName().equals("net.minecraft.server.EntityHuman")/* && stackTraceElement.getMethodName().equals("attack")*/){
			if(ChangeDamage.research)
				System.out.println("[ChangeDamage] [Research] We believe a critical hit happened. Please alert the developer.");
		}
		return super.nextInt(n);
	}
}
