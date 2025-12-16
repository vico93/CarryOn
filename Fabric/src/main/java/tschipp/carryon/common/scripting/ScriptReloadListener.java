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

package tschipp.carryon.common.scripting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;
import tschipp.carryon.Constants;
import tschipp.carryon.networking.clientbound.ClientboundSyncScriptsPacket;
import tschipp.carryon.platform.Services;

import java.util.Map;

public class ScriptReloadListener extends SimpleJsonResourceReloadListener<CarryOnScript>
{
	public ScriptReloadListener()
	{
		super(CarryOnScript.CODEC, FileToIdConverter.json("carryon/scripts"));
	}

	@Override
	protected void apply(Map<Identifier, CarryOnScript> scripts, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profilerFiller)
	{
		ScriptManager.SCRIPTS.clear();

		scripts.forEach((path, script) -> {
			if (script.isValid())
				ScriptManager.SCRIPTS.add(script);
		});

		ScriptManager.SCRIPTS.sort((s1, s2) -> Long.compare(s2.priority(), s1.priority()));
	}


	public static void syncScriptsWithClient(ServerPlayer player)
	{
		if (player != null)
		{
			DataResult<Tag> result = Codec.list(CarryOnScript.CODEC).encodeStart(NbtOps.INSTANCE, ScriptManager.SCRIPTS);
			Tag tag = result.getOrThrow(s -> {throw new RuntimeException("Error while synching Carry On Scripts: " + s);});

			Services.PLATFORM.sendPacketToPlayer(Constants.PACKET_ID_SYNC_SCRIPTS, new ClientboundSyncScriptsPacket(tag), player);
		}
	}
}

