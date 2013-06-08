package vacuum.changedamage.hooks;

import java.util.HashMap;
import java.util.List;

import vacuum.changedamage.ChangeDamagePlugin;


<<<<<<< .mine
import net.minecraft.server.v1_5_R3.EntityHuman;
import net.minecraft.server.v1_5_R3.EnumAnimation;
import net.minecraft.server.v1_5_R3.Item;
import net.minecraft.server.v1_5_R3.ItemPotion;
import net.minecraft.server.v1_5_R3.ItemStack;
import net.minecraft.server.v1_5_R3.MobEffect;
import net.minecraft.server.v1_5_R3.World;
=======
import net.minecraft.server.v1_5_R2.EntityHuman;
import net.minecraft.server.v1_5_R2.EnumAnimation;
import net.minecraft.server.v1_5_R2.Item;
import net.minecraft.server.v1_5_R2.ItemPotion;
import net.minecraft.server.v1_5_R2.ItemStack;
import net.minecraft.server.v1_5_R2.MobEffect;
import net.minecraft.server.v1_5_R2.World;
>>>>>>> .r52

public class PotionHook extends ItemPotion {

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
	public List<MobEffect> c(int id) {
		if(ChangeDamagePlugin.verbose)
<<<<<<< .mine
			System.out.println("[ChangeDamage] method b # 3 called");
		if(ChangeDamagePlugin.verbose)
			System.out.println("[ChangeDamage] Request for potion with id: " + id);
		List<MobEffect> effectList = (List<MobEffect>)customEffects.get(Integer.valueOf(id));
		if(ChangeDamagePlugin.verbose)
			System.out.println("[ChangeDamage] Resulting list: " + effectList.toString());
		return effectList == null ? ((List<MobEffect>) super.c(id)) : effectList;
	}

=======
			System.out.println("[ChangeDamage] method a called");
		return super.a(arg0, arg1, arg2);
	}



	@Override
	public ItemStack b(ItemStack arg0, World arg1, EntityHuman arg2) {
		if(ChangeDamagePlugin.verbose)
			System.out.println("[ChangeDamage] method b #1 called");
		return super.b(arg0, arg1, arg2);
	}



//	@Override
//	public List b(ItemStack arg0) {
//		if(ChangeDamagePlugin.verbose)
//			System.out.println("[ChangeDamage] method b # 2 called");
//		return super.b(arg0);
//	}
//
//
//
//	@Override
//	public int c(ItemStack arg0) {
//		if(ChangeDamagePlugin.verbose)
//			System.out.println("[ChangeDamage] method c called");
//		return super.c(arg0);
//	}
//
//
//
//	@Override
//	public EnumAnimation d(ItemStack arg0) {
//		if(ChangeDamagePlugin.verbose)
//			System.out.println("[ChangeDamage] method d called");
//		return super.d(arg0);
//	}
//
//
//
//	@Override
//	public boolean interactWith(ItemStack arg0, EntityHuman arg1, World arg2,
//			int arg3, int arg4, int arg5, int arg6) {
//		if(ChangeDamagePlugin.verbose)
//			System.out.println("[ChangeDamage] method interactWith called");
//		return super.interactWith(arg0, arg1, arg2, arg3, arg4, arg5, arg6);
//	}
//
//
//
//	@Override
//	public List<MobEffect> b(int id) {
//		if(ChangeDamagePlugin.verbose)
//			System.out.println("[ChangeDamage] method b # 3 called");
//		if(ChangeDamagePlugin.verbose)
//			System.out.println("[ChangeDamage] Request for potion with id: " + id);
//		List<MobEffect> effectList = (List<MobEffect>)customEffects.get(Integer.valueOf(id));
//		if(ChangeDamagePlugin.verbose)
//			System.out.println("[ChangeDamage] Resulting list: " + effectList.toString());
//		return effectList == null ? ((List<MobEffect>) super.b(id)) : effectList;
//	}

>>>>>>> .r52
	public void releaseHook(){
		Item.POTION = realVersion;
	}

}
