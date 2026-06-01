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

package melky.resourcepacks.features.packs;

import com.google.common.base.Strings;
import java.nio.file.Path;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import melky.resourcepacks.ResourcePacksConfig;
import static melky.resourcepacks.features.packs.PacksManager.PACKS_BASE_DIR;
import melky.resourcepacks.module.PluginLifecycleComponent;
import net.runelite.api.ChatMessageType;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;

@Singleton
public class PacksService implements PluginLifecycleComponent
{
	@Inject
	private ResourcePacksConfig config;

	@Inject
	private ConfigManager configManager;

	@Inject
	private ChatMessageManager chatMessageManager;

	@Getter
	private long currentProfile = Long.MIN_VALUE;

	@Override
	public void startUp()
	{
		currentProfile = configManager.getProfile().getId();
	}

	public boolean isActiveProfile()
	{
		return currentProfile == configManager.getProfile().getId();
	}

	public String getCurrentPackPath()
	{
		switch (config.resourcePack())
		{
			case SECOND:
				return config.resourcePack2Path();
			case THIRD:
				return config.resourcePack3Path();
			case HUB:
				return getLocalPath(config.selectedHubPack()) + "";
			case FIRST:
			default:
				return config.resourcePackPath();
		}
	}

	public boolean isPackPathEmpty()
	{
		switch (config.resourcePack())
		{
			case FIRST:
				return Strings.isNullOrEmpty(config.resourcePackPath());
			case SECOND:
				return Strings.isNullOrEmpty(config.resourcePack2Path());
			case HUB:
				return Strings.isNullOrEmpty(config.selectedHubPack());
			case THIRD:
				return Strings.isNullOrEmpty(config.resourcePack3Path());
		}

		return true;
	}

	public Path getPath(final String file)
	{
		return Path.of(getCurrentPackPath(), file);
	}

	public Path getLocalPath(String path)
	{
		return Path.of(PACKS_BASE_DIR + "", configManager.getProfile().getId() + "", path);
	}

	public void sendWarning(String msg)
	{
		String message = new ChatMessageBuilder()
			.append(ChatColorType.NORMAL)
			.append("[")
			.append(ChatColorType.HIGHLIGHT)
			.append("Resource Packs")
			.append(ChatColorType.NORMAL)
			.append("] " + msg)
			.build();

		chatMessageManager.queue(QueuedMessage.builder()
			.type(ChatMessageType.CONSOLE)
			.runeLiteFormattedMessage(message)
			.build());
	}
}
