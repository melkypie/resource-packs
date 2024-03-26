package melky.resourcepacks;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.annotations.Component;
import net.runelite.api.widgets.ComponentID;

@Getter
@RequiredArgsConstructor
public enum WidgetResize
{
	//fixed
	FIXED_VIEWPORT_COMBAT_TAB(ComponentID.FIXED_VIEWPORT_COMBAT_TAB, 6, 0, 6, 0, null, null, null, null, null),
	FIXED_VIEWPORT_STATS_TAB(ComponentID.FIXED_VIEWPORT_STATS_TAB, 44, 0, 44, 0, null, null, null, null, null),
	FIXED_VIEWPORT_QUESTS_TAB(ComponentID.FIXED_VIEWPORT_QUESTS_TAB, 77, 0, 77, 0, null, null, null, null, null),
	FIXED_VIEWPORT_INVENTORY_TAB(ComponentID.FIXED_VIEWPORT_INVENTORY_TAB, 110, 0, 110, 0, null, null, null, null, null),
	FIXED_VIEWPORT_EQUIPMENT_TAB(ComponentID.FIXED_VIEWPORT_EQUIPMENT_TAB, 143, 0, 143, 0, null, null, null, null, null),
	FIXED_VIEWPORT_PRAYER_TAB(ComponentID.FIXED_VIEWPORT_PRAYER_TAB, 176, 0, 176, 0, null, null, null, null, null),
	FIXED_VIEWPORT_MAGIC_TAB(ComponentID.FIXED_VIEWPORT_MAGIC_TAB, 209, 0, 209, 0, null, null, null, null, null),
	FIXED_VIEWPORT_COMBAT_ICON(ComponentID.FIXED_VIEWPORT_COMBAT_ICON, 10, 0, 10, 0, null, null, null, null, null),
	FIXED_VIEWPORT_STATS_ICON(ComponentID.FIXED_VIEWPORT_STATS_ICON, 44, 0, 44, 0, null, null, null, null, null),
	FIXED_VIEWPORT_QUESTS_ICON(ComponentID.FIXED_VIEWPORT_QUESTS_ICON, 77, 0, 77, 0, null, null, null, null, null),
	FIXED_VIEWPORT_INVENTORY_ICON(ComponentID.FIXED_VIEWPORT_INVENTORY_ICON, 110, 0, 110, 0, null, null, null, null, null),
	FIXED_VIEWPORT_EQUIPMENT_ICON(ComponentID.FIXED_VIEWPORT_EQUIPMENT_ICON, 143, 0, 143, 0, null, null, null, null, null),
	FIXED_VIEWPORT_PRAYER_ICON(ComponentID.FIXED_VIEWPORT_PRAYER_ICON, 176, 0, 176, 0, null, null, null, null, null),
	FIXED_VIEWPORT_MAGIC_ICON(ComponentID.FIXED_VIEWPORT_MAGIC_ICON, 210, 0, 210, 0, null, null, null, null, null),

	FIXED_VIEWPORT_FRIENDS_CHAT_TAB(ComponentID.FIXED_VIEWPORT_FRIENDS_CHAT_TAB,3, 0, 3, 0, null, null, null, null, null),
	FIXED_VIEWPORT_IGNORES_TAB(ComponentID.FIXED_VIEWPORT_IGNORES_TAB,74, 0, 74, 0, null, null, null, null, null),
	FIXED_VIEWPORT_FRIENDS_TAB(ComponentID.FIXED_VIEWPORT_FRIENDS_TAB,41, 0, 41, 0, null, null, null, null, null),
	FIXED_VIEWPORT_LOGOUT_TAB(ComponentID.FIXED_VIEWPORT_LOGOUT_TAB,107, 0, 107, 0, null, null, null, null, null),
	FIXED_VIEWPORT_OPTIONS_TAB(ComponentID.FIXED_VIEWPORT_OPTIONS_TAB,140, 0, 140, 0, null, null, null, null, null),
	FIXED_VIEWPORT_EMOTES_TAB(ComponentID.FIXED_VIEWPORT_EMOTES_TAB,173, 0, 173, 0, null, null, null, null, null),
	FIXED_VIEWPORT_MUSIC_TAB(ComponentID.FIXED_VIEWPORT_MUSIC_TAB,206, 0, 206, 0, null, null, null, null, null),
	FIXED_VIEWPORT_FRIENDS_CHAT_ICON(ComponentID.FIXED_VIEWPORT_FRIENDS_CHAT_ICON,7, 0, 7, 0, null, null, null, null, null),
	FIXED_VIEWPORT_IGNORES_ICON(ComponentID.FIXED_VIEWPORT_IGNORES_ICON,74, 0, 74, 0, null, null, null, null, null),
	FIXED_VIEWPORT_FRIENDS_ICON(ComponentID.FIXED_VIEWPORT_FRIENDS_ICON,41, 0, 41, 0, null, null, null, null, null),
	FIXED_VIEWPORT_LOGOUT_ICON(ComponentID.FIXED_VIEWPORT_LOGOUT_ICON,107, 0, 107, 0, null, null, null, null, null),
	FIXED_VIEWPORT_OPTIONS_ICON(ComponentID.FIXED_VIEWPORT_OPTIONS_ICON,140, 0, 140, 0, null, null, null, null, null),
	FIXED_VIEWPORT_EMOTES_ICON(ComponentID.FIXED_VIEWPORT_EMOTES_ICON,173, 0, 173, 0, null, null, null, null, null),
	FIXED_VIEWPORT_MUSIC_ICON(ComponentID.FIXED_VIEWPORT_MUSIC_ICON,207, 0, 207, 0, null, null, null, null, null),

