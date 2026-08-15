# BossBar Hider (Fabric, MC 26.2)

Client-side mod that automatically hides BossBars matching a specific given title.
No server-side installation required - the bar is simply not rendered locally on your client.

## Compatibility Notes

- ModMenu https://modrinth.com/mod/modmenu
- Cloth Config API https://modrinth.com/mod/cloth-config

## Known Limitation

If a server updates a BossBar's title retroactively via an update packet (e.g., changing it from a visible title to one you have set to hidden), it will only be detected on the next rendered frame. However, since frames render multiple times per second, this is not noticeable in practice.
