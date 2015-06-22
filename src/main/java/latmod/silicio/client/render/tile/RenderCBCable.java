package latmod.silicio.client.render.tile;

import latmod.ftbu.core.client.*;
import latmod.silicio.*;
import latmod.silicio.tile.cb.TileCBCable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.*;

import org.lwjgl.opengl.*;

import cpw.mods.fml.relauncher.*;

@SideOnly(Side.CLIENT)
public class RenderCBCable extends TileRenderer<TileCBCable>
{
	public static final ResourceLocation texture_off = Silicio.mod.getLocation("textures/blocks/cable_offline.png");
	public static final ResourceLocation texture_on = Silicio.mod.getLocation("textures/blocks/cable_online.png");
	public static final RenderCBCable instance = new RenderCBCable();
	
	public static RenderBlocksCustom renderBlocks;
	public static ModelCBCable model;
	
	public RenderCBCable()
	{
		renderBlocks = new RenderBlocksCustom();
		model = new ModelCBCable();
	}
	
	public void renderTile(TileCBCable t, double rx, double ry, double rz, float f)
	{
		//if(true) return;
		
		GL11.glPushMatrix();
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		GL11.glColor4f(1F, 1F, 1F, 1F);
		
		GL11.glTranslated(rx, ry + 1D, rz + 1D);
		GL11.glScaled(1D, -1D, -1D);
		GL11.glTranslated(0.5D, 0.5D, 0.5D);
		
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture_off);
		
		float s = 0.0625F;
		
		model.center.render(s);
		
		for(int i = 0; i < 6; i++)
		{
			if(t.renderCableSide[i])
			{
				model.cable[i].render(s);
				if(t.boards[i] != null)
					model.board[i].render(s);
			}
		}
		
		if(t.controller != null)
		{
			int c = 0;
			
			for(int i = 0; i < 6; i++)
				if(t.renderCableSide[i] || t.boards[i] != null) c++;
			
			if(c == 2)
			{
				if(!((t.renderCableSide[0] && t.renderCableSide[1] && !t.renderCableSide[2] && !t.renderCableSide[3] && !t.renderCableSide[4] && !t.renderCableSide[5])
				|| (!t.renderCableSide[0] && !t.renderCableSide[1] && t.renderCableSide[2] && t.renderCableSide[3] && !t.renderCableSide[4] && !t.renderCableSide[5])
				|| (!t.renderCableSide[0] && !t.renderCableSide[1] && !t.renderCableSide[2] && !t.renderCableSide[3] && t.renderCableSide[4] && t.renderCableSide[5])))
					c = 1;
			}
			
			if(c > 0)
			{
				float sc = 1.01F;
				GL11.glScalef(sc, sc, sc);
				LatCoreMCClient.pushMaxBrightness();
				GL11.glDisable(GL11.GL_LIGHTING);
				
				Minecraft.getMinecraft().getTextureManager().bindTexture(texture_on);
				
				if(t.controller.hasConflict)
					GL11.glColor4f(1F, 0F, 0F, 0.5F);
				else
					GL11.glColor4f(0F, 1F, 1F, 0.5F);
				
				GL11.glEnable(GL11.GL_BLEND);
				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
				
				if(c != 2) model.center.render(s);
				
				for(int i = 0; i < 6; i++)
				{
					if(t.boards[i] != null || t.getBlock(i) == SilItems.b_cbcontroller)
						model.cable[i].render(s);
				}
				
				GL11.glDisable(GL11.GL_BLEND);
				
				for(int i = 0; i < 6; i++)
				{
					if(t.boards[i] != null)
						model.board[i].render(s);
				}
				
				GL11.glColor4f(1F, 1F, 1F, 1F);
				LatCoreMCClient.popMaxBrightness();
				GL11.glEnable(GL11.GL_LIGHTING);
			}
		}
		
		GL11.glPopMatrix();
		GL11.glDisable(GL12.GL_RESCALE_NORMAL);
		
		if(t.hasCover)
		{
			GL11.glPushMatrix();
			GL11.glTranslated(rx + 0.5D, ry + 0.5D, rz + 0.5D);
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glDisable(GL11.GL_CULL_FACE);
			
			Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
			
			IIcon[] icons = new IIcon[6];
			
			for(int i = 0; i < 6; i++)
			{
				if(t.renderCover[i])
				{
					icons[i] = SilItems.b_cbcable.icon_cover;
					
					if(t.paint[i] != null && t.paint[i].block != null)
						icons[i] = t.paint[i].block.getBlockTextureFromSide(i);
				}
				else
				{
					icons[i] = LatCoreMCClient.blockNullIcon;
				}
			}
			
			double d = 0D;//0.001D;
			renderBlocks.fullBlock.setBounds(d, d, d, 1D - d, 1D - d, 1D - d);
			renderBlocks.blockAccess = t.getWorldObj();
			renderBlocks.renderAllFaces = true;
			renderBlocks.setRenderBounds(renderBlocks.fullBlock);
			renderBlocks.renderStandardBlockIcons(t.getBlockType(), t.xCoord, t.yCoord, t.zCoord, icons, true);
			
			GL11.glEnable(GL11.GL_CULL_FACE);
			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glPopMatrix();
		}
	}
}