package com.chinaex123.create_iceandfire.processing.fans.dragon;

import com.chinaex123.create_iceandfire.config.CommonConfig;
import com.chinaex123.create_iceandfire.processing.fans.FanCommonType;
import com.iafenvoy.iceandfire.entity.DragonSkullEntity;
import com.iafenvoy.iceandfire.registry.IafMobEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.function.Supplier;

/**
 * 龙息处理类型基类。
 * <p>
 * 在鼓风机处理类型的通用逻辑之上，描述一种具体龙息：
 * 龙类型标识、气流与龙息颜色、龙息粒子、优先级与音效，
 * 并提供方块侵蚀与龙息音效播放的抽象方法。
 * 对活体实体按固定间隔造成伤害并施加元素状态，
 * 同时跳过龙首自身与旁观者。
 */
public abstract class DragonBreathType extends FanCommonType {

    /** 对活体实体施加效果的间隔（刻）。 */
    private static final int EFFECT_INTERVAL = 20;

    /** 龙类型标识。 */
    private final String dragonTypeId;

    /** 气流颜色。 */
    private final int airflowColor;

    /** 龙息颜色。 */
    private final int breathColor;

    /** 龙息粒子。 */
    private final ParticleOptions breathParticle;

    /** 处理优先级。 */
    private final int priority;

    /** 龙息音效供应器。 */
    private final Supplier<SoundEvent> breathSound;

    /**
     * 构造龙息处理类型。
     *
     * @param dragonTypeId   龙类型标识
     * @param airflowColor   气流颜色
     * @param breathColor    龙息颜色
     * @param breathParticle 龙息粒子
     * @param priority       处理优先级
     * @param breathSound    龙息音效供应器
     */
    protected DragonBreathType(String dragonTypeId,
                               int airflowColor, int breathColor, ParticleOptions breathParticle,
                               int priority, Supplier<SoundEvent> breathSound) {
        this.dragonTypeId = dragonTypeId;
        this.airflowColor = airflowColor;
        this.breathColor = breathColor;
        this.breathParticle = breathParticle;
        this.priority = priority;
        this.breathSound = breathSound;
    }

    /**
     * 获取龙类型标识。
     *
     * @return 龙类型标识
     */
    public String dragonTypeId() {
        return dragonTypeId;
    }

    /**
     * 用龙息侵蚀方块。
     *
     * @param state 原方块状态
     * @return 侵蚀后的方块状态，不发生变化时返回 null
     */
    public abstract BlockState transform(BlockState state);

    /**
     * 在指定位置播放龙息音效。
     *
     * @param level 世界
     * @param pos   位置
     */
    public abstract void playBreathSound(Level level, BlockPos pos);

    /**
     * 获取龙息音效。
     *
     * @return 龙息音效
     */
    public SoundEvent breathSound() {
        return breathSound.get();
    }

    /**
     * 判断该龙息在指定位置是否有效。
     * <p>
     * 需启用鼓风机处理，且该位置存在对应龙类型的龙首作为催化剂。
     *
     * @param level 世界
     * @param pos   位置
     * @return 有效返回 true
     */
    @Override
    public boolean isValidAt(Level level, BlockPos pos) {
        if (!isFanProcessingEnabled()) return false;
        return isValidSkullCatalyst(level, pos, dragonTypeId);
    }

    /**
     * 获取处理优先级。
     *
     * @return 处理优先级
     */
    @Override
    public int getPriority() {
        return priority;
    }

    /**
     * 生成处理粒子。
     *
     * @param level 世界
     * @param pos   位置
     */
    @Override
    public void spawnProcessingParticles(Level level, Vec3 pos) {
        spawnDustWithParticle(level, pos, breathColor, breathParticle);
    }

    /**
     * 改变气流粒子的外观。
     *
     * @param particleAccess 气流粒子访问器
     * @param random         随机源
     */
    @Override
    public void morphAirFlow(AirFlowParticleAccess particleAccess, RandomSource random) {
        morphStandardAirFlow(particleAccess, random, airflowColor, breathColor, 0.04f, breathParticle);
    }

    /**
     * 对活体实体施加龙息效果。
     * <p>
     * 未启用伤害、目标为龙首或旁观者时跳过；
     * 每隔固定刻数按配置造成伤害、施加元素状态并播放音效。
     *
     * @param living 活体实体
     * @param level  世界
     */
    @Override
    protected final void affectLivingEntity(LivingEntity living, Level level) {
        if (!CommonConfig.HARM_ENTITIES.get() || living instanceof DragonSkullEntity) {
            return;
        }
        if (living.isSpectator()) {
            return;
        }
        if (level.getGameTime() % EFFECT_INTERVAL != 0) {
            return;
        }

        float damage = CommonConfig.BREATH_DAMAGE.get().floatValue();
        if (damage > 0.0f) {
            living.hurt(damageSource(level), damage);
        }
        applyElementStatus(level, living, CommonConfig.BREATH_EFFECT_SECONDS.get());
        playBreathSound(level, living.blockPosition());
    }

    /**
     * 获取该龙息对应的伤害类型键。
     *
     * @return 伤害类型键
     */
    protected abstract ResourceKey<DamageType> damageTypeKey();

    /**
     * 对目标施加该龙息的元素状态。
     *
     * @param level   世界
     * @param target  目标实体
     * @param seconds 持续秒数
     */
    protected abstract void applyElementStatus(Level level, LivingEntity target, int seconds);

    /**
     * 构造该龙息对应的伤害来源。
     * <p>
     * 从伤害类型注册表获取对应类型，取不到时回退到通用伤害。
     *
     * @param level 世界
     * @return 伤害来源
     */
    protected DamageSource damageSource(Level level) {
        Holder<DamageType> holder = level.registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolder(damageTypeKey())
                .orElse(null);
        if (holder == null) {
            return level.damageSources().generic();
        }
        return new DamageSource(holder, null, null);
    }

    /**
     * 构造冰冻状态效果。
     *
     * @param seconds 持续秒数
     * @return 冰冻状态效果
     */
    protected MobEffectInstance frozenEffect(int seconds) {
        return new MobEffectInstance(IafMobEffects.FROZEN, seconds * 20);
    }
}
