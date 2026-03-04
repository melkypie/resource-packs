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
import java.util.EnumMap;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import melky.resourcepacks.ResourcePacksConfig;
import melky.resourcepacks.event.UpdateAllOverrides;
import melky.resourcepacks.features.overrides.model.OverrideAction;
import melky.resourcepacks.features.packs.PacksManager;
import melky.resourcepacks.model.SpriteOverride;
import static melky.resourcepacks.model.SpriteOverride.SKILL_AGILITY;
import static melky.resourcepacks.model.SpriteOverride.SKILL_AGILITY_GLOW;
import static melky.resourcepacks.model.SpriteOverride.SKILL_ATTACK;
import static melky.resourcepacks.model.SpriteOverride.SKILL_ATTACK_GLOW;
import static melky.resourcepacks.model.SpriteOverride.SKILL_CONSTRUCTION;
import static melky.resourcepacks.model.SpriteOverride.SKILL_CONSTRUCTION_GLOW;
import static melky.resourcepacks.model.SpriteOverride.SKILL_COOKING;
import static melky.resourcepacks.model.SpriteOverride.SKILL_COOKING_GLOW;
import static melky.resourcepacks.model.SpriteOverride.SKILL_CRAFTING;
import static melky.resourcepacks.model.SpriteOverride.SKILL_CRAFTING_GLOW;
import static melky.resourcepacks.model.SpriteOverride.SKILL_DEFENCE;
import static melky.resourcepacks.model.SpriteOverride.SKILL_DEFENCE_GLOW;
import static melky.resourcepacks.model.SpriteOverride.SKILL_FARMING;
import static melky.resourcepacks.model.SpriteOverride.SKILL_FARMING_GLOW;
import static melky.resourcepacks.model.SpriteOverride.SKILL_FIREMAKING;
import static melky.resourcepacks.model.SpriteOverride.SKILL_FIREMAKING_GLOW;
import static melky.resourcepacks.model.SpriteOverride.SKILL_FISHING;
import static melky.resourcepacks.model.SpriteOverride.SKILL_FISHING_GLOW;
import static melky.resourcepacks.model.SpriteOverride.SKILL_FLETCHING;
import static melky.resourcepacks.model.SpriteOverride.SKILL_FLETCHING_GLOW;
import static melky.resourcepacks.model.SpriteOverride.SKILL_HERBLORE;
import static melky.resourcepacks.model.SpriteOverride.SKILL_HERBLORE_GLOW;
import static melky.resourcepacks.model.SpriteOverride.SKILL_HITPOINTS;
import static melky.resourcepacks.model.SpriteOverride.SKILL_HITPOINTS_GLOW;
import static melky.resourcepacks.model.SpriteOverride.SKILL_HUNTER;
import static melky.resourcepacks.model.SpriteOverride.SKILL_HUNTER_GLOW;
import static melky.resourcepacks.model.SpriteOverride.SKILL_MAGIC;
import static melky.resourcepacks.model.SpriteOverride.SKILL_MAGIC_GLOW;
import static melky.resourcepacks.model.SpriteOverride.SKILL_MINING;
import static melky.resourcepacks.model.SpriteOverride.SKILL_MINING_GLOW;
import static melky.resourcepacks.model.SpriteOverride.SKILL_PRAYER;
import static melky.resourcepacks.model.SpriteOverride.SKILL_PRAYER_GLOW;
import static melky.resourcepacks.model.SpriteOverride.SKILL_RANGED;
import static melky.resourcepacks.model.SpriteOverride.SKILL_RANGED_GLOW;
import static melky.resourcepacks.model.SpriteOverride.SKILL_RUNECRAFT;
import static melky.resourcepacks.model.SpriteOverride.SKILL_RUNECRAFT_GLOW;
import static melky.resourcepacks.model.SpriteOverride.SKILL_SAILING;
import static melky.resourcepacks.model.SpriteOverride.SKILL_SAILING_GLOW;
import static melky.resourcepacks.model.SpriteOverride.SKILL_SLAYER;
import static melky.resourcepacks.model.SpriteOverride.SKILL_SLAYER_GLOW;
import static melky.resourcepacks.model.SpriteOverride.SKILL_SMITHING;
import static melky.resourcepacks.model.SpriteOverride.SKILL_SMITHING_GLOW;
import static melky.resourcepacks.model.SpriteOverride.SKILL_STRENGTH;
import static melky.resourcepacks.model.SpriteOverride.SKILL_STRENGTH_GLOW;
import static melky.resourcepacks.model.SpriteOverride.SKILL_THIEVING;
import static melky.resourcepacks.model.SpriteOverride.SKILL_THIEVING_GLOW;
import static melky.resourcepacks.model.SpriteOverride.SKILL_WOODCUTTING;
import static melky.resourcepacks.model.SpriteOverride.SKILL_WOODCUTTING_GLOW;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.SpritePixels;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.util.ImageUtil;

@Slf4j
@Singleton
public class SpritesOverride extends OverrideAction
{
	private static EnumMap<SpriteOverride, SpriteOverride> GLOW_SPRITES = new EnumMap<>(SpriteOverride.class);

