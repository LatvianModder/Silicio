package latmod.silicio.tile;
import java.util.*;

import latmod.core.*;
import latmod.core.gui.ContainerEmpty;
import latmod.core.mod.LC;
import latmod.core.tile.*;
import latmod.core.util.*;
import latmod.silicio.*;
import latmod.silicio.gui.*;
import latmod.silicio.gui.container.*;
import latmod.silicio.item.modules.*;
import latmod.silicio.item.modules.config.ModuleConfigSegment;
import latmod.silicio.item.modules.io.ItemModuleEnergyInput;
import mcp.mobius.waila.api.*;
import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Container;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraftforge.common.util.ForgeDirection;
import cofh.api.energy.IEnergyReceiver;
import cpw.mods.fml.relauncher.*;

// BlockCBCable //
public class TileCBCable extends TileLM implements IPaintable, ICBNetTile, IGuiTile, IEnergyReceiver, IWailaTile.Body, ISecureTile
{
	public static final String ACTION_SET_CHANNEL = "silicio.channel";
	public static final String ACTION_MODULE_CONFIG = "silicio.mconfig";
	
	public final CircuitBoard[] boards = new CircuitBoard[6];
	public final Paint[] paint = new Paint[6];
	public boolean hasCover;
	private TileCBController controller;
	private final boolean[] canReceive = new boolean[6];
	private final boolean[] isDisabled = new boolean[6];
	public final boolean[] renderCableSide = new boolean[6];
	public final boolean[] renderCover = new boolean[6];
	
	public TileCBCable() { }
	
	private void updateRenderSides()
	{
		for(int i = 0; i < 6; i++)
		{
			renderCableSide[i] = boards[i] != null || connectCable(this, i);
			renderCover[i] = true;
			
			TileEntity te = getTile(i);
			if(te != null && !te.isInvalid() && te instanceof TileCBCable)
				if(((TileCBCable)te).hasCover && ((TileCBCable)te).paint[Facing.oppositeSide[i]] != null)
					renderCover[i] = false;
		}
	}
	
	public void onNeighborBlockChange(Block b)
	{
		super.onNeighborBlockChange(b);
		updateRenderSides();
		if(isServer()) markDirty();
	}
	
	public void onPlaced()
	{
		super.onPlaced();
		updateRenderSides();
	}
	
	public void onUpdatePacket()
	{
		updateRenderSides();
	}
	
	public boolean rerenderBlock()
	{ return false; }
	
	public TileCBController controller()
	{ return controller; }
	
	public void preUpdate(TileCBController c)
	{
		controller = c;
		
		for(int s = 0; s < boards.length; s++)
		{
			if(boards[s] != null)
			{
				boards[s].preUpdate();
				
				for(int i = 0; i < boards[s].items.length; i++)
				{
					if(boards[s].items[i] != null && boards[s].items[i].getItem() instanceof ISignalProvider)
						((ISignalProvider)boards[s].items[i].getItem()).provideSignals(boards[s], i, true);
				}
			}
		}
	}
	
	public void onUpdateCB()
	{
		for(int s = 0; s < 6; s++)
		{
			canReceive[s] = false;
			if(controller != null && canReceiveEnergy(s))
				canReceive[s] = true;
		}
		
		for(int s = 0; s < boards.length; s++)
		{
			if(boards[s] != null)
			{
				for(int i = 0; i < boards[s].items.length; i++)
				{
					if(boards[s].items[i] != null && boards[s].items[i].getItem() instanceof ICBModule)
					{
						if(boards[s].items[i].getItem() instanceof ISignalProvider)
							((ISignalProvider)boards[s].items[i].getItem()).provideSignals(boards[s], i, false);
						
						((ICBModule)boards[s].items[i].getItem()).onUpdate(boards[s], i);
					}
				}
			}
		}
		
		for(int j = 0; j < controller.channels.length; j++)
		{
			if(controller.channels[j].isEnabled() != controller.prevChannels[j].isEnabled())
			{
				for(int s = 0; s < boards.length; s++)
				{
					if(boards[s] != null)
					{
						for(int i = 0; i < boards[s].items.length; i++)
						{
							if(boards[s].items[i] != null && boards[s].items[i].getItem() instanceof IToggable)
								((IToggable)boards[s].items[i].getItem()).onChannelToggled(boards[s], i, controller.channels[j]);
						}
					}
				}
				
				controller.markDirty();
			}
		}
		
		for(int s = 0; s < boards.length; s++)
			if(boards[s] != null) boards[s].postUpdate();
	}
	
