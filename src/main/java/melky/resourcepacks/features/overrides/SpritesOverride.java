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
import java.nio.file.Files;
import java.nio.file.Path;
import javax.inject.Inject;
import javax.inject.Singleton;
import melky.resourcepacks.ResourcePacksConfig;
import melky.resourcepacks.SpriteOverride;
import melky.resourcepacks.event.HubPackSelected;
import melky.resourcepacks.event.UpdateAllOverrides;
import melky.resourcepacks.features.overrides.model.OverrideAction;
import melky.resourcepacks.features.packs.PacksManager;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.SpritePixels;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;

@Singleton
public class SpritesOverride extends OverrideAction
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

	@Override
	public boolean isEnabled(ResourcePacksConfig config)
	{
		return true;
	}

	@Override
	public void startUp()
	{
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
		reset();
		apply();
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
		if (packsManager.getCurrentProfile() != configManager.getProfile().getId())
		{
			return;
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGIN_SCREEN)
		{
			apply();
		}
	}

	@Override
	public void reset()
	{
		client.getWidgetSpriteCache().reset();

		for (SpriteOverride spriteOverride : SpriteOverride.values())
		{
			if (!shouldOverride(spriteOverride))
			{
				continue;
			}

			client.getSpriteOverrides().remove(spriteOverride.getSpriteID());
		}
	}

	@Override
	public void apply()
	{
		String currentPackPath = packsManager.getCurrentPackPath();
		SpriteOverride.getOverrides().asMap().forEach((key, collection) ->
		{
			if (!Files.isDirectory(Path.of(currentPackPath, key.name().toLowerCase())) ||
				(!config.allowSpellsPrayers() && (key.name().contains("SPELL") || key.equals(SpriteOverride.Folder.PRAYER))) ||
				key == SpriteOverride.Folder.CROSS_SPRITES)
			{
				return;
			}

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

				client.getSpriteOverrides().put(spriteOverride.getSpriteID(), spritePixels);
			}
		});
	}

	private boolean shouldOverride(SpriteOverride spriteOverride)
	{
		return !(SpriteOverride.LOGIN_SCREEN_BACKGROUND == spriteOverride ||
			spriteOverride.getSpriteID() < -200 ||
			spriteOverride == SpriteOverride.COMPASS);
	}
}
