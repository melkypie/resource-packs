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

package melky.resourcepacks.features.widgettracker;

import com.google.inject.Injector;
import java.awt.image.BufferedImage;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import melky.resourcepacks.ResourcePacksConfig;
import melky.resourcepacks.module.PluginLifecycleComponent;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

@Slf4j
@Singleton
public class WidgetTrackerModule implements PluginLifecycleComponent
{
	@Inject
	private WidgetTracker widgetTracker;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private Injector injector;

	@Inject
	private EventBus eventBus;

	@Inject
	@Named("developerMode")
	private boolean developerMode;

	private NavigationButton button;
	private NavigationButton button2;
	private WidgetTrackerPanel widgetTrackerPanel;
	private ScriptWatcherPanel scriptWatcherPanel;

	@Override
	public boolean isEnabled(ResourcePacksConfig config)
	{
		return developerMode && config.developerTools();
	}

	@Override
	public void startUp()
	{
		final BufferedImage icon2 = ImageUtil.loadImageResource(getClass(), "/help.png");
		widgetTrackerPanel = injector.getInstance(WidgetTrackerPanel.class);
		button = NavigationButton.builder()
			.tooltip("Debug Panel")
			.icon(icon2)
			.panel(widgetTrackerPanel)
			.build();

		final BufferedImage icon3 = ImageUtil.loadImageResource(getClass(), "/net/runelite/client/plugins/config/mdi_alert.png");
		scriptWatcherPanel = injector.getInstance(ScriptWatcherPanel.class);
		button2 = NavigationButton.builder()
			.tooltip("Script Panel")
			.icon(icon3)
			.panel(scriptWatcherPanel)
			.build();

		clientToolbar.addNavigation(button);
		clientToolbar.addNavigation(button2);

		eventBus.register(widgetTrackerPanel);
		eventBus.register(scriptWatcherPanel);
	}

	@Override
	public void shutDown()
	{
		widgetTracker.getTrackedWidgets().clear();

		clientToolbar.removeNavigation(button);
		clientToolbar.removeNavigation(button2);
		eventBus.unregister(widgetTrackerPanel);
		eventBus.unregister(scriptWatcherPanel);
		widgetTrackerPanel = null;
		scriptWatcherPanel = null;
	}
}
