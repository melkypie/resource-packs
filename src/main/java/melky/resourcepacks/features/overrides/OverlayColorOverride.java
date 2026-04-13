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
import java.awt.Color;
import javax.inject.Inject;
import javax.inject.Singleton;
import melky.resourcepacks.ResourcePacksConfig;
import melky.resourcepacks.event.UpdateAllOverrides;
import melky.resourcepacks.features.overrides.model.OverrideAction;
import melky.resourcepacks.features.packs.PacksManager;
import static melky.resourcepacks.model.RuneLiteConfig.OVERLAY_COLOR_CONFIG;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.RuneLiteConfig;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;

@Singleton
public class OverlayColorOverride extends OverrideAction
{
	@Inject
	private ConfigManager configManager;

	@Inject
	private ResourcePacksConfig config;

	@Inject
	private Overrides overrides;

	@Inject
	private PacksManager packsManager;

	@Inject
	private ClientThread clientThread;

	private boolean ignoreOverlayConfig;

	@Override
	public boolean isEnabled(ResourcePacksConfig config)
	{
		return config.allowOverlayColor() && !packsManager.isPackPathEmpty();
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

	@Subscribe(priority = Float.MIN_VALUE)
	public void onConfigChanged(ConfigChanged event)
	{
		if (!packsManager.isActiveProfile())
		{
			return;
		}

		if (config.allowOverlayColor() && !ignoreOverlayConfig &&
			event.getGroup().equals(RuneLiteConfig.GROUP_NAME) && event.getKey().equals(OVERLAY_COLOR_CONFIG))
		{
			config.originalOverlayColor(event.getNewValue());

			if (config.displayWarnings())
			{
				packsManager.sendWarning("Your overlay color will be overwritten by your resource pack. You can disable this feature by turning off 'Allow overlay color to be changed'.");
			}
		}
	}

	@Subscribe
	public void onUpdateAllOverrides(UpdateAllOverrides events)
	{
		startUp();
	}

	@Override
	public void apply()
	{
		if (Strings.isNullOrEmpty(config.originalOverlayColor()))
		{
			config.originalOverlayColor(configManager.getConfiguration(RuneLiteConfig.GROUP_NAME, OVERLAY_COLOR_CONFIG));
		}

		ignoreOverlayConfig = true;
		Color overlayColor = overrides.getOverlayColor();
		if (config.allowColorPack() && config.colorPack() != null && config.colorPack().getAlpha() != 0 && config.colorPackOverlay())
		{
			overlayColor = config.colorPack();
		}

		configManager.setConfiguration(RuneLiteConfig.GROUP_NAME, OVERLAY_COLOR_CONFIG, overlayColor);
		ignoreOverlayConfig = false;
	}

	@Override
	public void reset()
	{
		if (!Strings.isNullOrEmpty(config.originalOverlayColor()))
		{
			configManager.setConfiguration(RuneLiteConfig.GROUP_NAME, OVERLAY_COLOR_CONFIG, config.originalOverlayColor());
			configManager.unsetConfiguration(ResourcePacksConfig.GROUP_NAME, ResourcePacksConfig.ORIGINAL_OVERLAY_COLOR);
		}
	}
}
