# Compramos by Naiko11

Fabric mod project for Minecraft Java.

## Description

Compramos brings the Compramos meme to Minecraft: the Wandering Trader plays
the meme sound, and additional gimmicks create more meme moments.

Author: Naiko11

## Features

- Wandering Traders randomly play one of two Compramos sounds about every 60
  to 120 seconds.
- Each random cave sound has a 20% chance to spawn a Wandering Trader nearby
  inside the cave if a safe spot is available. The trader's own periodic sounds
  do not trigger spawns.
- A Montoya sound plays when a Wandering Trader despawns.
- A Terreneitor sound plays when a player mounts a donkey.
- Players in dark underground areas can hear quiet, random Compramos cave
  ambience about once every two hours, with a brief burst of enchantment
  particles.
- Client and operator commands are described in the [Commands](#commands)
  section.
- Optional Mod Menu settings let you toggle the sound features and cave
  particles, and adjust the trader and cave sound frequencies and Compramos
  sound volume.

## Commands

`/compramos help` lists the available commands and can be used by anyone.

The following server commands require operator permission level 2:

- `/compramos play <spieler> [full|simple|montoya|terreneitor]` plays the
  selected sound only for that player. If the optional sound is omitted, it
  randomly plays the full or simple Compramos sound.
- `/compramos trader spawn [spieler]` spawns a Wandering Trader near the
  command source, or near the selected player. If the location is in a dark
  cave, it searches for a safe spawn position in the cave.

The client-only test commands play a sound locally and do not require operator
permissions:

- `/compramos test full`
- `/compramos test simple`
- `/compramos test montoya`
- `/compramos test terreneitor`

The Minecraft and Fabric versions are defined in [gradle.properties](gradle.properties).
The target Java version is specified in [build.gradle](build.gradle); the
Gradle wrapper version is set in
[gradle-wrapper.properties](gradle/wrapper/gradle-wrapper.properties).

## Build and run

To build the project, you need an installed JDK matching the Java version defined
in the build. The commands can be run in PowerShell from the project directory:

```powershell
.\gradlew.bat build
.\gradlew.bat runClient
```

`build` creates the mod JAR in the `build/libs/` directory. `runClient` starts
the Minecraft client for development.

## Entry points

- [CompramosMod.java](src/main/java/de/compramos/CompramosMod.java) is the
  shared Fabric entry point.
- [CompramosClient.java](src/client/java/de/compramos/client/CompramosClient.java)
  is the client-side Fabric entry point.
- [fabric.mod.json](src/main/resources/fabric.mod.json) contains the mod ID,
  display name, entry points, and dependencies.

## Sound files

The Compramos sounds are registered as Minecraft sound events. While a
Wandering Trader is loaded in the world, it randomly chooses one of the
original two sounds about every 60 to 120 seconds.

The Montoya sound plays when a Wandering Trader despawns. The Terreneitor sound
plays when a player mounts a donkey.

As a separate cave ambience feature, players in dark underground areas can also
hear one of the sounds randomly about once every two hours. These quiet ambient
sounds are sent only to the player who triggered them and are accompanied by a
brief, subtle burst of enchantment particles around that player. Each cave
sound has a configurable chance to spawn a Wandering Trader near the player;
the Wandering Trader's own sounds do not trigger spawns.

## Settings

Mod Menu is optional and only needed to open Compramos' settings screen. The
settings let you toggle Wandering Trader sounds, trader spawning, and cave
ambience; choose the trader spawn chance and sound frequencies; enable the
Montoya and Terreneitor event sounds; toggle cave particles; and adjust the
volume of all Compramos sounds. Volume and spawn-chance changes are saved when
leaving the settings screen to
`config/compramos-by-naiko11.json`; other selections are saved immediately.

Without Mod Menu, you can edit `config/compramos-by-naiko11.json` directly in
the Minecraft instance's `config` folder. If the file does not exist yet, start
the game once to create it. For example, to disable trader spawning, set
`"traderSpawningEnabled": false`; `traderSpawnChance` accepts values from 1 to
100. Close the game before editing and restart it afterward.

In singleplayer, the integrated server uses the same settings. On a dedicated
server, edit the server's `config/compramos-by-naiko11.json` and restart it; a
player's local settings screen or config file does not change a remote server's
configuration or its sound volume.

Minecraft sound assets use OGG Vorbis. The source audio files are kept in
`src/main/resources/assets/compramos/`; registered OGG sound assets are stored
in `src/main/resources/assets/compramos-by-naiko11/sounds/`.

## Install on a client

For a simpler client installation, Compramos will be available through
Modrinth and CurseForge. Use a Fabric profile for the same Minecraft version
as the server, and install Fabric API and Compramos. Install Mod Menu only if
you want to open the settings screen.

## Install on a server

The server must run Fabric for the same Minecraft version as the mod. Use Java
25 or newer and install Fabric API on the server.

1. Stop the server and back up the world.
2. Build the mod from the project directory:

   ```powershell
   .\gradlew.bat build
   ```

3. Upload `build/libs/compramos-by-naiko11-1.0.0.jar` and a matching Fabric API
   JAR to the server's `mods` folder. Create the folder if it does not exist.
4. Each player must also install Fabric API and Compramos on their client to
   load the sound assets. See [Install on a client](#install-on-a-client).
5. Start the server and connect with clients running Fabric for the same
   Minecraft version.

The Wandering Trader plays sounds through the server, which sends them to nearby
players. A vanilla server cannot load this Fabric mod.

## License

The Java source code is licensed under the GNU General Public License v3.0 only
(GPL-3.0-only); see [LICENSE](LICENSE). This license does not cover the audio
files in `src/main/resources/assets/compramos/` or
`src/main/resources/assets/compramos-by-naiko11/sounds/`. No license for those
audio files is granted by this repository license.
