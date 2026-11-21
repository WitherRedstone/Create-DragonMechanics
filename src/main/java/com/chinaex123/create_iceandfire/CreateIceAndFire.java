package com.chinaex123.create_iceandfire;

import com.chinaex123.create_iceandfire.config.CommonConfig;
import com.chinaex123.create_iceandfire.init.CIBCapabilities;
import com.chinaex123.create_iceandfire.init.FanType;
import com.chinaex123.create_iceandfire.processing.fans.dragon.DragonBreathHandler;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.slf4j.Logger;

@Mod(CreateIceAndFire.MODID)
public class CreateIceAndFire {
    public static final String MODID = "create_iceandfire";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CreateIceAndFire(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener((RegisterEvent event) -> FanType.init());
        modEventBus.addListener(CIBCapabilities::registerCapabilities);
        modContainer.registerConfig(ModConfig.Type.COMMON, CommonConfig.SPEC);
        NeoForge.EVENT_BUS.addListener(DragonBreathHandler::onLevelTick);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
