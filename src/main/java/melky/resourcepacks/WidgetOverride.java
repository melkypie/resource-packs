package melky.resourcepacks;

import com.google.common.collect.ImmutableMultimap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import static melky.resourcepacks.WidgetOverride.Script.ACCOUNT_MANAGEMENT_INIT;
import static melky.resourcepacks.WidgetOverride.Script.ACHIEVEMENT_DIARY_ENTRY_REBUILD;
import static melky.resourcepacks.WidgetOverride.Script.AUTOCAST_SETUP;
import static melky.resourcepacks.WidgetOverride.Script.BANK_INIT;
import static melky.resourcepacks.WidgetOverride.Script.BANK_SETTINGS_OPEN;
import static melky.resourcepacks.WidgetOverride.Script.CHANNEL_TAB_INIT;
import static melky.resourcepacks.WidgetOverride.Script.CHATBOX_BACKGROUND;
import static melky.resourcepacks.WidgetOverride.Script.DEATHKEEP_REDRAW;
import static melky.resourcepacks.WidgetOverride.Script.FRIENDS_CHAT_BUILD;
import static melky.resourcepacks.WidgetOverride.Script.FRIEND_UPDATE;
import static melky.resourcepacks.WidgetOverride.Script.GE_OFFERS_INIT;
import static melky.resourcepacks.WidgetOverride.Script.GE_OFFERS_SETUP_INIT;
import static melky.resourcepacks.WidgetOverride.Script.IGNORE_UPDATE;
import static melky.resourcepacks.WidgetOverride.Script.KEYBINDS_INIT;
import static melky.resourcepacks.WidgetOverride.Script.KEYBINDS_OPEN_MENU;
import static melky.resourcepacks.WidgetOverride.Script.KOUREND_TAB_UPDATE;
import static melky.resourcepacks.WidgetOverride.Script.MAKE_ALL_ITEM_HOVER;
import static melky.resourcepacks.WidgetOverride.Script.MAKE_ALL_ITEM_OP;
import static melky.resourcepacks.WidgetOverride.Script.MAKE_ALL_QUANTITY_DRAW;
import static melky.resourcepacks.WidgetOverride.Script.MINIGAMES_TAB_REBUILD;
import static melky.resourcepacks.WidgetOverride.Script.OPTIONS_TABS_SWITCH;
import static melky.resourcepacks.WidgetOverride.Script.QUEST_TAB_TABS_DRAW;
import static melky.resourcepacks.WidgetOverride.Script.SETTINGS_SIDE_TABS_INIT;
import static melky.resourcepacks.WidgetOverride.Script.THINKBOX;
import static melky.resourcepacks.WidgetOverride.Script.WORLDMAP_INIT;
import static melky.resourcepacks.WidgetOverride.Script.WORLDSWITCHER_INIT;
import static melky.resourcepacks.WidgetOverride.Script.XPDROPS_SHOW_PANEL;
import static melky.resourcepacks.WidgetOverride.Script.ZEAH_FAVOUR_INIT;
import net.runelite.api.annotations.Interface;
import net.runelite.api.widgets.InterfaceID;

@Getter
@RequiredArgsConstructor
public enum WidgetOverride
{
	GE_BORDER_BUY_INNER(GE_OFFERS_INIT, 0x5a5245, InterfaceID.GRAND_EXCHANGE, new int[]{7, 8, 9, 10, 11, 12, 13, 14}, 0),
	GE_BORDER_BUY_OUTER(GE_OFFERS_INIT, 0x383023, InterfaceID.GRAND_EXCHANGE, new int[]{7, 8, 9, 10, 11, 12, 13, 14}, 3),
	GE_BORDER_SELL_INNER(GE_OFFERS_INIT, 0x5a5245, InterfaceID.GRAND_EXCHANGE, new int[]{7, 8, 9, 10, 11, 12, 13, 14}, 1),
	GE_BORDER_SELL_OUTER(GE_OFFERS_INIT, 0x383023, InterfaceID.GRAND_EXCHANGE, new int[]{7, 8, 9, 10, 11, 12, 13, 14}, 4),

