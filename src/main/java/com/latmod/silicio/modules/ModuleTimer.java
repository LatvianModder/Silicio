package com.latmod.silicio.modules;

import com.feed_the_beast.ftbl.api.recipes.IRecipes;
import com.latmod.lib.config.ConfigKey;
import com.latmod.lib.config.PropertyInt;
import com.latmod.silicio.Silicio;
import com.latmod.silicio.api.module.IModuleContainer;
import com.latmod.silicio.api.tile.ISilNetController;
import com.latmod.silicio.api.tile.ISocketBlock;
import com.latmod.silicio.api_impl.module.EnumSignalSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;

/**
 * Created by LatvianModder on 05.03.2016.
 */
public class ModuleTimer extends ModuleBase
{
    private static final ConfigKey TIMER = new ConfigKey("timer", new PropertyInt(Constants.NBT.TAG_SHORT, 20).setMin(0).setMax(32000));

    public ModuleTimer()
    {
        super(new ResourceLocation(Silicio.MOD_ID, "timer"));
        properties.add(EnumSignalSlot.OUT_1);
        properties.add(TIMER);
    }

    @Override
    public void addRecipes(ItemStack stack, IRecipes recipes)
    {
    }

    @Override
    public void provideSignals(ISocketBlock socketBlock, ISilNetController controller)
    {
        IModuleContainer container = socketBlock.getContainer();

        if(container.getTick() % container.getProperty(TIMER).getInt() == 0L)
        {
            controller.provideSignal(container.getProperty(EnumSignalSlot.OUT_1).getInt());
        }
    }
}