	public void readTileData(NBTTagCompound tag)
	{
		Arrays.fill(boards, null);
		
		NBTTagList l = (NBTTagList)tag.getTag("Boards");
		
		if(l != null && l.tagCount() > 0)
		for(int i = 0; i < l.tagCount(); i++)
		{
			NBTTagCompound tag1 = l.getCompoundTagAt(i);
			int id = tag1.getByte("ID");
			boards[id] = new CircuitBoard(this, id);
			boards[id].readTileData(tag1);
		}
		
		hasCover = tag.getBoolean("HasCover");
		Paint.readFromNBT(tag, "Paint", paint);

		Converter.toBools(isDisabled, tag.getIntArray("Disabled"), true);
	}
	
	public void writeTileData(NBTTagCompound tag)
	{
		NBTTagList l = new NBTTagList();
		
		for(int i = 0; i < boards.length; i++)
		if(boards[i] != null)
		{
			NBTTagCompound tag1 = new NBTTagCompound();
			boards[i].writeTileData(tag1);
			tag1.setByte("ID", (byte)i);
			l.appendTag(tag1);
		}
		
		if(l.tagCount() > 0)
			tag.setTag("Boards", l);
		
		tag.setBoolean("HasCover", hasCover);
		Paint.writeToNBT(tag, "Paint", paint);
		
		int[] idx = Converter.fromBools(isDisabled, true);
		if(idx.length > 0) tag.setIntArray("Disabled", idx);
	}
	
	public boolean setPaint(PaintData p)
	{
		if(p.paint != null && p.paint.block != null && p.paint.block != Blocks.glass && !p.paint.block.renderAsNormalBlock()) return false;
		
		if(p.player.isSneaking())
		{
			for(int i = 0; i < 6; i++)
				paint[i] = p.paint;
			markDirty();
			return true;
		}
		
		if(p.canReplace(paint[p.side]))
		{
			paint[p.side] = p.paint;
			markDirty();
			return true;
		}
		
		return false;
	}
	
	public static boolean connectCable(TileCBCable c, int s)
	{
		if(!c.isSideEnabled(s)) return false;
		TileEntity te = c.worldObj.getTileEntity(c.xCoord + Facing.offsetsXForSide[s], c.yCoord + Facing.offsetsYForSide[s], c.zCoord + Facing.offsetsZForSide[s]);
		return (te != null && te instanceof ICBNetTile && ((ICBNetTile)te).isSideEnabled(Facing.oppositeSide[s]));
	}
	
	public void onControllerDisconnected()
	{
		if(controller != null)
		{
			for(int i = 0; i < boards.length; i++)
			if(boards[i] != null) boards[i].preUpdate();
			
			markDirty();
			onNeighborBlockChange(Blocks.air);
		}
		
		for(int s = 0; s < 6; s++)
			canReceive[s] = false;
		
		controller = null;
	}
	
	private boolean canReceiveEnergy(int s)
	{
		if(boards[s] != null)
		{
			for(int i = 0; i < boards[s].items.length; i++)
			{
				if(boards[s].items[i] != null && boards[s].items[i].getItem() instanceof ItemModuleEnergyInput)
					return true;
			}
		}
		
		return false;
	}

	public boolean isOutputtingRS(int s)
	{ return isServer() && boards[s] != null && boards[s].redstoneOut; }

	public CircuitBoard getBoard(int side)
	{
		if(side >= 0 && side < boards.length)
			return boards[side]; return null;
	}
	
	public CircuitBoard getBoard(ForgeDirection dir)
	{
		if(dir == null || dir == ForgeDirection.UNKNOWN)
			return null;
		return getBoard(dir.ordinal());
	}
	
	public void onUpdate()
	{
	}
	