	//resizable classic
	RESIZABLE_VIEWPORT_COMBAT_TAB(ComponentID.RESIZABLE_VIEWPORT_COMBAT_TAB, 0, 0, 0, 0, null, null, null, null, null),
	RESIZABLE_VIEWPORT_STATS_TAB(ComponentID.RESIZABLE_VIEWPORT_STATS_TAB, 38, 0, 38, 0, null, null, null, null, null),
	RESIZABLE_VIEWPORT_QUESTS_TAB(ComponentID.RESIZABLE_VIEWPORT_QUESTS_TAB, 71, 0, 71, 0, null, null, null, null, null),
	RESIZABLE_VIEWPORT_INVENTORY_TAB(ComponentID.RESIZABLE_VIEWPORT_INVENTORY_TAB, 104, 0, 104, 0, null, null, null, null, null),
	RESIZABLE_VIEWPORT_EQUIPMENT_TAB(ComponentID.RESIZABLE_VIEWPORT_EQUIPMENT_TAB, 137, 0, 137, 0, null, null, null, null, null),
	RESIZABLE_VIEWPORT_PRAYER_TAB(ComponentID.RESIZABLE_VIEWPORT_PRAYER_TAB, 170, 0, 170, 0, null, null, null, null, null),
	RESIZABLE_VIEWPORT_MAGIC_TAB(ComponentID.RESIZABLE_VIEWPORT_MAGIC_TAB, 203, 0, 203, 0, null, null, null, null, null),
	RESIZABLE_VIEWPORT_COMBAT_ICON(ComponentID.RESIZABLE_VIEWPORT_COMBAT_ICON, 4, 0, 4, 0, null, null, null, null, null),
	RESIZABLE_VIEWPORT_STATS_ICON(ComponentID.RESIZABLE_VIEWPORT_STATS_ICON, 38, 0, 38, 0, null, null, null, null, null),
	RESIZABLE_VIEWPORT_QUESTS_ICON(ComponentID.RESIZABLE_VIEWPORT_QUESTS_ICON, 71, 0, 71, 0, null, null, null, null, null),
	RESIZABLE_VIEWPORT_INVENTORY_ICON(ComponentID.RESIZABLE_VIEWPORT_INVENTORY_ICON, 104, 0, 104, 0, null, null, null, null, null),
	RESIZABLE_VIEWPORT_EQUIPMENT_ICON(ComponentID.RESIZABLE_VIEWPORT_EQUIPMENT_ICON, 137, 0, 137, 0, null, null, null, null, null),
	RESIZABLE_VIEWPORT_PRAYER_ICON(ComponentID.RESIZABLE_VIEWPORT_PRAYER_ICON, 170, 0, 170, 0, null, null, null, null, null),
	RESIZABLE_VIEWPORT_MAGIC_ICON(ComponentID.RESIZABLE_VIEWPORT_MAGIC_ICON, 204, 0, 204, 0, null, null, null, null, null),

