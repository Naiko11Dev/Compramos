# Compramos by Naiko11

Bring the Compramos meme to Minecraft! Let its familiar sounds catch you off
guard, turn ordinary moments into hilarious surprises, and make your world a
little more unpredictable. Bring a bit more fun into your minecraft world. Enjoy!

## Features

<details>
<summary>Spoilers: special events</summary>

- Wandering Traders randomly play one of two Compramos sounds about every
  60 to 120 seconds.
- Players in dark caves can hear quiet Compramos ambience and see mystical particles roughly every two
  hours. Each cave sound has a 20% chance to spawn a Wandering Trader nearby
  inside the cave, provided a safe spot is available. Spawning can be disabled, and the chance is
  configurable.
- A Montoya sound plays when a Wandering Trader despawns.
- A Terreneitor sound plays when a player mounts a donkey.

</details>

## Installation

Requires Minecraft Java Edition 26.3, Java 25 or newer, Fabric Loader 0.20.1
or newer, and Fabric API for Minecraft 26.3.
Mod Menu is optional and is only needed to open the in-game settings screen.

### Client

Install Compramos through a launcher that supports Fabric, such as the
Modrinth App or CurseForge App, or manually copy the Compramos JAR and
matching Fabric API JAR into the Minecraft instance's `mods` folder. Use a
Fabric profile for Minecraft 26.3.

### Server

For multiplayer, install Compramos and Fabric API on the Fabric server through
its launcher or hosting panel, or manually copy both JARs into the server's
`mods` folder. Every connecting client must also have Compramos and Fabric API
installed to load the sound assets.

## Settings

| Setting | What you can change |
| --- | --- |
| Wandering Trader sounds | Toggle them and adjust how often they play. |
| Cave ambience | Toggle it and adjust how often it plays. |
| Trader spawning | Enable or disable it; set the chance (20% by default). |
| Event sounds | Toggle Montoya on despawn and Terreneitor on donkey mount. |
| Cave particles | Toggle the particles that accompany cave sounds. |
| Sound volume | Adjust the volume of Compramos sounds. |

Without Mod Menu, edit `config/compramos-by-naiko11.json` in the Minecraft
instance's `config` folder. On a dedicated server, edit the file in the
server's `config` folder and restart the server. A client's local settings do
not change a dedicated server's configuration.

## Commands

| Command | Where / permission | Description |
| --- | --- | --- |
| `/compramos help` | Server; everyone | Lists the available commands. |
| `/compramos play <player> [full\|simple\|montoya\|terreneitor]` | Server; operator level 2 | Plays a sound only for the selected player. Without a sound name, it randomly plays one of the two main sounds. |
| `/compramos trader spawn [player]` | Server; operator level 2 | Spawns a Wandering Trader near the command source or selected player. In a dark cave, it searches for a safe underground spawn location. |
| `/compramos test full` | Client only | Plays the full sound locally. |
| `/compramos test simple` | Client only | Plays the simple sound locally. |
| `/compramos test montoya` | Client only | Plays the Montoya sound locally. |
| `/compramos test terreneitor` | Client only | Plays the Terreneitor sound locally. |

## License

The Java source code is licensed under GPL-3.0-only. The audio files are not
covered by that license.
