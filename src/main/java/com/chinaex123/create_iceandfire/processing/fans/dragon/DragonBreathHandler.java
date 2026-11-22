package com.chinaex123.create_iceandfire.processing.fans.dragon;

import com.chinaex123.create_iceandfire.config.CommonConfig;
import com.chinaex123.create_iceandfire.init.FanType;
import com.iafenvoy.iceandfire.entity.DragonSkullEntity;
import com.iafenvoy.iceandfire.item.block.entity.DragonForgeBlockEntity;
import com.iafenvoy.iceandfire.item.block.entity.DragonForgeInputBlockEntity;
import com.iafenvoy.iceandfire.item.block.util.DragonProof;
import com.iafenvoy.iceandfire.registry.IafEntities;
import com.simibubi.create.AllTags;
import com.simibubi.create.content.kinetics.fan.IAirCurrentSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import org.jetbrains.annotations.Nullable;

/**
 * 龙息处理逻辑。
 * <p>
 * 服务端定期扫描已放置的龙首，若其前方存在正在吹风的气流源，
 * 则沿气流方向喷射龙息：加热沿途的龙煅炉输入方块，
 * 撞击到阻挡方块时加热贴墙的输入方块，并在允许破坏方块时侵蚀该方块。
 * 结算间隔随气流转速变化，气流被阻挡时立即中断。
 */
public final class DragonBreathHandler {

    /** 扫描间隔（刻）。 */
    private static final int SCAN_INTERVAL = 10;
    /** 寻找气流源的最大距离（格）。 */
    private static final int MAX_SCAN_DISTANCE = 24;
    /** 基础加热间隔（刻）。 */
    private static final int HEAT_INTERVAL_BASE = 10;
    /** 寻找龙煅炉的搜索半径（格）。 */
    private static final double FORGE_SEARCH_RADIUS = 2.0D;
    /** 能够加热龙煅炉的最低龙首等级。 */
    private static final int MIN_FORGE_HEATING_STAGE = 4;

    /** 龙首持久化数据中记录加热冷却的键。 */
    private static final String PULSE_TAG = "CreateIceAndFireBreathPulse";

    private DragonBreathHandler() {
    }