	RESIZABLE_VIEWPORT_FRIENDS_CHAT_TAB(ComponentID.RESIZABLE_VIEWPORT_FRIENDS_CHAT_TAB, 0, 0, 0, 0, null, null, null, null, null),
	RESIZABLE_VIEWPORT_IGNORES_TAB(ComponentID.RESIZABLE_VIEWPORT_IGNORES_TAB, 71, 0, 71, 0, null, null, null, null, null),
	RESIZABLE_VIEWPORT_FRIENDS_TAB(ComponentID.RESIZABLE_VIEWPORT_FRIENDS_TAB, 38, 0, 38, 0, null, null, null, null, null),
	RESIZABLE_VIEWPORT_LOGOUT_TAB(ComponentID.RESIZABLE_VIEWPORT_LOGOUT_TAB, 104, 0, 104, 0, null, null, null, null, null),
	RESIZABLE_VIEWPORT_OPTIONS_TAB(ComponentID.RESIZABLE_VIEWPORT_OPTIONS_TAB, 137, 0, 137, 0, null, null, null, null, null),
	RESIZABLE_VIEWPORT_EMOTES_TAB(ComponentID.RESIZABLE_VIEWPORT_EMOTES_TAB, 170, 0, 170, 0, null, null, null, null, null),
	RESIZABLE_VIEWPORT_MUSIC_TAB(ComponentID.RESIZABLE_VIEWPORT_MUSIC_TAB, 203, 0, 203, 0, null, null, null, null, null),
	RESIZABLE_VIEWPORT_FRIENDS_CHAT_ICON(ComponentID.RESIZABLE_VIEWPORT_FRIENDS_CHAT_ICON, 4, 0, 4, 0, null, null, null, null, null),
	RESIZABLE_VIEWPORT_IGNORES_ICON(ComponentID.RESIZABLE_VIEWPORT_IGNORES_ICON, 71, 0, 71, 0, null, null, null, null, null),
	RESIZABLE_VIEWPORT_FRIENDS_ICON(ComponentID.RESIZABLE_VIEWPORT_FRIENDS_ICON, 38, 0, 38, 0, null, null, null, null, null),
	RESIZABLE_VIEWPORT_LOGOUT_ICON(ComponentID.RESIZABLE_VIEWPORT_LOGOUT_ICON, 104, 0, 104, 0, null, null, null, null, null),
	RESIZABLE_VIEWPORT_OPTIONS_ICON(ComponentID.RESIZABLE_VIEWPORT_OPTIONS_ICON, 137, 0, 137, 0, null, null, null, null, null),
	RESIZABLE_VIEWPORT_EMOTES_ICON(ComponentID.RESIZABLE_VIEWPORT_EMOTES_ICON, 170, 0, 170, 0, null, null, null, null, null),
	RESIZABLE_VIEWPORT_MUSIC_ICON(ComponentID.RESIZABLE_VIEWPORT_MUSIC_ICON, 204, 0, 204, 0, null, null, null, null, null),

	//resizable modern
	RESIZABLE_VIEWPORT_BOTTOM_COMBAT_TAB(10747956, 0, 0, 0, 0, null, null, null, null, null),
	RESIZABLE_VIEWPORT_BOTTOM_STATS_TAB(10747957, 33, 0, 33, 0, null, null, null, null, null),
	RESIZABLE_VIEWPORT_BOTTOM_QUESTS_TAB(10747958, 66, 0, 66, 0, null, null, null, null, null),
	RESIZABLE_VIEWPORT_BOTTOM_INVENTORY_TAB(ComponentID.RESIZABLE_VIEWPORT_BOTTOM_LINE_INVENTORY_TAB, 99, 0, 99, 0, null, null, null, null, null),
	RESIZABLE_VIEWPORT_BOTTOM_EQUIPMENT_TAB(10747960, 132, 0, 132, 0, null, null, null, null, null),
	RESIZABLE_VIEWPORT_BOTTOM_PRAYER_TAB(ComponentID.RESIZABLE_VIEWPORT_BOTTOM_LINE_PRAYER_TAB, 165, 0, 165, 0, null, null, null, null, null),
	RESIZABLE_VIEWPORT_BOTTOM_MAGIC_TAB(10747962, 198, 0, 198, 0, null, null, null, null, null),
	RESIZABLE_VIEWPORT_BOTTOM_COMBAT_ICON(ComponentID.RESIZABLE_VIEWPORT_BOTTOM_LINE_CMB_ICON, 0, 0, 0, 0, null, null, null, null, null),
	RESIZABLE_VIEWPORT_BOTTOM_STATS_ICON(ComponentID.RESIZABLE_VIEWPORT_BOTTOM_LINE_SKILLS_ICON, 33, 0, 33, 0, null, null, null, null, null),
	RESIZABLE_VIEWPORT_BOTTOM_QUESTS_ICON(ComponentID.RESIZABLE_VIEWPORT_BOTTOM_LINE_QUESTS_ICON, 66, 0, 66, 0, null, null, null, null, null),
	RESIZABLE_VIEWPORT_BOTTOM_INVENTORY_ICON(ComponentID.RESIZABLE_VIEWPORT_BOTTOM_LINE_INVENTORY_ICON, 99, 0, 99, 0, null, null, null, null, null),
	RESIZABLE_VIEWPORT_BOTTOM_EQUIPMENT_ICON(ComponentID.RESIZABLE_VIEWPORT_BOTTOM_LINE_EQUIP_ICON, 132, 0, 132, 0, null, null, null, null, null),
	RESIZABLE_VIEWPORT_BOTTOM_PRAYER_ICON(ComponentID.RESIZABLE_VIEWPORT_BOTTOM_LINE_PRAYER_ICON, 165, 0, 165, 0, null, null, null, null, null),
	RESIZABLE_VIEWPORT_BOTTOM_MAGIC_ICON(ComponentID.RESIZABLE_VIEWPORT_BOTTOM_LINE_MAGIC_ICON, 198, 0, 198, 0, null, null, null, null, null),

