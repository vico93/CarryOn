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

package tschipp.carryon.mixin;

import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tschipp.carryon.common.carry.CarryOnData;
import tschipp.carryon.common.carry.CarryOnDataManager;
import tschipp.carryon.common.carry.CarryOnData.CarryType;

import java.util.Optional;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity  {

    private PlayerMixin(EntityType<? extends LivingEntity> type, Level level) {
        super(type, level);
    }

    //We leave this in here to ensure cross-compatibility if world are upgraded from <1.21.8. Should be removed in the future.
    @Inject(method = "readAdditionalSaveData(Lnet/minecraft/world/level/storage/ValueInput;)V", at = @At("RETURN"))
    private void onReadAdditionalSaveData(ValueInput input, CallbackInfo ci)
    {
        Optional<CarryOnData> res = input.read("CarryOnData", CarryOnData.CODEC);
        res.ifPresent(data -> CarryOnDataManager.setCarryData((Player)((Object)this), data));
    }


    @Override
    public void stopRiding() {
        Entity entity = this.getVehicle();
        if (entity instanceof Player && entity.getPassengers().size() < 2){
            CarryOnData carry = CarryOnDataManager.getCarryData((Player) entity);
            if (carry.getType() == CarryType.PLAYER){
                carry.clear();
                ((Player) entity).removeEffect(MobEffects.SLOWNESS);
            }
        }
        super.stopRiding();
    }
}
