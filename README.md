# Noctis Bordo

A dark, rose-tinted resource pack for the RuneLite [Resource Packs](https://github.com/melkypie/resource-packs) plugin, based on the **Bordo** variant of the [Noctis](https://github.com/liviuschera/noctis) colour scheme, with the chat and dialog parchment in Noctis **Lux** creams.

No sprite is redrawn. Every texture keeps the vanilla shapes, shading and transparency, and only the colours change, so the pack stays easy to keep in step with game updates. Icons, items, spells, skills and other coloured art are left vanilla.

## Screenshots

![Bank and chatbox in Resizeable mode](screenshots/bank.webp)

![Settings and chatbox in Resizeable mode](screenshots/settings.webp)

## What changes

| Area | Colours |
| --- | --- |
| Stone shell, tabs, buttons, bank, GE | Bordo rose-greys, from `#1a1517` outlines through `#322a2d` and `#413036` to `#a8838f` highlights |
| Borders and separators | Bordo `#1f191b` (outer) to `#5c4a52` (inner) |
| Chatbox and dialog parchment | Lux creams `#e6d7b2` to `#fef8ec`; vanilla dark text stays readable |
| Chat frame | Bordo |
| Selected and accent highlights | Bordo pink `#f18eb0` |
| Overlay | Translucent `#322a2d` |

## Building it yourself

`tools/` holds the PowerShell scripts that generate the pack from the vanilla sample pack:

- `recolor.ps1 -Scheme bordo` recolours the shell sprites onto the Bordo ramp (`schemes.ps1`).
- `config.ps1 -Scheme bordo` writes `vars.toml`, `overrides.toml` and `color.properties`.
- `icon.ps1` builds `icon.png`.

## Credits

- [Noctis](https://github.com/liviuschera/noctis) by Liviu Schera for the Bordo and Lux colour schemes.
- [melkypie](https://github.com/melkypie/resource-packs) for the Resource Packs plugin and the vanilla sample pack.
- The [Nord](https://github.com/melkypie/resource-packs/tree/pack-nord) and [Browntown](https://github.com/melkypie/resource-packs/tree/pack-browntown) packs, which were the inspiration for recolouring the vanilla shell.
