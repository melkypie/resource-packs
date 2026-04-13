/*
 * Copyright (c) 2025, Ron Young <https://github.com/raiyni>
 * Copyright (c) 2025, LlemonDuck
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

package melky.resourcepacks.module;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import melky.resourcepacks.ResourcePacksConfig;
import melky.resourcepacks.event.ComponentStateChanged;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.GameEventManager;

/**
 * Manages all the subcomponents of the plugin
 * so they can register themselves to RuneLite resources
 * e.g. EventBus/OverlayManager/init on startup/etc
 */
@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
@Slf4j
public class ComponentManager
{
	private final EventBus eventBus;
	private final OverlayManager overlayManager;
	private final GameEventManager gameEventManager;
	private final ResourcePacksConfig config;
	private final Set<PluginLifecycleComponent> components;

	private final Map<PluginLifecycleComponent, Boolean> states = new HashMap<>();

	public void onPluginStart()
	{
		eventBus.register(this);
		components.forEach(c -> states.put(c, false));
		revalidateComponentStates();
	}

	public void onPluginStop()
	{
		eventBus.unregister(this);
		components.stream()
			.filter(states::get)
			.forEach(this::tryShutDown);
	}

	@Subscribe
	public void onComponentStateChanged(ComponentStateChanged event)
	{
		revalidateComponentStates();
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged e)
	{
		if (!ResourcePacksConfig.GROUP_NAME.equals(e.getGroup()))
		{
			return;
		}

		revalidateComponentStates();
	}

	private void revalidateComponentStates()
	{
		components.forEach(c ->
		{
			boolean shouldBeEnabled = c.isEnabled(config);
			boolean isEnabled = states.get(c);
			if (shouldBeEnabled == isEnabled)
			{
				return;
			}

			if (shouldBeEnabled)
			{
				tryStartUp(c);
			}
			else
			{
				tryShutDown(c);
			}
		});
	}

	private void tryStartUp(PluginLifecycleComponent component)
	{
		if (states.get(component))
		{
			return;
		}

		if (log.isDebugEnabled())
		{
			log.debug("Enabling resource packs component [{}]", component.getClass().getName());
		}

		try
		{
			component.startUp();

			eventBus.register(component);
			if (component instanceof Overlay)
			{
				overlayManager.add((Overlay) component);
			}

			gameEventManager.simulateGameEvents(component);
			states.put(component, true);
		}
		catch (Throwable e)
		{
			log.error("Failed to start resource packs component [{}]", component.getClass().getName(), e);
		}
	}

	private void tryShutDown(PluginLifecycleComponent component)
	{
		eventBus.unregister(component);
		if (component instanceof Overlay)
		{
			overlayManager.remove((Overlay) component);
		}

		if (!states.get(component))
		{
			return;
		}

		if (log.isDebugEnabled())
		{
			log.debug("Disabling resource packs component [{}]", component.getClass().getName());
		}

		try
		{
			component.shutDown();
		}
		catch (Throwable e)
		{
			log.error("Failed to cleanly shut down resource packs component [{}]", component.getClass().getName());
		}
		finally
		{
			states.put(component, false);
		}
	}

}
