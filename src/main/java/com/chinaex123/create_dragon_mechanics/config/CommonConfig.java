package com.chinaex123.create_dragon_mechanics.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class CommonConfig {
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.BooleanValue ENABLE_FIRE_DRAGON_BREATH_FAN;
    public static final ModConfigSpec.BooleanValue ENABLE_ICE_DRAGON_BREATH_FAN;
    public static final ModConfigSpec.BooleanValue ENABLE_LIGHTNING_DRAGON_BREATH_FAN;

    public static final ModConfigSpec.BooleanValue ENABLE_FORGE_HEATING;
    public static final ModConfigSpec.IntValue FULL_BREATH_RPM;
    public static final ModConfigSpec.IntValue STREAM_RANGE;

    public static final ModConfigSpec.BooleanValue HARM_ENTITIES;
    public static final ModConfigSpec.DoubleValue BREATH_DAMAGE;
    public static final ModConfigSpec.IntValue BREATH_EFFECT_SECONDS;

    public static final ModConfigSpec.BooleanValue TRANSFORM_BLOCKS;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.comment("龙首触媒").push("Catalysts");
        ENABLE_FIRE_DRAGON_BREATH_FAN = builder
                .comment("是否启用火龙首触媒")
                .comment("Enable the fire dragon skull catalyst")
                .define("enableFireDragonBreathFan", true);
        ENABLE_ICE_DRAGON_BREATH_FAN = builder
                .comment("是否启用冰龙首触媒")
                .comment("Enable the ice dragon skull catalyst")
                .define("enableIceDragonBreathFan", true);
        ENABLE_LIGHTNING_DRAGON_BREATH_FAN = builder
                .comment("是否启用闪电龙首触媒")
                .comment("Enable the lightning dragon skull catalyst")
                .define("enableLightningDragonBreathFan", true);
        builder.pop();

        builder.comment("鼓风机处理").push("Fan processing");
        ENABLE_FORGE_HEATING = builder
                .comment("是否让龙息射流加热龙煅炉输入方块(需要龙首与龙煅炉属性相同)")
                .comment("Whether the breath jet heats a dragon forge input block (element must match)")
                .define("enableForgeHeating", true);
        builder.pop();

        builder.comment("气流").push("Airflow");
        FULL_BREATH_RPM = builder
                .comment("转速达到该值时龙息完全连续，转速越低，喷吐与加热的间隔越长")
                .comment("RPM at which the breath becomes continuous. Lower speeds increase the interval")
                .defineInRange("fullBreathRpm", 256, 16, Integer.MAX_VALUE);
        STREAM_RANGE = builder
                .comment("龙息射流的最大长度(格)，用于寻找可被加热的龙煅炉输入方块或可被侵蚀的方块")
                .comment("Maximum length of the breath jet in blocks")
                .defineInRange("streamRange", 8, 1, Integer.MAX_VALUE);
        builder.pop();

        builder.comment("生物效果").push("Entity effects");
        HARM_ENTITIES = builder
                .comment("龙息射流是否伤害/点燃路径上的生物")
                .comment("Whether the breath jet harms living entities in its path")
                .define("harmEntities", true);
        BREATH_DAMAGE = builder
                .comment("龙息每秒对生物造成的伤害，使用冰与火的龙息伤害类型")
                .comment("Damage dealt to entities by the breath every second, using Ice and Fire's breath damage types")
                .defineInRange("breathDamage", 1.0D, 0.0D, Integer.MAX_VALUE);
        BREATH_EFFECT_SECONDS = builder
                .comment("龙息附加状态的持续时间(秒)：火龙=点燃，冰龙=冰冻(FROZEN)，闪电龙=击退强度参照")
                .comment("Duration in seconds of the breath's on-hit effect: fire = burning, ice = FROZEN, lightning = knockback reference")
                .defineInRange("breathEffectSeconds", 8, 1, Integer.MAX_VALUE);
        builder.pop();

        builder.comment("方块转化").push("Block transformation");
        TRANSFORM_BLOCKS = builder
                .comment("龙息射流是否像真龙一样侵蚀方块(泥土变焦土/冻土/裂土、水结冰、木头变灰烬等)")
                .comment("需要 mobGriefing 游戏规则为 true，且不会影响龙煅炉本身与 DragonProof 方块")
                .comment("Whether the jet corrodes blocks like a real dragon")
                .comment("Requires the mobGriefing gamerule and never affects dragon forge blocks or DragonProof blocks")
                .define("transformBlocks", true);
        builder.pop();

        SPEC = builder.build();
    }
}
