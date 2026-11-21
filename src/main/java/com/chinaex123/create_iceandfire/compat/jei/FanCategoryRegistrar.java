package com.chinaex123.create_iceandfire.compat.jei;

import com.chinaex123.create_iceandfire.compat.jei.fan.FanJeiCategories;
import com.chinaex123.create_iceandfire.init.FanRecipeType;
import com.iafenvoy.iceandfire.registry.IafItems;

/**
 * 鼓风机配方类别注册器。
 * <p>
 * 创建并持有鼓风机 JEI 配方类别管理器，登记各类龙息相关的分类：
 * 火龙息、冰龙息与闪电龙息，分别对应龙煅炉配方与对应地形侵蚀。
 */
public final class FanCategoryRegistrar {

    /** 鼓风机配方类别管理器。 */
    private final FanJeiCategories fanCategories = new FanJeiCategories();

    /**
     * 注册全部配方类别。
     */
    public void registerAll() {
        registerVanillaCategories();
    }

    /**
     * 注册原版相关的配方类别。
     * <p>
     * 为火龙息、冰龙息与闪电龙息各登记一个分类，
     * 分别使用火龙、冰龙、闪电龙头骨作为分类图标。
     */
    private void registerVanillaCategories() {
        // 批量火龙息：龙煅炉配方 + 焦土侵蚀
        fanCategories.addHeadCategory("fan_fire_dragon_breath", FanRecipeType.FIRE_DRAGON_FORGE,
                IafItems.DRAGON_SKULL_FIRE.get());
        // 批量冰龙息：龙煅炉配方 + 冻土侵蚀
        fanCategories.addHeadCategory("fan_ice_dragon_breath", FanRecipeType.ICE_DRAGON_FORGE,
                IafItems.DRAGON_SKULL_ICE.get());
        // 批量闪电龙息：龙煅炉配方 + 裂土侵蚀
        fanCategories.addHeadCategory("fan_lightning_dragon_breath", FanRecipeType.LIGHTNING_DRAGON_FORGE,
                IafItems.DRAGON_SKULL_LIGHTNING.get());
    }

    /**
     * 获取鼓风机配方类别管理器
     *
     * @return 鼓风机配方类别管理器
     */
    public FanJeiCategories getFanCategories() {
        return fanCategories;
    }
}
