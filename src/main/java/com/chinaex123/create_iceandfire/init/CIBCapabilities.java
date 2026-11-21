package com.chinaex123.create_iceandfire.init;

import com.chinaex123.create_iceandfire.compat.DragonForgeItemHandler;
import com.iafenvoy.iceandfire.item.block.DragonForgeBrickBlock;
import com.iafenvoy.iceandfire.item.block.entity.DragonForgeBlockEntity;
import com.iafenvoy.iceandfire.item.block.entity.DragonForgeBrickBlockEntity;
import com.iafenvoy.iceandfire.registry.IafBlockEntities;
import com.iafenvoy.iceandfire.util.DragonTypeProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.jetbrains.annotations.Nullable;

/**
 * 模组能力注册类。
 * <p>
 * 为龙煅炉核心与炉砖方块实体注册物品处理器能力：
 * 核心的侧面在对应方向为同类炉砖或未组装时对外开放，上下两面与龙息口始终不开放；
 * 炉砖仅在紧贴核心、类型一致且位于水平侧面时转发核心的物品处理器，
 * 上下层砖与四角骨头块一律不开放。
 */
public final class CIBCapabilities {

    /**
     * 注册方块实体能力。
     * <p>
     * 为龙煅炉核心注册物品处理器能力，按朝向判断是否开放；
     * 为龙煅炉炉砖注册物品处理器能力，仅在满足邻近、类型与方位条件时转发核心。
     *
     * @param event 能力注册事件
     */
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        // 核心方块：上下两面与龙息口不开放，三个炉砖侧面可输入可输出
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                IafBlockEntities.DRAGONFORGE_CORE.get(),
                (blockEntity, side) -> blockEntity instanceof DragonForgeBlockEntity core
                        && isOpenCoreFace(core, side)
                        ? new DragonForgeItemHandler(core)
                        : null);

        // 炉砖：只有紧贴核心的水平侧面砖才转发，上下两层的砖与四角骨头块一律不开放
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                IafBlockEntities.DRAGONFORGE_BRICK.get(),
                (blockEntity, side) -> {
                    DragonForgeBrickBlockEntity brick = blockEntity;
                    DragonForgeBlockEntity core = findAdjacentCore(brick);
                    return core != null && isMatchingBrick(brick, core) && isHorizontalSideOf(brick, core)
                            ? new DragonForgeItemHandler(core)
                            : null;
                });
    }

    /**
     * 判断炉砖与核心的龙类型是否一致。
     *
     * @param brick 炉砖方块实体
     * @param core  核心方块实体
     * @return 类型一致返回 true
     */
    private static boolean isMatchingBrick(DragonForgeBrickBlockEntity brick, DragonForgeBlockEntity core) {
        return brick.getBlockState().getBlock() instanceof DragonTypeProvider provider
                && provider.getDragonType().equals(core.getDragonType());
    }

    /**
     * 判断核心在指定面是否开放物品接口。
     * <p>
     * 上下两面与龙息口不开放；无朝向时开放；
     * 有朝向时该方向为同类炉砖则开放，核心未组装时也开放。
     *
     * @param core 核心方块实体
     * @param side 面朝向，可为 null
     * @return 开放返回 true
     */
    private static boolean isOpenCoreFace(DragonForgeBlockEntity core, @Nullable Direction side) {
        if (side == Direction.UP || side == Direction.DOWN) {
            return false;
        }
        if (side == null) {
            return true;
        }
        Level level = core.getLevel();
        if (level == null) {
            return false;
        }
        boolean brick = level.getBlockState(core.getBlockPos().relative(side)).getBlock()
                == DragonForgeBrickBlock.getBlockByType(core.getDragonType());
        return brick || !core.assembled();
    }

    /**
     * 查找炉砖相邻的核心方块实体。
     *
     * @param brick 炉砖方块实体
     * @return 相邻核心，未找到时返回 null
     */
    @Nullable
    private static DragonForgeBlockEntity findAdjacentCore(DragonForgeBrickBlockEntity brick) {
        Level level = brick.getLevel();
        if (level == null) {
            return null;
        }
        BlockPos pos = brick.getBlockPos();
        for (Direction direction : Direction.values()) {
            if (level.getBlockEntity(pos.relative(direction)) instanceof DragonForgeBlockEntity core) {
                return core;
            }
        }
        return null;
    }

    /**
     * 判断炉砖是否位于核心的水平侧面。
     * <p>
     * 要求与核心处于同一高度且水平方向恰好相差一格。
     *
     * @param brick 炉砖方块实体
     * @param core  核心方块实体
     * @return 位于水平侧面返回 true
     */
    private static boolean isHorizontalSideOf(DragonForgeBrickBlockEntity brick, DragonForgeBlockEntity core) {
        BlockPos delta = brick.getBlockPos().subtract(core.getBlockPos());
        return delta.getY() == 0 && Math.abs(delta.getX()) + Math.abs(delta.getZ()) == 1;
    }
}
