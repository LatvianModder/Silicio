package com.latmod.silicio.block;

import com.latmod.lib.util.LMInvUtils;
import com.latmod.silicio.api.SilicioAPI;
import com.latmod.silicio.api.module.impl.SocketBlock;
import com.latmod.silicio.api.tile.ISocketBlock;
import com.latmod.silicio.tile.TileSocketBlock;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nullable;

/**
 * Created by LatvianModder on 04.03.2016.
 */
public class BlockSocketBlock extends BlockSil
{
    private static final PropertyBool MODULE_D = PropertyBool.create("down");
    private static final PropertyBool MODULE_U = PropertyBool.create("up");
    private static final PropertyBool MODULE_N = PropertyBool.create("north");
    private static final PropertyBool MODULE_S = PropertyBool.create("south");
    private static final PropertyBool MODULE_W = PropertyBool.create("west");
    private static final PropertyBool MODULE_E = PropertyBool.create("east");

    public BlockSocketBlock()
    {
        super(Material.IRON);
        setDefaultState(blockState.getBaseState().withProperty(MODULE_D, false).withProperty(MODULE_U, false).withProperty(MODULE_N, false).withProperty(MODULE_S, false).withProperty(MODULE_W, false).withProperty(MODULE_E, false));
    }

    @Override
    public boolean hasTileEntity(IBlockState state)
    {
        return true;
    }

    @Override
    public TileEntity createTileEntity(World w, IBlockState state)
    {
        return new TileSocketBlock();
    }

    @Override
    @Deprecated
    public IBlockState getStateFromMeta(int meta)
    {
        return getDefaultState();
    }

    @Override
    public int getMetaFromState(IBlockState state)
    {
        return 0;
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, MODULE_D, MODULE_U, MODULE_N, MODULE_S, MODULE_W, MODULE_E);
    }

    @Override
    @Deprecated
    public IBlockState getActualState(IBlockState state, IBlockAccess w, BlockPos pos)
    {
        boolean modD = false, modU = false, modN = false, modS = false, modW = false, modE = false;

        TileEntity te = w.getTileEntity(pos);

        if(te instanceof TileSocketBlock)
        {
            TileSocketBlock tile = (TileSocketBlock) te;
            modD = tile.hasCapability(SilicioAPI.SOCKET_BLOCK, EnumFacing.DOWN);
            modU = tile.hasCapability(SilicioAPI.SOCKET_BLOCK, EnumFacing.UP);
            modN = tile.hasCapability(SilicioAPI.SOCKET_BLOCK, EnumFacing.NORTH);
            modS = tile.hasCapability(SilicioAPI.SOCKET_BLOCK, EnumFacing.SOUTH);
            modW = tile.hasCapability(SilicioAPI.SOCKET_BLOCK, EnumFacing.WEST);
            modE = tile.hasCapability(SilicioAPI.SOCKET_BLOCK, EnumFacing.EAST);
        }

        return state.withProperty(MODULE_D, modD).withProperty(MODULE_U, modU).withProperty(MODULE_N, modN).withProperty(MODULE_S, modS).withProperty(MODULE_W, modW).withProperty(MODULE_E, modE);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        TileEntity te = worldIn.getTileEntity(pos);

        if(te == null)
        {
            return false;
        }

        if(te.hasCapability(SilicioAPI.SOCKET_BLOCK, side))
        {
            if(!worldIn.isRemote)
            {
                if(heldItem == null && playerIn.isSneaking())
                {
                    SocketBlock c = (SocketBlock) te.getCapability(SilicioAPI.SOCKET_BLOCK, side);

                    if(c.hasContainer())
                    {
                        c.getContainer().getModule().onRemoved(c, (EntityPlayerMP) playerIn);
                        LMInvUtils.giveItem(playerIn, c.getStack());
                        c.setItem(null);
                    }

                    te.markDirty();
                }
            }

            return true;
        }
        else if(heldItem != null && heldItem.hasCapability(SilicioAPI.MODULE_CONTAINER, null))
        {
            if(!worldIn.isRemote && te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side))
            {
                ItemStack itemStack = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side).insertItem(0, heldItem, false);

                if(itemStack == null || itemStack.stackSize == heldItem.stackSize - 1)
                {
                    heldItem.stackSize--;
                    ISocketBlock socketBlock = te.getCapability(SilicioAPI.SOCKET_BLOCK, side);
                    socketBlock.getContainer().getModule().onAdded(socketBlock, (EntityPlayerMP) playerIn);
                    te.markDirty();
                }
            }

            return true;
        }

        return false;
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state)
    {
        if(!worldIn.isRemote)
        {
            TileEntity te = worldIn.getTileEntity(pos);

            if(te instanceof TileSocketBlock)
            {
                for(EnumFacing facing : EnumFacing.VALUES)
                {
                    if(te.hasCapability(SilicioAPI.SOCKET_BLOCK, facing))
                    {
                        ISocketBlock c = te.getCapability(SilicioAPI.SOCKET_BLOCK, facing);
                        InventoryHelper.spawnItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), c.getStack());
                    }
                }
            }
        }

        super.breakBlock(worldIn, pos, state);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer()
    {
        return BlockRenderLayer.CUTOUT;
    }
}
