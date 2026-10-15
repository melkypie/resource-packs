/*
 * Copyright (c) 2025, Ron Young <https://github.com/raiyni>
 * Copyright (c) 2018 Abex
 * Copyright (c) 2017, Kronos <https://github.com/KronosDesign>
 * Copyright (c) 2017, Adam <Adam@sigterm.info>
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

package melky.resourcepacks.features.creators.sprites;

import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Stream;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import melky.resourcepacks.ResourcePacksConfig;
import melky.resourcepacks.features.creators.events.SpritePicked;
import melky.resourcepacks.features.state.LocalConfig;
import melky.resourcepacks.module.PluginLifecycleComponent;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.SpriteID;
import net.runelite.api.widgets.JavaScriptCallback;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetConfig;
import net.runelite.api.widgets.WidgetType;
import net.runelite.api.widgets.WidgetUtil;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.ui.overlay.OverlayManager;

@Slf4j
@Singleton
public class SpritePicker implements PluginLifecycleComponent
{
	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private EventBus eventBus;

	@Inject
	private SpriteTracker spriteTracker;

	@Inject
	private SpritePickerOverlay overlay;

	@Inject
	private OverlayManager overlayManager;

	@Getter
	private boolean pickerSelected = false;
	private Widget picker;

	@Override
	public boolean isEnabled(ResourcePacksConfig config, LocalConfig localConfig)
	{
		return config.developerTools() && localConfig.getState(SpritePicker.class);
	}

	@Override
	public void startUp()
	{
		overlayManager.add(overlay);

		clientThread.invokeLater(() ->
		{
			if (client.getGameState() != GameState.LOGGED_IN)
			{
				return false;
			}

			addPickerWidget();
			return true;
		});
	}

	@Override
	public void shutDown()
	{
		clientThread.invokeLater(() ->
		{
			onPickerDeselect();
			removePickerWidget();
		});

		overlayManager.remove(overlay);
	}

	private void removePickerWidget()
	{
		if (picker == null)
		{
			return;
		}

		Widget parent = picker.getParent();
		if (parent == null)
		{
			return;
		}

		Widget[] children = parent.getChildren();
		if (children == null || children.length <= picker.getIndex() || children[picker.getIndex()] != picker)
		{
			return;
		}

		children[picker.getIndex()] = null;
	}

	private void addPickerWidget()
	{
		removePickerWidget();

		int x = 10, y = 2;
		Widget parent = client.getWidget(InterfaceID.Orbs.UNIVERSE);
		if (parent == null || parent.isHidden())
		{
			parent = client.getWidget(InterfaceID.OrbsNomap.UNIVERSE);
			x = 32;
			y = 0;
		}
		if (parent == null || parent.isHidden())
		{
			Widget[] roots = client.getWidgetRoots();

			parent = Stream.of(roots)
				.filter(w -> w.getType() == WidgetType.LAYER && w.getContentType() == 0 && !w.isSelfHidden())
				.sorted(Comparator.comparingInt((Widget w) -> w.getRelativeX() + w.getRelativeY())
					.reversed()
					.thenComparingInt(Widget::getId)
					.reversed())
				.findFirst().get();
			x = 4;
			y = 4;
		}

		picker = parent.createChild(-1, WidgetType.GRAPHIC);

		log.info("Sprite picker is {}.{} [{}]", WidgetUtil.componentToInterface(picker.getId()), WidgetUtil.componentToId(picker.getId()), picker.getIndex());

		picker.setSpriteId(SpriteID.ICON_CABBAGE);
		picker.setOriginalWidth(15);
		picker.setOriginalHeight(17);
		picker.setOriginalX(x);
		picker.setOriginalY(y);
		picker.revalidate();
		picker.setTargetVerb("Select");
		picker.setName("Pick");
		picker.setClickMask(WidgetConfig.USE_WIDGET);
		picker.setNoClickThrough(true);
		picker.setOnTargetEnterListener((JavaScriptCallback) ev ->
		{
			pickerSelected = true;
			picker.setOpacity(30);
			client.setAllWidgetsAreOpTargetable(true);
		});
		picker.setOnTargetLeaveListener((JavaScriptCallback) ev -> onPickerDeselect());
	}

	private void onPickerDeselect()
	{
		client.setAllWidgetsAreOpTargetable(false);
		pickerSelected = false;
		if (picker != null)
		{
			picker.setOpacity(0);
		}
	}

	@Subscribe
	private void onMenuEntryAdded(MenuEntryAdded event)
	{
		if (!pickerSelected)
		{
			return;
		}

		var menu = client.getMenu();
		if (menu == null)
		{
			return;
		}

		var entries = menu.getMenuEntries();
		if (entries == null)
		{
			return;
		}

		MenuEntry[] filtered = new MenuEntry[entries.length];
		int count = 0;
		for (MenuEntry entry : entries)
		{
			if (entry.getType() == MenuAction.WIDGET_TARGET_ON_WIDGET)
			{
				Widget target = client.getWidget(entry.getParam1());
				if (entry.getParam0() != -1 && target != null)
				{
					target = target.getChild(entry.getParam0());
				}

				var highlight = target == null ? null : spriteTracker.getHighlight(target.getSpriteId());
				if (highlight == null)
				{
					continue;
				}

				entry.setTarget(highlight.getOverride().toString());
			}

			filtered[count++] = entry;
		}

		menu.setMenuEntries(Arrays.copyOf(filtered, count));
	}

	@Subscribe
	private void onMenuOptionClicked(MenuOptionClicked ev)
	{
		if (!pickerSelected)
		{
			return;
		}

		onPickerDeselect();
		client.setWidgetSelected(false);
		ev.consume();

		Widget target = getWidgetForMenuOption(ev.getMenuAction(), ev.getParam0(), ev.getParam1());
		if (target == null)
		{
			return;
		}

		var highlight = spriteTracker.getHighlight(target.getSpriteId());
		if (highlight == null)
		{
			return;
		}

		highlight.setVisible(true);
		eventBus.post(new SpritePicked());
	}

	Widget getWidgetForMenuOption(MenuAction type, int param0, int param1)
	{
		if (type == MenuAction.WIDGET_TARGET_ON_WIDGET)
		{
			Widget w = client.getWidget(param1);
			if (param0 != -1 && w != null)
			{
				w = w.getChild(param0);
			}
			return w;
		}

		return null;
	}
}
