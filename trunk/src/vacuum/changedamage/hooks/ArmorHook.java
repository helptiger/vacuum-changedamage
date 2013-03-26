package vacuum.changedamage.hooks;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;

import net.minecraft.server.v1_5_R2.Item;
import net.minecraft.server.v1_5_R2.ItemArmor;

public class ArmorHook {
	
	private static final String armorFieldName = "c";
	
	private HashMap<ItemArmor, Integer> oldValues = new HashMap<ItemArmor, Integer>();

	private Field armorValueField;
	
	public ArmorHook() throws SecurityException, NoSuchFieldException, IllegalAccessException{
		Class<ItemArmor> itemArmor = ItemArmor.class;
		armorValueField = itemArmor.getField(armorFieldName);
		Field modifiersField = Field.class.getDeclaredField("modifiers");
		modifiersField.setAccessible(true);
		modifiersField.setInt(armorValueField, armorValueField.getModifiers() & ~Modifier.FINAL);
	}

	public void modifyArmorValue(int id, int value){
		Item i = Item.byId[id];
		if(i instanceof ItemArmor){
			int val;
			try {
				val = armorValueField.getInt(i);
				armorValueField.set(i, value);
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
				armorValueField.set(i, oldValues.get(i));
				oldValues.remove(i);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}
}
