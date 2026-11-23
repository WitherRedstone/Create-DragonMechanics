package com.chinaex123.create_dragon_mechanics.processing.fans.dragon;

import com.chinaex123.create_dragon_mechanics.config.CommonConfig;
import com.chinaex123.create_dragon_mechanics.processing.fans.FanProcessingSounds;
import com.iafenvoy.iceandfire.entity.DragonSkullEntity;
import com.iafenvoy.iceandfire.entity.util.dragon.IafDragonDestructionManager;
import com.iafenvoy.iceandfire.registry.IafDamageTypes;
import com.iafenvoy.iceandfire.registry.IafSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 闪电龙息处理类型。
 * <p>
 * 对应闪电龙的龙息：气流呈紫色、龙息呈金色，伴随电火花粒子，
 * 并像闪电龙一样沿气流绘制闪电弧；
 * 被吹到的方块按冰与火的闪电破坏规则侵蚀为裂土，
 * 对活体实体造成闪电伤害，并沿远离最近闪电龙首的方向将其击退，
 * 龙息音效为闪电龙吐息。
 */
public final class LightningDragonBreathType extends DragonBreathType {

    /** 气流颜色。 */
    private static final int AIRFLOW_COLOR = 0xCC66FF;

    /** 龙息颜色。 */
    private static final int BREATH_COLOR = 0xFFD966;

    /**
     * 构造闪电龙息处理类型。
     */
    public LightningDragonBreathType() {
        super("lightning", AIRFLOW_COLOR, BREATH_COLOR,
                ParticleTypes.ELECTRIC_SPARK, 2003, IafSounds.LIGHTNINGDRAGON_BREATH);
    }

    /**
     * 判断闪电龙息鼓风机处理是否启用。
     *
     * @return 启用返回 true
     */
    @Override
    protected boolean isFanProcessingEnabled() {
        return CommonConfig.ENABLE_LIGHTNING_DRAGON_BREATH_FAN.get();
    }

    /**
     * 播放闪电龙息音效。
     *
     * @param level 世界
     * @param pos   位置
     */
    @Override
    public void playBreathSound(Level level, BlockPos pos) {
        FanProcessingSounds.lightningDragonBreathSound(level, pos);
    }

    /**
     * 以闪电破坏规则侵蚀方块。
     *
     * @param state 原方块状态
     * @return 侵蚀后的方块状态
     */
    @Override
    public BlockState transform(BlockState state) {
        return IafDragonDestructionManager.transformBlockLightning(state);
    }

    /**
     * 获取闪电伤害类型键。
     *
     * @return 闪电伤害类型键
     */
    @Override
    protected ResourceKey<DamageType> damageTypeKey() {
        return IafDamageTypes.DRAGON_LIGHTNING_TYPE;
    }

    /**
     * 对目标施加击退效果。
     *
     * @param level   世界
     * @param target  目标实体
     * @param seconds 持续秒数
     */
    @Override
    protected void applyElementStatus(Level level, LivingEntity target, int seconds) {
        DragonSkullEntity skull = findNearestSkull(level, target, dragonTypeId());
        double dx = 0.0D;
        double dz = 0.0D;
        if (skull != null) {
            dx = target.getX() - skull.getX();
            dz = target.getZ() - skull.getZ();
        }
        double length = Math.sqrt(dx * dx + dz * dz);
        if (length < 1.0E-4D) {
            dx = 0.0D;
            dz = 0.0D;
            length = 1.0D;
        }
        target.knockback(0.4D + 0.05D * seconds, dx / length, dz / length);
    }
}
