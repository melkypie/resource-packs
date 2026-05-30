/*
 * Copyright (c) 2026, Ron Young <https://github.com/raiyni>
 * All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *     list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package melky.resourcepacks.model.runelite;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ChatColorKey
{

	PUBLIC_CHAT("PublicChat"),
	PUBLIC_CHAT_HIGHLIGHT("PublicChatHighlight"),
	PRIVATE_MESSAGE_SENT("PrivateMessageSent"),
	PRIVATE_MESSAGE_SENT_HIGHLIGHT("PrivateMessageSentHighlight"),
	PRIVATE_MESSAGE_RECEIVED("PrivateMessageReceived"),
	PRIVATE_MESSAGE_RECEIVED_HIGHLIGHT("PrivateMessageReceivedHighlight"),
	FRIENDS_CHAT_INFO("FriendsChatInfo"),
	CLAN_CHAT_INFO_HIGHLIGHT("ClanChatInfoHighlight"),
	CLAN_CHAT_MESSAGE("ClanChatMessage"),
	CLAN_CHAT_MESSAGE_HIGHLIGHT("ClanChatMessageHighlight"),
	CLAN_INFO("ClanInfo"),
	CLAN_INFO_HIGHLIGHT("ClanInfoHighlight"),
	CLAN_MESSAGE("ClanMessage"),
	CLAN_MESSAGE_HIGHLIGHT("ClanMessageHighlight"),
	CLAN_GUEST_INFO("ClanGuestInfo"),
	CLAN_GUEST_INFO_HIGHLIGHT("ClanGuestInfoHighlight"),
	CLAN_GUEST_MESSAGE("ClanGuestMessage"),
	CLAN_CHAT_GUEST_MESSAGE_HIGHLIGHT("ClanChatGuestMessageHighlight"),
	AUTOCHAT_MESSAGE("AutochatMessage"),
	AUTOCHAT_MESSAGE_HIGHLIGHT("AutochatMessageHighlight"),
	TRADE_CHAT_MESSAGE("TradeChatMessage"),
	TRADE_CHAT_MESSAGE_HIGHLIGHT("TradeChatMessageHighlight"),
	SERVER_MESSAGE("ServerMessage"),
	SERVER_MESSAGE_HIGHLIGHT("ServerMessageHighlight"),
	GAME_MESSAGE("GameMessage"),
	GAME_MESSAGE_HIGHLIGHT("GameMessageHighlight"),
	EXAMINE("Examine"),
	EXAMINE_HIGHLIGHT("ExamineHighlight"),
	FILTERED("Filtered"),
	FILTERED_HIGHLIGHT("FilteredHighlight"),
	USERNAME("Username"),
	PRIVATE_USERNAMES("PrivateUsernames"),
	CLAN_CHANNEL_NAME("ClanChannelName"),
	CLAN_CHAT_CHANNEL_NAME("ClanChatChannelName"),
	CLAN_CHAT_GUEST_CHANNEL_NAME("ClanChatGuestChannelName"),
	CLAN_USERNAMES("ClanUsernames"),
	CLAN_CHAT_USERNAMES("ClanClanUsernames"),
	CLAN_CHAT_GUEST_USERNAMES("ClanClanGuestUsernames"),
	PUBLIC_FRIEND_USERNAMES("PublicFriendUsernames"),
	PLAYER_USERNAME("PlayerUsername");

	public static final String OVERRIDE_KEY = "chat_colors";
	public static final String TRANSPARENT_KEY = "transparent";
	public static final String OPAQUE_KEY = "opaque";

	private final String configKey;

	public String opaqueConfig()
	{
		return OPAQUE_KEY + configKey;
	}

	public String transparentConfig()
	{
		return TRANSPARENT_KEY + configKey;
	}

	public String toOverrideKey()
	{
		return toString().toLowerCase();
	}

	public String transparentOverride()
	{
		return TRANSPARENT_KEY + "." + toOverrideKey();
	}

	public String opaqueOverride()
	{
		return OPAQUE_KEY + "." + toOverrideKey();
	}
}
