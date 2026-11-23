package com.chinaex123.create_dragon_mechanics;

import com.chinaex123.create_dragon_mechanics.config.CommonConfig;
import com.chinaex123.create_dragon_mechanics.init.CIBCapabilities;
import com.chinaex123.create_dragon_mechanics.init.FanType;
import com.chinaex123.create_dragon_mechanics.processing.fans.dragon.DragonBreathHandler;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.slf4j.Logger;

@Mod(CreateDragonMechanics.MODID)
public class CreateDragonMechanics {
    public static final String MODID = "create_dragon_mechanics";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CreateDragonMechanics(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener((RegisterEvent event) -> FanType.init());
        modEventBus.addListener(CIBCapabilities::registerCapabilities);
        NeoForge.EVENT_BUS.addListener(DragonBreathHandler::onLevelTick);
        modContainer.registerConfig(ModConfig.Type.COMMON, CommonConfig.SPEC);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
