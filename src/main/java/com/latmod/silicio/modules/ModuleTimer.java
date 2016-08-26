package com.latmod.silicio.modules;

import com.feed_the_beast.ftbl.api.recipes.IRecipes;
import com.latmod.silicio.api.EnumSignalSlot;
import com.latmod.silicio.api.IModule;
import com.latmod.silicio.api.IModuleContainer;
import com.latmod.silicio.api.ISilNetController;
import com.latmod.silicio.api_impl.properties.ModulePropertyKey;
import com.latmod.silicio.api_impl.properties.PropertyShort;
import net.minecraft.item.ItemStack;

/**
 * Created by LatvianModder on 05.03.2016.
 */
public class ModuleTimer implements IModule
{
    private static final ModulePropertyKey<PropertyShort> TIMER = new ModulePropertyKey<>("timer", new PropertyShort(20), null);

    @Override
    public void init(IModuleContainer container)
    {
        container.addConnection(EnumSignalSlot.OUT_1);
        container.addProperty(TIMER);
    }

    @Override
    public void addRecipes(ItemStack stack, IRecipes recipes)
    {
    }

    @Override
    public void provideSignals(IModuleContainer container, ISilNetController controller)
    {
        if(container.getTick() % container.getProperty(TIMER).getLong() == 0L)
        {
            controller.provideSignal(container.getChannel(EnumSignalSlot.OUT_1));
        }
    }
}