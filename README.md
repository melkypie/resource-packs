# Gruvbox 2005

[2005](https://github.com/melkypie/resource-packs/tree/pack-2005) by **GNASH0R**, recoloured to the [Gruvbox](https://github.com/morhetz/gruvbox) dark colour scheme for the RuneLite [Resource Packs](https://github.com/melkypie/resource-packs) plugin.

No sprite is redrawn. The 2005-specific art keeps its original shapes, shading and transparency and only the colours change, while the standard buttons and stones are the same recoloured vanilla sprites as the main Gruvbox pack. The compass dial keeps its red-and-orange look in Gruvbox orange, and icons are left exactly as the original author drew them.

## Screenshots

![Bank and chatbox in Resizeable mode](screenshots/bank.webp)

## Installation

Install it from the Resource packs hub in RuneLite. As the original notes, 2005 can't match the original exactly in Resizeable mode because of the plugin's limits.

## What changes

| Area | Source |
| --- | --- |
| Buttons, tab stones, bank, scrollbars, checkboxes and sliders, GE, equipment slots, dialog panels | Taken as-is from the [main Gruvbox pack](https://github.com/opher290/gruvbox-resource-pack), so they match it exactly |
| Side panel, tab rows, minimap frame, window edges, the black steel dialog borders, chat buttons | The original 2005 art recoloured to Gruvbox dark greys (`#141617` through `#282828` to `#a89984`), darkened to the main pack's tone |
| Chat parchment | Gruvbox grey tan `#a89984` to `#bdae93`, keeping the original shading and burnt edges |
| Compass dial | Keeps its red-and-orange look, moved onto Gruvbox red #cc241d and orange #fe8019 |
| Icons, emotes, prayer and quest art, selected tabs | Left exactly as the original author drew them |

## Building it yourself

`tools/build.ps1` regenerates the pack from a checkout of the original (`pack-2005`):

- `recolor.ps1 -Scheme gruvbox` recolours the shell sprites onto the Gruvbox ramp (`schemes.ps1`). The 2005 stone is about twice as bright as vanilla's, so with `-RefSrc` (the vanilla sample pack, passed as `-Vanilla` to `build.ps1`) each sprite is darkened to its vanilla counterpart's tone.
- `icon.ps1` builds `icon.png`.

## Credits

- **GNASH0R** for the 2005 pack this is based on.
- [Gruvbox](https://github.com/morhetz/gruvbox) by Pavel Pertsev (morhetz) for the colour scheme.
- [melkypie](https://github.com/melkypie/resource-packs) for the Resource Packs plugin.