	static
	{
		GLOW_SPRITES.put(SKILL_ATTACK_GLOW, SKILL_ATTACK);
		GLOW_SPRITES.put(SKILL_STRENGTH_GLOW, SKILL_STRENGTH);
		GLOW_SPRITES.put(SKILL_DEFENCE_GLOW, SKILL_DEFENCE);
		GLOW_SPRITES.put(SKILL_RANGED_GLOW, SKILL_RANGED);
		GLOW_SPRITES.put(SKILL_PRAYER_GLOW, SKILL_PRAYER);
		GLOW_SPRITES.put(SKILL_MAGIC_GLOW, SKILL_MAGIC);
		GLOW_SPRITES.put(SKILL_HITPOINTS_GLOW, SKILL_HITPOINTS);
		GLOW_SPRITES.put(SKILL_AGILITY_GLOW, SKILL_AGILITY);
		GLOW_SPRITES.put(SKILL_HERBLORE_GLOW, SKILL_HERBLORE);
		GLOW_SPRITES.put(SKILL_THIEVING_GLOW, SKILL_THIEVING);
		GLOW_SPRITES.put(SKILL_CRAFTING_GLOW, SKILL_CRAFTING);
		GLOW_SPRITES.put(SKILL_FLETCHING_GLOW, SKILL_FLETCHING);
		GLOW_SPRITES.put(SKILL_MINING_GLOW, SKILL_MINING);
		GLOW_SPRITES.put(SKILL_SMITHING_GLOW, SKILL_SMITHING);
		GLOW_SPRITES.put(SKILL_FISHING_GLOW, SKILL_FISHING);
		GLOW_SPRITES.put(SKILL_COOKING_GLOW, SKILL_COOKING);
		GLOW_SPRITES.put(SKILL_FIREMAKING_GLOW, SKILL_FIREMAKING);
		GLOW_SPRITES.put(SKILL_WOODCUTTING_GLOW, SKILL_WOODCUTTING);
		GLOW_SPRITES.put(SKILL_RUNECRAFT_GLOW, SKILL_RUNECRAFT);
		GLOW_SPRITES.put(SKILL_SLAYER_GLOW, SKILL_SLAYER);
		GLOW_SPRITES.put(SKILL_FARMING_GLOW, SKILL_FARMING);
		GLOW_SPRITES.put(SKILL_HUNTER_GLOW, SKILL_HUNTER);
		GLOW_SPRITES.put(SKILL_CONSTRUCTION_GLOW, SKILL_CONSTRUCTION);
		GLOW_SPRITES.put(SKILL_SAILING_GLOW, SKILL_SAILING);

	}

	@Inject
	private Client client;

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
		log.debug("applying sprite overrides");

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
					if (GLOW_SPRITES.containsKey(spriteOverride))
					{
						var override = GLOW_SPRITES.get(spriteOverride);
						var baseSprite = client.getSpriteOverrides().get(override.getSpriteID());
						if (baseSprite == null)
						{
							continue;
						}

						var image = baseSprite.toBufferedImage();
						applyScreenGlow(image);

						var sprite = ImageUtil.getImageSpritePixels(image, client);
						client.getSpriteOverrides().put(spriteOverride.getSpriteID(), sprite);
					}

					continue;
				}

				client.getSpriteOverrides().put(spriteOverride.getSpriteID(), spritePixels);
			}
		});
	}

	private static boolean shouldOverride(SpriteOverride spriteOverride)
	{
		return !(SpriteOverride.LOGIN_SCREEN_BACKGROUND == spriteOverride ||
			spriteOverride.getSpriteID() < -200 ||
			spriteOverride == SpriteOverride.COMPASS);
	}

	public static void applyScreenGlow(BufferedImage image)
	{
		int width = image.getWidth();
		int height = image.getHeight();

		for (int y = 0; y < height; y++)
		{
			for (int x = 0; x < width; x++)
			{
				int pixel = image.getRGB(x, y);
				int alpha = (pixel >> 24) & 0xff;

				if (alpha == 0)
				{
					continue;
				}

				int r = (pixel >> 16) & 0xff;
				int g = (pixel >> 8) & 0xff;
				int b = pixel & 0xff;

				int min = Math.min(r, Math.min(g, b));

				int newR = (int) (208 + (r * 0.055) - (g * 0.080) - (b * 0.094) + (min * 0.11));
				int newG = (int) (208 - (r * 0.041) + (g * 0.064) - (b * 0.096) + (min * 0.07));
				int newB = (int) (142 - (r * 0.024) + (g * 0.106) + (b * 0.269) - (min * 0.17));

				newR = Math.min(255, Math.max(0, newR));
				newG = Math.min(255, Math.max(0, newG));
				newB = Math.min(255, Math.max(0, newB));

				int newPixel = (alpha << 24) | (newR << 16) | (newG << 8) | newB;
				image.setRGB(x, y, newPixel);
			}
		}
	}
}
