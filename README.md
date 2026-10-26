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

The full and simple Compramos sounds are registered as Minecraft sound events.
Run the client and enter `/compramos test full` or `/compramos test simple` to
test them. While a Wandering Trader is loaded in the world, it randomly chooses
one of the two sounds about every 60 to 120 seconds.

Minecraft sound assets use OGG Vorbis. Place the converted files at
`src/main/resources/assets/compramos-by-naiko11/sounds/full_compramos.ogg` and
`src/main/resources/assets/compramos-by-naiko11/sounds/simple_compramos.ogg`;
the original MP3 files can remain in `src/main/resources/assets/compramos/`.

## Install on a server

The server must run Fabric for the same Minecraft version as the mod. Install
Java 25 or newer and Fabric API on both the server and each player's client.

1. Stop the server and back up the world.
2. Build the mod from the project directory:

   ```powershell
   .\gradlew.bat build
   ```

3. Upload `build/libs/compramos-by-naiko11-0.1.0.jar` and a matching Fabric API
   JAR to the server's `mods` folder. Create the folder if it does not exist.
4. Install the Compramos mod and Fabric API in each player's client `mods`
   folder too, so clients have the sound assets.
5. Start the server and connect with clients running the same Minecraft version.

The Wandering Trader plays sounds through the server, which sends them to nearby
players. A vanilla server cannot load this Fabric mod.
