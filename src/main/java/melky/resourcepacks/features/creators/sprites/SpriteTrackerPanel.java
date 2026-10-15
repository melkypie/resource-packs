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

package melky.resourcepacks.features.creators.sprites;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeSet;
import javax.inject.Inject;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Scrollable;
import lombok.extern.slf4j.Slf4j;
import static melky.resourcepacks.features.creators.sprites.SpriteTracker.TRACKER_DEFAULT_VISIBLE;
import melky.resourcepacks.features.state.LocalConfig;
import melky.resourcepacks.model.SpriteOverride.Folder;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

@Slf4j
public class SpriteTrackerPanel extends PluginPanel
{
	private final SpriteTracker spriteTracker;
	private final LocalConfig localConfig;
	private final Map<Folder, SpriteTrackerGroup> groups = new LinkedHashMap<>();
	private final Map<Integer, SpriteTrackerPanelItem> itemsBySpriteId = new HashMap<>();

	private JPanel spriteListPanel;

	@Inject
	public SpriteTrackerPanel(SpriteTracker spriteTracker, LocalConfig localConfig)
	{
		super(false);

		this.spriteTracker = spriteTracker;
		this.localConfig = localConfig;

		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		add(createSpriteListPanel(), BorderLayout.CENTER);

		updateAllSpriteVisibility(TRACKER_DEFAULT_VISIBLE);
		updateSpriteList();
	}

	private JPanel createSpriteListPanel()
	{
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBackground(ColorScheme.DARK_GRAY_COLOR);

		JLabel titleLabel = new JLabel("Tracked Sprites:");
		titleLabel.setForeground(Color.WHITE);
		titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));

		spriteListPanel = new WidthTrackingPanel();
		spriteListPanel.setLayout(new BoxLayout(spriteListPanel, BoxLayout.Y_AXIS));
		spriteListPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		JScrollPane scrollPane = new JScrollPane(spriteListPanel);
		scrollPane.setPreferredSize(new Dimension(0, 400));
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		scrollPane.getViewport().setBackground(ColorScheme.DARKER_GRAY_COLOR);

		panel.add(titleLabel, BorderLayout.NORTH);
		panel.add(scrollPane, BorderLayout.CENTER);

		return panel;
	}

	public void updateSpriteList()
	{
		spriteListPanel.removeAll();
		groups.clear();
		itemsBySpriteId.clear();

		for (var highlight : spriteTracker.getHighlightList())
		{
			var group = groups.computeIfAbsent(highlight.getOverride().getFolder(), folder ->
				new SpriteTrackerGroup(folder, this::onSpriteVisibilityChanged));
			var item = new SpriteTrackerPanelItem(highlight);
			group.addItem(item);
			itemsBySpriteId.put(highlight.getSpriteId(), item);
		}

		var sortedFolders = new TreeSet<>(Comparator.comparing(Folder::name));
		sortedFolders.addAll(groups.keySet());

		for (var folder : sortedFolders)
		{
			var group = groups.get(folder);
			group.refreshGroupState();
			spriteListPanel.add(group);
		}

		spriteListPanel.revalidate();
		spriteListPanel.repaint();
	}

	private void onSpriteVisibilityChanged(boolean visible)
	{
		if (visible && !localConfig.getState(SpriteOverlay.class))
		{
			localConfig.setState(SpriteOverlay.class, true);
		}
	}

	private void updateAllSpriteVisibility(boolean visible)
	{
		for (var highlight : spriteTracker.getHighlightList())
		{
			highlight.setVisible(visible);
		}
		onSpriteVisibilityChanged(visible);
	}

	boolean areAllSpritesVisible()
	{
		return spriteTracker.getHighlightList().stream().allMatch(SpriteTracker.HighlightOptions::isVisible);
	}

	public void toggleAllSprites()
	{
		updateAllSpriteVisibility(!areAllSpritesVisible());
		updateSpriteList();
	}

	public boolean isSpriteVisible(int spriteId)
	{
		return false;
	}

	@Override
	public void onActivate()
	{
	}

	@Override
	public void onDeactivate()
	{
		if (localConfig.getState(SpritePicker.class))
		{
			localConfig.setState(SpritePicker.class, false);
		}
	}

	public void refreshVisibility()
	{
		for (var item : itemsBySpriteId.values())
		{
			item.setSelected(item.getOptions().isVisible());
		}
	}

	public void rebuild()
	{
		boolean hasNew = false;
		for (var highlight : spriteTracker.getHighlightList())
		{
			if (!itemsBySpriteId.containsKey(highlight.getSpriteId()))
			{
				hasNew = true;
				break;
			}
		}

		if (hasNew)
		{
			updateSpriteList();
		}
	}

	void clear()
	{
		spriteListPanel.removeAll();
		groups.clear();
		itemsBySpriteId.clear();
		spriteListPanel.revalidate();
		spriteListPanel.repaint();
	}

	private static class WidthTrackingPanel extends JPanel implements Scrollable
	{
		WidthTrackingPanel()
		{
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		}

		@Override
		public Dimension getPreferredScrollableViewportSize()
		{
			return new Dimension(0, 400);
		}

		@Override
		public boolean getScrollableTracksViewportWidth()
		{
			return true;
		}

		@Override
		public boolean getScrollableTracksViewportHeight()
		{
			return false;
		}

		@Override
		public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction)
		{
			return 16;
		}

		@Override
		public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction)
		{
			return orientation == javax.swing.SwingConstants.VERTICAL ? visibleRect.height : visibleRect.width;
		}
	}

}
