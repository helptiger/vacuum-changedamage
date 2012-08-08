package vacuum.changedamage.hooks;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

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
				synchronized (oldValues) {
					oldValues.put((ItemArmor) i, val);
				}
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
		synchronized (oldValues) {
			Set<Entry<ItemArmor, Integer>> entries;
			entries = oldValues.entrySet();
			Iterator<Entry<ItemArmor, Integer>> it = entries.iterator();
			while(it.hasNext()){
				try{
				Entry<ItemArmor, Integer> entry = it.next();
					System.out.println("Restoring " + entry.getKey().getName() + " to " + entry.getValue());
					b.set(entry.getKey(), entry.getValue());
					oldValues.remove(entry.getKey());
				} catch (Exception ex) {
					try{
						it.remove();
					} catch (Exception ex2){
						return;
					}
				}
			}
		}
	}
}