	public boolean onRightClick(EntityPlayer ep, ItemStack is, int side, float x, float y, float z)
	{
		if(is != null && is.getItem() instanceof IPaintable.IPainterItem)
			return false;
		
		MovingObjectPosition mop = MathHelperLM.rayTrace(ep);
		
		if(mop == null) return false;
		
		int id = -1;
		
		if(!hasCover)
		{
			if(is != null && SilMat.coverBlock != null && InvUtils.itemsEquals(is, SilMat.coverBlock, false, true))
			{
				if(isServer())
				{
					hasCover = true;
					
					if(!ep.capabilities.isCreativeMode)
						is.stackSize--;
					
					markDirty();
				}
				
				return true;
			}
			
			if(mop != null && mop.subHit >= 0 && mop.subHit <= 6)
			{
				id = (mop.subHit == 6) ? mop.sideHit : mop.subHit;
				
				if(isServer() && mop.subHit == 6 && ep.isSneaking() && LatCoreMC.isWrench(is))
				{
					if(worldObj.setBlockToAir(xCoord, yCoord, zCoord))
						InvUtils.dropItem(worldObj, xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D, new ItemStack(SilItems.b_cbcable), 8);
				}
			}
		}
		else id = side;
		
		if(id < 0 || id >= 6) return true;
		
		if(is != null && boards[id] == null && is.getItem() == SilItems.b_cbcable.getItem()) return false;
		
		if(LatCoreMC.isWrench(is) && !ep.isSneaking())
		{
			if(boards[id] != null) return true;
			setDisabled(id, !isDisabled[id]);
			return true;
		}
		
		if(!isServer()) return true;
		
		if(boards[id] == null)
		{
			if(is != null && is.getItem() == SilItems.i_circuit_board)
			{
				setDisabled(id, false);
				boards[id] = new CircuitBoard(this, id);
				if(!ep.capabilities.isCreativeMode) is.stackSize--;
				markDirty();
			}
			else if(LatCoreMC.isWrench(is))
			{
				if(!ep.isSneaking())
				{
					isDisabled[id] = !isDisabled[id];
					markDirty();
				}
				else if(hasCover)
				{
					hasCover = false;
					if(!ep.capabilities.isCreativeMode && SilMat.coverBlock != null)
						InvUtils.dropItem(ep, SilMat.coverBlock);
					markDirty();
				}
			}
		}
		else
		{
			if(is != null && ep.isSneaking() && LatCoreMC.isWrench(is))
			{
				if(!ep.capabilities.isCreativeMode)
					InvUtils.dropItem(ep, new ItemStack(SilItems.i_circuit_board));
				
				for(int i = 0; i < boards[id].items.length; i++)
				{
					if(boards[id].items[i] != null && boards[id].items[i].stackSize > 0)
						InvUtils.dropItem(ep, boards[id].items[i]);
				}
				
				boards[id] = null;
				markDirty();
			}
			else
			{
				LatCoreMC.openGui(ep, this, guiData(id, 0, -1));
			}
		}
		
		return true;
	}
	
	public void onBroken()
	{
		super.onBroken();
		
		if(isServer())
		{
			if(hasCover && SilMat.coverBlock != null)
				InvUtils.dropItem(worldObj, xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D, SilMat.coverBlock, 8);
			
			for(int i = 0; i < boards.length; i++)
			{
				if(boards[i] != null)
				{
					InvUtils.dropItem(worldObj, xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D, new ItemStack(SilItems.i_circuit_board), 8);
					InvUtils.dropAllItems(worldObj, xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D, boards[i].items);
					boards[i] = null;
				}
			}
		}
	}
	
	public boolean isAABBEnabled(int i)
	{
		if(i == 6 || boards[i] != null) return true;
		
		Block block = worldObj.getBlock(xCoord + Facing.offsetsXForSide[i], yCoord + Facing.offsetsYForSide[i], zCoord + Facing.offsetsZForSide[i]);
		if(block == SilItems.b_cbcable || block == SilItems.b_cbcontroller) return true;
		
		EntityPlayer clientP = LC.proxy.getClientPlayer();
		
		if(clientP != null && clientP.getHeldItem() != null)
		{
			Item item = clientP.getHeldItem().getItem();
			if((item == SilItems.i_circuit_board && block != Blocks.air)) return true;
		}
		
		return false;
	}
	
	public double getRelStoredEnergy()
	{ return (controller == null || controller.isInvalid()) ? 0D : (controller.storage.getEnergyStored() / (double)controller.storage.getMaxEnergyStored()); }
	
	public static NBTTagCompound guiData(int side, int gui, int module)
	{
		NBTTagCompound data = new NBTTagCompound();
		data.setByte("Side", (byte)side);
		data.setByte("Gui", (byte)gui);
		if(module > 0) data.setByte("MID", (byte)module);
		return data;
	}
	
	public Container getContainer(EntityPlayer ep, NBTTagCompound data)
	{
		int side = data.getByte("Side");
		int gui = data.getByte("Gui");
		
		CircuitBoard t = getBoard(side);
		
		if(t != null)
		{
			if(gui == 0) return new ContainerCircuitBoard(ep, t);
			else if(gui == 1) return new ContainerCircuitBoardSettings(ep, t);
			else if(gui == 2) return new ContainerModuleSettings(ep, t);
			else if(gui == 3) return new ContainerEmpty(ep, t);
		}
		
		return null;
	}
	
