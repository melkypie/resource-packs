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

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import melky.resourcepacks.model.ConfigKeys;
import melky.resourcepacks.ResourcePacksConfig;
import melky.resourcepacks.model.SpriteOverride;
import melky.resourcepacks.event.UpdateAllOverrides;
import melky.resourcepacks.features.overrides.model.OverrideAction;
import melky.resourcepacks.features.packs.PacksManager;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.SpritePixels;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.RuneLiteConfig;
import net.runelite.client.eventbus.Subscribe;

@Singleton
public class CrossSpriteOverride extends OverrideAction
{
	@Inject
	private Client client;

	@Inject
	private ConfigManager configManager;

	@Inject
	private ClientThread clientThread;

	@Inject
	private PacksManager packsManager;

	final List<SpritePixels> clientCrossSprites = new ArrayList<>();
	private boolean rsCrossSprites;

	@Override
	public boolean isEnabled(ResourcePacksConfig config)
	{
		return config.allowCrossSprites() && !packsManager.isPackPathEmpty();
	}

	@Override
	public void startUp()
	{
		clientThread.invokeLater(() ->
		{
			save();
			reset();
			apply();
		});
	}

	@Override
	public void shutDown()
	{
		reset();
		clientCrossSprites.clear();
	}

	@Subscribe
	public void onUpdateAllOverrides(UpdateAllOverrides event)
	{
		startUp();
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
	public void save()
	{
		boolean interfaceStylesEnabled = configManager.getConfiguration(RuneLiteConfig.GROUP_NAME, ConfigKeys.Plugins.interfacestylesplugin, Boolean.class);
		boolean isRs3 = configManager.getConfiguration(ConfigKeys.InterfaceStyles.GROUP_NAME, ConfigKeys.InterfaceStyles.rsCrossSprites, Boolean.class);
		isRs3 &= interfaceStylesEnabled;
		if (!clientCrossSprites.isEmpty() && rsCrossSprites == isRs3)
		{
			return;
		}

		clientCrossSprites.clear();
		rsCrossSprites = isRs3;

		String path = "/cross_sprites/" + (rsCrossSprites ? "rs3" : "osrs") + "/";

		SpritePixels[] crossSprites = client.getCrossSprites();
		if (crossSprites == null)
		{
			return;
		}

		int frames = crossSprites.length;
		for (int i = 0; i < frames; i++)
		{
			SpritePixels newSprite = packsManager.loadResourceSprite(path + i + ".png");
			if (newSprite == null)
			{
				continue;
			}

			clientCrossSprites.add(newSprite);
		}
	}

	@Override
	public void reset()
	{
		SpritePixels[] crossSprites = client.getCrossSprites();

		if (crossSprites != null && clientCrossSprites.size() == crossSprites.length)
		{
			System.arraycopy(clientCrossSprites.toArray(new SpritePixels[0]), 0, crossSprites, 0, crossSprites.length);
		}

	}

	@Override
	public void apply()
	{
		SpritePixels[] crossSprites = client.getCrossSprites();
		if (crossSprites == null)
		{
			return;
		}

		String currentPackPath = packsManager.getCurrentPackPath();
		SpriteOverride.getOverrides().asMap().forEach((key, collection) ->
		{
			if (key != SpriteOverride.Folder.CROSS_SPRITES ||
				!Files.isDirectory(Path.of(currentPackPath, key.name().toLowerCase())))
			{
				return;
			}

			for (SpriteOverride spriteOverride : collection)
			{
				SpritePixels spritePixels = packsManager.getSpritePixels(spriteOverride, currentPackPath);
				if (spritePixels == null)
				{
					continue;
				}

				crossSprites[spriteOverride.getFrameID()] = spritePixels;
			}
		});
	}
}