	GE_BORDER_OFFER_BUY_OFFER_FULL_INNER(GE_OFFERS_SETUP_INIT, 0x5a5245, InterfaceID.GRAND_EXCHANGE, 25, 14),
	GE_BORDER_OFFER_BUY_OFFER_FULL_OUTER(GE_OFFERS_SETUP_INIT, 0x383023, InterfaceID.GRAND_EXCHANGE, 25, 15),
	GE_BORDER_OFFER_BUY_OFFER_BOTTOM_INNER(GE_OFFERS_SETUP_INIT, 0x5a5245, InterfaceID.GRAND_EXCHANGE, 25, 16),
	GE_BORDER_OFFER_BUY_OFFER_BOTTOM_OUTER(GE_OFFERS_SETUP_INIT, 0x383023, InterfaceID.GRAND_EXCHANGE, 25, 17),
	GE_BORDER_OFFER_CHOOSE_ITEM_INNER(GE_OFFERS_SETUP_INIT, 0x5a5245, InterfaceID.GRAND_EXCHANGE, 25, 23),
	GE_BORDER_OFFER_CHOOSE_ITEM_OUTER(GE_OFFERS_SETUP_INIT, 0x383023, InterfaceID.GRAND_EXCHANGE, 25, 24),
	GE_BORDER_OFFER_QUANTITY_INNER(GE_OFFERS_SETUP_INIT, 0x5a5245, InterfaceID.GRAND_EXCHANGE, 25, 26),
	GE_BORDER_OFFER_QUANTITY_OUTER(GE_OFFERS_SETUP_INIT, 0x383023, InterfaceID.GRAND_EXCHANGE, 25, 27),
	GE_BORDER_OFFER_PRICE_INNER(GE_OFFERS_SETUP_INIT, 0x5a5245, InterfaceID.GRAND_EXCHANGE, 25, 33),
	GE_BORDER_OFFER_PRICE_OUTER(GE_OFFERS_SETUP_INIT, 0x383023, InterfaceID.GRAND_EXCHANGE, 25, 34),

	GE_CHATBOX_BUY_SEARCH_BORDER_INNER(GE_OFFERS_SETUP_INIT, 0x463214, InterfaceID.CHATBOX, 49, -1),
	GE_CHATBOX_BUY_SEARCH_BORDER_OUTER(GE_OFFERS_SETUP_INIT, 0x463214, InterfaceID.CHATBOX, 48, -1),
	GE_CHATBOX_BUY_SEARCH_BACKGROUND(GE_OFFERS_SETUP_INIT, 0x463214, InterfaceID.CHATBOX, 47, -1),

	KEYBINDS_SMALL_BACKGROUND(KEYBINDS_INIT, 0x2e281d, 121, new int[]{6, 13, 20, 27, 34, 41, 48, 55, 62, 69, 76, 83, 90, 97}, -1),
	KEYBINDS_SMALL_BORDER(KEYBINDS_INIT, 0x544834, 121, new int[]{7, 14, 21, 28, 35, 42, 49, 56, 63, 70, 77, 84, 91, 98}, -1),
	KEYBINDS_MENU_BACKGROUND(KEYBINDS_OPEN_MENU, 0x2e281d, 121, 109, -1),
	KEYBINDS_MENU_BORDER(KEYBINDS_OPEN_MENU, 0x544834, 121, 110, -1),

	MAKE_ALL_BACKGROUND_HOVER(MAKE_ALL_ITEM_HOVER, 0x89785e, 270, new int[]{14, 15, 16, 17, 18, 19, 20, 21, 22, 23}, 29),
	MAKE_ALL_BACKGROUND_CLICKED(MAKE_ALL_ITEM_OP, 0x89785e, 270, new int[]{14, 15, 16, 17, 18, 19, 20, 21, 22, 23}, 29),
	MAKE_ALL_QUANTITY_SELECTED(MAKE_ALL_QUANTITY_DRAW, 0x89785e, 270, new int[]{7, 8, 9, 10, 11, 12}, 0),

	ACCOUNT_MANAGEMENT_TAB_LINE(ACCOUNT_MANAGEMENT_INIT, 0x5d5848, 109, new int[]{2, 3, 4, 5}, -1),

	CHATBOX_SEPERATOR_LINE(CHATBOX_BACKGROUND, 0x807660, InterfaceID.CHATBOX, 54, 0),

