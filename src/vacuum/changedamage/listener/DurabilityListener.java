package vacuum.changedamage.listener;

import java.util.HashMap;

import org.bukkit.craftbukkit.v1_5_R2.inventory.CraftItemStack;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class DurabilityListener implements Listener{
	
	HashMap<Integer, Double> changeMap = new HashMap<Integer, Double>();

	@EventHandler(priority=EventPriority.HIGHEST)
	public void beforeBlockBreak(BlockBreakEvent evt){
		Double change = changeMap.get(evt.getPlayer().getItemInHand().getTypeId());
		if(change != null && change < 0){
			change = -change;
			net.minecraft.server.v1_5_R2.ItemStack stack = CraftItemStack.asNMSCopy(evt.getPlayer().getItemInHand());
			int x = (int)Math.floor(change) + ((Math.random() < (change - Math.floor(change))) ? 1 : 0);
			stack.setData(stack.getData() + x);
		}
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void afterBlockBreak(BlockBreakEvent evt){
		Double change = changeMap.get(evt.getPlayer().getItemInHand().getTypeId());
		if(change != null && change > 0){
			change = -change;
			net.minecraft.server.v1_5_R2.ItemStack stack =  CraftItemStack.asNMSCopy(evt.getPlayer().getItemInHand());
			int x = (int)Math.floor(change) + ((Math.random() < (change - Math.floor(change))) ? 1 : 0);
			stack.setData(stack.getData() - x);
		}
	}
	
}
