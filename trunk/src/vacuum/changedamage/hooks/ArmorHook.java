package vacuum.changedamage.hooks;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;

import net.minecraft.server.Item;
import net.minecraft.server.ItemArmor;

public class ArmorHook {
	
	private HashMap<ItemArmor, Integer> oldValues = new HashMap<ItemArmor, Integer>();

	private Field b;
	
	public ArmorHook() throws SecurityException, NoSuchFieldException, IllegalAccessException{
		Class<ItemArmor> itemArmor = ItemArmor.class;
		b = itemArmor.getField("b");
		Field modifiersField = Field.class.getDeclaredField("modifiers");
		modifiersField.setAccessible(true);
		modifiersField.setInt(b, b.getModifiers() & ~Modifier.FINAL);
	}

	public void modifyArmorValue(int id, int value){
		Item i = Item.byId[id];
		if(i instanceof ItemArmor){
			int val;
			try {
				val = b.getInt(i);
				b.set(i, value);
				oldValues.put((ItemArmor) i, val);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		} else {
			throw new IllegalArgumentException("Invalid armor ID: " + id);
		}
	}
	
	public void restore(){
		for(ItemArmor i : oldValues.keySet()){
			try {
				System.out.println("Restoring " + i + " to " + oldValues.get(i));
				b.set(i, oldValues.get(i));
				oldValues.remove(i);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}
}