	@SideOnly(Side.CLIENT)
	public GuiScreen getGui(EntityPlayer ep, NBTTagCompound data)
	{
		int side = data.getByte("Side");
		int gui = data.getByte("Gui");
		int moduleID = data.getByte("MID");
		
		CircuitBoard cb = getBoard(side);
		
		if(cb != null)
		{
			if(gui == 0) return new GuiCircuitBoard(new ContainerCircuitBoard(ep, cb));
			else if(gui == 1) return new GuiCircuitBoardSettings(new ContainerCircuitBoardSettings(ep, cb));
			else if(gui == 2) return new GuiModuleSettings(new ContainerModuleSettings(ep, cb), moduleID);
			else if(gui == 3) return new GuiSelectChannels(new ContainerEmpty(ep, cb), moduleID);
		}
		
		return null;
	}
	
	public void onClientAction(EntityPlayer ep, String action, NBTTagCompound data)
	{
		if(action.equals(ACTION_SET_CHANNEL))
		{
			int side = data.getByte("F");
			int moduleID = data.getByte("M");
			int id = data.getByte("I");
			int ch = data.getInteger("C");
			
			ICBModule m = boards[side].getModule(moduleID);
			m.setChannel(boards[side], moduleID, id, ch);
			markDirty();
		}
		else if(action.equals(ACTION_MODULE_CONFIG))
		{
			int side = data.getByte("F");
			int moduleID = data.getByte("M");
			int id = data.getByte("I");
			NBTTagCompound tag = (NBTTagCompound)data.getTag("D");
			
			for(ModuleConfigSegment mcs : ((ICBModule)boards[side].items[moduleID].getItem()).getModuleConfig())
			{
				if(mcs.ID == id)
				{
					mcs.onConfigReceived(boards[side], moduleID, tag);
					return;
				}
			}
		}
		else super.onClientAction(ep, action, data);
	}
	
	public void clientSetChannel(int side, int moduleID, int id, int ch)
	{
		NBTTagCompound data = new NBTTagCompound();
		data.setByte("F", (byte)side);
		data.setByte("M", (byte)moduleID);
		data.setByte("I", (byte)id);
		data.setInteger("C", ch);
		sendClientAction(ACTION_SET_CHANNEL, data);
	}
	
	public void clientModuleConfig(CircuitBoard cb, int moduleID, int c, NBTTagCompound tag)
	{
		NBTTagCompound data = new NBTTagCompound();
		data.setByte("F", (byte)cb.side);
		data.setByte("M", (byte)moduleID);
		data.setByte("I", (byte)c);
		if(tag != null) data.setTag("D", tag);
		sendClientAction(ACTION_MODULE_CONFIG, data);
	}
	
	public boolean canConnectEnergy(ForgeDirection f)
	{ return canReceive[f.ordinal()]; }
	
	public int getEnergyStored(ForgeDirection f)
	{ return (controller() == null) ? 0 : controller().getEnergyStored(f); }
	
	public int getMaxEnergyStored(ForgeDirection f)
	{ return (controller() == null) ? 0 : controller().getMaxEnergyStored(f); }
	
	public int receiveEnergy(ForgeDirection f, int e, boolean b)
	{ return (controller() == null || !canConnectEnergy(f)) ? 0 : controller().receiveEnergy(f, e, b); }
	
	public void setDisabled(int side, boolean b)
	{
		if(isDisabled[side] != b)
		{
			isDisabled[side] = b;
			notifyNeighbors();
			onNeighborBlockChange(getBlockType());
			markDirty();
			
			TileEntity te = worldObj.getTileEntity(xCoord + Facing.offsetsXForSide[side], yCoord + Facing.offsetsYForSide[side], zCoord + Facing.offsetsZForSide[side]);
			if(te != null && !te.isInvalid() && te instanceof TileCBCable)
			{
				TileCBCable t = (TileCBCable)te;
				if(t.isDisabled[Facing.oppositeSide[side]] != b)
				{
					t.isDisabled[Facing.oppositeSide[side]] = b;
					t.notifyNeighbors();
					t.onNeighborBlockChange(getBlockType());
					t.markDirty();
				}
			}
		}
	}
	
	public boolean isSideEnabled(int side)
	{
		if(side < 0 || side >= 6) return false;
		return !isDisabled[side];
	}
	
	public void addWailaBody(IWailaDataAccessor data, IWailaConfigHandler config, List<String> info)
	{
		int i = data.getPosition().subHit;
		if(i >= 0 && i < 6) { if(!isSideEnabled(i)) info.add("Disabled"); }
	}
	
	public LMSecurity getSecurity()
	{ if(controller != null) return controller.getSecurity(); return security; }

	public boolean isOnline()
	{ return controller != null && !controller.hasConflict; }
	
	@SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox()
    { return AxisAlignedBB.getBoundingBox(xCoord, yCoord, zCoord, xCoord + 1D, yCoord + 1D, zCoord + 1D); }
}