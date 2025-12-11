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

import java.awt.image.BufferedImage;
import javax.inject.Inject;
import javax.inject.Singleton;
import melky.resourcepacks.ConfigKeys;
import melky.resourcepacks.ResourcePacksConfig;
import melky.resourcepacks.SpriteOverride;
import melky.resourcepacks.event.UpdateAllOverrides;
import melky.resourcepacks.features.overrides.model.OverrideAction;
import melky.resourcepacks.features.packs.PacksManager;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.SpritePixels;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.gameval.SpriteID;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.plugins.interfacestyles.Skin;
import net.runelite.client.util.ImageUtil;

@Singleton
public class GameFrameOverride extends OverrideAction
{
	@Inject
	private Client client;

	@Inject
	private ConfigManager configManager;

	@Inject
	private ClientThread clientThread;

	@Inject
	private PacksManager packsManager;

	@Inject
	private ResourcePacksConfig config;

	@Inject
	private SpriteManager spriteManager;

	@Override
	public boolean isEnabled(ResourcePacksConfig config)
	{
		return config.allowCrossSprites() && !packsManager.isPackPathEmpty();
	}

	@Override
	public void startUp()
	{
		if (client.getGameState() == GameState.LOGGED_IN &&
			configManager.getConfiguration(ConfigKeys.InterfaceStyles.GROUP_NAME, ConfigKeys.InterfaceStyles.gameframe, Skin.class) != Skin.DEFAULT &&
			!config.disableInterfaceStylesPrompt())
		{
			setInterfaceStylesGameframeOption();
		}

		clientThread.invokeLater(this::apply);
	}

	@Override
	public void shutDown()
	{
		clientThread.invokeLater(this::reset);
	}

	@Subscribe
	public void onUpdateAllOverrides(UpdateAllOverrides event)
	{
		clientThread.invokeLater(() ->
		{
			reset();
			apply();
		});
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGIN_SCREEN)
		{
			apply();
		}

		if (client.getGameState() == GameState.LOGGED_IN &&
			configManager.getConfiguration(ConfigKeys.InterfaceStyles.GROUP_NAME, ConfigKeys.InterfaceStyles.gameframe, Skin.class) != Skin.DEFAULT &&
			!config.disableInterfaceStylesPrompt())
		{
			setInterfaceStylesGameframeOption();

			clientThread.invokeLater(packsManager::updateAllOverrides);
		}
	}

	@Override
	public void reset()
	{
		BufferedImage compassImage = spriteManager.getSprite(SpriteID.COMPASS, 0);

		if (compassImage != null)
		{
			SpritePixels compass = ImageUtil.getImageSpritePixels(compassImage, client);
			client.setCompass(compass);
		}
	}

	@Override
	public void apply()
	{
		String currentPackPath = packsManager.getCurrentPackPath();
		SpriteOverride.getOverrides().asMap().forEach((key, collection) ->
		{
			for (SpriteOverride spriteOverride : collection)
			{
				if (!shouldOverride(spriteOverride))
				{
					continue;
				}

				SpritePixels spritePixels = packsManager.getSpritePixels(spriteOverride, currentPackPath);
				if (spritePixels == null)
				{
					continue;
				}

				if (spriteOverride.getSpriteID() == SpriteID.COMPASS)
				{
					client.setCompass(spritePixels);
				}
			}
		});
	}

	private boolean shouldOverride(SpriteOverride spriteOverride)
	{
		return (spriteOverride == SpriteOverride.COMPASS);
	}

	private void setInterfaceStylesGameframeOption()
	{
		if (config.displayWarnings())
		{
			packsManager.sendWarning("Your interface styles gameframe option was set to default to fix interfaces being misaligned. You can disable Resource packs changing it to default inside it's config");
		}

		configManager.setConfiguration(ConfigKeys.InterfaceStyles.GROUP_NAME, ConfigKeys.InterfaceStyles.gameframe, Skin.DEFAULT);
	}
}
