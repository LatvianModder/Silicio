package com.latmod.silicio.tile;

import com.feed_the_beast.ftbl.api.tile.EnumSync;
import com.latmod.silicio.api.ISilNetController;
import com.latmod.silicio.api.SilicioAPI;
import gnu.trove.TIntCollection;
import gnu.trove.impl.Constants;
import gnu.trove.map.TIntByteMap;
import gnu.trove.map.hash.TIntByteHashMap;
import gnu.trove.set.hash.TIntHashSet;
import net.darkhax.tesla.api.implementation.BaseTeslaContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

/**
 * Created by LatvianModder on 05.03.2016.
 */
public class TileSilNetController extends TileSilNet implements ITickable, ISilNetController
{
    public final BaseTeslaContainer energyTank = new BaseTeslaContainer(0, 500000, 200, 0);
    private final TIntCollection signals = new TIntHashSet();
    private final TIntCollection signalsPrev = new TIntHashSet();
    private final TIntByteMap changedSignals = new TIntByteHashMap(3, Constants.DEFAULT_LOAD_FACTOR, 0, (byte) -1);
    private Collection<TileEntity> network = new ArrayList<>();
    private boolean updateNetwork = true;

    @Override
    public EnumSync getSync()
    {
        return EnumSync.RERENDER;
    }

    @Override
    @Nullable
    public UUID getControllerID()
    {
        if(controllerID == null)
        {
            controllerID = UUID.randomUUID();
        }

        return controllerID;
    }

    @Override
    public void setControllerID(@Nullable UUID id, @Nonnull EntityPlayer playerIn)
    {
    }

    @Override
    public void readTileData(@Nonnull NBTTagCompound nbt)
    {
        super.readTileData(nbt);
        signals.clear();
        signalsPrev.clear();
        signals.addAll(nbt.getIntArray("Signals"));
        signalsPrev.addAll(nbt.getIntArray("PrevSignals"));
    }

    @Override
    public void writeTileData(@Nonnull NBTTagCompound nbt)
    {
        super.writeTileData(nbt);
        nbt.setIntArray("Signals", signals.toArray());
        nbt.setIntArray("PrevSignals", signalsPrev.toArray());
    }

    @Override
    public void readTileClientData(@Nonnull NBTTagCompound nbt)
    {
        super.readTileClientData(nbt);
        signals.clear();
        signalsPrev.clear();
        signals.addAll(nbt.getIntArray("S"));
    }

    @Override
    public void writeTileClientData(@Nonnull NBTTagCompound nbt)
    {
        super.writeTileClientData(nbt);
        nbt.setIntArray("S", signals.toArray());
    }

    @Override
    public void update()
    {
        if(getSide().isClient())
        {
            return;
        }

        getNetwork();

        if(!network.isEmpty())
        {
            signalsPrev.clear();
            signalsPrev.addAll(signals);

            for(TileEntity tile : network)
            {
                if(tile.hasCapability(SilicioAPI.SILNET_TILE, null))
                {
                    tile.getCapability(SilicioAPI.SILNET_TILE, null).provideSignals(this);
                }
            }

            if(!signalsPrev.isEmpty())
            {
                signalsPrev.forEach(id ->
                {
                    if(!signals.contains(id))
                    {
                        changedSignals.put(id, (byte) 0);
                    }

                    return true;
                });
            }

            if(!changedSignals.isEmpty())
            {
                for(TileEntity tile : network)
                {
                    if(tile.hasCapability(SilicioAPI.SILNET_TILE, null))
                    {
                        tile.getCapability(SilicioAPI.SILNET_TILE, null).onSignalsChanged(this, changedSignals);
                    }
                }
            }

            signals.clear();
            changedSignals.clear();
        }

        checkIfDirty();
    }

    @Override
    public void onSilNetUpdate()
    {
        updateNetwork = true;
    }

    @Override
    public Collection<TileEntity> getNetwork()
    {
        if(updateNetwork)
        {
            network.clear();
            SilicioAPI.get().findSilNetTiles(network, getControllerID());
            network.remove(this);
            updateNetwork = false;
        }

        return network;
    }

    @Override
    public boolean getSignal(int id)
    {
        return id != 0 && signals.contains(id);
    }

    @Override
    public void provideSignal(int id)
    {
        if(id != 0)
        {
            signals.add(id);

            if(!signalsPrev.contains(id))
            {
                changedSignals.put(id, (byte) 1);
            }
        }
    }
}