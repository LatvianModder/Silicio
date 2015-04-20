package latmod.silicio.item.modules.logic;

import latmod.silicio.SilItems;
import latmod.silicio.item.modules.*;
import latmod.silicio.item.modules.config.ModuleCSInt;
import latmod.silicio.tile.CircuitBoard;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

public class ItemModuleSequencer extends ItemModuleLogic implements ISignalProvider
{
	public static final ModuleCSInt cs_timer = new ModuleCSInt(0, "Timer");
	
	public ItemModuleSequencer(String s)
	{
		super(s);
		
		cs_timer.defaultValue = 20;
		cs_timer.min = 1;
		cs_timer.max = 48000;
		moduleConfig.add(cs_timer);
		
		channelNames[0] = "Output 1";
		channelNames[1] = "Output 2";
		channelNames[2] = "Output 3";
		channelNames[3] = "Output 4";
		channelNames[4] = "Input";
	}
	
	public int getChannelCount()
	{ return 5; }
	
	public IOType getChannelType(int c)
	{ return c == 4 ? IOType.INPUT : IOType.OUTPUT; }
	
	public void loadRecipes()
	{
		mod.recipes.addRecipe(new ItemStack(this), " R ", "RTR", " R ", 
				'T', SilItems.Modules.i_timer,
				'R', Blocks.redstone_torch);
	}
	
	public void provideSignals(CircuitBoard cb, int MID)
	{
		if(getChannel(cb, MID, 4).isEnabled()) return;
		
		int t = cs_timer.get(cb.items[MID]);
		int p = (int)((cb.tick / t) % 4L);
		getChannel(cb, MID, p).enable();
	}
}