# Resource packs 
[![Active Installs](http://img.shields.io/endpoint?url=https://i.pluginhub.info/shields/installs/plugin/resource-packs)](https://runelite.net/plugin-hub/melky) [![Plugin Rank](http://img.shields.io/endpoint?url=https://i.pluginhub.info/shields/rank/plugin/resource-packs)](https://runelite.net/plugin-hub)

[![Discord](https://user-images.githubusercontent.com/5113962/116616080-e2a0ee80-a944-11eb-8c1f-b838233b29d5.png)](https://discord.com/invite/DsDhUz4NNN)

Allows you to create your own custom interface styles by not being only restricted to 2005/2006/2010 ones.

## How to use it:
#### **New method:**
1. Add the plugin from the runelite [plugin-hub](https://github.com/runelite/runelite/wiki/Information-about-the-Plugin-Hub)
2. Look for this icon in the side panel: ![image](https://i.imgur.com/gpg7VJK.png)
3. To install one of the packs, click the green **Install** button  ![image](https://user-images.githubusercontent.com/5113962/85605474-dbd98900-b65a-11ea-9f32-e16a9aca9f75.png)
4. The pack will automatically install and be applied.
5. If you want to switch between the installed packs use the selected pack dropdown menu: ![image](https://user-images.githubusercontent.com/5113962/85605673-0f1c1800-b65b-11ea-96aa-c7682c92cda6.png)
6. You can easily remove a pack by pressing the red Remove button.
7. Make sure your interface styles plugin gameframe is set to the DEFAULT skin (it can cause weird issues otherwise). Everything else is fine but will require a restart of the plugin should you activate any of them after you have resource pack set up.

The installed packs are automatically updated when starting the client or you can press the update button in the panel to update that pack.

#### Old method
(For people who want to have a custom made pack and not the ones from the hub)
1. Add it from the runelite [plugin-hub](https://github.com/runelite/runelite/wiki/Information-about-the-Plugin-Hub)
2. Download a resource pack from down below or somewhere else
3. Extract the .zip file if you downloaded from below
4. Set the path to where your resource pack is stored in (without the trailing /)  
ex. C:\Users\user\Downloads\resource-packs-pack-osrs-dark (Windows) where user is your Windows username  
![image](https://i.imgur.com/JRuVXFQ.png)  
/Users/user/Downloads/resource-packs-pack-osrs-dark (Mac) - where user is your Mac username  
[How to find your file path on Mac](https://www.maketecheasier.com/reveal-path-file-mac/)  
(It should be the path to where you can see all the folders - button, bank, chatbox, etc.)
5. Make sure your interface styles plugin gameframe is set to the DEFAULT skin (it can cause weird issues otherwise). Everything else is fine but will require a restart of the plugin should you activate any of them after you have resource pack set up.
6. After you have the path copy and paste it into where the image below has it in. Make sure there is no \ (Windows) / (Mac) at the end of it.  
![image](https://user-images.githubusercontent.com/5113962/84203078-7e1d3c80-aab1-11ea-9c75-e684c91730b1.png)
6. Restart the plugin and your resource pack should load in

## How to create a resource pack
1. Download one of the sample packs or resource packs
2. Edit the images while keeping the image sizes and folder structure the same  
**DO NOT PUT OR EDIT YOUR RESOURCE PACK INSIDE .runelite/resource-packs-repository OR IT WILL GET DELETED.**
3. If you want your resource pack to be published on here among the other resource packs and shown inside the resource pack hub, Contact me on discord (You can find me by the same in Runelite discord) and I will explain how.


## Available resource packs in the hub can be found [in the wiki](https://github.com/melkypie/resource-packs/wiki/Resource-packs-hub)

## Sample packs

- [Vanilla](https://github.com/melkypie/resource-packs/tree/sample-vanilla) ([Download](https://github.com/melkypie/resource-packs/archive/sample-vanilla.zip)) (Includes regular runescape sprites)

<img src="https://user-images.githubusercontent.com/5113962/82244509-02b0eb00-994a-11ea-8343-0a7dd7ddaa82.png" width="765"><br/>

## Change log

### 1.3.2
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
