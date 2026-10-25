// Modified 2026-10-22 for the unofficial Forge 1.16.5 backport.
package me.pepperbell.continuity.client.util;

import net.minecraft.client.texture.MissingSprite;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;

public final class TextureUtil {
	public static final SpriteIdentifier MISSING_SPRITE_ID = toSpriteId(MissingSprite.getMissingSpriteId());

	public static SpriteIdentifier toSpriteId(Identifier id) {
		return new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, id);
	}

	public static boolean isMissingSprite(Sprite sprite) {
		return sprite.getId().equals(MissingSprite.getMissingSpriteId());
	}
}
