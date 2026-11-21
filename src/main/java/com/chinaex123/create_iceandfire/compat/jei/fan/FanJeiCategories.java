package com.chinaex123.create_iceandfire.compat.jei.fan;

import com.chinaex123.create_iceandfire.CreateIceAndFire;
import com.chinaex123.create_iceandfire.init.FanRecipeType;
import com.iafenvoy.iceandfire.entity.DragonSkullEntity;
import com.iafenvoy.iceandfire.recipe.DragonForgeRecipe;
import com.iafenvoy.iceandfire.registry.IafEntities;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.compat.jei.DoubleItemIcon;
import com.simibubi.create.compat.jei.EmptyBackground;
import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import com.simibubi.create.compat.jei.category.ProcessingViaFanCategory;
import com.simibubi.create.compat.jei.category.animations.AnimatedKinetics;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.createmod.catnip.gui.element.GuiGameElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 鼓风机 JEI 配方类别管理器。
 * <p>
 * 维护鼓风机相关配方类别的集合，统一向 JEI 注册类别、配方与催化剂。
 * 每个类别以龙首物品作为催化剂，渲染鼓风机前放置对应龙首实体，
 * 并展示龙煅炉配方的龙血、输入物与产物槽位。
 */
public final class FanJeiCategories {

    /** 已注册的配方类别，按名称有序保存。 */
    private final Map<String, CreateRecipeCategory<?>> categories = new LinkedHashMap<>();

