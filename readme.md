# 2012 Interface
## Latest version: v4.8 (13-6-2022)

*If you have any questions or find any issues, please message Discord **@Shredit#6109** or **@Leyline#5438***

```diff
! This plugin can be installed automatically by using the in-game resource packs hub. Manually installing this pack however, will also allow you to customise your preferences such as the different prayer styles included in the pack. If you prefer using the vanilla spell/prayer icons, you can select so in the resource packs plugin settings.
```
## How to install
1. Install the resource packs plugin for RuneLite found in the [plugin-hub](https://github.com/runelite/runelite/wiki/Information-about-the-Plugin-Hub), or download it [manually](https://github.com/melkypie/resource-packs)
2. Extract the .zip file you downloaded to a suitable location ***(do not use %userprofile%/.runelite/resource-packs-repository)***
3. (RuneLite) Configuration > Resource packs > Resource pack path > Set the path to where your resource pack is stored in (without the trailing /) ![!image](https://i.imgur.com/K8iEzgt.png)
```diff
- Do not PUT or EDIT your pack in .runelite/resource-packs-repository OR IT WILL GET DELETED.
```
4. Make sure your interface styles plugin gameframe is set to the DEFAULT skin (it can cause weird issues otherwise). Everything else is fine but will require a restart of the plugin should you activate any of them after you have resource pack set up.
5. After you have the path copy and paste it into where the image below has it in. Make sure there is no \ (Windows) or / (Mac, Linux) at the end of it.
6. Restart the plugin or swap the 'Use resource pack' option to reload the pack
7. Enjoy!

## Previews
### Fixed:
![!image](https://i.imgur.com/IEZbhN4.png)
### Resizable:
![!image](https://i.imgur.com/gpilfbb.png)
### Resizable (modern):
![!image](https://i.imgur.com/1c1gL3e.png)

# Customizable XP drops settings
*This plugin can be found in the RuneLite plugin hub.*

![!image](https://i.imgur.com/X1L6Xrh.png)

![!image](https://i.imgur.com/7kcRA8T.png)

# Changelog:
## Issues:
```diff
- When you get poisoned or venomed, your HP icon will reset to vanilla sprites due to a conflict with the "Poison" plugin, to fix this. Simply turn off "Change HP Orb Icon" tab from the plugin.
```
## v4.8 (13-6-2022)
![!image](https://i.imgur.com/fcAXPmz.png)
- Removed an unnecessary file
- Updated the pack preview image with the updated skilling icons
- Updated interface previews
- Appended the customizable xp drop settings to the readme
  - This plugin now supports the resource packs plugin! Custom skilling icons will now show next to your XP drops provided the setting is enabled.
- Improved and upgraded the trade screen interface
  - Added texture to the background
  - Added rivets and changed some of its sprites to accomodate for its unusual style and indents
  
  *The trade screen was one of the last things that was on the list of things that still needed to be worked on (Sorry that it has taken this long!). It had been postponed for a variety of reasons, namely the way the 2012 rivets are indented and are notorious for causing all sorts of problems with the way the oldschool interface is assembled, as well as having to keep the trade window open (this interface has too many differences to take any other as a template).*
- Upgraded the GE interface windows
  - Main GE window
  - Collection box
  - Sell/buy
  - Colour alterations

  *Although the Grand Exchange interfaces did get an upgrade in our previous iterations, they were mostly based on the RS3 style. This is because this interface never got updated in 2012, so the sprites do not exist. If we were to take the 2012 GE interface, it would look more like the 2010 version. The screens have now been completely remade in the 2012 style with custom buttons.*

  *The colours have also been changed. Instead of the blue RS3 style, we now use the combination of white/silvery rivets, black, darkblue and bright orange. This creates a higher contrast which improves the visibility for colourblind people (and people that are not of course). Enjoy!*

## v4.7 (31-5-2022)
![!image](https://i.imgur.com/BMr5ons.gif)
- Restored the true 2012 skilling icons
  - *Some skilling icons will also look visibly different from the old ones, this is intended as they actually differ from the RS3 icons*
- Altered the skilling icons slightly to be compatible without alpha values
  - *Similarly to other alpha conversion fixes, this improves the quality, removes artifacts, or increases the contrast of the sprites. Notably the firemaking and prayer icons in this case*

## v4.6 (20-5-2022)
![!image](https://i.imgur.com/GCwSEeP.png)
- Upgraded a variety of checkboxes to the 2012 styles
- Improved the true colours of the quick-prayer selection checkboxes
- Slightly improved the side panel rivets
  - *Unfortunately due to how the old school sprites work it's not possible to remove the black behind the rivets entirely, since it's not actually part of the sprite*

## v4.5 (9-may-2022)
- Upgraded progress bars in the combat achievements interface
- Removed some random pixels on the disabled boost potion icon on the lunar spellbook
- The shading on the selected buttons in the combat options interface has been improved
- Updated the preview icon for the pack
- Removed unused images
- Updated compatible pack version

## v4.4 (5-may-2022)
![!image](https://i.imgur.com/KH4fmfq.png) ![!image](https://i.imgur.com/klVPALp.png)
- Improved all emotes except for the penguin suit
  - Upgraded the following (oldschool) specific emotes to custom ones
    - Hypermobile drinker
    - Sit-up
    - Push-up
    - Star jump
    - Jog
    - Premier shield
    - Relic unlock
    - Relic unlock trailblazer
    - Uri transform
  - Restored the following emotes
    - Crazy dance (Troubadour dance)
    - Flex (Muscle-man Pose)
    - Smooth dance
  - Improved 20 locked emote icons
    - Restored the real colours that were lost during alpha conversion
  - Inverted panic and cry icons

## v4.3 (4-May-2022)
![!image](https://i.imgur.com/5KILROG.png)
- Bank
  - Search icon has been moved slightly
  - Changed out the smaller magnifying glass for the GE and music player interface search functions
  - Improved the alias and colouring on the bank shuffle icon in the group ironman bank interface
- Several osrs buttons have been upgraded to the 2012 style
  - Several buttons throughout the interface such as equipment
  - Left/right arrows
  - Combat achievements interface buttons
- Inverted the shading on the 'thumbs up' and 'thumbs down' icons on the logout menu
  - It is now more clear when you hover over them
- Slightly improved the contrast on running icons
- Highly improved the alias quality of the prayer icon and resolved its transparency issues
  - Previously black pixels were visible due to 'glow' of the icon, which resulted in darkening during alpha conversion

## v4.2 (2-May-2022)
![!image](https://i.imgur.com/4dHmXQt.png)
- Restored HD cross sprites
- Settings sliders have been improved
  - Smoothened out shading
  - Restored the '+' caret on the right side of the sliders
- The bond pouch background has been restored with the old money pouch background
- Added combat achievement icons


## v4.1 (2-May-2022 midnight)


![!image](https://i.imgur.com/fOMlsgb.png)
- Added highlight for selected tab on side panel
- Improved corners of the small rivets used in the combat achievement window
- Updated combat achievements tab icon
- Restored smaller iron rivets for main interfaces
- Fixed bank tabs hovered and selected icons to be correct
- Fixed Achievement Diaries and Kourend Favor tab icons to their previous ones
- Fixed Clan Chat icon in the settings menu
- Upgraded the 'notes' icon to 'kourend favour' icon for tabs


## v4.0 (1-May-2022)

It was about time this pack got some love! To start with, we've put together a bunch of additions that are subject to change or to be refined later.

![!image](https://i.imgur.com/mltug3e.png)
- Restored several border rivets to be more 2012-like (side panel also changes to this on modern interface mode)
  - Added small borders (character summary)
  - Added medium borders (settings)
  - Fixed the shading of the side panel background (on non-transparent mode)
- Added new prayer styles
  - Restored the 2011 prayers
    - Rigour (Rigour, Dungeoneering)
    - Augury (Augury, Dungeoneering)
    - Preserve (Berserker, Ancient Curses)
  - Restored all 2012 prayers
    - This is now the default. ***To change this, make a copy of this pack (or download it from git) and simply rename the folder. The 2013 prayers can be found under prayers_2013.***
- Quest Icons restored and upgraded
   - Character Summary
    - Quest List
    - Achievement Diaries
    - Kourend Favour
      - Upgraded all the Arceuus house icons
    - Minigame
- A range of icons have been improved to better suit the old style tab sizes
  - Combat Options
  - Worn Equipment
  - Prayer
  - Regular Spellbook
    - Improved the lvl-7 enchant icon to better resemble the hydrix colours
  - Ancient Spellbook
  - Lunar Spellbook
  - Arceuus Spellbook (upgraded)
    - Adjusted the contrast to match the oldschool style of the Arceuus spellbook
    - All spell icons have also been remade in HD fashion and have been upgraded
  - A variety of lock icons have been updated
- Minimap
  - Improved the contrast on several icons due to the lack of background shade during conversion
    - Inner heart icon on the hitpoints globe
    - All three inner run energy globe icons
    - Inner special attack globe icon
  - Changed the wiki icon to be more silvery
  - Enlarged the exit cross to accomodate for an update
    - *Previously this made the icon blurry, this has somewhat improved visibility*
  - Adjusted the minimap to accomodate for compass rotation on resizable mode
  - Upgraded bond pouch icon and border
- Added several new icons for groups and clans
  - Group Ironman
  - Hardcore Group Ironman
  - Clan channel
  - Guest clan channel
  - Minigames / Grouping
  - Refresh button
- Upgraded account management
  - Account, Store, Bonds, Inbox, Name changer
  - Community, Poll, History, Newspost, Archive icons
  - Links
- Upgraded settings
  - Controls, audio, display
  - Restored disabled setting icon
  - Restored new sliders (unfortunately no more dark to bright shades..)
- Bank
  - Improved the bank placeholder and search buttons
    - They are now highlighted orange
    - *Previously, the contrast between active or inactive was too low, making it unclear whether it was active or not*
  - Restored the magnifying glass for search functions
    - Also added a smaller version to the Grand Exchange interface
- Upgraded favourite and unfavourite icons in the fairy ring interface
- PvP World and Deadman mode skull icons have been reversed to more clearly identify a dangerous PvP world
  - PvP worlds are now **red**
  - Deadman worlds are now **silver**
- Upgraded make-x interface
- Added a texture to the red 'click here to play' button in the welcome screen
### 22-Aug-2020:
- Fixed sizing on FIXED_MODE corner tabs
- Remade the thumbs_up and thumbs_down button on the logout screen
- Changed some other small stuff

### 04-Aug-2020:
- Remade the border framing to match the 2012 look for various interfaces (iron rivets)
- Changed the Worn Equipment tab icon to match the 2012 one'
- Cleaned up a lot of sprites in the BUTTONS -folder
- Changed a few colors in the color.properties file
- Fixed the chatbox buttons (they were upside down when selected or hovered over)
- Remade the listing sorting arrows in the OTHER -folder
- Remade the BANK_PIN buttons

### 01-Aug-2020:
- Remade the GE interface
- Remade the Bank interface
- Changed icon.png for the hub
- Remade the FIXED_MODE and RESIZABLE_MODE enabled and disabled buttons in the OPTIONS -folder
- Changed a few colors in the color.properties file
- Fixed a weird shadowing on some bank buttons and combat style buttons

### 26-Jul-2020:
- Remade the bank tabs
- Reverted a few sprites to vanilla sprites as they were the same in 2012-13 era
- Realigned the minimap and compass sprite a little bit
- Remade the combat style buttons
- Remade the stash unit sprites used in ex. collection log
- Redid all checkbox sprites used in various interfaces
- Cleaned some sprites
- Remade the TASK_OPEN and TASK_CLOSED buttons for the Kourend Task overlay in the QUEST_TAB folder
21-Jul-2020:
- Remade the resizable mode minimap and compass sprite again
- Added all spell icons (except arcaeus as that spellbook never existed in 2012)
- Added all prayer icons
- Added all emote icons (except a few ones since they never existed in 2012)
- Redid the scrollbar icons on the chatbox for the transparent chatbox -mode.

### 15-Jul-2020 - 19-Jul-2020:
- Realigned some buttons so they dont overlap with any corners/framing of the interfaces
- Redid the Hitpoints, Prayer and Run icons on the minimap status orbs
- Reverted a few icons in BANK -folder back to vanilla style as they were the same in 2012
- Cleaned all the sprites in the COMBAT -folder
- Cleaned all the sprites in the EQUIPMENT -folder
- Remade the iron borders for various interfacesi´in the DIALOGUE -folder
- Replaced a few sprites that were added in the last update of the base plugin
- Remade the quest tab sprites
- Fixed a missing sprite in the trading screen borders
- Made the mouse overlay info box colors match with the blueish/darkish color of the inventory

### 13-Jul-2020:
- Remade the whole FIXED_MODE -folder
- Remade some sprites in OTHER -folder
- Remade the whole trading screen in DIALOGUE -folder (there's still something Id like to do there but its good for now)
- Cleaned all sprites in SKILL -folder
- Cleaned all sprites in TAB -folder
- Fixed the THUMB_SLIDERS for the screen brightness adjustment in OPTIONS -folder
- Cleaned all sprites in BANK -folder

### 10-Jul-2020:
- Updated the chatbox to match the original 2012 look
- Updated the dialog borders to match the original 2012 look
- Fixed a problem with sprites that caused weird flickering in the trading screen (temporary fix)
- Remade the HP, PRAY and RUN icons on the minimap status orbs
- Changed the compass sprite witht the original 2012 one
- Changed the whole minimap sprite to match the original 2012 look
- Changed the logout buttons to correct ones
- Remade the report abuse buttons to match the original 2012 look
- Cleaned up alot of sprites from TAB and SKILL -folders
- Cleaned up alot of sprites from OPTIONS -folders

### 09-Jul-2020:
- Fixed a problem with a sprite that may have caused weird issues in Grand Exchange interface
- Fixed a size problem with login screen background
- More clean up for various sprites
- Updated the sprites for various icons in the QUEST_TAB -folder
- Updated the sprites for various icons in the OPTIONS -folder
- Fixed the logout buttons for FIXED_MODE in the TAB -folder
- Fixed naming order for the XP Orbs in the OTHER -folder
- Cleaned alot of sprites in the COMBAT -folder
- Added new style for Decorated scrollbars in SCROLLBARS -folder

### 08-Jul-2020:
- Updated the sprites for various icons in the OPTIONS -folder
- Redid the whole tab area for both FIXED_MODE and RESIZEABLE_MODE -folders
- Cleaned up all the icons used on tabs in the TAB -folder
- Remade the buttons and framing for Grand Exchange in the GE -folder
- Updated the minimap for fixed mode in the FIXED_MODE -folder

### 07-Jul-2020:
- Updated the sprites for decorated scrollbar in the SCROLLBAR -folder
- Updated the sprites for various icons in the OPTIONS -folder
- Updated the sprites for Increment and Decrement buttons for GE -folder
- Updated the sprites for various buttons in Worn Equipment screen in BUTTON -folder
- Updated the sprites for various icons in the BANK -folder
- Updated the sprites for minimap and compass in the RESIZEABLE_MODE -folder


## Current progress:
Almost everything has been replaced as far as it is possible to do so with this plugin. If you have any questions or feedback, feel free to DM us or join the resource pack plugin server!

## To be done:
- **Welcome screen:** Perhaps improve buttons
- **Trade screen:** Has to be redone whilst having the interface open. Probably needs custom sprites.


## Credits:

Jagex - Base assets

Melkypie - Plugin author

Shredit - Author

Leyline - Co-Author
