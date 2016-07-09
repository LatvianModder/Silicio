package com.latmod.silicio.modules;

import com.latmod.silicio.api.SignalChannel;
import com.latmod.silicio.api.modules.EnumModuleIO;
import com.latmod.silicio.api.modules.Module;
import com.latmod.silicio.api.modules.ModuleContainer;
import net.minecraft.entity.player.EntityPlayerMP;

/**
 * Created by LatvianModder on 04.03.2016.
 */
public class ModuleChatOutput extends Module
{
    @Override
    public void init(ModuleContainer c)
    {
        c.addConnection(EnumModuleIO.IN_1);
    }

    @Override
    public void onAdded(ModuleContainer c, EntityPlayerMP player)
    {
    }

    @Override
    public void onSignalChanged(ModuleContainer c, SignalChannel id, boolean on)
    {
        if(on && id.equals(c.getChannel(EnumModuleIO.IN_1)))
        {
        }
    }
}