	RESIZABLE_VIEWPORT_BOTTOM_FRIENDS_CHAT_TAB(10747942, 99, 0, 99, 0, null, null, null, null, null),
	RESIZABLE_VIEWPORT_BOTTOM_IGNORES_TAB(1074793, 132, 0, 132, 0, null, null, null, null, null),
	RESIZABLE_VIEWPORT_BOTTOM_FRIENDS_TAB(1074794, 165, 0, 165, 0, null, null, null, null, null),
	RESIZABLE_VIEWPORT_BOTTOM_OPTIONS_TAB(1074795, 66, 0, 66, 0, null, null, null, null, null),
	RESIZABLE_VIEWPORT_BOTTOM_EMOTES_TAB(1074796, 33, 0, 33, 0, null, null, null, null, null),
	RESIZABLE_VIEWPORT_BOTTOM_MUSIC_TAB(1074797, 0, 0, 0, 0, null, null, null, null, null),
	RESIZABLE_VIEWPORT_BOTTOM_FRIENDS_CHAT_ICON(ComponentID.RESIZABLE_VIEWPORT_BOTTOM_LINE_FC_ICON, 99, 0, 99, 0, null, null, null, null, null),
	RESIZABLE_VIEWPORT_BOTTOM_IGNORES_ICON(10747949, 132, 0, 132, 0, null, null, null, null, null),
	RESIZABLE_VIEWPORT_BOTTOM_FRIENDS_ICON(ComponentID.RESIZABLE_VIEWPORT_BOTTOM_LINE_FRIEND_ICON, 165, 0, 165, 0, null, null, null, null, null),
	RESIZABLE_VIEWPORT_BOTTOM_OPTIONS_ICON(ComponentID.RESIZABLE_VIEWPORT_BOTTOM_LINE_SETTINGS_ICON, 66, 0, 66, 0, null, null, null, null, null),
	RESIZABLE_VIEWPORT_BOTTOM_EMOTES_ICON(ComponentID.RESIZABLE_VIEWPORT_BOTTOM_LINE_EMOTE_ICON, 33, 0, 33, 0, null, null, null, null, null),
	RESIZABLE_VIEWPORT_BOTTOM_MUSIC_ICON(ComponentID.RESIZABLE_VIEWPORT_BOTTOM_LINE_MUSIC_ICON, 0, 0, 0, 0, null, null, null, null, null),

	//was squished, due to default width and height set to 16x16 instead of 20x18 (fairy ring plug-in)
	FAIRY_RING_SEARCH_ICON(ComponentID.FAIRY_RING_PANEL_HEADER, null, null, null, null, 20, 18, 20, 18, 8),

	//close button was squished
	FOSSIL_POOL_HOPPER_CLOSE_BUTTON_PARENT(40173570, -2, -1, null, null, 26, null, 26, null, null),
	FOSSIL_POOL_HOPPER_CLOSE_BUTTON(40173570, -2, -1, null, null, 26, null, 26, null, 0),
	FOSSIL_POOL_HOPPER_CLOSE_BUTTON_HOVER(40173570, -2, -1, null, null, 26, null, 26, null, 1),

