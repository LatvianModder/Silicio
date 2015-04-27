package latmod.silicio.item.modules.io;

import latmod.silicio.SilItems;
import latmod.silicio.item.modules.*;
import latmod.silicio.item.modules.config.ModuleCSItem;
import latmod.silicio.tile.*;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

public class ItemModuleCrafting extends ItemModuleIO implements IToggable
{
	public static final ModuleCSItem cs_recipe = new ModuleCSItem(0, "Recipe")
	{
		public boolean isValid(ItemStack is)
		{ return true; }
	};
	
	public ItemModuleCrafting(String s)
	{
		super(s);
		
		moduleConfig.add(cs_recipe);
	}
	
	public int getChannelCount()
	{ return 1; }
	
	public IOType getModuleType()
	{ return IOType.NONE; }
	
	public IOType getChannelType(int c)
	{ return IOType.INPUT; }
	
	public void loadRecipes()
	{
		mod.recipes.addRecipe(new ItemStack(this), " C ", "IMO",
				'C', Blocks.crafting_table,
				'M', SilItems.Modules.EMPTY,
				'I', SilItems.Modules.i_item_in,
				'O', SilItems.Modules.i_item_out);
	}
	
	public void onChannelToggled(CircuitBoard cb, int MID, CBChannel c)
	{
		if(isEnabled(c, cb, MID, 0))
		{
		}
	}
}