package com.latmod.silicio.item;

import com.latmod.lib.LangKey;
import com.latmod.silicio.api.SilicioAPI;
import com.latmod.silicio.api.module.IModule;
import com.latmod.silicio.api.module.IModuleContainer;
import com.latmod.silicio.api.module.IModulePropertyKey;
import com.latmod.silicio.api.module.impl.ModuleContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

/**
 * Created by LatvianModder on 04.03.2016.
 */
public class ItemModule extends ItemSil
{
    private static final LangKey DESC = new LangKey("silicio.item.module_desc");

    private final IModule module;

    public ItemModule(IModule m)
    {
        module = m;
        setMaxStackSize(1);
        setMaxDamage(0);
    }

    public IModule getModule()
    {
        return module;
    }

    @Override
    public ICapabilityProvider initCapabilities(final ItemStack stack, final NBTTagCompound nbt)
    {
        return new ModuleContainer(getModule());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced)
    {
        tooltip.add(DESC.translate());

        if(advanced)
        {
            IModuleContainer moduleContainer = stack.getCapability(SilicioAPI.MODULE_CONTAINER, null);
            tooltip.add("Tick: " + moduleContainer.getTick());
            tooltip.add("Properties:");

            for(IModulePropertyKey key : moduleContainer.getModule().getProperties())
            {
                tooltip.add("> " + key.getName() + ": " + moduleContainer.getProperty(key).getString());
            }
        }
    }
}
