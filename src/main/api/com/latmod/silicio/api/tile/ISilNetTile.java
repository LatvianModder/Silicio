package com.latmod.silicio.api.tile;

import gnu.trove.map.TShortByteMap;
import net.minecraft.entity.player.EntityPlayer;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Created by LatvianModder on 03.03.2016.
 */
public interface ISilNetTile
{
    @Nullable
    UUID getControllerID();

    void setControllerID(@Nullable UUID id, EntityPlayer playerIn);

    void provideSignals(ISilNetController controller);

    void onSignalsChanged(ISilNetController controller, TShortByteMap channels);
}