# Compramos by Naiko11

Fabric mod project for Minecraft Java.

## Description

Compramos brings the Compramos meme to Minecraft: the Wandering Trader plays
the meme sound, and additional gimmicks create more meme moments.

## Project status

The mod registers two Compramos sounds, provides client commands to test them,
and plays a random sound near Wandering Traders at intervals.

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

The Compramos sounds are registered as Minecraft sound events. Test them with
`/compramos test full`, `/compramos test simple`, `/compramos test montoya`, or
`/compramos test terreneitor`. While a Wandering Trader is loaded in the world,
it randomly chooses one of the original two sounds about every 60 to 120 seconds.

The Montoya sound plays when a Wandering Trader despawns. The Terreneitor sound
plays when a player mounts a donkey.

As a separate cave ambience feature, players in dark underground areas can also
hear one of the sounds randomly about once every two hours. These quiet ambient
sounds are sent only to the player who triggered them and are accompanied by a
brief, subtle burst of enchantment particles around that player.

## Settings

Install Mod Menu to open Compramos' settings from its entry in the Mods menu.
The settings let you toggle Wandering Trader sounds and cave ambience, choose
their frequency, enable the Montoya and Terreneitor event sounds, and toggle the
cave particles. The screen saves changes immediately to
`config/compramos-by-naiko11.json`.

In singleplayer, the integrated server uses the same settings. On a dedicated
server, change the settings in the server's
`config/compramos-by-naiko11.json` and restart it; a player's local settings
screen does not change a remote server's configuration. Install Mod Menu on
the client to access the settings screen.

Minecraft sound assets use OGG Vorbis. The source audio files are kept in
`src/main/resources/assets/compramos/`; registered OGG sound assets are stored
in `src/main/resources/assets/compramos-by-naiko11/sounds/`.

## Install on a server

The server must run Fabric for the same Minecraft version as the mod. Install
Java 25 or newer and Fabric API on both the server and each player's client.

1. Stop the server and back up the world.
2. Build the mod from the project directory:

   ```powershell
   .\gradlew.bat build
   ```

3. Upload `build/libs/compramos-by-naiko11-0.2.0.jar` and a matching Fabric API
   JAR to the server's `mods` folder. Create the folder if it does not exist.
4. Install the Compramos mod and Fabric API in each player's client `mods`
   folder too, so clients have the sound assets. Install Mod Menu on clients if
   they want to open the settings screen.
5. Start the server and connect with clients running the same Minecraft version.

The Wandering Trader plays sounds through the server, which sends them to nearby
players. A vanilla server cannot load this Fabric mod.
