package com.chinaex123.create_iceandfire.processing.fans;

import com.iafenvoy.iceandfire.entity.DragonSkullEntity;
import com.simibubi.create.content.kinetics.fan.processing.FanProcessingType;
import net.createmod.catnip.theme.Color;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 鼓风机处理类型公共基类。
 * <p>
 * 在机械动力的鼓风机处理类型之上，统一处理配置开关与实体效果分发：
 * 实体效果仅由服务端分发给子类的活体处理方法。
 * 同时提供粒子生成、气流外观变换以及龙首催化剂的查找工具方法。
 */
public abstract class FanCommonType implements FanProcessingType {

    /** 查找最近龙首的搜索半径（格）。 */
    private static final double SKULL_SEARCH_RADIUS = 12.0D;

    /**
     * 判断该鼓风机处理是否启用。
     *
     * @return 启用返回 true
     */
    protected abstract boolean isFanProcessingEnabled();

    /**
     * 本模组的龙息不加工物品。
     * <p>
     * 物品加工只能由冰与火自己的龙煅炉完成，气流只负责加热炉子，
     * 因此这里始终返回 false。
     *
     * @param stack 物品堆叠
     * @param level 世界
     * @return 始终返回 false
     */
    @Override
    public boolean canProcess(ItemStack stack, Level level) {
        return false;
    }

    /**
     * 本模组的龙息不产出物品。
     *
     * @param stack 物品堆叠
     * @param level 世界
     * @return 始终返回 null
     */
    @Nullable
    @Override
    public List<ItemStack> process(ItemStack stack, Level level) {
        return null;
    }

    /**
     * 对实体施加效果。
     * <p>
     * 未启用时直接返回；仅服务端对活体实体分发给子类的处理方法。
     *
     * @param entity 目标实体
     * @param level  世界
     */
    @Override
    public final void affectEntity(Entity entity, Level level) {
        if (!isFanProcessingEnabled()) {
            return;
        }
        if (level.isClientSide || !(entity instanceof LivingEntity living)) {
            return;
        }
        affectLivingEntity(living, level);
    }

    /**
     * 对活体实体施加该处理的效果。
     *
     * @param living 活体实体
     * @param level  世界
     */
    protected abstract void affectLivingEntity(LivingEntity living, Level level);

    /**
     * 以一定概率生成尘埃粒子与附加粒子。
     *
     * @param level     世界
     * @param pos       位置
     * @param colorLight 尘埃颜色
     * @param secondary 附加粒子
     */
    protected static void spawnDustWithParticle(Level level, Vec3 pos, int colorLight, ParticleOptions secondary) {
        if (level.random.nextInt(12) != 0) {
            return;
        }
        DustParticleOptions dust = new DustParticleOptions(new Color(colorLight).asVectorF(), 1);
        double x = pos.x + (level.random.nextFloat() - 0.5f) * 0.5f;
        double y = pos.y + 0.5f;
        double z = pos.z + (level.random.nextFloat() - 0.5f) * 0.5f;
        level.addParticle(dust, x, y, z, 0, 0.125f, 0);
        level.addParticle(secondary, x, y, z, 0, 0.125f, 0);
    }

    /**
     * 按标准规则改变气流粒子的颜色与透明度，并按概率生成附加粒子。
     *
     * @param particleAccess 气流粒子访问器
     * @param random         随机源
     * @param colorDark      深色
     * @param colorLight     浅色
     * @param extraChance    生成附加粒子的概率
     * @param extraParticle  附加粒子
     */
    protected static void morphStandardAirFlow(AirFlowParticleAccess particleAccess, RandomSource random,
                                               int colorDark, int colorLight, float extraChance,
                                               ParticleOptions extraParticle) {
        particleAccess.setColor(Color.mixColors(colorDark, colorLight, random.nextFloat()));
        particleAccess.setAlpha(1f);
        if (random.nextFloat() < extraChance) {
            particleAccess.spawnExtraParticle(extraParticle, 0.125f);
        }
    }

    /**
     * 判断指定位置是否具备有效的龙首催化剂。
     * <p>
     * 要求该位置为空气，且附近存在对应龙类型的龙首。
     *
     * @param level        世界
     * @param pos          位置
     * @param dragonTypeId 龙类型标识
     * @return 有效返回 true
     */
    protected static boolean isValidSkullCatalyst(Level level, BlockPos pos, String dragonTypeId) {
        if (!level.getBlockState(pos).isAir()) {
            return false;
        }
        return findSkull(level, new AABB(pos).inflate(0.5D), dragonTypeId) != null;
    }

    /**
     * 在指定范围内查找对应龙类型的龙首。
     *
     * @param level        世界
     * @param area         搜索范围
     * @param dragonTypeId 龙类型标识
     * @return 匹配的龙首，未找到时返回 null
     */
    @Nullable
    protected static DragonSkullEntity findSkull(Level level, AABB area, String dragonTypeId) {
        for (DragonSkullEntity skull : level.getEntitiesOfClass(DragonSkullEntity.class, area)) {
            if (!skull.isRemoved() && dragonTypeId.equals(skull.getDragonType())) {
                return skull;
            }
        }
        return null;
    }

    /**
     * 查找距离目标最近的对应龙类型龙首。
     *
     * @param level        世界
     * @param target       目标实体
     * @param dragonTypeId 龙类型标识
     * @return 最近的龙首，未找到时返回 null
     */
    @Nullable
    protected static DragonSkullEntity findNearestSkull(Level level, Entity target, String dragonTypeId) {
        DragonSkullEntity closest = null;
        double closestDistance = Double.MAX_VALUE;
        for (DragonSkullEntity skull : level.getEntitiesOfClass(DragonSkullEntity.class,
                target.getBoundingBox().inflate(SKULL_SEARCH_RADIUS))) {
            if (skull.isRemoved() || !dragonTypeId.equals(skull.getDragonType())) {
                continue;
            }
            double distance = skull.distanceToSqr(target);
            if (distance < closestDistance) {
                closestDistance = distance;
                closest = skull;
            }
        }
        return closest;
    }
}
