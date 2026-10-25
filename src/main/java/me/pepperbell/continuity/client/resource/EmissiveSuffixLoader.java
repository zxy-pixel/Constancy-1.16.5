// Modified 2026-10-22 for the unofficial Forge 1.16.5 backport.
package me.pepperbell.continuity.client.resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;

import org.jetbrains.annotations.Nullable;

import me.pepperbell.continuity.client.ContinuityClient;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

public final class EmissiveSuffixLoader {
	public static final Identifier LOCATION = new Identifier("optifine/emissive.properties");

	private static String emissiveSuffix;

	@Nullable
	public static String getEmissiveSuffix() {
		return emissiveSuffix;
	}

	public static void load(ResourceManager manager) {
		emissiveSuffix = null;

		Optional<Resource> optionalResource = org.thinkingstudio.constancy.client.ResourceUtil.optional(manager, LOCATION);
		if (optionalResource.isPresent()) {
			Resource resource = optionalResource.get();
			try (InputStream inputStream = resource.getInputStream()) {
				Properties properties = new Properties();
				properties.load(inputStream);
				emissiveSuffix = properties.getProperty("suffix.emissive");
			} catch (IOException e) {
				ContinuityClient.LOGGER.error("Failed to load emissive suffix from file '" + LOCATION + "'", e);
			}
		}
	}
}
