package vacuum.changedamage.listener;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

<<<<<<< .mine
import net.minecraft.server.v1_5_R3.Item;
import net.minecraft.server.v1_5_R3.ItemArmor;
=======
import net.minecraft.server.v1_5_R2.Item;
import net.minecraft.server.v1_5_R2.ItemArmor;
>>>>>>> .r52

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;

import vacuum.changedamage.equations.ExpressionParser;
import vacuum.changedamage.equations.PostfixNotation;
import vacuum.changedamage.equations.element.number.Variable;
import vacuum.changedamage.equations.element.number.VariablePool;

public class FallListener implements Listener{

	private PostfixNotation expression;
	private Variable d;
	private Variable a;
	private EventPriority priority;

	public FallListener(){
		VariablePool pool = new VariablePool(false);
		pool.register("d", 0);
		expression = ExpressionParser.parsePostfix("d 3 -", pool);
		this.d = pool.getVariable("d");
	}

	public void setEventPriority(String priority){
		try{
			this.priority = EventPriority.valueOf(priority.toUpperCase());
		} catch (Exception ex){
			this.priority = EventPriority.NORMAL;
		}
	}

	@EventHandler(priority=EventPriority.LOWEST)
	public void onEntityDamagedLOWEST(EntityDamageByEntityEvent evt){
		if(priority.equals(EventPriority.LOWEST))
			onEntityDamaged(evt);
	}

	@EventHandler(priority=EventPriority.LOW)
	public void onEntityDamagedLOW(EntityDamageByEntityEvent evt){
		if(priority.equals(EventPriority.LOW))
			onEntityDamaged(evt);
	}

	@EventHandler(priority=EventPriority.NORMAL)
	public void onEntityDamagedNORMAL(EntityDamageByEntityEvent evt){
		if(priority.equals(EventPriority.NORMAL))
			onEntityDamaged(evt);
	}

	@EventHandler(priority=EventPriority.HIGH)
	public void onEntityDamagedHIGH(EntityDamageByEntityEvent evt){
		if(priority.equals(EventPriority.HIGH))
			onEntityDamaged(evt);
	}

	@EventHandler(priority=EventPriority.HIGHEST)
	public void onEntityDamagedHIGHEST(EntityDamageByEntityEvent evt){
		if(priority.equals(EventPriority.HIGHEST))
			onEntityDamaged(evt);
	}

	@EventHandler(priority=EventPriority.NORMAL)
	public void onEntityDamaged(EntityDamageEvent evt){
		if(evt.getCause().equals(DamageCause.FALL) && evt.getEntity() instanceof Player){
			int fallDist = evt.getDamage() + 3;
			d.setValue(fallDist);

			int armorValue = 0;
			ItemStack[] armor = ((Player)evt.getEntity()).getInventory().getArmorContents();
			Field b;
			try {
				b = ItemArmor.class.getField("b");
				Field modifiersField = Field.class.getDeclaredField("modifiers");
				modifiersField.setAccessible(true);
				modifiersField.setInt(b, b.getModifiers() & ~Modifier.FINAL);
				for (ItemStack itemStack : armor) {
					ItemArmor itemArmor = (ItemArmor)Item.byId[itemStack.getTypeId()];
					if(itemArmor != null)
						armorValue += b.getInt(itemArmor);
				}
			} catch(ClassCastException e){
				//do nothing
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			a.setValue(armorValue);
			int damage = (int) Math.round(expression.evaluate());
			evt.setDamage(damage);
		}
	}

	public void setExpression(PostfixNotation expression, Variable d, Variable a){
		this.expression = expression;
		this.d = d;
		this.a = a;
	}
}