	FRIENDS_CHAT_BORDER_FULL_INNER(FRIENDS_CHAT_BUILD, 0x474745, InterfaceID.FRIENDS_CHAT, 9, 1),
	FRIENDS_CHAT_BORDER_FULL_OUTER(FRIENDS_CHAT_BUILD, 0xe0e0c, InterfaceID.FRIENDS_CHAT, 9, 0),
	FRIENDS_CHAT_BORDER_TOP_INNER(FRIENDS_CHAT_BUILD, 0x474745, InterfaceID.FRIENDS_CHAT, 10, 1),
	FRIENDS_CHAT_BORDER_TOP_OUTER(FRIENDS_CHAT_BUILD, 0xe0e0c, InterfaceID.FRIENDS_CHAT, 10, 0),

	FRIENDS_TOP_LINE_ABOVE(FRIEND_UPDATE, 0x474745, InterfaceID.FRIEND_LIST, 6, 1),
	FRIENDS_TOP_LINE_BELOW(FRIEND_UPDATE, 0xe0e0c, InterfaceID.FRIEND_LIST, 6, 0),

	IGNORE_TOP_LINE_ABOVE(IGNORE_UPDATE, 0x474745, InterfaceID.IGNORE_LIST, 6, 1),
	IGNORE_TOP_LINE_BELOW(IGNORE_UPDATE, 0xe0e0c, InterfaceID.IGNORE_LIST, 6, 0),

	QUEST_TAB_TABS_UNDERLINE(QUEST_TAB_TABS_DRAW, 0x5d5848, 629, 2, -1),

	XPDROPS_BORDER_INNER(XPDROPS_SHOW_PANEL, 0x5a5245, InterfaceID.EXPERIENCE_TRACKER, new int[]{8, 13}, -1),
	XPDROPS_BORDER_OUTER(XPDROPS_SHOW_PANEL, 0x383023, InterfaceID.EXPERIENCE_TRACKER, new int[]{7, 12}, -1),

