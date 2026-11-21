package com.chinaex123.create_iceandfire.processing.fans.dragon;

import net.minecraft.core.Direction;

/**
 * 龙首前方的气流信息。
 *
 * @param direction 气流方向
 * @param speed     气流速度
 */
public record BreathFlow(Direction direction, float speed) {
}
