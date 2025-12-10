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

import com.google.common.base.Strings;
import javax.inject.Inject;
import javax.inject.Singleton;
import melky.resourcepacks.ConfigKeys;
import melky.resourcepacks.ResourcePacksConfig;
import melky.resourcepacks.SpriteOverride;
import melky.resourcepacks.event.HubPackSelected;
import melky.resourcepacks.features.overrides.model.OverrideAction;
import melky.resourcepacks.features.packs.PacksManager;
import net.runelite.api.Client;
import net.runelite.api.SpritePixels;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;

@Singleton
public class LoginScreenOverride extends OverrideAction
{
	@Inject
	private EventBus eventBus;

	@Inject
	private ResourcePacksConfig config;

	@Inject
	private ClientThread clientThread;

	@Inject
	private PacksManager packsManager;

	@Inject
	private Client client;

	@Override
	public boolean isEnabled(ResourcePacksConfig config)
	{
		return config.allowLoginScreen();
	}

	@Override
	public void startUp()
	{
		clientThread.invokeLater(this::apply);
	}

	@Override
	public void shutDown()
	{
		reset();
	}

	@Subscribe
	public void onHubPackSelected(HubPackSelected event)
	{
		if (Strings.isNullOrEmpty(config.selectedHubPack()))
		{
			clientThread.invokeLater(this::reset);
		}
	}

	@Subscribe(priority = Float.MIN_VALUE)
	public void onConfigChanged(ConfigChanged event)
	{
		if (!packsManager.isActiveProfile())
		{
			return;
		}

		if (event.getGroup().equals(ResourcePacksConfig.GROUP_NAME) && event.getKey().equals("allowLoginScreen"))
		{
			if (config.allowLoginScreen())
			{
				clientThread.invokeLater(packsManager::updateAllOverrides);
			}
			else
			{
				reset();
			}
		}
	}

	@Override
	public void apply()
	{
		SpritePixels spritePixels = packsManager.getSpritePixels(SpriteOverride.LOGIN_SCREEN_BACKGROUND, packsManager.getCurrentPackPath());
		if (spritePixels != null)
		{
			client.setLoginScreen(spritePixels);
		}
		else
		{
			reset();
		}
	}

	@Override
	public void reset()
	{
		ConfigChanged loginScreenConfigChanged = new ConfigChanged();
		loginScreenConfigChanged.setGroup(ConfigKeys.LoginScreen.GROUP_NAME);
		loginScreenConfigChanged.setKey("doesn't matter");
		loginScreenConfigChanged.setOldValue(null);
		loginScreenConfigChanged.setNewValue("");
		eventBus.post(loginScreenConfigChanged);
	}
}
