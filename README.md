# 2005 Gruvbox Dark

GNASH0R's [2005](https://github.com/melkypie/resource-packs/tree/pack-2005) interface, recoloured to the [Gruvbox](https://github.com/morhetz/gruvbox) dark colour scheme for the RuneLite [Resource Packs](https://github.com/melkypie/resource-packs) plugin, with the chat parchment in Gruvbox light cream. Built for the Resizeable layout.

No sprite is redrawn. The 2005-specific art keeps its original shapes, shading and transparency and only the colours change, while the standard buttons and stones are the same recoloured vanilla sprites as the [Vanilla Gruvbox Dark pack](https://github.com/opher290/gruvbox-resource-pack), so the two sit side by side seamlessly. The compass dial keeps its original red-to-orange gradient in Gruvbox colours, and icons are left exactly as the original author drew them.

**Why is the chatbox light?** The chatbox is deliberately not dark. It follows the Gruvbox **light** theme, so the chat parchment uses the Gruvbox light grey-cream (`#bdae93`, fg3) and the dark chat text sits on it with good contrast, while the rest of the interface stays on the dark Gruvbox greys.

## Screenshots

![Skills tab in Resizeable mode](https://raw.githubusercontent.com/opher290/gruvbox-2005-resource-pack/main/screenshots/skills.webp)

![Settings and chatbox in Resizeable mode](https://raw.githubusercontent.com/opher290/gruvbox-2005-resource-pack/main/screenshots/settings.webp)

![Bank in Resizeable mode](https://raw.githubusercontent.com/opher290/gruvbox-2005-resource-pack/main/screenshots/bank.webp)

Screenshots are taken in RuneLite at a game size of 1376x905 with Stretched Mode set to 70%.
## Installation

Install the **Resource packs** plugin from the Plugin Hub, then install this pack from its hub. To use the 2005 interface style correctly, follow GNASH0R's setup from the original 2005 packs:

1. Go to RuneLite's plugins, enable **Interface Styles**, open its settings and set `Gameframe` to `2005`.
2. Go to the **Resource packs** plugin settings and tick the experimental setting at the bottom, `Don't change Interface Styles`.
3. Disable and re-enable the Resource packs plugin, and you're done.

If you want to use other packs afterwards, make sure to disable the **Interface Styles** plugin and disable/re-enable Resource packs for them to work correctly, unless their creators specify otherwise.

As the original notes, 2005 can't match the original exactly in Resizeable mode because of the plugin's limits.

### Recommended layout

Set **Game client layout** to **Resizable - Classic layout** (Settings > Display Settings). This pack is built for it.

- **Resizable - Modern layout** works, but it doesn't look its best. Interface Styles' 2005 gameframe and the 2005 sprites are placed for the classic layouts, so in Modern the tab icons don't sit centred on their stones.
- **Fixed - Classic layout** isn't supported yet. A Fixed - Classic version may come in the future.

## Chat colours

On the opaque chatbox this pack recolours the chat text to the Gruvbox palette, so it reads on the light Gruvbox parchment. Here is what changed, from the default colour to the one this pack uses:

| Chat text | Before (default) | Current |
| --- | --- | --- |
| **Public chat** | | |
| Public chat | `#0000FF` | `#254849` |
| Public chat highlight | `#000000` | `#1d2021` |
| **Private messages** | | |
| Sent private messages | `#7F0000` | `#772702` |
| Sent private messages highlight | `#002783` | `#304831` |
| Received private messages | `#7F0000` | `#772702` |
| Received private messages highlight | `#002783` | `#304831` |
| **Friends chat** | | |
| Friends chat info | `#000000` | `#1d2021` |
| Friends chat info highlight | `#FF0000` | `#8d0005` |
| Friends chat message | `#7F0000` | `#772702` |
| Friends chat message highlight | `#000000` | `#1d2021` |
| Friends chat channel name | `#0000FF` | `#254849` |
| **Clan chat** | | |
| Clan chat info | `#000000` | `#1d2021` |
| Clan chat info highlight | `#FF0000` | `#8d0005` |
| Clan chat message | `#7F0000` | `#772702` |
| Clan chat message highlight | `#000000` | `#1d2021` |
| Clan chat channel name | `#0000FF` | `#254849` |
| **Guest clan chat** | | |
| Guest clan chat info | `#000000` | `#1d2021` |
| Guest clan chat info highlight | `#FF0000` | `#8d0005` |
| Guest clan chat message | `#7F0000` | `#772702` |
| Guest clan chat message highlight | `#000000` | `#1d2021` |
| Guest clan chat channel name | `#0000FF` | `#254849` |
| **Game and system messages** | | |
| Server message | `#000000` | `#1d2021` |
| Game message | `#000000` | `#1d2021` |
| Game message highlight | `#EF1020` | `#8f3f71` |
| Examine | `#000000` | `#1d2021` |
| Examine highlight | `#0000FF` | `#254849` |

Everything else keeps its default colour.

### Colours the pack cannot change

A pack can only set the slots in the table above. These few chat colours come from the game or from your own settings, so you can change them yourself to match this pack:

| Chat text | Where its colour comes from | Suggested to match this pack |
| --- | --- | --- |
| Broadcasts (server and world announcements) | Your own in-game chat colour settings | `#772702` (the message red) |
| Challenge requests (duel and challenge invitations) | Your own in-game chat colour settings | `#772702` (the message red) |
| What you type in the chat box | The game: blue on the opaque chatbox, light blue on the transparent one | `#254849` (the pack's blue) |
| Coloured words inside game messages (the red and green lines, cyan numbers) | The game draws them itself | Not changeable; only the highlight slots above affect words the game marks as highlighted |

Clan broadcasts, group ironman chat and broadcasts, and moderator chat have no colour of their own: they reuse the clan chat, friends chat and public chat slots, so they already follow this pack's colours. Usernames, the filtered, autochat and trade lines, and the **transparent** chatbox are left at their defaults.
### Turning the chat colours off

If you prefer your own chat colours, untick **Allow chat colors to be changed** in the Resource packs plugin settings (under *Configurable Overrides*):

![The Allow chat colors to be changed setting, unticked](https://raw.githubusercontent.com/opher290/gruvbox-2005-resource-pack/main/screenshots/allow-chat-colors.png)

## What changes

| Area | Source |
| --- | --- |
| Buttons, tab stones, bank, scrollbars, checkboxes and sliders, GE, equipment slots, dialog panels | Taken as-is from the [Vanilla Gruvbox Dark pack](https://github.com/opher290/gruvbox-resource-pack), so they match it exactly |
| Side panel, tab rows, minimap frame, window edges, the black steel dialog borders, chat buttons | The original 2005 art recoloured to Gruvbox dark greys (`#141617` through `#282828` to `#a89984`), darkened to the Vanilla Gruvbox Dark pack's tone |
| Chat parchment | Lifted onto the Gruvbox grey-cream `#bdae93` (fg3), keeping the original shading and torn edges, with its texture kept in the middle and flattened only near the torn edge, and the dark outline of the tear blended almost fully into the cream so the edge is seamless |
| Compass dial | Keeps the original smooth gradient, a deep red rim fading through Gruvbox red and orange (`#c2411a`, `#d65d0e`) to a bright orange `#fe8019` centre |
| Overlay, borders and separators | The Vanilla Gruvbox Dark pack's `vars.toml` and `overrides.toml`, unchanged |
| Selected tab stones | GNASH0R's own chamfered selected stones, tinted to Gruvbox brick red #8f3f2a and keeping their shape, shading and outline |
| Emote plates, and the grey squares behind the equipment stats, prices, items-lost, call-follower, sailing options and combat options icons | The original grey-olive plates moved onto the Gruvbox greys; the yellow and green labels and the icon art stay as drawn |
| Bank PIN screen | The red marble buttons tinted to brick red `#8f3f2a`, keeping the bevel and texture; the olive marble behind Exit / I don't know it moved onto the Gruvbox greys |
| Make-all panel (What would you like to make?) | The item buttons get a face `#ab9c85`, a shade darker than the chat parchment so they stand out, and the selected item and quantity buttons are brick red `#8f3f2a` like the selected tab stones, drawn a little see-through so the grain shows faintly; the frames are the recoloured vanilla ones |
| Bank Deposit inventory, equipment, looting bag and Worn items buttons | The [2005 Overhaul](https://github.com/melkypie/resource-packs/tree/pack-2005-overhaul) versions by Demonseance, as drawn; the inventory bag only gains the black outline every other icon has |
| Seven tab icons: Chat-channel, Clan, Guest clan, the three group ironman tabs and Red minigames | Blank in the original; taken from the [2005 Overhaul](https://github.com/melkypie/resource-packs/tree/pack-2005-overhaul) pack by Demonseance, as drawn; only the Chat-channel icon is moved 5px right and 2px down to sit in its corner stone |
| Tab icons | The [2005 Overhaul](https://github.com/melkypie/resource-packs/tree/pack-2005-overhaul) icon art by Demonseance, each placed exactly where GNASH0R's icon sits so the resizable layout stays aligned, with the 2005 shadow formula, a solid black shadow two pixels deep down and to the right, added to the prayer star and the sailing anchor, a one pixel version on the combat swords (with no stray dots) and a single soft row on the music harp; the other icons, including Skills, Inventory, Equipment, Emotes and Account management, are left as drawn. The music harp is moved one pixel up and two left to sit in the brick red corner stone, and the Sailing anchor stays GNASH0R's, moved two pixels up into its stone (the Overhaul helm looked rough scaled down) |
| Other icons and quest art | Left exactly as the original author drew them |

## Related packs

- [Vanilla Gruvbox Dark](https://github.com/opher290/gruvbox-resource-pack): the vanilla-interface version of this palette.
- [2005 Noctis Bordo](https://github.com/opher290/bordo-2005-resource-pack): the same 2005 approach on a rose-tinted palette.

## Building it yourself

`tools/build.ps1` regenerates the pack from a checkout of the original (`pack-2005`):

- `recolor.ps1 -Scheme gruvbox` recolours the shell sprites onto the Gruvbox ramp (`schemes.ps1`). The 2005 stone is about twice as bright as vanilla's, so with `-RefSrc` (the vanilla sample pack, passed as `-Vanilla` to `build.ps1`) each sprite is darkened to its vanilla counterpart's tone. `-GruvboxPack` is a checkout of the main Gruvbox pack: its buttons, stones, bank, scrollbars, options, GE, equipment and dialog sprites are used as-is, and it fills anything the original leaves out.
- `tonematch.ps1` nudges the recoloured frames onto the Vanilla Gruvbox Dark pack's exact tone, `compass.ps1` recolours the compass dial, `chatedge.ps1` moves the chat parchment's burnt edge onto the Gruvbox ramp, `chatfill.ps1` lifts the parchment onto the Gruvbox light cream and softens the tear outline, `emote.ps1` and `iconbg.ps1` move the emote and icon-button plates onto the ramp, `pin.ps1` tints the bank PIN screen, `makeall.ps1` gives the Make-all item buttons a face and a brick red selected state, `shadow.ps1` evens out the tab icon drop shadows, `stone.ps1` tints the selected-tab stones to Gruvbox brick red, and `icon.ps1` builds `icon.png`.

## Credits

All credit for the resizable interface goes to **GNASH0R**, whose [2005](https://github.com/melkypie/resource-packs/tree/pack-2005) pack provides the interface art, the layout (the tab stones, the frames and where every icon sits in them) and the original icons, to **Demonseance** for the tab and bank button icon artwork in the [2005 Overhaul](https://github.com/melkypie/resource-packs/tree/pack-2005-overhaul) pack, and to the [Resource Packs](https://github.com/melkypie/resource-packs) plugin by melkypie. This pack only recolours (filters) that work and applies a few minor fixes; no sprite is redrawn.

- **GNASH0R** for the [2005 pack](https://github.com/melkypie/resource-packs/tree/pack-2005) this is based on: the Resizeable interface art, tab stones, frames and layout, which this pack is built for, and the icon positions every other icon is placed to match.
- **Demonseance** for the [2005 Overhaul](https://github.com/melkypie/resource-packs/tree/pack-2005-overhaul) pack and its icon artwork: the tab icons (including the seven the original leaves blank: Chat-channel, Clan, Guest clan, the three group ironman tabs and Red minigames) and the bank Deposit inventory, Deposit equipment, Deposit looting bag and Worn items buttons. Each Overhaul icon is placed where GNASH0R's icon sits, so the layout GNASH0R built is kept.
- **melkypie** for the [Resource Packs](https://github.com/melkypie/resource-packs) plugin and the vanilla sample pack.
- [Gruvbox](https://github.com/morhetz/gruvbox) by Pavel Pertsev (morhetz) for the colour scheme.
