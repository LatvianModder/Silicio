package latmod.silicio.block;
import cpw.mods.fml.relauncher.*;
import latmod.ftbu.core.tile.TileLM;
import latmod.silicio.tile.cb.TileCBController;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import net.minecraft.world.*;

public class BlockCBController extends BlockSil
{
	@SideOnly(Side.CLIENT)
	public IIcon icon_conflict;
	
	public BlockCBController(String s)
	{
		super(s, Material.iron);
		setHardness(1.5F);
		isBlockContainer = true;
		mod.addTile(TileCBController.class, s);
	}
	
	public void loadRecipes()
	{
	}
	
	public TileLM createNewTileEntity(World w, int m)
	{ return new TileCBController(); }
	
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister ir)
	{
		blockIcon = ir.registerIcon(mod.assets + "controller");
		icon_conflict = ir.registerIcon(mod.assets + "controller_conflict");
	}
	
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(IBlockAccess iba, int x, int y, int z, int s)
	{
		TileCBController t = getTile(TileCBController.class, iba, x, y, z);
		return (t != null && t.getCBNetwork().hasConflict) ? icon_conflict : blockIcon;
	}
}