	// Needs to be updated if an achievement diary is added
	ACHIEVEMENT_DIARY_BORDER(ACHIEVEMENT_DIARY_ENTRY_REBUILD, 0xff9933, InterfaceID.ACHIEVEMENT_DIARY, 2, new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11}),
	ACHIEVEMENT_DIARY_PROGRESS_BORDER(ACHIEVEMENT_DIARY_ENTRY_REBUILD, 0x000000, InterfaceID.ACHIEVEMENT_DIARY, 2, new int[]{15, 27, 39, 51, 63, 75, 87, 99, 111, 123, 135, 147}),

	KOUREND_fAVVOUR_TAB_ARCEUUS_BORDER_INNER(KOUREND_TAB_UPDATE, 0x000000, 245, 6, -1),
	KOUREND_fAVVOUR_TAB_ARCEUUS_BORDER_OUTER(KOUREND_TAB_UPDATE, 0x2000c8, 245, 5, 0),
	KOUREND_fAVVOUR_TAB_ARCEUUS_FILL(KOUREND_TAB_UPDATE, 0x2000c8, 245, 7, 0),
	KOUREND_fAVVOUR_TAB_HOSIDIUS_BORDER_INNER(KOUREND_TAB_UPDATE, 0x000000, 245, 9, -1),
	KOUREND_fAVVOUR_TAB_HOSIDIUS_BORDER_OUTER(KOUREND_TAB_UPDATE, 0x20c008, 245, 8, 0),
	KOUREND_fAVVOUR_TAB_HOSIDIUS_FILL(KOUREND_TAB_UPDATE, 0x20c008, 245, 10, 0),
	KOUREND_fAVVOUR_TAB_LOVAKENGJ_BORDER_INNER(KOUREND_TAB_UPDATE, 0x000000, 245, 12, -1),
	KOUREND_fAVVOUR_TAB_LOVAKENGJ_BORDER_OUTER(KOUREND_TAB_UPDATE, 0xc05008, 245, 11, 0),
	KOUREND_fAVVOUR_TAB_LOVAKENGJ_FILL(KOUREND_TAB_UPDATE, 0xc05008, 245, 13, 0),
	KOUREND_fAVVOUR_TAB_PISCARILIUS_BORDER_INNER(KOUREND_TAB_UPDATE, 0x000000, 245, 15, -1),
	KOUREND_fAVVOUR_TAB_PISCARILIUS_BORDER_OUTER(KOUREND_TAB_UPDATE, 0x98a0, 245, 14, 0),
	KOUREND_fAVVOUR_TAB_PISCARILIUS_FILL(KOUREND_TAB_UPDATE, 0x98a0, 245, 16, 0),
	KOUREND_fAVVOUR_TAB_SHAYZIEN_BORDER_INNER(KOUREND_TAB_UPDATE, 0x000000, 245, 18, -1),
	KOUREND_fAVVOUR_TAB_SHAYZIEN_BORDER_OUTER(KOUREND_TAB_UPDATE, 0x980810, 245, 17, 0),
	KOUREND_fAVVOUR_TAB_SHAYZIEN_FILL(KOUREND_TAB_UPDATE, 0x980810, 245, 19, 0),

	MINIGAMES_TAB_BORDER_INNER(MINIGAMES_TAB_REBUILD, 0xff981f, InterfaceID.MINIGAMES, new int[]{7, 11, 17}, -1),
	MINIGAMES_TAB_BORDER_OUTER(MINIGAMES_TAB_REBUILD, 0x191900, InterfaceID.MINIGAMES, new int[]{6, 10, 16}, -1),

	OPTIONS_CONTROLS_MENU_BORDER_INNER(OPTIONS_TABS_SWITCH, 0x474745, InterfaceID.SETTINGS_SIDE, new int[]{91, 92}, 2),
	OPTIONS_CONTROLS_MENU_BORDER_OUTER(OPTIONS_TABS_SWITCH, 0xe0e0c, InterfaceID.SETTINGS_SIDE, new int[]{91, 92}, 1),
	OPTIONS_CONTROLS_DROPDOWN_BORDER_INNER(THINKBOX, 0x474745, InterfaceID.SETTINGS_SIDE, 106, 1),
	OPTIONS_CONTROLS_DROPDOWN_BORDER_OUTER(THINKBOX, 0xe0e0c, InterfaceID.SETTINGS_SIDE, 106, 0),

	COMBAT_CHOOSE_SPELL_BACKGROUND(AUTOCAST_SETUP, 0x000000, 201, 3, -1),
	COMBAT_CHOOSE_SPELL_INNER(AUTOCAST_SETUP, 0x726451, 201, 4, -1),
	COMBAT_CHOOSE_SPELL_OUTER(AUTOCAST_SETUP, 0x2e2b23, 201, 5, -1),

	ITEMS_KEPT_ON_DEATH_BORDER_INNER(DEATHKEEP_REDRAW, 0xe0e0c, InterfaceID.KEPT_ON_DEATH, new int[]{4, 5, 12}, new int[]{0, 4}),
	ITEMS_KEPT_ON_DEATH_BORDER_OUTER(DEATHKEEP_REDRAW, 0x474745, InterfaceID.KEPT_ON_DEATH, new int[]{4, 5, 12}, new int[]{1, 5}),

	WORLD_SWITCH_TOP_AND_BOTTOM_LINES(WORLDSWITCHER_INIT, 0x73654a, InterfaceID.WORLD_SWITCHER, new int[]{6, 7}, -1),

	WORLD_MAP_SEARCH_BORDER_INNER(WORLDMAP_INIT, 0x474745, InterfaceID.WORLD_MAP, 25, 4),
	WORLD_MAP_SEARCH_BORDER_OUTER(WORLDMAP_INIT, 0xe0e0c, InterfaceID.WORLD_MAP, 25, 3),
	WORLD_MAP_PLACE_BORDER_INNER(WORLDMAP_INIT, 0x474745, InterfaceID.WORLD_MAP, 26, 2),
	WORLD_MAP_PLACE_BORDER_OUTER(WORLDMAP_INIT, 0xe0e0c, InterfaceID.WORLD_MAP, 26, 1),
	WORLD_MAP_PLACE_DROPDOWN_BORDER_INNER(WORLDMAP_INIT, 0x474745, InterfaceID.WORLD_MAP, 33, 1),
	WORLD_MAP_PLACE_DROPDOWN_BORDER_OUTER(WORLDMAP_INIT, 0xe0e0c, InterfaceID.WORLD_MAP, 33, 0),

	BANK_TAB_UNDERLINE(BANK_INIT, 0x2e2b23, InterfaceID.BANK, 11, -1),
	BANK_OPTIONS_BORDER_TAB_DISPLAY_INNER(BANK_SETTINGS_OPEN, 0x2e2b23, InterfaceID.BANK, 49, 13),
	BANK_OPTIONS_BORDER_TAB_DISPLAY_OUTER(BANK_SETTINGS_OPEN, 0x726451, InterfaceID.BANK, 49, 14),
	BANK_OPTIONS_BORDER_BANK_FILLERS_INNER(BANK_SETTINGS_OPEN, 0x2e2b23, InterfaceID.BANK, 56, 1),
	BANK_OPTIONS_BORDER_BANK_FILLERS_OUTER(BANK_SETTINGS_OPEN, 0x726451, InterfaceID.BANK, 56, 2),

	KOUREND_FAVOUR_DIALOG_SEPERATOR_LINE_UPPER(ZEAH_FAVOUR_INIT, 0x2e2b23, 626, 5, -1),
	KOUREND_FAVOUR_DIALOG_SEPERATOR_LINE_LOWER(ZEAH_FAVOUR_INIT, 0x2e2b23, 626, 4, -1),
	OPTIONS_TAB_SEPERATOR_LINE(SETTINGS_SIDE_TABS_INIT, 0x5d5848, InterfaceID.SETTINGS_SIDE, new int[]{102, 103, 104, 105}, -1),
	CHANNEL_TAB_SEPERATOR_LINE(CHANNEL_TAB_INIT, 0x5d5848, 707, 2, -1),
	;

	private final int scriptId;
	private final int defaultColor;
	@Interface
	private final int widgetInterfaceId;
	private final int[] widgetChildIds;
	private final int[] widgetArrayIds;


	WidgetOverride(int scriptId, int defaultColor, @Interface int widgetInterfaceId, int[] widetChildIds, int widgetArrayId)
	{
		this(scriptId, defaultColor, widgetInterfaceId, widetChildIds, new int[]{widgetArrayId});
	}

	WidgetOverride(int scriptId, int defaultColor, @Interface int widgetInterfaceId, int widetChildId, int widgetArrayId)
	{
		this(scriptId, defaultColor, widgetInterfaceId, widetChildId, new int[]{widgetArrayId});
	}

	WidgetOverride(int scriptId, int defaultColor, @Interface int widgetInterfaceId, int widetChildId, int[] widgetArrayIds)
	{
		this(scriptId, defaultColor, widgetInterfaceId, new int[]{widetChildId}, widgetArrayIds);
	}

	static final ImmutableMultimap<Integer, WidgetOverride> scriptWidgetOverrides;

	static
	{
		ImmutableMultimap.Builder<Integer, WidgetOverride> builder = new ImmutableMultimap.Builder<>();

		for (WidgetOverride widgetOverride : values())
		{
			builder.put(widgetOverride.scriptId, widgetOverride);
		}

		scriptWidgetOverrides = builder.build();
	}


	static class Script
	{
		static final int GE_OFFERS_INIT = 803;
		static final int GE_OFFERS_SETUP_INIT = 773;
		static final int KEYBINDS_INIT = 984;
		static final int KEYBINDS_OPEN_MENU = 985;
		static final int MAKE_ALL_ITEM_HOVER = 2049;
		static final int MAKE_ALL_QUANTITY_DRAW = 2058;
		static final int MAKE_ALL_ITEM_OP = 2044;
		static final int ACCOUNT_MANAGEMENT_INIT = 2497;
		static final int CHATBOX_BACKGROUND = 923;
		static final int FRIENDS_CHAT_BUILD = 1658;
		static final int FRIEND_UPDATE = 125;
		static final int IGNORE_UPDATE = 129;
		static final int QUEST_TAB_TABS_DRAW = 2800;
		static final int XPDROPS_SHOW_PANEL = 997;
		static final int ACHIEVEMENT_DIARY_ENTRY_REBUILD = 2199;
		static final int KOUREND_TAB_UPDATE = 1229;
		static final int MINIGAMES_TAB_REBUILD = 435;
		static final int AUTOCAST_SETUP = 2098;
		static final int DEATHKEEP_REDRAW = 974;
		static final int WORLDSWITCHER_INIT = 747;
		static final int WORLDMAP_INIT = 1707;
		static final int BANK_INIT = 274;
		static final int BANK_SETTINGS_OPEN = 3276;
		static final int THINKBOX = 715;
		static final int OPTIONS_TABS_SWITCH = 529;
		static final int ZEAH_FAVOUR_INIT = 1203;
		static final int SETTINGS_SIDE_TABS_INIT = 3909;
		static final int CHANNEL_TAB_INIT = 4471;
	}
}
