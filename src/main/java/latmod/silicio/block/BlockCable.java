package latmod.silicio.block;

import latmod.silicio.multiparts.MultipartCable;
import mcmultipart.multipart.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.world.*;
import net.minecraftforge.fml.relauncher.*;

import java.util.List;

/**
 * Created by LatvianModder on 05.02.2016.
 */
public class BlockCable extends BlockSil
{
	public static final PropertyBool CON_D = PropertyBool.create("down");
	public static final PropertyBool CON_U = PropertyBool.create("up");
	public static final PropertyBool CON_N = PropertyBool.create("north");
	public static final PropertyBool CON_S = PropertyBool.create("south");
	public static final PropertyBool CON_W = PropertyBool.create("west");
	public static final PropertyBool CON_E = PropertyBool.create("east");
	public static final PropertyBool ACTIVE = PropertyBool.create("active");
	
	public static final float pipeBorder = 1F / 32F * 12F;
	public static final AxisAlignedBB boxes[] = new AxisAlignedBB[7];
	
	static
	{
		double d = pipeBorder;
		boxes[0] = new AxisAlignedBB(d, 0D, d, 1D - d, d, 1D - d);
		boxes[1] = new AxisAlignedBB(d, 1D - d, d, 1D - d, 1D, 1D - d);
		boxes[2] = new AxisAlignedBB(d, d, 0D, 1D - d, 1D - d, d);
		boxes[3] = new AxisAlignedBB(d, d, 1D - d, 1D - d, 1D - d, 1D);
		boxes[4] = new AxisAlignedBB(0D, d, d, d, 1D - d, 1D - d);
		boxes[5] = new AxisAlignedBB(1D - d, d, d, 1D, 1D - d, 1D - d);
		boxes[6] = new AxisAlignedBB(d, d, d, 1D - d, 1D - d, 1D - d);
	}
	
	public BlockCable(String s)
	{
		super(s, Material.rock);
		setHardness(0.5F);
	}
	
	public boolean canHarvestBlock(IBlockAccess world, BlockPos pos, EntityPlayer player)
	{ return true; }
	
	@SideOnly(Side.CLIENT)
	public EnumWorldBlockLayer getBlockLayer()
	{ return EnumWorldBlockLayer.CUTOUT_MIPPED; }
	
	public boolean isFullCube()
	{ return false; }
	
	public boolean isOpaqueCube()
	{ return false; }
	
	public TileEntity createNewTileEntity(World w, int m)
	{ return null; }
	
	@SideOnly(Side.CLIENT)
	public boolean shouldSideBeRendered(IBlockAccess worldIn, BlockPos pos, EnumFacing side)
	{ return true; }
	
	public IBlockState getStateFromMeta(int meta)
	{ return getDefaultState(); }
	
	public int getMetaFromState(IBlockState state)
	{ return 0; }
	
	protected BlockState createBlockState()
	{ return new BlockState(this, CON_D, CON_U, CON_N, CON_S, CON_W, CON_E, ACTIVE); }
	
	public IBlockState getActualState(IBlockState state, IBlockAccess w, BlockPos pos)
	{
		boolean conD = canConnectTo(w, pos.down());
		boolean conU = canConnectTo(w, pos.up());
		boolean conN = canConnectTo(w, pos.north());
		boolean conS = canConnectTo(w, pos.south());
		boolean conW = canConnectTo(w, pos.west());
		boolean conE = canConnectTo(w, pos.east());
		
		boolean active = false;
		
		/*
		TileEntity te = w.getTileEntity(pos);
		
		if(te != null && !te.isInvalid() && te instanceof TileCBCable)
		{
			active = ((TileCBCable)te).isActive();
		}*/
		
		return state.withProperty(CON_D, conD).withProperty(CON_U, conU).withProperty(CON_N, conN).withProperty(CON_S, conS).withProperty(CON_W, conW).withProperty(CON_E, conE).withProperty(ACTIVE, active);
	}
	
	public boolean canConnectTo(IBlockAccess w, BlockPos pos)
	{
		IBlockState state = w.getBlockState(pos);
		if(state.getBlock() == this) return true;
		
		if(state.getBlock().hasTileEntity(state))
		{
			TileEntity te = w.getTileEntity(pos);
			
			if(te instanceof IMultipartContainer)
			{
				for(IMultipart m : ((IMultipartContainer) te).getParts())
				{
					if(m instanceof MultipartCable) return true;
				}
			}
		}
		
		return false;
	}
	
	public void setBlockBoundsForItemRender()
	{
		float s = pipeBorder;
		setBlockBounds(0F, s, s, 1F, 1F - s, 1F - s);
	}
	
	public void addCollisionBoxesToList(World w, BlockPos pos, IBlockState state, AxisAlignedBB mask, List<AxisAlignedBB> list, Entity e)
	{
		double ox = pos.getX();
		double oy = pos.getY();
		double oz = pos.getZ();
		
		addIfIntersects(list, mask, boxes[6], ox, oy, oz);
		
		for(int i = 0; i < 6; i++)
		{
			if(canConnectTo(w, pos.offset(EnumFacing.VALUES[i])))
			{
				addIfIntersects(list, mask, boxes[i], ox, oy, oz);
			}
		}
	}
	
	private static void addIfIntersects(List<AxisAlignedBB> list, AxisAlignedBB mask, AxisAlignedBB box, double ox, double oy, double oz)
	{
		AxisAlignedBB box1 = box.offset(ox, oy, oz);
		if(mask.intersectsWith(box1)) list.add(box1);
	}
	
	public void setBlockBoundsBasedOnState(IBlockAccess w, BlockPos pos)
	{
		float s = pipeBorder - 1 / 32F;
		
		boolean x0 = canConnectTo(w, pos.west());
		boolean x1 = canConnectTo(w, pos.east());
		boolean y0 = canConnectTo(w, pos.down());
		boolean y1 = canConnectTo(w, pos.up());
		boolean z0 = canConnectTo(w, pos.north());
		boolean z1 = canConnectTo(w, pos.south());
		
		setBlockBounds(x0 ? 0F : s, y0 ? 0F : s, z0 ? 0F : s, x1 ? 1F : 1F - s, y1 ? 1F : 1F - s, z1 ? 1F : 1F - s);
	}
}