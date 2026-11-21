package com.chinaex123.create_iceandfire.processing.fans;

import com.iafenvoy.iceandfire.registry.IafSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;

/**
 * 鼓风机龙息音效工具类。
 * <p>
 * 为火龙息、冰龙息与闪电龙息分别播放双层音效：
 * 以冰与火的龙息音效为主音，叠加对应的原版辅助音效。
 * 播放以位置与时间为相位错开，避免同一区域的多个龙首同时发声而过响。
 */
public final class FanProcessingSounds {

    private FanProcessingSounds() {
    }

    /**
     * 播放火龙息音效
     *
     * @param level 世界实例
     * @param pos 音效播放位置
     */
    public static void fireDragonBreathSound(Level level, BlockPos pos) {
        if (shouldNotPlayOnTick(level, pos, 31, 11)) return;
        playLayered(level, pos,
                IafSounds.FIREDRAGON_BREATH.get(), 0.3f, 1.0f, 1.2f,
                SoundEvents.BLAZE_SHOOT, 0.15f, 0.9f, 1.1f);
    }

    /**
     * 播放冰龙息音效
     *
     * @param level 世界实例
     * @param pos 音效播放位置
     */
    public static void iceDragonBreathSound(Level level, BlockPos pos) {
        if (shouldNotPlayOnTick(level, pos, 33, 13)) return;
        playLayered(level, pos,
                IafSounds.ICEDRAGON_BREATH.get(), 0.3f, 1.0f, 1.2f,
                SoundEvents.PLAYER_HURT_FREEZE, 0.15f, 0.9f, 1.1f);
    }

    /**
     * 播放闪电龙息音效
     *
     * @param level 世界实例
     * @param pos 音效播放位置
     */
    public static void lightningDragonBreathSound(Level level, BlockPos pos) {
        if (shouldNotPlayOnTick(level, pos, 29, 17)) return;
        playLayered(level, pos,
                IafSounds.LIGHTNINGDRAGON_BREATH.get(), 0.3f, 1.0f, 1.2f,
                SoundEvents.LIGHTNING_BOLT_IMPACT, 0.15f, 0.9f, 1.1f);
    }

    /**
     * 判断本刻是否不应播放音效。
     * <p>
     * 以位置与盐值计算相位，使同一位置的音效按固定间隔错开播放。
     *
     * @param level    世界实例
     * @param pos      位置
     * @param interval 播放间隔（刻）
     * @param salt     相位盐值
     * @return 本刻不应播放返回 true
     */
    private static boolean shouldNotPlayOnTick(Level level, BlockPos pos, int interval, int salt) {
        long phaseSeed = pos.asLong() ^ salt;
        int phase = Math.floorMod(Long.hashCode(phaseSeed), interval);
        return Math.floorMod(level.getGameTime() + phase, interval) != 0;
    }

    /**
     * 播放双层音效。
     * <p>
     * 仅服务端播放；依次播放主音效与辅助音效。
     *
     * @param level            世界实例
     * @param pos              位置
     * @param primary          主音效
     * @param primaryVolume    主音效音量
     * @param primaryMinPitch  主音效最低音调
     * @param primaryMaxPitch  主音效最高音调
     * @param secondary        辅助音效
     * @param secondaryVolume  辅助音效音量
     * @param secondaryMinPitch 辅助音效最低音调
     * @param secondaryMaxPitch 辅助音效最高音调
     */
    private static void playLayered(Level level, BlockPos pos, SoundEvent primary, float primaryVolume, float primaryMinPitch, float primaryMaxPitch,
                                    SoundEvent secondary, float secondaryVolume, float secondaryMinPitch, float secondaryMaxPitch) {
        if (level.isClientSide) {
            return;
        }

        play(level, pos, primary, primaryVolume, primaryMinPitch, primaryMaxPitch);
        play(level, pos, secondary, secondaryVolume, secondaryMinPitch, secondaryMaxPitch);
    }

    /**
     * 在指定位置播放一个音效，音调在给定区间内随机。
     *
     * @param level    世界实例
     * @param pos      位置
     * @param event    音效
     * @param volume   音量
     * @param minPitch 最低音调
     * @param maxPitch 最高音调
     */
    private static void play(Level level, BlockPos pos, SoundEvent event, float volume, float minPitch, float maxPitch) {
        level.playSound(null, pos, event, SoundSource.BLOCKS, volume,
                minPitch + (level.random.nextFloat() * (maxPitch - minPitch)));
    }
}
