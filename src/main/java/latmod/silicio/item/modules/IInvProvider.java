package latmod.silicio.item.modules;

import latmod.core.util.FastList;
import latmod.silicio.tile.cb.*;

public interface IInvProvider
{
	public void updateInvNet(ModuleEntry e, FastList<InvEntry> l);
}