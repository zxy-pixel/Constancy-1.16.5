# Constancy — Forge 1.16.5 Backport

> [!WARNING]
> **SECURITY NOTICE**
> This is the true Continuity port for Minecraft 1.16.5 Forge. Recently, there has been a rise in malicious actors uploading trojans disguised as Minecraft mods on GitHub. Please be careful with files from unknown authors. Extra caution should be taken with mod files from unknown authors that do not have publicly available source code. if you are unsure about safety, feel free to decompile and inspect the code yourself before running it. Keep your antivirus software active.

**⚠️ NOTE: This repository is archived and unmaintained.**
**No Issues or Pull Requests will be accepted. I will not be providing technical support.** 
If you encounter bugs, feel free to fork this repository and fix them yourself. 
Please do NOT report issues to the original Constancy/Continuity authors.

---

This is an unofficial backport of [Constancy](https://github.com/ThinkingStudios/Constancy) (formerly [Continuity](https://github.com/PepperCode1/Continuity)) for Forge 1.16.5, based on upstream commit `0595171`.

## Requirements
- **Minecraft:** 1.16.5
- **Forge:** 36.2.34 / 36.2.42
- **Java:** 64-bit Java 8
- **Side:** Client-side only (No Fabric or FFAPI required)

## Features
- Supports Connected Textures (CTM), Overlays, Emissive textures, and custom block render layers.
- Compatible with native 1.16.5 resource packs (1.20+ blocks are not backported).
- Preserves vanilla rendering for moving/falling blocks to prevent visual bugs.
- **Optional Integration:** Compatible with **Embeddium 0.3.18+mc1.16.5**. The mod works fine on a plain Forge client without Embeddium. 

## Installation & Configuration
1. Drop the `.jar` file into your Forge 1.16.5 `mods` folder.
2. Open the config via `Mods -> Constancy -> Config` in-game (or edit `config/continuity.json`).
3. Two optional built-in resource packs ("Default Connected Textures" and "Glass Pane Culling Fix") can be enabled in the Resource Packs menu. Place third-party CTM packs above them.

## Building from Source
Gradle requires **JDK 21** to build, but the output mod is compiled to **Java 8** bytecode.

```bash
# Linux / macOS
export JAVA_HOME=/path/to/jdk21
./gradlew build

# Windows
set JAVA_HOME=C:\path\to\jdk21
gradlew.bat build