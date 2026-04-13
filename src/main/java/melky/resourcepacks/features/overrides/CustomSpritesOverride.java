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
import java.nio.file.Files;
import java.nio.file.Path;
import javax.inject.Inject;
import javax.inject.Singleton;
import melky.resourcepacks.ResourcePacksConfig;
import melky.resourcepacks.event.UpdateAllOverrides;
import melky.resourcepacks.features.overrides.model.OverrideAction;
import melky.resourcepacks.features.packs.PacksManager;
import melky.resourcepacks.model.SpriteOverride;
import melky.resourcepacks.model.TabSprites;
import net.runelite.api.Client;
import net.runelite.api.SpritePixels;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.util.ImageUtil;

@Singleton
public class CustomSpritesOverride extends OverrideAction
{
	@Inject
	private PacksManager packsManager;

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Override
	public boolean isEnabled(ResourcePacksConfig config)
	{
		return !packsManager.isPackPathEmpty();
	}

	@Override
	public void startUp()
	{
		clientThread.invokeLater(() ->
		{
			reset();
			apply();
		});
	}

	@Override
	public void shutDown()
	{
		clientThread.invokeLater(this::reset);
	}

	@Subscribe
	public void onUpdateAllOverrides(UpdateAllOverrides event)
	{
		startUp();
	}

	@Subscribe(priority = Float.MIN_VALUE)
	public void onConfigChanged(ConfigChanged event)
	{
		if (!packsManager.isActiveProfile())
		{
			return;
		}

		if (event.getGroup().equals("banktags") && event.getKey().equals("useTabs"))
		{
			clientThread.invoke(this::apply);
		}
	}


	@Override
	public void reset()
	{
		for (TabSprites tabSprite : TabSprites.values())
		{
			BufferedImage image = ImageUtil.loadImageResource(getClass(), tabSprite.getFileName());
			SpritePixels sp = ImageUtil.getImageSpritePixels(image, client);
			client.getSpriteOverrides().put(tabSprite.getSpriteId(), sp);
		}
	}

	@Override
	public void apply()
	{
		String currentPackPath = packsManager.getCurrentPackPath();
		SpriteOverride.getOverrides().asMap().forEach((key, collection) ->
		{
			if (!Files.isDirectory(Path.of(currentPackPath, key.name().toLowerCase())))
			{
				return;
			}

			for (SpriteOverride spriteOverride : collection)
			{
				if (spriteOverride.getSpriteID() < -200)
				{
					SpritePixels spritePixels = packsManager.getSpritePixels(spriteOverride, currentPackPath);
					if (spritePixels == null)
					{
						continue;
					}

					client.getSpriteOverrides().remove(spriteOverride.getSpriteID());
					client.getSpriteOverrides().put(spriteOverride.getSpriteID(), spritePixels);
				}
			}
		});
	}
}
