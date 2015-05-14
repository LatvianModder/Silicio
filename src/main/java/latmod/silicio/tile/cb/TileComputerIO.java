package latmod.silicio.tile.cb;

import latmod.core.tile.TileLM;
import latmod.core.util.IntList;
import latmod.silicio.item.modules.events.*;
import net.minecraft.nbt.NBTTagCompound;
import dan200.computercraft.api.lua.*;
import dan200.computercraft.api.peripheral.*;

public class TileComputerIO extends TileLM implements ICBNetTile, IPeripheral, IToggableTile // BlockComputerIO
{
	public IntList enabledChannels = new IntList();
	private TileCBController controller = null;
	private IComputerAccess attachedComputer = null;
	
	public void readTileData(NBTTagCompound tag)
	{
		super.readTileData(tag);
		
		enabledChannels.clear();
		enabledChannels.addAll(tag.getIntArray("Enabled"));
	}
	
	public void writeTileData(NBTTagCompound tag)
	{
		super.writeTileData(tag);
		tag.setIntArray("Enabled", enabledChannels.toArray());
	}
	
	public void preUpdate(TileCBController c)
	{
		controller = c;
		c.channels.addAll(enabledChannels);
	}
	
	public void onUpdateCB()
	{
	}
	
	public void onControllerConnected(EventControllerConnected e)
	{ if(controller == null) controller = e.controller; }
	
	public void onControllerDisconnected(EventControllerDisconnected e)
	{ if(controller != null && controller.equals(e.controller)) controller = null; }
	
	public boolean isSideEnabled(int side)
	{ return true; }
	
	public String getType()
	{ return "cb_io"; }
	
	public String[] getMethodNames()
	{ return new String[] { "setChannel", "getChannel" }; }
	
	public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) throws LuaException, InterruptedException
	{
		if(method == 0)
		{
			if(arguments != null && arguments.length == 2)
			{
				int c = ((Number)arguments[0]).intValue();
				boolean b = ((Boolean)arguments[1]).booleanValue();
				
				if(!b) enabledChannels.removeValue(c);
				else if(!enabledChannels.contains(c))
					enabledChannels.add(c);
			}
		}
		else if(method == 1)
		{
			if(controller == null || arguments == null || arguments.length < 1)
				return new Object[] { false };
			
			int c = ((Number)arguments[0]).intValue() - 1;
			return new Object[] { controller.channels.contains(c) };
		}
		
		return null;
	}
	
	public void attach(IComputerAccess computer)
	{ if(attachedComputer == null) attachedComputer = computer; }
	
	public void detach(IComputerAccess computer)
	{ if(attachedComputer != null && computer.equals(attachedComputer)) attachedComputer = null; }
	
	public boolean equals(IPeripheral other)
	{ return super.equals(other); }
	
	public void onChannelToggledTile(EventChannelToggledTile e)
	{
		if(controller != null && attachedComputer != null)
			attachedComputer.queueEvent("cb_channel", new Object[] { (e.channel + 1), e.on });
	}
}