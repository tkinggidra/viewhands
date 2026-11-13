# ViewHands

Tiny client-side Fabric mod: change held item size, position and rotation, reshape the swing
animation and change swing speed, from one small tabbed screen with a live preview.
Target: Minecraft **26.2**.

> Generated with Claude Fable 5.2.

**Status: written against the 26.2 names but not compiled or run yet** (it was produced without
access to the Minecraft jars). Expect to fix a name or two on first build; see "If something breaks".

## Use

Needs Fabric Loader + Fabric API. Open the editor with any of:

- **V** in a world (rebind under Options > Controls > Key Binds > ViewHands)
- the **`/viewhands`** command
- **Mod Menu** > ViewHands > configure (optional; from the title screen there is no hand to preview)

| Tab | Settings |
| --- | --- |
| Item | Size (0 hides the item), offset X/Y/Z, pitch/yaw/roll |
| Swing | Speed mode, swing speed (0 = never swing), per-Efficiency-level bonus, swing distance, swing rotation, equip animation on/off, preview swing on/off |
| Camera | Preview only: focus the active arm, camera X/Y, zoom, reset |

- The panel draws no background in a world, so your real hand is the preview.
- Sliders apply instantly. **Save** writes `config/viewhands.json`; **Cancel**/Esc drops everything
  since the last save; **Reset** goes back to vanilla values (still needs Save).
- **Speed mode "Overall"** uses one multiplier. **"By Efficiency"** multiplies it by
  `1 + bonus x Efficiency level` of the tool in your main hand.
- **Camera** moves the hands only while the editor is open and is never saved. "Focus active arm"
  pulls whichever arm holds your item (left or right) to the middle of the screen.
- Swing distance / rotation at x1.00 leave vanilla's swing code completely untouched.

## Build

Needs JDK 25 (26.x) and JDK 21 (1.21.11). Open the folder in IntelliJ, or:

    ./gradlew :26.2:build          # jar in versions/26.2/build/libs
    ./gradlew :26.2:runClient

Switch the version the files in `src/` are written for with the Gradle task
`Set active project to <version>` (group "stonecutter"). Don't edit `stonecutter active` by hand.

## How it is put together

| File | Job |
| --- | --- |
| `ViewHands` | Entrypoint: loads config, registers the key and `/viewhands`. |
| `ViewConfig` | The saved settings + JSON load/save. |
| `ViewTransform` | The maths: camera, scale/offset/rotation and swing applied to the PoseStack. |
| `mixin/ItemInHandRendererMixin` | Hooks the start and end of vanilla's first-person arm+item method. |
| `mixin/LivingEntityMixin` | Divides the swing duration by the speed multiplier (own player only). |
| `ViewHandsScreen` | The UI. |
| `ModMenuIntegration` | Gives Mod Menu the config screen. |
| `Compat` | Every version difference except one method in `ViewHandsScreen`. |

## Versions (Stonecutter)

One source tree; `//? if >=26.2 { ... //?} else { /* ... */ //?}` comments pick the code per version.

| Node | Buildscript | State |
| --- | --- | --- |
| 26.2 | `build.gradle.kts` | main target |
| 26.1.2 | `build.gradle.kts` | wired up, untested |
| 1.21.11 | `build.obf.gradle.kts` (remapping Loom, Mojang mappings) | wired up, untested |

Add a version:

1. Add it to `settings.gradle.kts` (`versions(...)`; pre-26.1 goes on the `build.obf.gradle.kts` line).
2. Create `versions/<version>/gradle.properties` (copy a neighbour, change the four values).
3. Switch to it, build, and wrap whatever fails to compile in a `//? if` block, ideally in `Compat`.

Known differences already handled: `renderArmWithItem` -> `submitArmWithItem` (26.2),
`Minecraft.setScreen` -> `Minecraft.gui.setScreen` (26.2), `KeyBindingHelper` -> `KeyMappingHelper`
and `ClientCommandManager` -> `ClientCommands` (26.1), `Screen.renderBackground` -> `extractBackground` (26.1).

Known for later: 26.3 renames `ItemInHandRenderer` to `FirstPersonHandsAndItemsRenderer`; versions
before 1.21.11 call `Identifier` `ResourceLocation`, and before 1.21.9 key categories are Strings.

## If something breaks

- **Build error in a `.java` file:** a Minecraft name differs from what was assumed. Ctrl-click the
  vanilla class in IntelliJ, find the real name, fix it (in `Compat` if it is version-specific).
- **Crash at startup mentioning a mixin / "injection failed":** the target method name is wrong.
  Check `Compat.ARM_METHOD` against `ItemInHandRenderer`, and `getCurrentSwingDuration` against
  `LivingEntity`.
- **Plugin or dependency not found:** bump the versions in `settings.gradle.kts`,
  `stonecutter.gradle.kts`, `gradle.properties` and `versions/*/gradle.properties`
  (Fabric numbers: https://fabricmc.net/develop, Mod Menu: https://modrinth.com/mod/modmenu/versions).
  The Mod Menu version for 1.21.11 is the least certain one.

## Limits

- With swing distance/rotation changed, every held item uses the reshaped "whack" swing, including
  spears, which normally stab.
- Item settings skip empty hands and held maps (the preview camera and swing speed 0 still apply).
- Swing speed is visual and local; other players and the server see vanilla timing, and at speed 0
  your third-person arm still swings.
- "By Efficiency" reads the vanilla `mining_efficiency` attribute, so server-side custom enchant
  systems that don't set it count as Efficiency 0.
