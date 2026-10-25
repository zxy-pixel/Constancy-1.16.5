# Third-party source notices

The `org.thinkingstudio.constancy.render.api`, `render.impl`, and `render.support`
packages contain relocated and adapted mesh/material utilities from Fabric API
and Indigo, commit `3ede25f24c295e245658edcf9b202552aa4e0515` (Minecraft 1.16).

Upstream: https://github.com/FabricMC/fabric/tree/3ede25f24c295e245658edcf9b202552aa4e0515

Copyright (c) FabricMC and the contributors identified in the source headers.
Licensed under Apache License 2.0. A copy is included at
`META-INF/licenses/fabric-renderer-Apache-2.0.txt`.

Modifications dated 2026-10-22: package relocation, internal API compatibility
methods, and Forge-specific delegation. These utilities do not install Fabric
Loader, Fabric API or the Indigo renderer as separate mods.

Constancy/Continuity source remains subject to the original LGPL-3.0-only license
in `LICENSE`. Preserve all original authorship and license notices on distribution.

Embeddium `0.3.18+mc1.16.5` (Modrinth version `9PbX7IEh`) is an optional,
compile-only dependency used to compile compatibility mixins. No Embeddium or
Sodium classes or JARs are embedded in this mod. Players obtain Embeddium
separately if they want to use that renderer.
