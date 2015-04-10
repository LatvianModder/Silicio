package latmod.silicio.item.modules.io;

import latmod.silicio.SilItems;
import latmod.silicio.item.modules.IOType;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

public class ItemModuleChatOutput extends ItemModuleIO
{
	public ItemModuleChatOutput(String s)
	{ super(s); }
	
	public int getChannelCount()
	{ return 1; }
	
	public IOType getModuleType()
	{ return IOType.NONE; }
	
	public IOType getChannelType(int c)
	{ return IOType.INPUT; }
	
	public void loadRecipes()
	{
		mod.recipes.addRecipe(new ItemStack(this), "B", "M",
				'B', Blocks.beacon,
				'M', SilItems.Modules.i_sign_out);
	}
}