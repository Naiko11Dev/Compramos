# Compramos by Naiko11

Bring the Compramos sound meme into Minecraft with this mod. Wandering
Traders can play Compramos sounds, while cave ambience and special events add
more unexpected moments.

## Features

- Wandering Traders randomly play one of two Compramos sounds about every
  60 to 120 seconds.
- Players in dark caves can hear quiet Compramos ambience roughly every two
  hours. Each cave sound has a 20% chance to spawn a Wandering Trader nearby
  inside the cave, provided a safe spot is available. Trader sounds do not
  trigger additional spawns.
- A Montoya sound plays when a Wandering Trader despawns.
- A Terreneitor sound plays when a player mounts a donkey.
- Optional Mod Menu settings let you toggle sound features and particles,
  adjust sound frequencies and spawn chance, and set the sound volume.

## Commands

`/compramos help` lists the available commands and is available to everyone.

The following server commands require operator permission level 2:

- `/compramos play <player> [full|simple|montoya|terreneitor]` plays a sound
  only for the selected player. Without a sound name, it randomly plays the
  full or simple Compramos sound.
- `/compramos trader spawn [player]` spawns a Wandering Trader near the
  command source or selected player. In a dark cave, it searches for a safe
  underground spawn location.

These client-only commands play sounds locally and do not require operator
permissions:

- `/compramos test full`
- `/compramos test simple`
- `/compramos test montoya`
- `/compramos test terreneitor`

## Installation

Requires Minecraft Java Edition 26.3, Java 25 or newer, Fabric Loader 0.20.1
or newer, and Fabric API for Minecraft 26.3.

Install Compramos and Fabric API on the client to load the sound assets. To
use the mod on a multiplayer server, install it and Fabric API on the Fabric
server as well. Clients connecting to that server also need the mod.

Mod Menu is optional and is only needed to open the in-game settings screen.

## Settings

Without Mod Menu, edit `config/compramos-by-naiko11.json` in the Minecraft
instance's `config` folder. On a dedicated server, edit the file in the
server's `config` folder and restart the server. A client's local settings do
not change a dedicated server's configuration.

## License

The Java source code is licensed under GPL-3.0-only. The audio files are not
covered by that license.
