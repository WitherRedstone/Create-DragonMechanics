package com.chinaex123.create_iceandfire.init;

import com.chinaex123.create_iceandfire.CreateIceAndFire;
import com.iafenvoy.iceandfire.recipe.DragonForgeRecipe;
import com.iafenvoy.iceandfire.registry.IafRecipes;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.Optional;

/**
 * 鼓风机配方类型入口。
 * <p>
 * 包装冰与火的龙煅炉配方类型，按龙类型（火、冰、闪电）划分入口，
 * 对外暴露本模组使用的标识与配方序列化器、类型，
 * 并提供按龙类型查找匹配输入配方的便捷方法。
 */
public final class FanRecipeType {

    /** 火龙锻炉配方入口 */
    public static final RecipeTypeEntry FIRE_DRAGON_FORGE = registerStandard("fire_dragon_forge", "fire");
    /** 冰龙锻炉配方入口 */
    public static final RecipeTypeEntry ICE_DRAGON_FORGE = registerStandard("ice_dragon_forge", "ice");
    /** 闪电龙锻炉配方入口 */
    public static final RecipeTypeEntry LIGHTNING_DRAGON_FORGE = registerStandard("lightning_dragon_forge", "lightning");

    /**
     * 注册标准配方类型入口。
     *
     * @param name       入口名称（用作本模组标识路径）
     * @param dragonType 龙类型字符串
     * @return 配方类型入口
     */
    private static RecipeTypeEntry registerStandard(String name, String dragonType) {
        return new RecipeTypeEntry(CreateIceAndFire.id(name), IafRecipes.DRAGON_FORGE_TYPE, dragonType);
    }

    /**
     * 配方类型入口：包装冰与火的龙煅炉配方类型，并按龙类型筛选可用的配方
     * @param id 本模组使用的标识
     * @param recipeType 冰与火的龙煅炉配方类型
     * @param dragonType 对应的龙类型字符串(fire / ice / lightning)
     */
    public record RecipeTypeEntry(ResourceLocation id,
                                  DeferredHolder<RecipeType<?>, RecipeType<DragonForgeRecipe>> recipeType,
                                  String dragonType) implements IRecipeTypeInfo {

        /** 冰与火龙煅炉配方序列化器的标识。 */
        private static final ResourceLocation SERIALIZER_ID =
                ResourceLocation.fromNamespaceAndPath("iceandfire", "dragonforge");

        /**
         * 获取本模组使用的标识。
         *
         * @return 标识
         */
        @Override
        public ResourceLocation getId() {
            return id;
        }

        /**
         * 获取冰与火龙煅炉配方序列化器。
         *
         * @param <T> 序列化器类型
         * @return 配方序列化器
         */
        @SuppressWarnings("unchecked")
        @Override
        public <T extends RecipeSerializer<?>> T getSerializer() {
            return (T) BuiltInRegistries.RECIPE_SERIALIZER.get(SERIALIZER_ID);
        }

        /**
         * 获取冰与火龙煅炉配方类型。
         *
         * @param <I> 配方输入类型
         * @param <R> 配方类型
         * @return 配方类型
         */
        @SuppressWarnings("unchecked")
        @Override
        public <I extends RecipeInput, R extends Recipe<I>> RecipeType<R> getType() {
            return (RecipeType<R>) (RecipeType<?>) recipeType.get();
        }

        /**
         * 按龙类型查找匹配输入的配方。
         * <p>
         * 输入为空时直接返回空；否则遍历该配方类型的所有配方，
         * 跳过龙类型不一致的条目，返回首个接受该输入值的配方。
         *
         * @param input 输入物品堆叠
         * @param level 世界
         * @return 匹配的配方，未找到时返回空
         */
        public Optional<DragonForgeRecipe> find(ItemStack input, Level level) {
            if (input.isEmpty()) {
                return Optional.empty();
            }
            for (var holder : level.getRecipeManager().getAllRecipesFor(recipeType.get())) {
                DragonForgeRecipe recipe = holder.value();
                if (!dragonType.equals(recipe.getDragonType())) {
                    continue;
                }
                if (recipe.isValidInput(input)) {
                    return Optional.of(recipe);
                }
            }
            return Optional.empty();
        }
    }
}
