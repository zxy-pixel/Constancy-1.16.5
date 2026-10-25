package org.thinkingstudio.constancy.client;

import me.pepperbell.continuity.client.ContinuityClient;
import me.pepperbell.continuity.client.config.ContinuityConfigScreen;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public final class ClientBootstrap {
    public static void init() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        new ContinuityClient().onInitializeClient(bus);
        bus.addListener(NativeResources::onModelBake);
        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.CONFIGGUIFACTORY,
                () -> (client, parent) -> new ContinuityConfigScreen(parent));
    }
}
