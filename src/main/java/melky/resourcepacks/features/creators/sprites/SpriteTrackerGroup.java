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
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import lombok.Getter;
import melky.resourcepacks.model.SpriteOverride.Folder;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

public class SpriteTrackerGroup extends JPanel
{
	@Getter
	private final Folder folder;

	private final TriStateCheckBox groupCheckbox;
	private final JLabel expandLabel;
	private final JLabel countLabel;
	private final JPanel itemsPanel;
	private final List<SpriteTrackerPanelItem> items = new ArrayList<>();
	private final Consumer<Boolean> onVisibilityChanged;
	private boolean collapsed = true;

	public SpriteTrackerGroup(Folder folder)
	{
		this(folder, visible ->
		{
		});
	}

	public SpriteTrackerGroup(Folder folder, Consumer<Boolean> onVisibilityChanged)
	{
		this.folder = folder;
		this.onVisibilityChanged = onVisibilityChanged;

		setLayout(new BorderLayout());
		setBackground(ColorScheme.DARKER_GRAY_COLOR);
		setAlignmentX(LEFT_ALIGNMENT);

		JPanel header = new JPanel(new BorderLayout(0, 0));
		header.setBackground(ColorScheme.DARK_GRAY_COLOR);
		header.setBorder(BorderFactory.createEmptyBorder(2, 3, 2, 3));
		header.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

		groupCheckbox = new TriStateCheckBox();
		groupCheckbox.setBackground(ColorScheme.DARK_GRAY_COLOR);
		groupCheckbox.setForeground(Color.WHITE);
		groupCheckbox.setFocusPainted(false);
		groupCheckbox.addActionListener(e -> toggleGroup());

		expandLabel = new JLabel("▶");
		expandLabel.setForeground(Color.WHITE);
		expandLabel.setFont(expandLabel.getFont().deriveFont(10f));
		expandLabel.setPreferredSize(new Dimension(12, 12));
		expandLabel.setMinimumSize(new Dimension(12, 12));
		expandLabel.setMaximumSize(new Dimension(12, 12));
		expandLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		groupCheckbox.setIcon(new SmallCheckBoxIcon(false));
		groupCheckbox.setSelectedIcon(new SmallCheckBoxIcon(true));
		Dimension checkboxDimension = new Dimension(14, 14);
		groupCheckbox.setPreferredSize(checkboxDimension);
		groupCheckbox.setMinimumSize(checkboxDimension);
		groupCheckbox.setMaximumSize(checkboxDimension);

		JLabel folderLabel = new JLabel(folder.name());
		folderLabel.setFont(FontManager.getRunescapeBoldFont());
		folderLabel.setForeground(Color.WHITE);
		folderLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		folderLabel.setToolTipText(folder.name());

		countLabel = new JLabel();
		countLabel.setForeground(ColorScheme.MEDIUM_GRAY_COLOR);
		countLabel.setHorizontalAlignment(JLabel.RIGHT);

		MouseAdapter collapseToggle = new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent e)
			{
				toggleCollapsed();
			}
		};

		header.addMouseListener(collapseToggle);
		folderLabel.addMouseListener(collapseToggle);
		expandLabel.addMouseListener(collapseToggle);

		JPanel leading = new JPanel(new FlowLayout(FlowLayout.LEFT, 1, 0));
		leading.setOpaque(false);
		leading.add(groupCheckbox);
		leading.add(expandLabel);
		header.add(leading, BorderLayout.WEST);
		header.add(folderLabel, BorderLayout.CENTER);
		header.add(countLabel, BorderLayout.EAST);

		itemsPanel = new JPanel();
		itemsPanel.setLayout(new BoxLayout(itemsPanel, BoxLayout.Y_AXIS));
		itemsPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		itemsPanel.setBorder(BorderFactory.createEmptyBorder());

		add(header, BorderLayout.NORTH);
		add(itemsPanel, BorderLayout.CENTER);

		setCollapsed(true);
	}

	public void addItem(SpriteTrackerPanelItem item)
	{
		items.add(item);
		itemsPanel.add(item);
		item.setOnVisibilityChanged(() ->
		{
			refreshGroupState();
			onVisibilityChanged.accept(item.isSelected());
		});
	}

	private void toggleGroup()
	{
		boolean allSelected = items.stream().allMatch(SpriteTrackerPanelItem::isSelected);
		boolean newState = !allSelected;

		for (var item : items)
		{
			item.setSelected(newState);
		}

		refreshGroupState();
	}

	private void toggleCollapsed()
	{
		setCollapsed(!collapsed);
	}

	private void setCollapsed(boolean collapsed)
	{
		this.collapsed = collapsed;
		itemsPanel.setVisible(!collapsed);
		expandLabel.setText(collapsed ? "▶" : "▼");
		revalidate();
		repaint();
	}

	public void refreshGroupState()
	{
		boolean anySelected = items.stream().anyMatch(SpriteTrackerPanelItem::isSelected);
		boolean allSelected = items.stream().allMatch(SpriteTrackerPanelItem::isSelected);

		groupCheckbox.setSelected(allSelected);
		groupCheckbox.setIndeterminate(!allSelected && anySelected);
		countLabel.setText(items.stream().filter(SpriteTrackerPanelItem::isSelected).count() + "/" + items.size());
	}

	@Override
	public Dimension getMaximumSize()
	{
		return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
	}

	private static class TriStateCheckBox extends JCheckBox
	{
		private boolean indeterminate;

		@Override
		public void setSelected(boolean selected)
		{
			super.setSelected(selected);
			indeterminate = false;
			repaint();
		}

		public void setIndeterminate(boolean indeterminate)
		{
			if (this.indeterminate != indeterminate)
			{
				this.indeterminate = indeterminate;
				repaint();
			}
		}

		@Override
		protected void paintComponent(Graphics g)
		{
			super.paintComponent(g);

			if (indeterminate)
			{
				int size = getCheckBoxIconSize();
				Insets insets = getInsets();
				int x = insets.left + size / 2;
				int y = getHeight() / 2;
				int half = Math.max(2, size / 5);

				g.setColor(getForeground());
				g.fillRect(x - half, y - half, half * 2, half * 2);
			}
		}

		private static int getCheckBoxIconSize()
		{
			return 12;
		}
	}

	private static class SmallCheckBoxIcon implements Icon
	{
		private final boolean selected;

		SmallCheckBoxIcon(boolean selected)
		{
			this.selected = selected;
		}

		@Override
		public void paintIcon(java.awt.Component component, Graphics graphics, int x, int y)
		{
			graphics.setColor(ColorScheme.MEDIUM_GRAY_COLOR);
			graphics.drawRect(x, y, getIconWidth() - 1, getIconHeight() - 1);
			if (selected)
			{
				graphics.setColor(Color.WHITE);
				graphics.drawLine(x + 2, y + 5, x + 4, y + 7);
				graphics.drawLine(x + 4, y + 7, x + 8, y + 2);
			}
		}

		@Override
		public int getIconWidth()
		{
			return 12;
		}

		@Override
		public int getIconHeight()
		{
			return 10;
		}
	}
}
