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

package melky.resourcepacks.features.widgettracker;

import java.awt.Color;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Stream;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import melky.resourcepacks.ResourcePacksConfig;
import melky.resourcepacks.event.ComponentStateChanged;
import melky.resourcepacks.features.widgettracker.event.WidgetSelected;
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
import net.runelite.client.util.ColorUtil;

@Slf4j
@Singleton
public class WidgetSelector implements PluginLifecycleComponent
{
	static final Color SELECTED_WIDGET_COLOR = Color.CYAN;
	private static final float SELECTED_WIDGET_HUE;

	static
	{
		float[] hsb = new float[3];
		Color.RGBtoHSB(SELECTED_WIDGET_COLOR.getRed(), SELECTED_WIDGET_COLOR.getGreen(), SELECTED_WIDGET_COLOR.getBlue(), hsb);
		SELECTED_WIDGET_HUE = hsb[0];
	}

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private EventBus eventBus;

	@Inject
	private WidgetSelectorOverlay overlay;

	@Inject
	private OverlayManager overlayManager;

	private boolean enabled = false;

	@Getter
	private boolean pickerSelected = false;
	private Widget picker;

	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
		eventBus.post(new ComponentStateChanged());
	}

	@Override
	public boolean isEnabled(ResourcePacksConfig config)
	{
		return enabled;
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


	private static boolean allowedType(int id)
	{
		return id == WidgetType.LINE || id == WidgetType.RECTANGLE;
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

	public void addPickerWidget()
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

		log.info("Picker is {}.{} [{}]", WidgetUtil.componentToInterface(picker.getId()), WidgetUtil.componentToId(picker.getId()), picker.getIndex());

		picker.setSpriteId(SpriteID.OptionsIcons.MOBILE_FINGER_ON_INTERFACE);
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

		MenuEntry[] menuEntries = client.getMenuEntries();

		for (int i = 0; i < menuEntries.length; i++)
		{
			MenuEntry entry = menuEntries[i];
			if (entry.getType() != MenuAction.WIDGET_TARGET_ON_WIDGET)
			{
				continue;
			}

			Widget target = client.getWidget(entry.getParam1());
			String name = WidgetUtil.componentToInterface(entry.getParam1()) + "." + WidgetUtil.componentToId(entry.getParam1());
			if (entry.getParam0() != -1)
			{
				name += " [" + entry.getParam0() + "]";
				target = target.getChild(entry.getParam0());
			}

			if (!allowedType(target.getType()))
			{
				client.setMenuEntries(Arrays.copyOf(client.getMenuEntries(), client.getMenuEntries().length - 1));
				continue;
			}

			Color color = colorForWidget(i, menuEntries.length);

			entry.setTarget(ColorUtil.wrapWithColorTag(name, color));
		}
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

		eventBus.post(new WidgetSelected(target));
	}

	Color colorForWidget(int index, int length)
	{
		float h = SELECTED_WIDGET_HUE + .1f + (.8f / length) * index;

		return Color.getHSBColor(h, 1, 1);
	}

	Widget getWidgetForMenuOption(MenuAction type, int param0, int param1)
	{
		if (type == MenuAction.WIDGET_TARGET_ON_WIDGET)
		{
			Widget w = client.getWidget(param1);
			if (param0 != -1)
			{
				w = w.getChild(param0);
			}

			return w;
		}

		return null;
	}
}
