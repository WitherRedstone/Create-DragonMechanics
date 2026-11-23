package com.chinaex123.create_dragon_mechanics.processing.fans.dragon;

import com.chinaex123.create_dragon_mechanics.config.CommonConfig;
import com.chinaex123.create_dragon_mechanics.processing.fans.FanProcessingSounds;
import com.iafenvoy.iceandfire.entity.util.dragon.IafDragonDestructionManager;
import com.iafenvoy.iceandfire.particle.DragonFrostParticleType;
import com.iafenvoy.iceandfire.registry.IafDamageTypes;
import com.iafenvoy.iceandfire.registry.IafSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 冰龙息处理类型。
 * <p>
 * 对应冰龙的龙息：气流与龙息呈冰蓝色，使用龙霜粒子；
 * 被吹到的方块按冰与火的冰冻破坏规则侵蚀为冻土，
 * 对活体实体造成冰冻伤害并施加冰冻状态，龙息音效为冰龙吐息。
 */
public final class IceDragonBreathType extends DragonBreathType {

    /** 气流颜色。 */
    private static final int AIRFLOW_COLOR = 0x44CCFF;

    /** 龙息颜色。 */
    private static final int BREATH_COLOR = 0x99E6FF;

    /**
     * 构造冰龙息处理类型。
     */
    public IceDragonBreathType() {
        super("ice", AIRFLOW_COLOR, BREATH_COLOR,
                new DragonFrostParticleType(2.0F), 2002, IafSounds.ICEDRAGON_BREATH);
    }

    /**
     * 判断冰龙息鼓风机处理是否启用。
     *
     * @return 启用返回 true
     */
    @Override
    protected boolean isFanProcessingEnabled() {
        return CommonConfig.ENABLE_ICE_DRAGON_BREATH_FAN.get();
    }

    /**
     * 播放冰龙息音效。
     *
     * @param level 世界
     * @param pos   位置
     */
    @Override
    public void playBreathSound(Level level, BlockPos pos) {
        FanProcessingSounds.iceDragonBreathSound(level, pos);
    }

    /**
     * 以冰冻破坏规则侵蚀方块。
     *
     * @param state 原方块状态
     * @return 侵蚀后的方块状态
     */
    @Override
    public BlockState transform(BlockState state) {
        return IafDragonDestructionManager.transformBlockIce(state);
    }

    /**
     * 获取冰冻伤害类型键。
     *
     * @return 冰冻伤害类型键
     */
    @Override
    protected ResourceKey<DamageType> damageTypeKey() {
        return IafDamageTypes.DRAGON_ICE_TYPE;
    }

    /**
     * 对目标施加冰冻状态。
     *
     * @param level   世界
     * @param target  目标实体
     * @param seconds 持续秒数
     */
    @Override
    protected void applyElementStatus(Level level, LivingEntity target, int seconds) {
        target.addEffect(frozenEffect(seconds));
    }
}
