package vacuum.changedamage.listener;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import net.minecraft.server.Item;
import net.minecraft.server.ItemArmor;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
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

	public FallListener(){
		VariablePool pool = new VariablePool(false);
		pool.register("d", 0);
		expression = ExpressionParser.parsePostfix("d 3 -", pool);
		this.d = pool.getVariable("d");
	}

	@EventHandler
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
			} catch (Exception e) {
				// TODO Auto-generated catch block
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
