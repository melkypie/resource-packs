## Change log

### 1.16.0
- Use a toml format for overrides
- Add opacity support
- Add support for changing types
- Update stale sprite id's
- Add the following overrides:
  - friends chat setup
  - chat channel border
  - cox party
  - cox overlay
  - bh overlay
  - lms kda overlay
  - lms settings
  - lms shop
  - pvp arena kits
  - pvp arena
  - soul wars shop
  - looting bag
  - combat achievements
  - collection log
  - tob party menu
  - toa party menu
  - cox party menu
  - forestry shop
  - forestry kit
  - colosseum popup
  - poh build room
  - portal nexus
  - lectern popup
  - jewellery box
  - costumes popup
  - rune pouch
  - bonds pouch
  - incinerator popup
  - skill guide
  - world_switcher_beta_flag
  - ge inventory glow
  - filled ge offers

### 1.15.2
- Update GE child id's
- Add sprites
- Support overrides that use the same id's

### 1.15.0
- Use profile specific folders for packs

### 1.14.11
- Fix changing profiles
- Reset cross sprites properly
- Fix original height

### 1.4.9
- Fix FlatLaf compatibility

### 1.4.8
- Add the unranked group ironman sprites
- Add the Dinh's bulwark pummel combat style sprite

### 1.4.7
- Add the resized spell sprites
- Add the party emote
- Add the World switch speedrunning start
- Fix make all background where there are more items in make-all window than I previously had considered
- Fix the config name for Interace Styles conflict option

### 1.4.6.1
- Change the Interface Style gameframe prompt to a chat message

