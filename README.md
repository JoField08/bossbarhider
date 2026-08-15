# BossBar Hider (Fabric, MC 26.2)

Clientseitige Mod, die BossBars mit einem bestimmten Titel ausblendet.
Server-seitig ist nichts erforderlich - die Bar wird nur bei dir lokal nicht gezeichnet.

## Wichtiger Hinweis zur Kompatibilitaet

MC 26.2 liegt nach meinem Wissensstand und hat in Version 26.1/26.2 einen
groesseren internen Umbau erfahren (Gui/Hud wurden aufgeteilt, es werden jetzt
offizielle Mojang-Mappings statt Yarn benutzt, kein Remapping mehr noetig).
Ich konnte deshalb NICHT zu 100% verifizieren, ob die Klasse
`net.minecraft.client.gui.components.BossHealthOverlay` und das Feld
`events` in 26.2 noch exakt so heissen.

**Bevor du baust, bitte kurz pruefen (dauert ~5 Minuten, da die Mappings
jetzt offiziell/unobfuskiert sind):**

1. `./gradlew genSources` ausfuehren (oder in IntelliJ: "Generate Minecraft Sources").
2. Im generierten Quellcode nach `BossHealthOverlay` suchen (Umbenennung
   moeglich, z.B. in Richtung `Hud`/`BossOverlay`).
3. Darin die `render(...)`-Methode und das Feld vom Typ
   `Map<UUID, ...BossEvent...>` (im Original `events` genannt) suchen.
4. Falls Namen abweichen, in genau zwei Dateien anpassen:
   - `src/main/kotlin/de/jo_field/bossbarhider/mixin/BossHealthOverlayAccessor.java`
   - `src/main/kotlin/de/jo_field/bossbarhider/mixin/BossHealthOverlayMixin.java`
     (Methoden-Deskriptor in den beiden `@Inject(method = "...")`-Zeilen)

Der Rest der Mod (Config, Matching-Logik, Mod-Metadaten) ist unabhaengig
von diesen internen Namen und funktioniert unveraendert.

## Konfiguration

Beim ersten Start wird `config/bossbarhider.json` erzeugt:

```json
{
  "hiddenTitles": ["Beispiel Titel"],
  "matchContains": true,
  "ignoreCase": true
}
```

- `hiddenTitles`: Liste der Titel (oder Titel-Teilstrings), die ausgeblendet werden.
- `matchContains`: `true` = Titel muss den String nur enthalten, `false` = exakte Uebereinstimmung.
- `ignoreCase`: Gross-/Kleinschreibung ignorieren.

Trag hier einfach den gewuenschten Titel ein und starte das Spiel neu
(oder baue spaeter einen `/reload`-Command dafuer ein, falls gewuenscht).

## Bauen

```bash
./gradlew build
```

Das fertige Jar liegt danach in `build/libs/`. Fabric API (Mod-Dependency)
muss zusaetzlich im `mods`-Ordner liegen.

## Bekannte Einschraenkung

Aendert ein Server den Titel einer BossBar nachtraeglich per
Update-Paket (z.B. von "sichtbar" zu deinem versteckten Titel), wird das
erst beim naechsten gerenderten Frame erkannt - das passiert aber ohnehin
mehrfach pro Sekunde und ist in der Praxis nicht spuerbar.