    /**
     * 注册所有配方类别到 JEI
     *
     * @param registration JEI 类别注册器
     */
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(categories.values().toArray(CreateRecipeCategory[]::new));
    }

    /**
     * 注册所有配方到 JEI
     *
     * @param registration JEI 配方注册器
     */
    public void registerRecipes(IRecipeRegistration registration) {
        categories.values().forEach(cat -> cat.registerRecipes(registration));
    }

    /**
     * 注册所有催化剂到 JEI
     *
     * @param registration JEI 催化剂注册器
     */
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        categories.values().forEach(cat -> cat.registerCatalysts(registration));
    }

    /**
     * 添加龙首催化剂的配方类别
     *
     * @param name 类别名称标识
     * @param recipeType 配方类型入口
     * @param catalystItem 催化剂物品(龙首)
     */
    public void addHeadCategory(String name, FanRecipeType.RecipeTypeEntry recipeType, ItemLike catalystItem) {
        if (!categories.containsKey(name)) {
            categories.put(name, createFanCategory(name, recipeType, catalystItem));
        }
    }

    /**
     * 创建鼓风机加工配方类别（龙首催化剂）
     *
     * @param name 类别名称标识
     * @param recipeType 配方类型入口
     * @param catalystItem 催化剂物品
     * @return 构建完成的配方类别实例
     */
    private static CreateRecipeCategory<DragonForgeRecipe> createFanCategory(String name,
                                                                             FanRecipeType.RecipeTypeEntry recipeType,
                                                                             ItemLike catalystItem) {
        return new FanProcessingCategory(createInfo(name, recipeType, catalystItem),
                catalystItem.asItem().getDefaultInstance(), recipeType.dragonType());
    }

    /**
     * 创建 JEI Info 对象，包含催化剂堆栈列表以支持 U 键查询
     *
     * @param name 类别名称
     * @param recipeType 配方类型入口
     * @param catalystItem 催化剂物品
     * @return Info 对象
     */
    private static CreateRecipeCategory.Info<DragonForgeRecipe> createInfo(String name,
                                                                           FanRecipeType.RecipeTypeEntry recipeType,
                                                                           ItemLike catalystItem) {
        Component title = Component.translatable("create_iceandfire.recipe." + name);
        IDrawable background = new EmptyBackground(178, 72);
        IDrawable icon = new DoubleItemIcon(
                AllItems.PROPELLER::asStack,
                () -> catalystItem.asItem().getDefaultInstance()
        );
        Supplier<ItemStack> catalystStackSupplier = () -> catalystItem.asItem().getDefaultInstance();
        Supplier<ItemStack> fanStackSupplier = AllBlocks.ENCASED_FAN::asStack;

        RecipeType<RecipeHolder<DragonForgeRecipe>> type = getOrCreateRecipeType(name);

        return new CreateRecipeCategory.Info<>(
                type,
                title,
                background,
                icon,
                () -> {
                    if (Minecraft.getInstance().level != null) {
                        return Minecraft.getInstance().level.getRecipeManager()
                                .getAllRecipesFor(recipeType.recipeType().get())
                                .stream()
                                .filter(holder -> recipeType.dragonType().equals(holder.value().getDragonType()))
                                .toList();
                    }
                    return List.of();
                },
                List.of(fanStackSupplier, catalystStackSupplier)
        );
    }

    /** 按名称缓存的 JEI 配方类型。 */
    private static final Map<String, RecipeType<RecipeHolder<DragonForgeRecipe>>> RECIPE_TYPES = new HashMap<>();

    /**
     * 获取或创建指定名称的 JEI 配方类型。
     *
     * @param name 类别名称标识
     * @return 配方类型
     */
    private static RecipeType<RecipeHolder<DragonForgeRecipe>> getOrCreateRecipeType(String name) {
        return RECIPE_TYPES.computeIfAbsent(name,
                k -> RecipeType.createRecipeHolderType(CreateIceAndFire.id(k)));
    }

    /**
     * 鼓风机加工配方类别。
     * <p>
     * 在 JEI 中渲染鼓风机与前方放置的龙首实体，并布置龙血、输入物与产物槽位。
     */
    private static final class FanProcessingCategory extends ProcessingViaFanCategory<DragonForgeRecipe> {

        /** JEI 中鼓风机本体渲染尺寸 */
        private static final int FAN_SCALE = 24;
        /** 龙首实体在鼓风机前方（+Z）偏移的格子距离 */
        private static final double HEAD_OFFSET = 2.0D;
        /** 龙首实体额外缩放 */
        private static final float HEAD_SCALE = 0.55F;
        /** 龙首实体在屏幕上的水平微调 */
        private static final double HEAD_SIDE = 0.6D;
        /** 龙首实体在屏幕上的竖直微调 */
        private static final double HEAD_DROP = 0.0D;
        /** 龙首实体朝向（面向观察者） */
        private static final float HEAD_YAW = 180.0F;

        /** 催化剂物品堆叠。 */
        private final ItemStack catalystStack;

        /** 龙首类型。 */
        private final String dragonType;

        /**
         * 构造鼓风机加工配方类别。
         *
         * @param info         类别信息
         * @param catalystStack 催化剂物品堆叠
         * @param dragonType   龙首类型
         */
        private FanProcessingCategory(Info<DragonForgeRecipe> info, ItemStack catalystStack, String dragonType) {
            super(info);
            this.catalystStack = catalystStack;
            this.dragonType = dragonType;
        }

        /**
         * 布置配方槽位：龙血(催化剂)、输入物、产物
         *
         * @param builder 配方布局构建器
         * @param recipe 龙煅炉配方
         * @param focuses JEI 焦点组
         */
        @Override
        public void setRecipe(@NotNull IRecipeLayoutBuilder builder, DragonForgeRecipe recipe, @NotNull IFocusGroup focuses) {
            builder.addSlot(RecipeIngredientRole.INPUT, 1, 48).addIngredients(recipe.getBlood());
            builder.addSlot(RecipeIngredientRole.INPUT, 21, 48).addIngredients(recipe.getInput());
            builder.addSlot(RecipeIngredientRole.OUTPUT, 141, 48).addItemStack(getResultItem(recipe));
        }

        /**
         * 渲染鼓风机前附加的龙首。
         * <p>
         * 世界不可用时以物品形式渲染催化剂；否则创建仅用于展示的龙首实体，
         * 设置类型、阶段与朝向，并按设定的缩放与偏移渲染到鼓风机前方。
         *
         * @param graphics 绘制上下文
         */
        @Override
        protected void renderAttachedBlock(@NotNull GuiGraphics graphics) {
            Minecraft minecraft = Minecraft.getInstance();
            Level level = minecraft.level;
            if (level == null) {
                GuiGameElement.of(catalystStack)
                        .scale(FAN_SCALE)
                        .atLocal(0, 0, HEAD_OFFSET)
                        .lighting(AnimatedKinetics.DEFAULT_LIGHTING)
                        .render(graphics);
                return;
            }

            DragonSkullEntity skull = new DisplaySkull(level);
            skull.setDragonType(dragonType);
            skull.setStage(4);
            skull.setDragonAge(75);
            skull.setYRot(HEAD_YAW);
            skull.setYHeadRot(HEAD_YAW);
            skull.setYBodyRot(HEAD_YAW);

            EntityRenderer<? super DragonSkullEntity> renderer = minecraft.getEntityRenderDispatcher().getRenderer(skull);

            PoseStack pose = graphics.pose();
            pose.pushPose();
            pose.scale(FAN_SCALE, FAN_SCALE, FAN_SCALE);
            pose.translate(0, 0, HEAD_OFFSET);
            pose.scale(1, -1, 1);
            pose.translate(HEAD_SIDE, HEAD_DROP, 0);
            pose.scale(HEAD_SCALE, HEAD_SCALE, HEAD_SCALE);

            RenderSystem.enableDepthTest();
            RenderSystem.enableBlend();
            AnimatedKinetics.DEFAULT_LIGHTING.applyLighting();

            MultiBufferSource.BufferSource buffer = minecraft.renderBuffers().bufferSource();
            renderer.render(skull, HEAD_YAW, 1.0F, pose, buffer, LightTexture.FULL_BRIGHT);
            buffer.endBatch();

            Lighting.setupFor3DItems();
            pose.popPose();
        }
    }

    /**
     * 仅用于 JEI 展示的龙首实体。
     * <p>
     * 重写墙面判定使其始终按地面龙首渲染，避免展示时被当作挂墙实体。
     */
    private static final class DisplaySkull extends DragonSkullEntity {

        /**
         * 构造展示用龙首。
         *
         * @param level 世界
         */
        private DisplaySkull(Level level) {
            super(IafEntities.DRAGON_SKULL.get(), level);
        }

        /**
         * 判断是否挂在墙上。
         *
         * @return 恒为 false，始终按地面龙首展示
         */
        @Override
        public boolean isOnWall() {
            return false;
        }
    }
}
