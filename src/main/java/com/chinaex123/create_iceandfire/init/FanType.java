package com.chinaex123.create_iceandfire.init;

import com.chinaex123.create_iceandfire.CreateIceAndFire;
import com.chinaex123.create_iceandfire.processing.fans.dragon.DragonBreathType;
import com.chinaex123.create_iceandfire.processing.fans.dragon.FireDragonBreathType;
import com.chinaex123.create_iceandfire.processing.fans.dragon.IceDragonBreathType;
import com.chinaex123.create_iceandfire.processing.fans.dragon.LightningDragonBreathType;
import com.simibubi.create.api.registry.CreateBuiltInRegistries;
import com.simibubi.create.content.kinetics.fan.processing.FanProcessingType;
import net.minecraft.core.Registry;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * 鼓风机龙息处理类型注册类。
 * <p>
 * 向机械动力的鼓风机处理类型注册表注册火、冰、闪电三种龙息类型，
 * 并提供按冰与火龙类型字符串查找对应龙息类型的映射。
 */
public final class FanType {

    /** 火龙首：点燃生物、侵蚀方块为焦土 */
    public static final FireDragonBreathType FIRE_DRAGON_BREATH = register("fire_dragon_breath", new FireDragonBreathType());
    /** 冰龙首：冻结生物、侵蚀方块为冻土 */
    public static final IceDragonBreathType ICE_DRAGON_BREATH = register("ice_dragon_breath", new IceDragonBreathType());
    /** 闪电龙首：击退生物、侵蚀方块为裂土 */
    public static final LightningDragonBreathType LIGHTNING_DRAGON_BREATH = register("lightning_dragon_breath", new LightningDragonBreathType());

    /**
     * 向鼓风机处理类型注册表注册龙息类型。
     *
     * @param name 注册名
     * @param type 处理类型
     * @param <T>  处理类型泛型
     * @return 注册后的处理类型
     */
    private static <T extends FanProcessingType> T register(String name, T type) {
        return Registry.register(CreateBuiltInRegistries.FAN_PROCESSING_TYPE, CreateIceAndFire.id(name), type);
    }

    /**
     * 冰与火龙类型字符串 -> 对应的龙息类型
     */
    private static final Map<String, DragonBreathType> BY_DRAGON_TYPE = Map.of(
            "fire", FIRE_DRAGON_BREATH,
            "ice", ICE_DRAGON_BREATH,
            "lightning", LIGHTNING_DRAGON_BREATH
    );

    /**
     * 按龙类型字符串查找对应的龙息类型。
     *
     * @param dragonType 龙类型字符串
     * @return 龙息类型，无法匹配时返回 null
     */
    @Nullable
    public static DragonBreathType byDragonType(@Nullable String dragonType) {
        return dragonType == null ? null : BY_DRAGON_TYPE.get(dragonType);
    }

    public static void init() {
    }
}
