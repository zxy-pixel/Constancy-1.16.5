// Modified 2026-10-22 for the unofficial Forge 1.16.5 backport.
package org.thinkingstudio.constancy;

import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.FMLNetworkConstants;
import org.apache.commons.lang3.tuple.Pair;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import org.thinkingstudio.constancy.client.ClientBootstrap;

/** Non-official 1.16.5 backport of Constancy/Continuity. */
@Mod(Constancy.ID)
public final class Constancy {
    public static final String ID = "constancy";
    public Constancy() {
        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST,
                () -> Pair.of(() -> FMLNetworkConstants.IGNORESERVERONLY, (remoteVersion, server) -> true));
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> ClientBootstrap::init);
    }
}