	//reposition border_left 1 pixel to the right to fix being cut-off
	GRAND_EXCHANGE_OFFER_1_BORDER_LEFT(30474247, -13, null, -14, null, null, null, null, null, 7),
	GRAND_EXCHANGE_OFFER_2_BORDER_LEFT(30474248, -13, null, -14, null, null, null, null, null, 7),
	GRAND_EXCHANGE_OFFER_3_BORDER_LEFT(30474249, -13, null, -14, null, null, null, null, null, 7),
	GRAND_EXCHANGE_OFFER_4_BORDER_LEFT(30474250, -13, null, -14, null, null, null, null, null, 7),
	GRAND_EXCHANGE_OFFER_5_BORDER_LEFT(30474251, -13, null, -14, null, null, null, null, null, 7),
	GRAND_EXCHANGE_OFFER_6_BORDER_LEFT(30474252, -13, null, -14, null, null, null, null, null, 7),
	GRAND_EXCHANGE_OFFER_7_BORDER_LEFT(30474253, -13, null, -14, null, null, null, null, null, 7),
	GRAND_EXCHANGE_OFFER_8_BORDER_LEFT(30474254,  -13, null, -14, null, null, null, null, null, 7),

	GRAND_EXCHANGE_COLLECTION_BOX_1_BORDER_LEFT(26345477, -13, null, -14, null, null, null, null, null, 7),
	GRAND_EXCHANGE_COLLECTION_BOX_2_BORDER_LEFT(26345478, -13, null, -14, null, null, null, null, null, 7),
	GRAND_EXCHANGE_COLLECTION_BOX_3_BORDER_LEFT(26345479, -13, null, -14, null, null, null, null, null, 7),
	GRAND_EXCHANGE_COLLECTION_BOX_4_BORDER_LEFT(26345480, -13, null, -14, null, null, null, null, null, 7),
	GRAND_EXCHANGE_COLLECTION_BOX_5_BORDER_LEFT(26345481, -13, null, -14, null, null, null, null, null, 7),
	GRAND_EXCHANGE_COLLECTION_BOX_6_BORDER_LEFT(26345482, -13, null, -14, null, null, null, null, null, 7),
	GRAND_EXCHANGE_COLLECTION_BOX_7_BORDER_LEFT(26345483, -13, null, -14, null, null, null, null, null, 7),
	GRAND_EXCHANGE_COLLECTION_BOX_8_BORDER_LEFT(26345484, -13, null, -14, null, null, null, null, null, 7),

	SPELL_FILTER_BORDER_LEFT(14287043, -13, null, -13, null, null, null, null, null, 2),

	PRAYER_FILTER_BORDER_LEFT(35454980, -13, null, -13, null, null, null, null, null, 2),

	MORE_INFO_PATH_BORDER_LEFT1(42074121, -13, null, -13, null, null, null, null, null, 2),
	MORE_INFO_PATH_BORDER_LEFT2(42074126, -13, null, -13, null, null, null, null, null, 2),

	PATH_BORDER_LEFT1(42074140, -13, null, -13, null, null, null, null, null, 2),
	PATH_BORDER_LEFT2(42074118, -13, null, -13, null, null, null, null, null, 2),

	PATH_REWARDS_BORDER_LEFT1(42139651, -12, null, -12, null, null, null, null, null, 18),
	PATH_REWARDS_BORDER_LEFT2(42139651, 60, null, 60, null, null, null, null, null, 27),

	TOB_LOBBY_PARTY_BORDER_LEFT(ComponentID.TOB_PARTY_INTERFACE, -13, null, -13, null, null, null, null, null, 2),
	TOA_LOBBY_PARTY_BORDER_LEFT(ComponentID.TOA_PARTY_LAYER, -13, null, -13, null, null, null, null, null, 2),

	SOUL_WARS_LOBBY_BORDER_LEFT(28442626, -13, null, -13, null, null, null, null, null, 2),
	SOUL_WARS_OVERLAY_BORDER_LEFT(24576007, -13, null, -13, null, null, null, null, null, 2),
	SOUL_WARS_CONTRIBUTION_BAR_BORDER_LEFT(24576003, -13, null, -13, null, null, null, null, null, 2),

	TEMPOROSS_LOBBY_BORDER_LEFT(ComponentID.TEMPOROSS_LOBBY_LOBBY, -13, null, -13, null, null, null, null, null, 2),

	XP_DROP_SETUP_TOP_BORDER_LEFT(8978436, -13, null, -13, null, null, null, null, null, 2),
	XP_DROP_SETUP_BOTTOM_BORDER_LEFT(8978447, -13, null, -13, null, null, null, null, null, 2),

	PVP_ARENA_SIDE_PANEL_OPTIONS_BORDER_LEFT(49741824, -13, null, -13, null, null, null, null, null, 2),

	;
	
	@Component
	private final Integer componentId;
	private final Integer modifiedX;
	private final Integer modifiedY;
	private final Integer originalX;
	private final Integer originalY;
	private final Integer modifiedWidth;
	private final Integer modifiedHeight;
	private final Integer originalWidth;
	private final Integer originalHeight;
	private final Integer childIndex;
}
