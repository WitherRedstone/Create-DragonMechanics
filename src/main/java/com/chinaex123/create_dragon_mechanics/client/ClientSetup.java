package com.chinaex123.create_dragon_mechanics.client;

import com.chinaex123.create_dragon_mechanics.CreateDragonMechanics;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = CreateDragonMechanics.MODID, dist = Dist.CLIENT)
public class ClientSetup {

    public ClientSetup(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        NeoForge.EVENT_BUS.addListener(LightningArcRenderer::onClientTick);
        NeoForge.EVENT_BUS.addListener(LightningArcRenderer::onRenderLevelStage);
    }

    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
    }
}
