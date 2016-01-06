package latmod.silicio.item;
import cpw.mods.fml.relauncher.*;
import ftb.lib.item.ODItems;
import latmod.ftbu.item.*;
import latmod.ftbu.util.LMMod;
import latmod.silicio.*;
import net.minecraft.creativetab.CreativeTabs;

public class ItemMaterialsSil extends ItemMaterialsLM // ItemMaterialsLB
{
	public static MaterialItem SILICON_GEM;
	
	public static MaterialItem CIRCUIT;
	public static MaterialItem BLUE_CRYSTAL;
	public static MaterialItem RED_CRYSTAL;
	public static MaterialItem XSUIT_PLATE;
	
	public ItemMaterialsSil(String s)
	{ super(s); }
	
	public LMMod getMod()
	{ return Silicio.mod; }
	
	@SideOnly(Side.CLIENT)
	public CreativeTabs getCreativeTab()
	{ return Silicio.tab; }
	
	public void onPostLoaded()
	{
		add(SilItems.Modules.EMPTY = new MaterialItem(this, 0, "module_empty")
		{
			public void loadRecipes()
			{
			}
		});
		
		add(SilItems.Modules.INPUT = new MaterialItem(this, 1, "module_input")
		{
			public void loadRecipes()
			{
			}
		});
		
		add(SilItems.Modules.OUTPUT = new MaterialItem(this, 2, "module_output")
		{
			public void loadRecipes()
			{
			}
		});
		
		add(SilItems.Modules.LOGIC = new MaterialItem(this, 7, "module_logic")
		{
			public void loadRecipes()
			{
				getMod().recipes.addShapelessRecipe(getStack(), SilItems.Modules.EMPTY, ODItems.QUARTZ, BLUE_CRYSTAL, RED_CRYSTAL);
			}
		});
		
		add(SILICON_GEM = new MaterialItem(this, 3, "silicon_gem")
		{
			public void onPostLoaded()
			{
				ODItems.add(ODItems.SILICON, getStack());
			}
			
			public void loadRecipes()
			{
			}
		});
		
		add(CIRCUIT = new MaterialItem(this, 5, "circuit")
		{
			public void loadRecipes()
			{
				getMod().recipes.addRecipe(getStack(), "CCC", "ISI", "CCC",
						'C', SilItems.b_cbcable,
						'S', ODItems.SILICON,
						'I', ODItems.IRON);
			}
		});
		
		add(BLUE_CRYSTAL = new MaterialItem(this, 6, "blue_crystal")
		{
			public void loadRecipes()
			{
			}
		});
		
		add(RED_CRYSTAL = new MaterialItem(this, 8, "red_crystal")
		{
			public void loadRecipes()
			{
			}
		});
		
		add(XSUIT_PLATE = new MaterialItem(this, 9, "xsuit_plate")
		{
			public void loadRecipes()
			{
			}
		});
		
		super.onPostLoaded();
	}
}