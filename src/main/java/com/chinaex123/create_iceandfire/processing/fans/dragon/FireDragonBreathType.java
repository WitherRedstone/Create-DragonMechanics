package com.chinaex123.create_iceandfire.processing.fans.dragon;

import com.chinaex123.create_iceandfire.config.CommonConfig;
import com.chinaex123.create_iceandfire.processing.fans.FanProcessingSounds;
import com.iafenvoy.iceandfire.entity.util.dragon.IafDragonDestructionManager;
import com.iafenvoy.iceandfire.particle.DragonFlameParticleType;
import com.iafenvoy.iceandfire.registry.IafDamageTypes;
import com.iafenvoy.iceandfire.registry.IafSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 火龙息处理类型。
 * <p>
 * 对应火龙的龙息：气流与龙息呈橙红色，使用龙焰粒子；
 * 被吹到的方块按冰与火的火焰破坏规则侵蚀为焦土，
 * 对活体实体造成火焰伤害并点燃，龙息音效为火龙吐息。
 */
public final class FireDragonBreathType extends DragonBreathType {

    /** 气流颜色。 */
    private static final int AIRFLOW_COLOR = 0xFF4400;

    /** 龙息颜色。 */
    private static final int BREATH_COLOR = 0xFF8855;

    /**
     * 构造火龙息处理类型。
     */
    public FireDragonBreathType() {
        super("fire", AIRFLOW_COLOR, BREATH_COLOR,
                new DragonFlameParticleType(2.0F), 2001, IafSounds.FIREDRAGON_BREATH);
    }

    /**
     * 判断火龙息鼓风机处理是否启用。
     *
     * @return 启用返回 true
     */
    @Override
    protected boolean isFanProcessingEnabled() {
        return CommonConfig.ENABLE_FIRE_DRAGON_BREATH_FAN.get();
    }

    /**
     * 播放火龙息音效。
     *
     * @param level 世界
     * @param pos   位置
     */
    @Override
    public void playBreathSound(Level level, BlockPos pos) {
        FanProcessingSounds.fireDragonBreathSound(level, pos);
    }

    /**
     * 以火焰破坏规则侵蚀方块。
     *
     * @param state 原方块状态
     * @return 侵蚀后的方块状态
     */
    @Override
    public BlockState transform(BlockState state) {
        return IafDragonDestructionManager.transformBlockFire(state);
    }

    /**
     * 获取火焰伤害类型键。
     *
     * @return 火焰伤害类型键
     */
    @Override
    protected ResourceKey<DamageType> damageTypeKey() {
        return IafDamageTypes.DRAGON_FIRE_TYPE;
    }

    /**
     * 对目标施加点燃状态。
     *
     * @param level   世界
     * @param target  目标实体
     * @param seconds 持续秒数
     */
    @Override
    protected void applyElementStatus(Level level, LivingEntity target, int seconds) {
        target.igniteForSeconds(seconds);
    }
}
