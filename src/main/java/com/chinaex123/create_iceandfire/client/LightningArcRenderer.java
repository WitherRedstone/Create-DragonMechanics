package com.chinaex123.create_iceandfire.client;

import com.chinaex123.create_iceandfire.CreateIceAndFire;
import com.chinaex123.create_iceandfire.config.CommonConfig;
import com.chinaex123.create_iceandfire.processing.fans.dragon.BreathFlow;
import com.chinaex123.create_iceandfire.processing.fans.dragon.DragonBreathHandler;
import com.iafenvoy.iceandfire.entity.DragonSkullEntity;
import com.iafenvoy.iceandfire.render.misc.LightningBoltData;
import com.iafenvoy.iceandfire.render.misc.LightningRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllTags;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * 闪电龙息电弧渲染器。
 * <p>
 * 客户端定期扫描正在被鼓风机吹拂的闪电龙首，按气流方向计算电弧长度，
 * 在实体渲染之后使用冰与火的闪电渲染器绘制闪电弧。
 * 电弧长度遇阻挡方块或被透明气流标签放行的方块时相应截断。
 */
public final class LightningArcRenderer {

    /** 闪电弧刷新间隔（刻）。 */
    private static final int REFRESH_INTERVAL = 4;
    /** 闪电弧起点相对龙首的高度（格）。 */
    private static final double ARC_HEIGHT = 0.35D;
    /** 闪电弧粗细。 */
    private static final float ARC_SIZE = 0.05F;
    /** 闪电弧分段数（与冰与火闪电龙一致）。 */
    private static final int ARC_SEGMENTS = 15;
    /** 闪电弧最短寿命（刻）。 */
    private static final int ARC_LIFESPAN = 4;

    /** 冰与火闪电龙的类型 id。 */
    private static final String LIGHTNING_TYPE = "lightning";

    /** 冰与火提供的闪电弧渲染器。 */
    private static final LightningRenderer RENDERER = new LightningRenderer();

    /** 当前需要绘制的闪电弧。 */
    private static final List<Arc> ARCS = new ArrayList<>();

    /** 是否已经输出过首次渲染日志。 */
    private static boolean logged;

    private LightningArcRenderer() {
    }

    /**
     * 客户端每刻刷新电弧列表。
     * <p>
     * 按固定间隔清空并重建电弧；未启用闪电龙息时直接清空。
     * 遍历可见实体中的闪电龙首，找到气流后计算电弧起终点并记录。
     *
     * @param event 客户端每刻更新事件
     */
    public static void onClientTick(ClientTickEvent.Post event) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null || level.getGameTime() % REFRESH_INTERVAL != 0) {
            return;
        }

        ARCS.clear();
        if (!CommonConfig.ENABLE_LIGHTNING_DRAGON_BREATH_FAN.get()) {
            return;
        }

        for (Entity entity : level.entitiesForRendering()) {
            if (!(entity instanceof DragonSkullEntity skull) || !LIGHTNING_TYPE.equals(skull.getDragonType())) {
                continue;
            }

            BlockPos skullPos = skull.blockPosition();
            BreathFlow flow = DragonBreathHandler.findFlow(level, skullPos);
            if (flow == null) {
                continue;
            }

            Vec3 start = new Vec3(skull.getX(), skull.getY() + ARC_HEIGHT, skull.getZ());
            double length = streamLength(level, skullPos, flow);
            if (length < 1.0D) {
                continue;
            }
            Vec3 end = start.add(length * flow.direction().getStepX(),
                    length * flow.direction().getStepY(),
                    length * flow.direction().getStepZ());
            ARCS.add(new Arc(skull, start, end));
        }

        if (!logged && !ARCS.isEmpty()) {
            logged = true;
        }
    }

    /**
     * 在实体渲染之后绘制闪电弧。
     * <p>
     * 逐条根据起终点构造闪电数据并交给冰与火的闪电渲染器更新，
     * 最后统一渲染并提交缓冲区。
     *
     * @param event 关卡渲染事件
     */
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES || ARCS.isEmpty()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }

        float partialTick = event.getPartialTick().getGameTimeDeltaTicks();
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();

        for (Arc arc : ARCS) {
            LightningBoltData bolt = new LightningBoltData(LightningBoltData.BoltRenderInfo.ELECTRICITY,
                    arc.start(), arc.end(), ARC_SEGMENTS)
                    .size(ARC_SIZE)
                    .lifespan(ARC_LIFESPAN)
                    .spawn(LightningBoltData.SpawnFunction.NO_DELAY)
                    .fade(LightningBoltData.FadeFunction.fade(0.1F));
            RENDERER.update(arc.owner(), bolt, partialTick);
        }

        Vec3 cameraPos = event.getCamera().getPosition();
        poseStack.pushPose();
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        RENDERER.render(partialTick, poseStack, buffers);
        poseStack.popPose();
        buffers.endBatch(RenderType.lightning());
    }

    /**
     * 计算气流方向上电弧可延伸的长度。
     * <p>
     * 逐格检查前方方块：区块未加载或遇到有碰撞体积的非透明方块时截断，
     * 空气与透明气流标签方块继续前进，到达射程上限时返回射程。
     *
     * @param level  客户端世界
     * @param origin 龙首位置
     * @param flow   气流方向与风速
     * @return 电弧长度（格）
     */
    private static double streamLength(ClientLevel level, BlockPos origin, BreathFlow flow) {
        int range = CommonConfig.STREAM_RANGE.get();
        for (int step = 1; step <= range; step++) {
            BlockPos probe = origin.relative(flow.direction(), step);
            if (!level.isLoaded(probe)) {
                return step - 1;
            }
            BlockState state = level.getBlockState(probe);
            if (state.isAir() || AllTags.AllBlockTags.FAN_TRANSPARENT.matches(state)) {
                continue;
            }
            if (!state.getCollisionShape(level, probe).isEmpty()) {
                return step - 1;
            }
        }
        return range;
    }

    /**
     * 一条待绘制的闪电弧。
     *
     * @param owner 电弧所属实体（用于渲染器分组）
     * @param start 起点
     * @param end   终点
     */
    private record Arc(Object owner, Vec3 start, Vec3 end) {
    }
}
