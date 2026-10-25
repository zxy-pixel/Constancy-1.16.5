package org.thinkingstudio.constancy.client;

import java.io.IOException;
import java.util.Optional;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

public final class ResourceUtil {
    public static Optional<Resource> optional(ResourceManager manager, Identifier id) {
        try { return Optional.of(manager.getResource(id)); }
        catch (IOException e) { return Optional.empty(); }
    }
}
