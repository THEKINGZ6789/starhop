# Starhop (Minecraft 26.3, Fabric)

Galaxy-style platforming for Minecraft, built around an original theme.

## What's in v0.1

- **Triple jump**: jump, land, and jump again quickly while moving. The 2nd and 3rd jumps go higher.
- **Spin**: press `R` (rebindable under "Starhop") once per jump to hover briefly in mid-air with a particle ring.
- **Comet Shard**: new item (Ingredients tab). Right-click for 15 seconds of Speed III, Jump Boost II and Resistance V.

## Controller support

- **Spin** also fires from a connected gamepad's X button (Square on PlayStation pads). It's ignored while a menu is open.
- Jumping and moving are vanilla actions, so the triple jump works with whatever input method moves your character.
- To use a controller for the rest of the game, a controller mod such as Controlify is the usual route. Spin is also a normal rebindable key, so it should appear in that mod's bindings (not tested).
- To change the gamepad button, edit `SPIN_GAMEPAD_BUTTON` in `StarhopClient.java`.

## Build without installing anything (GitHub Actions)

1. Create a free GitHub account and a new empty repository.
2. Upload the contents of this folder to it (drag and drop on the repo page, including the hidden `.github` folder).
3. Open the repo's **Actions** tab. The "Build Starhop" workflow runs automatically (or click "Run workflow").
4. When it finishes, open the run and download the **starhop-mod** artifact. Unzip it and put the `.jar` in your `mods` folder.
5. If the run fails, open it, copy the red error lines and send them to Claude.

## Setup

1. Install **JDK 25** and **IntelliJ IDEA 2025.3 or newer**.
2. Open this folder as a Gradle project.
3. Create the Gradle wrapper once: `gradle wrapper --gradle-version latest` (needs Gradle 9.x installed),
   or copy `gradlew`, `gradlew.bat` and the `gradle/` folder from the official Fabric template.
4. Run `./gradlew runClient` to test, `./gradlew build` for the jar in `build/libs/`.

If the build files give you trouble, generate a fresh 26.3 project at
https://fabricmc.net/develop/template/ and copy this project's `src/` folder into it.

## Not compiled yet

These files were written against the Fabric 26.1 docs and the 26.3 version numbers, but were **not compiled**.
The most likely spots to need a rename are:

- `Starhop.java`: `CreativeModeTabEvents.modifyOutputEvent` (the old name was `ItemGroupEvents.modifyEntriesEvent`)
- `build.gradle`: the Loom version (`1.17-SNAPSHOT`)
- Anything in `StarhopClient.java` that Mojang renamed between 26.1 and 26.3

Paste any compiler errors back into the chat and they're usually quick to fix.

## Tuning

Constants at the top of `StarhopClient.java` control jump heights, the chain window and spin length.
Jumps above about 3 blocks cause fall damage.

## Roadmap

- v0.2: ground pound with a server-side shockwave, wall kick
- v0.3: your own hero character (mob or player skin) with your own art
- v0.4: small planetoids with custom gravity