    /**
     * 服务端世界刻事件：定期扫描所有已放置的龙首
     *
     * @param event 世界刻事件
     */
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level) || level.getGameTime() % SCAN_INTERVAL != 0) {
            return;
        }
        for (DragonSkullEntity skull : level.getEntities(IafEntities.DRAGON_SKULL.get(),
                skull -> !skull.isRemoved())) {
            tickSkull(level, skull);
        }
    }

    /**
     * 处理单个龙首：寻找气流、播放音效、按转速决定本次是否结算
     *
     * @param level 服务端世界
     * @param skull 龙首实体
     */
    private static void tickSkull(ServerLevel level, DragonSkullEntity skull) {
        DragonBreathType breath = FanType.byDragonType(skull.getDragonType());
        if (breath == null) {
            return;
        }

        BlockPos skullPos = skull.blockPosition();
        if (!level.isLoaded(skullPos)) {
            return;
        }

        BreathFlow flow = findFlow(level, skullPos);
        if (flow == null) {
            return;
        }

        breath.playBreathSound(level, skullPos);

        CompoundTag data = skull.getPersistentData();
        int cooldown = data.getInt(PULSE_TAG) - SCAN_INTERVAL;
        boolean emit = cooldown <= 0;
        if (emit) {
            cooldown = heatInterval(flow.speed());
        }
        data.putInt(PULSE_TAG, cooldown);

        // 只有四级及以上的成年龙首才够格给龙煅炉加热，其余效果不受等级限制
        boolean forgeGrade = skull.getStage() >= MIN_FORGE_HEATING_STAGE;
        emitBreath(level, skullPos, flow, breath, emit, forgeGrade);
    }

    /**
     * 沿气流方向喷吐龙息
     *
     * @param level 服务端世界
     * @param origin 龙首位置
     * @param flow 气流方向与风速
     * @param breath 龙息类型
     * @param emit 本次是否应当结算效果
     * @param forgeGrade 该龙首的等级是否足以加热龙煅炉
     */
    private static void emitBreath(ServerLevel level, BlockPos origin, BreathFlow flow, DragonBreathType breath,
                                   boolean emit, boolean forgeGrade) {
        int range = CommonConfig.STREAM_RANGE.get();
        boolean heating = forgeGrade && CommonConfig.ENABLE_FORGE_HEATING.get();
        boolean griefing = CommonConfig.TRANSFORM_BLOCKS.get()
                && level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
        double power = 0.05D * Mth.clamp(flow.speed() / 32.0D, 1.0D, 8.0D);

        for (int step = 1; step <= range; step++) {
            BlockPos probe = origin.relative(flow.direction(), step);
            if (!level.isLoaded(probe)) {
                return;
            }

            if (level.getBlockEntity(probe) instanceof DragonForgeInputBlockEntity input) {
                if (emit && heating && matchesForgeType(level, probe, breath)) {
                    input.onHitWithFlame(power);
                }
                return;
            }

            if (blocksFlow(level, probe)) {
                if (emit) {
                    // 气流被龙煅炉的外壳挡住时，同样加热紧贴这面墙的输入方块
                    if (heating) {
                        heatAdjacentForge(level, probe, breath, power);
                    }
                    if (griefing) {
                        transformBlock(level, probe, breath);
                    }
                }
                return;
            }
        }
    }

    /**
     * 加热被气流吹到的墙附近的龙煅炉输入方块
     *
     * @param level 服务端世界
     * @param wall 气流撞到的方块位置
     * @param breath 龙息类型
     * @param power 单次加热量
     */
    private static void heatAdjacentForge(ServerLevel level, BlockPos wall, DragonBreathType breath, double power) {
        int radius = (int) FORGE_SEARCH_RADIUS;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    cursor.setWithOffset(wall, dx, dy, dz);
                    if (!(level.getBlockEntity(cursor) instanceof DragonForgeInputBlockEntity input)) {
                        continue;
                    }
                    if (matchesForgeType(level, cursor.immutable(), breath)) {
                        input.onHitWithFlame(power);
                    }
                }
            }
        }
    }

    /**
     * 从龙首向六个方向寻找正在吹风的风扇(或任何气流源)。
     * <p>
     * 服务端用于结算龙息效果，客户端用于渲染闪电弧。
     *
     * @param level 世界
     * @param origin 龙首位置
     * @return 气流方向与风速，没有气流则返回 null
     */
    @Nullable
    public static BreathFlow findFlow(Level level, BlockPos origin) {
        for (Direction towards : Direction.values()) {
            Direction expected = towards.getOpposite();
            for (int step = 1; step <= MAX_SCAN_DISTANCE; step++) {
                BlockPos probe = origin.relative(towards, step);
                if (!level.isLoaded(probe)) {
                    break;
                }
                if (!(level.getBlockEntity(probe) instanceof IAirCurrentSource source)) {
                    continue;
                }
                if (source.isSourceRemoved() || source.getAirFlowDirection() != expected) {
                    break;
                }
                if (step > source.getMaxDistance()) {
                    break;
                }
                float speed = Math.abs(source.getSpeed());
                if (speed < 1.0F) {
                    break;
                }
                return new BreathFlow(expected, speed);
            }
        }
        return null;
    }

    /**
     * 检查龙煅炉输入方块是否与龙首属性相同
     *
     * @param level 服务端世界
     * @param inputPos 输入方块位置
     * @param breath 龙息类型
     * @return 属性是否匹配
     */
    private static boolean matchesForgeType(ServerLevel level, BlockPos inputPos, DragonBreathType breath) {
        int radius = (int) FORGE_SEARCH_RADIUS;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    cursor.setWithOffset(inputPos, dx, dy, dz);
                    if (!(level.getBlockEntity(cursor) instanceof DragonForgeBlockEntity forge)) {
                        continue;
                    }
                    if (forge.getDragonType() != null
                            && breath.dragonTypeId().equals(forge.getDragonType().name())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 该方块是否会阻挡气流
     *
     * @param level 服务端世界
     * @param pos 方块位置
     * @return 是否阻挡
     */
    private static boolean blocksFlow(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir() || AllTags.AllBlockTags.FAN_TRANSPARENT.matches(state)) {
            return false;
        }
        return !state.getCollisionShape(level, pos).isEmpty();
    }

    /**
     * 用龙息侵蚀方块
     *
     * @param level 服务端世界
     * @param pos 方块位置
     * @param breath 龙息类型
     */
    private static void transformBlock(ServerLevel level, BlockPos pos, DragonBreathType breath) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir() || state.getBlock() instanceof DragonProof) {
            return;
        }
        if (level.getBlockEntity(pos) instanceof DragonForgeInputBlockEntity) {
            return;
        }
        BlockState transformed = breath.transform(state);
        if (transformed != null && !transformed.equals(state)) {
            level.setBlockAndUpdate(pos, transformed);
        }
    }

    /**
     * 根据转速计算结算间隔。
     *
     * @param speed 气流速度
     * @return 间隔刻数
     */
    private static int heatInterval(float speed) {
        double rate = Mth.clamp(speed / (double) CommonConfig.FULL_BREATH_RPM.get(), 0.05D, 1.0D);
        return (int) Mth.clamp(Math.round(HEAT_INTERVAL_BASE / rate), 5, 200);
    }
}
