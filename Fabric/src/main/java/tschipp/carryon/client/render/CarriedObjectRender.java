/*
 * GNU Lesser General Public License v3
 * Copyright (C) 2024 Tschipp
 * mrtschipp@gmail.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package tschipp.carryon.client.render;

import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import tschipp.carryon.Constants;
import tschipp.carryon.common.carry.CarryOnData;
import tschipp.carryon.common.carry.CarryOnData.CarryType;
import tschipp.carryon.common.carry.CarryOnDataManager;
import tschipp.carryon.common.scripting.CarryOnScript;
import tschipp.carryon.common.scripting.CarryOnScript.ScriptRender;
import tschipp.carryon.platform.Services;

import java.util.*;

public class CarriedObjectRender
{
	public static boolean draw(Player player, PoseStack matrix, int light, float partialTicks,SubmitNodeCollector nodeCollector, boolean firstPerson)
	{
		if(Services.PLATFORM.isModLoaded("firstperson") || Services.PLATFORM.isModLoaded("firstpersonmod") || player == null)
			return false;

		CarryOnData carry = CarryOnDataManager.getCarryData(player);
		try {
			if (carry.isCarrying(CarryType.BLOCK))
				drawBlock(player,  matrix, light, CarryRenderHelper.getRenderState(player), nodeCollector, firstPerson);
			else if (carry.isCarrying(CarryType.ENTITY))
				drawEntity(player, matrix, light, partialTicks,nodeCollector, firstPerson);
		}
		catch (Exception e)
		{
			//hehe
		}

		if(carry.getActiveScript().isPresent())
		{
			ScriptRender render = carry.getActiveScript().get().scriptRender();
			if(!render.renderLeftArm() && player.getMainArm() == HumanoidArm.LEFT)
				return false;

			if(!render.renderRightArm() && player.getMainArm() == HumanoidArm.RIGHT)
				return false;
		}

		return carry.isCarrying();
	}

	private static void drawBlock(Player player, PoseStack matrix, int light, BlockState state,SubmitNodeCollector nodeCollector, boolean firstPerson)
	{
		matrix.pushPose();
		if (firstPerson){
			matrix.scale(2.5f, 2.5f, 2.5f);
			matrix.translate(0, -0.5, -1);
		}else{
			matrix.scale(0.6f, 0.6f, 0.6f);
			matrix.translate(0, 0.5, -0.8);
			matrix.mulPose(Axis.ZN.rotationDegrees(180));

		}

		CarryOnData carry = CarryOnDataManager.getCarryData(player);
		ItemStackRenderState renderState = new ItemStackRenderState();
		var layer = renderState.newLayer();
		layer.setRenderType(RenderTypes.glint());

		if (Constants.CLIENT_CONFIG.facePlayer != CarryRenderHelper.isChest(state.getBlock())) {
			matrix.mulPose(Axis.YP.rotationDegrees(180));
			matrix.mulPose(Axis.XN.rotationDegrees(8));
		} else {
			matrix.mulPose(Axis.XP.rotationDegrees(8));
		}

		if(carry.getActiveScript().isPresent())
			CarryRenderHelper.performScriptTransformation(matrix, carry.getActiveScript().get());

		ItemStack renderStack = CarryRenderHelper.getRenderItemStack(player);
		Minecraft.getInstance().getItemModelResolver().updateForTopItem(renderState, renderStack, ItemDisplayContext.NONE, player.level(), null, 0);
		renderState.submit(matrix, nodeCollector, light,  OverlayTexture.NO_OVERLAY, 0);
		matrix.popPose();
	}

	private static void drawEntity(Player player, PoseStack matrix, int light, float partialTicks,SubmitNodeCollector nodeCollector, boolean firstPerson) {
		EntityRenderDispatcher manager = Minecraft.getInstance().getEntityRenderDispatcher();
		
				
		Entity entity = CarryRenderHelper.getRenderEntity(player);
		CarryOnData carry = CarryOnDataManager.getCarryData(player);

		if (entity != null)
		{
			Vec3 playerpos = CarryRenderHelper.getExactPos(player, partialTicks);

			entity.setPos(playerpos.x, playerpos.y, playerpos.z);
			entity.xRotO = 0.0f;
			entity.yRotO = 0.0f;
			entity.setYHeadRot(0.0f);

			float height = entity.getBbHeight();
			float width = entity.getBbWidth();
		    matrix.pushPose();
		    matrix.mulPose(Axis.YP.rotationDegrees(180));
			if (firstPerson){
				matrix.scale(0.8f, 0.8f, 0.8f);
				matrix.translate(0.0, -height - .2, width * 1.3 + 0.1);
			}else{
				float multiplier = Math.min(9.9f, height * width) ;
				matrix.scale((10 - multiplier) * 0.08f, (10 - multiplier) * 0.08f, (10 - multiplier) * 0.08f);
				matrix.translate(0.0, height / 2 + -(height / 4) + 0.5f, width - 0.1 < 0.7 ? width - 0.1 + (0.7 - (width - 0.1)) : width - 0.1);
				matrix.mulPose(Axis.ZN.rotationDegrees(180));
			}

			Optional<CarryOnScript> res = carry.getActiveScript();
			if(res.isPresent())
			{
				CarryOnScript script = res.get();
				CarryRenderHelper.performScriptTransformation(matrix, script);
			}

			if(Constants.CLIENT_CONFIG.rotateEntitiesSideways)
				matrix.mulPose(Axis.YP.rotationDegrees(90));

			if (entity instanceof LivingEntity)
				((LivingEntity) entity).hurtTime = 0;

			try {
		       manager.submit(manager.extractEntity(entity, 0), new CameraRenderState(), 0, 0, 0, matrix, nodeCollector);
			}
			catch (Exception e)
			{
			}
			matrix.popPose();
		}
	}
}

