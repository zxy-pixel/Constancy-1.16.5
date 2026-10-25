/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// Modified 2026-10-22: relocated and adapted for the Constancy Forge backport.
package org.thinkingstudio.constancy.render.support;

import org.thinkingstudio.constancy.render.api.Renderer;
import org.thinkingstudio.constancy.render.api.RendererAccess;

public final class RendererAccessImpl implements RendererAccess {
	public static final RendererAccessImpl INSTANCE = new RendererAccessImpl();

	// private constructor
	private RendererAccessImpl() { }

	@Override
	public void registerRenderer(Renderer renderer) {
		if (renderer == null) {
			throw new NullPointerException("Attempt to register a NULL rendering plug-in.");
		} else if (activeRenderer != null) {
			throw new UnsupportedOperationException("A second rendering plug-in attempted to register. Multiple rendering plug-ins are not supported.");
		} else {
			activeRenderer = renderer;
			hasActiveRenderer = true;
		}
	}

	private Renderer activeRenderer = org.thinkingstudio.constancy.render.impl.IndigoRenderer.INSTANCE;

	/** avoids null test every call to {@link #hasRenderer()}. */
	private boolean hasActiveRenderer = true;

	@Override
	public Renderer getRenderer() {
		return activeRenderer;
	}

	@Override
	public boolean hasRenderer() {
		return hasActiveRenderer;
	}
}
