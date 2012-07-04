package vacuum.changedamage.hooks;

import java.util.HashMap;
import java.util.List;


import net.minecraft.server.Item;
import net.minecraft.server.ItemPotion;
import net.minecraft.server.MobEffect;

public class PotionHook extends ItemPotion{

	private ItemPotion realVersion;
	private HashMap<Integer, List<MobEffect>> customEffects = new HashMap<Integer, List<MobEffect>>();

	
	public PotionHook(HashMap<Integer, List<MobEffect>> customEffects) {
		super(Item.POTION.id);
		
		this.customEffects = customEffects;
		
		//create hook
		realVersion = Item.POTION;
		Item.POTION = this;
	}

	@Override
	public List<MobEffect> b(int id) {
		List<MobEffect> effectList = (List<MobEffect>)customEffects.get(Integer.valueOf(id));
		return effectList == null ? ((List<MobEffect>) super.b(id)) : effectList;
	}

	public void releaseHook(){
		Item.POTION = realVersion;
	}

}
