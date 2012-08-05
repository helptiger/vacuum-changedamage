package vacuum.changedamage.hooks;

import java.util.HashMap;
import java.util.List;

import vacuum.changedamage.ChangeDamagePlugin;


import net.minecraft.server.EntityHuman;
import net.minecraft.server.EnumAnimation;
import net.minecraft.server.Item;
import net.minecraft.server.ItemPotion;
import net.minecraft.server.ItemStack;
import net.minecraft.server.MobEffect;
import net.minecraft.server.World;

public class PotionHook extends ItemPotion{

	private ItemPotion realVersion;
	private HashMap<Integer, List<MobEffect>> customEffects = new HashMap<Integer, List<MobEffect>>();


	public PotionHook(HashMap<Integer, List<MobEffect>> customEffects) {
		super(Item.POTION.id);

		this.customEffects = customEffects;

		if(ChangeDamagePlugin.verbose)
			System.out.println("Creating potion hook...");
		
		//create hook
		realVersion = Item.POTION;
		Item.POTION = this;
	}

	@Override
	public List<MobEffect> f(int id) {
		if(ChangeDamagePlugin.verbose)
			System.out.println("[ChangeDamage] method b # 3 called");
		if(ChangeDamagePlugin.verbose)
			System.out.println("[ChangeDamage] Request for potion with id: " + id);
		List<MobEffect> effectList = (List<MobEffect>)customEffects.get(Integer.valueOf(id));
		if(ChangeDamagePlugin.verbose)
			System.out.println("[ChangeDamage] Resulting list: " + effectList.toString());
		return effectList == null ? ((List<MobEffect>) super.f(id)) : effectList;
	}

	public void releaseHook(){
		Item.POTION = realVersion;
	}

}
