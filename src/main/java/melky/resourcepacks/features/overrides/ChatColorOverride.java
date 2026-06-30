/*
 * Copyright (c) 2025, Ron Young <https://github.com/raiyni>
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

package melky.resourcepacks.features.overrides;

import com.google.common.annotations.VisibleForTesting;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import melky.resourcepacks.ResourcePacksConfig;
import melky.resourcepacks.event.PackParsed;
import melky.resourcepacks.features.overrides.model.OverrideAction;
import melky.resourcepacks.features.packs.PacksService;
import melky.resourcepacks.model.runelite.ChatColorKey;
import static melky.resourcepacks.model.runelite.ConfigKeys.ResourcePacks.CHAT_COLOR_BACKUP_PREFIX;
import static melky.resourcepacks.model.runelite.ConfigKeys.ResourcePacks.GROUP_NAME;
import static melky.resourcepacks.model.runelite.ConfigKeys.RuneLiteConfig.CHAT_COLOR_CONFIG;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import org.tomlj.TomlTable;

@Singleton
@Slf4j
public class ChatColorOverride extends OverrideAction
{
	@Inject
	private ResourcePacksConfig config;

	@Inject
	private ConfigManager configManager;

	@Inject
	private PacksService packsService;

	private static final long WARNING_THROTTLE_MS = 5000;
	private long lastWarningSent;

	@Getter
	@VisibleForTesting
	private Map<String, Color> savedColors = new HashMap<>();

	@Getter
	@VisibleForTesting
	private Map<String, Color> parsedColors = new HashMap<>();

	private boolean ignoreEvent;


	@Override
	public boolean isEnabled(ResourcePacksConfig config)
	{
		return config.allowChatColors() && !packsService.isPackPathEmpty();
	}

	@Override
	public void startUp()
	{
		var backupKeys = configManager.getConfigurationKeys(GROUP_NAME + "." + CHAT_COLOR_BACKUP_PREFIX);
		if (backupKeys != null)
		{
			for (var fullKey : backupKeys)
			{
				String colorKey = fullKey.substring((GROUP_NAME + "." + CHAT_COLOR_BACKUP_PREFIX).length());

				Color color = configManager.getConfiguration(GROUP_NAME, CHAT_COLOR_BACKUP_PREFIX + colorKey, Color.class);
				if (color != null)
				{
					configManager.setConfiguration(CHAT_COLOR_CONFIG, colorKey, color);
				}

				configManager.unsetConfiguration(GROUP_NAME, CHAT_COLOR_BACKUP_PREFIX + colorKey);
			}
		}
	}

	@Override
	public void shutDown()
	{
		reset();
	}

	@Subscribe
	public void onPackParsed(PackParsed event)
	{
		ignoreEvent = true;

		var pack = event.getPack();
		var chatColors = pack.getChatColors();

		reset();

		if (chatColors == null || chatColors.isEmpty())
		{
			ignoreEvent = false;
			return;
		}

		save();
		parseColors(chatColors);
		apply();

		ignoreEvent = false;
	}

	@VisibleForTesting
	public void parseColors(TomlTable colors)
	{
		for (var chatColor : ChatColorKey.values())
		{
			Color opaque = toColor(colors.get(chatColor.opaqueOverride()));
			if (opaque != null)
			{
				parsedColors.put(chatColor.opaqueConfig(), opaque);
			}

			Color transparent = toColor(colors.get(chatColor.transparentOverride()));
			if (transparent != null)
			{
				parsedColors.put(chatColor.transparentConfig(), transparent);
			}
		}
	}

	private static Color toColor(Object value)
	{
		if (value instanceof Long)
		{
			return new Color(((Long) value).intValue());
		}
		return null;
	}

	@Override
	public void reset()
	{
		if (savedColors.isEmpty())
		{
			return;
		}

		for (var chatColor : ChatColorKey.values())
		{
			configManager.unsetConfiguration(CHAT_COLOR_CONFIG, chatColor.opaqueConfig());
			configManager.unsetConfiguration(CHAT_COLOR_CONFIG, chatColor.transparentConfig());
		}

		for (var entry : savedColors.entrySet())
		{
			configManager.setConfiguration(CHAT_COLOR_CONFIG, entry.getKey(), entry.getValue());
		}

		savedColors.clear();

		var backupKeys = configManager.getConfigurationKeys(GROUP_NAME + "." + CHAT_COLOR_BACKUP_PREFIX);
		if (backupKeys != null)
		{
			for (var fullKey : backupKeys)
			{
				String colorKey = fullKey.substring((GROUP_NAME + "." + CHAT_COLOR_BACKUP_PREFIX).length());
				configManager.unsetConfiguration(GROUP_NAME, CHAT_COLOR_BACKUP_PREFIX + colorKey);
			}
		}
	}

	@Override
	public void save()
	{
		for (var chatColor : ChatColorKey.values())
		{
			Color opaque = configManager.getConfiguration(CHAT_COLOR_CONFIG, chatColor.opaqueConfig(), Color.class);
			Color transparent = configManager.getConfiguration(CHAT_COLOR_CONFIG, chatColor.transparentConfig(), Color.class);

			if (opaque != null)
			{
				savedColors.put(chatColor.opaqueConfig(), opaque);
			}

			if (transparent != null)
			{
				savedColors.put(chatColor.transparentConfig(), transparent);
			}
		}

		for (var entry : savedColors.entrySet())
		{
			configManager.setConfiguration(GROUP_NAME, CHAT_COLOR_BACKUP_PREFIX + entry.getKey(), entry.getValue());
		}
	}

	@Override
	public void apply()
	{
		ignoreEvent = true;

		for (var chatColor : ChatColorKey.values())
		{
			var oKey = chatColor.opaqueConfig();
			var tKey = chatColor.transparentConfig();

			configManager.unsetConfiguration(CHAT_COLOR_CONFIG, oKey);
			configManager.unsetConfiguration(CHAT_COLOR_CONFIG, tKey);

			if (parsedColors.containsKey(oKey))
			{
				configManager.setConfiguration(CHAT_COLOR_CONFIG, oKey, parsedColors.get(oKey));
			}

			if (parsedColors.containsKey(tKey))
			{
				configManager.setConfiguration(CHAT_COLOR_CONFIG, tKey, parsedColors.get(tKey));
			}
		}

		ignoreEvent = false;
	}


	@Subscribe(priority = Float.MIN_VALUE)
	public void onConfigChanged(ConfigChanged event)
	{
		if (!packsService.isActiveProfile() || ignoreEvent)
		{
			return;
		}

		if (config.allowChatColors() && event.getGroup().equals(CHAT_COLOR_CONFIG))
		{
			if (config.displayWarnings() && System.currentTimeMillis() - lastWarningSent >= WARNING_THROTTLE_MS)
			{
				lastWarningSent = System.currentTimeMillis();
				packsService.sendWarning("Your chat colors will be overwritten by your resource pack. You can disable this feature by turning off 'Allow chat colors to be changed'.");
			}

			apply();
		}
	}
}
