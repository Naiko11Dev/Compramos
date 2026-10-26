# Compramos

Fabric-mod-Grundgeruest fuer Minecraft Java 26.3.

## Voraussetzungen

- JDK 25
- IntelliJ IDEA oder eine andere Java-IDE mit Gradle-Unterstuetzung

## Bauen und starten

Unter Windows:

```powershell
.\gradlew.bat build
.\gradlew.bat runClient
```

Das gebaute Mod-JAR liegt danach unter `build/libs/`.

## Projektaufbau

- `src/main/java`: gemeinsame Mod-Initialisierung und spaetere Spiellogik
- `src/client/java`: clientseitige Initialisierung und spaetere Client-Features
- `src/main/resources/fabric.mod.json`: Mod-Metadaten und Fabric-Einstiegspunkte
- `src/main/resources/assets/compramos`: spaetere Texturen, Sounds und Sprachdateien

Der Compramos-Sound und die Wandering-Trader-Mechanik sind noch nicht implementiert.
Der Sound soll spaeter als eigene, rechtmaessig nutzbare OGG-Datei unter
`assets/compramos/sounds/meme/compramos.ogg` liegen und in `sounds.json`
registriert werden. Die zugehoerige Spiellogik kommt in ein eigenes Paket, sobald
das erste Feature umgesetzt wird.
