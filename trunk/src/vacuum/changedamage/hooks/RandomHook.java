package vacuum.changedamage.hooks;

import java.lang.reflect.Field;
import java.util.Random;

import org.bukkit.craftbukkit.v1_5_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_5_R2.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import vacuum.changedamage.ChangeDamagePlugin;
import vacuum.changedamage.equations.PostfixNotation;
import vacuum.changedamage.equations.element.number.Variable;

@Deprecated
public class RandomHook extends Random{

	private static final long serialVersionUID = -8825810946367249961L;

	public static boolean applyHook(Player p, PostfixNotation expression, Variable n){
		try {
			System.out.println("[ChangeDamage] Applying Random hook to " + p.getName());
			new RandomHook(p, expression, n);
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
			Field random = net.minecraft.server.v1_5_R2.Entity.class.getDeclaredField("random");
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

	private PostfixNotation expression;
	private Variable n;

	private RandomHook(Entity entity, PostfixNotation expression, Variable n) throws SecurityException, NoSuchFieldException, IllegalAccessException{
		super();
		Field random = net.minecraft.server.v1_5_R2.Entity.class.getDeclaredField("random");
		random.setAccessible(true);
		random.set(((CraftEntity)entity).getHandle(), this);
		this.expression = expression;
		this.n = n;
	}

	public int nextInt(int n){
		StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[2];
		if(stackTraceElement.getMethodName() != "aA" && ChangeDamagePlugin.research)
			System.out.println("[ChangeDamage] [Research] Please alert the developer!" + stackTraceElement.getClassName() + ", " + stackTraceElement.getMethodName());
		if(stackTraceElement.getClassName().equals("net.minecraft.server.EntityHuman") && stackTraceElement.getMethodName().equals("attack")){
//			if(ChangeDamage.research)
//				System.out.println("[ChangeDamage] [Research] We believe a critical hit happened. Please alert the developer.");
			this.n.setValue(n);
			return (int) Math.round(expression.evaluate());
			
		}
		
		return super.nextInt(n);
	}
}
