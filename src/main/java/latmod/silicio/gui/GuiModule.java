package latmod.silicio.gui;

import latmod.ftbu.core.gui.*;
import latmod.silicio.Silicio;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import cpw.mods.fml.relauncher.*;

@SideOnly(Side.CLIENT)
public class GuiModule extends GuiLM
{
	public GuiModule(EntityPlayer ep, ResourceLocation tex)
	{ super(new ContainerEmpty(ep, null), tex); }
	
	public static ResourceLocation getTex(String s)
	{ return Silicio.mod.getLocation("textures/gui/" + s); }
}