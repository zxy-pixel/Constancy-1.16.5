# 1.16.5 backport.2 — 2026-10-23

- Add optional integration with Embeddium 0.3.18+mc1.16.5. Prepare CTM/emissive
  geometry at its chunk renderer entry while keeping Embeddium's buffers,
  lighting, sprite tracking and rendering backend.
- Read biome conditions from Embeddium's cloned biome snapshot on chunk workers.
- Load compatibility mixins only when Embeddium is installed. The dependency is
  compile-only and is not embedded or required for plain Forge clients.
- Add actual framebuffer assertions for CTM selection, emissive brightness under
  occlusion, and resource-pack removal/restoration, plus snapshot biome tests.

# 1.16.5 backport.1 — 2026-10-22

Unofficial backport of ThinkingStudios/Constancy commit
`0595171a9af3c076de38c838080df7f719a3ce5f` (1.20.1/forge).

- Target Minecraft 1.16.5 / Forge 36.2.34, with Java 8 bytecode and runtime.
- Replace FFAPI/Indigo mod dependencies with a render-local Forge bridge and
  relocated Apache-2.0 mesh/material utilities from Fabric API's 1.16 branch.
- Adapt the atlas lifecycle, resource redirects, model baking, IModelData,
  block/item rendering, extra render layers, lighting and resource reloads.
- Retain CTM/compact/overlay methods, aliases, multipass rules and emissive textures.
- Adapt biome lookup through chunk render regions and native dynamic registries.
- Port both optional builtin packs to resource format 6; omit the tinted-glass
  rules and textures because that block does not exist in Minecraft 1.16.5.
- Provide the four configuration toggles through Forge's Mods configuration entry.
- Preserve vanilla rendering for moving/falling block states that differ from
  their current world position, matching the original feature suppression.
- Add an optional hidden-client regression harness and original CC0 fixtures.

Modified source files are identified in Git history and with dated comments.
See THIRD_PARTY_NOTICES.md for reused source attribution and licenses.
