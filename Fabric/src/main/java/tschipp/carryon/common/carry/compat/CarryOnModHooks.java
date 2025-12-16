package tschipp.carryon.common.carry.compat;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class CarryOnModHooks {

    public record CanPickupBlockHook(ServerPlayer player, BlockPos pos, BlockState state) {

    }

    public record CanPickupEntityHook(ServerPlayer player, Entity entity) {

    }

    public record SaveBlockHook(ServerPlayer player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity) {

    }

}