### 1.4.6
- Add a prompt to set Interface Styles gameframe option to Default if it is not set to Default
- Add option to hide the sidepanel icon. Thanks to [m0bilebtw](https://github.com/melkypie/resource-packs/pull/120)

### 1.4.5
- Add the Giants foundry button borders
- Add the activity adviser minimap orb and the icon
- Add hitpoints low life minimap orb icon
- Add Greater and Lesser corruption prayer minimap orb icons
- Fix the Grand Exchange offer completion borders (Note: This requires pack owners to update ge/border_offer_right.png)

### 1.4.4
- Add the new group ironman sprites
- Add minimap orb frame flashing (seems to only be used in tutorial island)
- Add store button sprites
- Minigame sprites are now the same as grouping sprites (after Jagex's rename of the minigame tab)
- Updated some sprites to match vanilla sprites

More info on sprites added can be seen [here](https://github.com/melkypie/resource-packs/compare/e1f8ae705d53623dc0162625916e4f8592dfd1f7..3398ecae733f74d47c36a07c46fcf7fd7f939392)

### 1.4.3
- Add new folder combat_achievements
- Add dialog/progress_bar_grey and green (used in combat_achievements dialog currently)
- Add tab/side_bar_* corners and tab/combat_achievements
- Add quests_tab/combat_achievements_* icons in combat_achievements tab
- Add other/plus* and other/minus*
- Add button/menu*
- Fix xp tracker border coloring

More info on sprites added can be seen [here](https://github.com/melkypie/resource-packs/compare/4df6e8ee1b4bc8f9691fdafc002257c824ec273d..e1f8ae705d53623dc0162625916e4f8592dfd1f7)

### 1.4.2
- Add relic unlock trailblazer emote
- Correct the ids of the arceuus salve graveyard teleport sprites
- Potentially fix a bug where if the selected pack is being updated on the startup, the hub panel would stop loading, and some sprites may not have loaded

More info on sprites added/changed can be seen [here](https://github.com/melkypie/resource-packs/commit/d085fbe1a96e41b20665ef51e71c470ed8a1c788)

### 1.4.1
- Add support for new arceuus spells
- Add spell requirement locks
- Fix some bugs with cross sprites

More info on sprites/colors added can be seen [here](https://github.com/melkypie/resource-packs/commit/50f02b1607913a8afaf1c28fd34ac317c18e395c)

### 1.4.0
- Add support for changing cross sprites (left click/mouse click sprites)
- Add a new left arrow button (seems to only be used in ToB party interface)
- Add missing options display icon and account management icons for F2P

More info on sprites/colors added can be seen [here](https://github.com/melkypie/resource-packs/commit/4ce88aedd864166455f4784556d404ccd3cf51bb)

### 1.3.4
- Add the Settings window keybinding channel icon
- Allow to recolor settings tabs, channel tabs and kourend favour seperator lines
- Fix the chatbox seperator line not being recolored
- Fix the plugin being shut down not setting the colors back to vanilla

More info on sprites/colors added can be seen [here](https://github.com/melkypie/resource-packs/commit/fb85fdbe6acff0a8b384c68782928fb550c52d7f)

### 1.3.3
- Changes the chatbox and report buttons to match with latest osrs update (the sprites luckily don't need to be updated by pack makers and only report button size has changed)
- Add the new tab clan_channel and guest_clan_channel sprites
- Add a new folder clans_tab
- Some sprites have been cleaned up by Jagex so those have been updated accordingly in sample pack

More info on sprites added can be seen [here](https://github.com/melkypie/resource-packs/compare/f7238b1a85c0...17583c24df5c)

### 1.3.2.1
- Remove the previously added special attack bar recoloring due to it being a conditional recolor based on if you have enough special attack

More info can be found here [here](https://github.com/melkypie/resource-packs/commit/f7238b1a85c0a4d5b42b4dd454bc6d5faeda084b)

### 1.3.2
- Quite a lot of changes to arceuus spelbook due to the rework
- Added the ge cancel offer button
- Added the special attack bar coloring
- Added a discord invite link to the resource packs hub panel
- Some sprites had been changed by Jagex at some point, the changes are pretty minor.

More info on sprites added can be seen [here](https://github.com/melkypie/resource-packs/commit/81b665d438d7835a24563572e4f9a7edf84953c4)

### 1.3.0
- Added medium tabs in the bond pouch interface
- Added the side tabs in the new options interface
- Added the new options checkboxes, sliders and the audio tab icon
- Added a test which allows for generation of sample-vanilla automatically from a sprite dump
  - Due to this the sample-vanilla pack has changed a bit (quite a bit of dimensions have been corrected). These changes won't affect existing packs but I suggest you look through the list which can be found [here](https://github.com/melkypie/resource-packs/commit/4d5639e2a3b7dc853f6499e9aba64b7a9f27da5c)

More info on sprites added can be seen [here](https://github.com/melkypie/resource-packs/commit/294f9d6f613300f82bc282a9f5ce30a904887f94)

### 1.2.1.1
- Added a new sprite DIALOG_IRON_RIVETS_BOTTOM which is the same sprite as DIALOG_IRON_RIVETS_HORIZONTAL (pack creators beware due to the addition of the new sprite both of the sprites have changed a bit. All hub packs will be updated)

### 1.2.1
- Fixes the login screen and overlay not toggling when switching the hub option to None
- Adds a new color option `make_all_background_clicked`
- Fixes not being able to recolor the xpgoals overlay goal border.
- Rename Advanced options to Experimental options and added a toggle to enable color current pack option and toggle to disable overlay recoloring

More info on sprites added can be seen [here](https://github.com/melkypie/resource-packs/commit/1417a71e39463d3b46cb8f042882aa5a5c335710)

### 1.2.0
- Add ability to change colors

More info on colors that can be changed [here](https://github.com/melkypie/resource-packs/commit/1a39e794634072fa1ff3ff9239223527782ecdbd)

### 1.1.5
- Add a Advanced options section which includes the ability to color the current pack
- More new sprites

More info on sprites added can be seen [here](https://github.com/melkypie/resource-packs/commit/1417a71e39463d3b46cb8f042882aa5a5c335710)

### 1.1.4
- Added the ability to customize spells/prayers/emotes
- Added transparent chatbox scrollbars and house viewer refresh icon


More info on the exact changes made can be seen [here](https://github.com/melkypie/resource-packs/commit/27140f067e6cc42b2634962b3f3ad87521e99061)

### 1.1.3
- Add the widely request quest tab sprites along with some other sprites
- The plugin-hub and the resource hub icons have been changed to rainbow paint brushes (All credits go to [psyda](https://github.com/melkypie/resource-packs/commits?author=Psyda) for creating them)


More info on the exact sprites added can be found [here](https://github.com/melkypie/resource-packs/compare/16a3a76f7cbeeab8152eb91062e13d46cefbfdf7...b1184a15ebfdde33909e278eb43608adc30a113c)

### 1.1.2
- Add option to allow resource packs to take over login screen background (on by default, will save your configuration)
- Add option to allow resource packs to take over your overlay background color (on by default, will save your configuration)

### 1.1.1
- Optimize the sprite loading
- Over 200 new sprites were added. What was changed can be found [here](https://github.com/melkypie/resource-packs/compare/95f1adeae29701e12b66c2644c10c2d821d8eeff...8038e1340344926832fa0c3a5f7917fd4691ee3e) 

### 1.1
- Add resource pack hub

### 1.0.5
- Add for `BUTTON` - `TUTORIAL`, `TUTORIAL_HOVERED`, `SLAYER_REWARDS_AND_POLL_HISTORY`, `SLAYER_REWARDS_AND_POLL_HISTORY_SELECTED`
- Add for `DIALOG` - `BOTTOM_LINE_MODE_SIDE_PANEL_INTERSECTION_TOP`, `BOTTOM_LINE_MODE_SIDE_PANEL_INTERSECTION_BOTTOM` (Also fixed the vanilla pack to have proper naming for `bottom_line_mode_window_close_small` and `bottom_line_mode_window_close_small_hovered`)
- Add for `BANK` - `TAB_EMPTY`, `TAB_ADD_ICON`, `TAB_ALL_ITEMS_ICON`, `TAG_TAB`, `TAG_TAB_ACTIVE`, `TAG_UP_ARROW`, `TAG_DOWN_ARROW`, `TAG_NEW_TAB`
- Add for `OPTIONS` - `ROUND_CHECK_BOX`, `ROUND_CHECK_BOX_CHECKED`, `ROUND_CHECK_BOX_CROSSED`
- Create a new folder `GE`

Exact changes can be found [here](https://github.com/melkypie/resource-packs/commit/e29edab9d22ed59918d73d21ab864c86f1428d2d)
Exact changes made to vanilla pack [here](https://github.com/melkypie/resource-packs/compare/506f8fd559a1cb8b058939c4722837072b9f3c63...95f1adeae29701e12b66c2644c10c2d821d8eeff)

### 1.0.4
- Add for `DIALOG` - `BACKGROUND_BRIGHTER`
- Add for `EQUIPMENT` - `SLOT_HEAD`, `SLOT_CAPE`, `SLOT_NECK`, `SLOT_WEAPON`, `SLOT_RING`, `SLOT_TORSO`, `SLOT_SHIELD`, `SLOT_LEGS`, `SLOT_HANDS`, `SLOT_FEET`, `SLOT_AMMUNITION`
- Create a new folder `OPTIONS`

### 1.0.3
- Add the ability to switch between 3 resource packs
- Add for `BUTTON` - `BOTTOM_LINE_MODE_WINDOW_CLOSE_SMALL`, `BOTTOM_LINE_MODE_WINDOW_CLOSE_SMALL_HOVERED`
- Add for `OTHER` - `WINDOW_CLOSE_BUTTON`, `WINDOW_CLOSE_BUTTON_HOVERED`, `WINDOW_FRAME_EDGE_LEFT`
- Created a new folder `DIALOG` with usable textures
- Created a new folder `BANK` with usable textures

Exact changes can be found [here](https://github.com/melkypie/resource-packs/compare/7c6b300e9a8a5e309740feb1d63b6164208938e9...256b3f9f9bc409f133905f5a74130aae8f51dfaa)

### 1.0.2
- Add sprites for stats - tile_half_left_black, tile_half_right_black
- Add a sprite for resizeable mode - side_panel_background

### 1.0.1
- Added more sprites that can be used
- Move chatbox.png to /chatbox and rename it to background.png

### 1.0
- Initial release