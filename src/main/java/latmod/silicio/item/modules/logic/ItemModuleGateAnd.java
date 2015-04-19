package latmod.silicio.item.modules.logic;

import latmod.core.ODItems;
import latmod.silicio.SilItems;
import latmod.silicio.item.modules.*;
import latmod.silicio.tile.*;
import net.minecraft.item.ItemStack;

public class ItemModuleGateAnd extends ItemModuleLogic implements ISignalProvider
{
	public ItemModuleGateAnd(String s)
	{
		super(s);
		
		channelNames[0] = "Input 1";
		channelNames[1] = "Input 2";
		channelNames[2] = "Input 3";
		channelNames[3] = "Input 4";
		channelNames[4] = "Output";
	}
	
	public int getChannelCount()
	{ return 5; }
	
	public IOType getChannelType(int c)
	{ return (c == 4) ? IOType.OUTPUT : IOType.INPUT; }
	
	public void loadRecipes()
	{
		mod.recipes.addShapelessRecipe(new ItemStack(this), SilItems.Modules.i_gate_or, ODItems.REDSTONE);
	}
	
	public void provideSignals(CircuitBoard cb, int MID)
	{
		for(int i = 0; i < 4; i++)
		{
			CBChannel c = getChannel(cb, MID, i);
			if(c != CBChannel.NONE && !c.isEnabled())
				return;
		}
		
		getChannel(cb, MID, 4).enable();
	}
}