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
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import lombok.Getter;
import net.runelite.client.ui.ColorScheme;

@Getter
public class SpriteTrackerPanelItem extends JPanel
{
	@Getter
	private final SpriteTracker.HighlightOptions options;

	private final JLabel nameLabel;
	private final JCheckBox visibleCheckbox;
	private Runnable onVisibilityChanged;
	private boolean hovered;

	public SpriteTrackerPanelItem(SpriteTracker.HighlightOptions options)
	{
		this.options = options;
		var visible = options.isVisible();

		setLayout(new BorderLayout());
		setBackground(ColorScheme.DARKER_GRAY_COLOR);
		setBorder(BorderFactory.createEmptyBorder(1, 2, 1, 2));
		setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));

		String fileName = options.getOverride().getFileName();
		nameLabel = new EllipsisLabel(fileName.endsWith(".png") ? fileName.substring(0, fileName.length() - 4) : fileName);
		nameLabel.setForeground(options.getColor());
		nameLabel.setToolTipText(fileName);
		nameLabel.setCursor(getCursor());

		visibleCheckbox = new JCheckBox("", visible);
		visibleCheckbox.setBackground(getBackground());
		visibleCheckbox.setFocusPainted(false);
		visibleCheckbox.setMargin(new Insets(0, 0, 0, 2));
		int checkboxSize = 12;
		Dimension checkboxDimension = new Dimension(checkboxSize + 2, checkboxSize + 2);
		visibleCheckbox.setPreferredSize(checkboxDimension);
		visibleCheckbox.setMinimumSize(checkboxDimension);
		visibleCheckbox.setMaximumSize(checkboxDimension);
		visibleCheckbox.setIcon(new CheckBoxIcon(false));
		visibleCheckbox.setSelectedIcon(new CheckBoxIcon(true));
		visibleCheckbox.setCursor(getCursor());
		visibleCheckbox.addChangeListener(c -> applyVisibility(visibleCheckbox.isSelected()));

		MouseAdapter hoverListener = new MouseAdapter()
		{
			@Override
			public void mouseEntered(MouseEvent e)
			{
				hovered = true;
				updateBackground();
			}

			@Override
			public void mouseExited(MouseEvent e)
			{
				hovered = false;
				updateBackground();
			}
		};
		addMouseListener(hoverListener);
		nameLabel.addMouseListener(hoverListener);
		visibleCheckbox.addMouseListener(hoverListener);

		add(visibleCheckbox, BorderLayout.WEST);
		add(nameLabel, BorderLayout.CENTER);
	}

	@Override
	public Dimension getMaximumSize()
	{
		return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
	}

	public boolean isSelected()
	{
		return visibleCheckbox.isSelected();
	}

	public void setSelected(boolean selected)
	{
		visibleCheckbox.setSelected(selected);
	}

	void setOnVisibilityChanged(Runnable onVisibilityChanged)
	{
		this.onVisibilityChanged = onVisibilityChanged;
	}

	private void applyVisibility(boolean visible)
	{
		options.setVisible(visible);
		updateBackground();

		if (onVisibilityChanged != null)
		{
			onVisibilityChanged.run();
		}
	}

	private void updateBackground()
	{
		Color background = hovered ? ColorScheme.DARK_GRAY_COLOR : ColorScheme.DARKER_GRAY_COLOR;
		setBackground(background);
		visibleCheckbox.setBackground(background);
	}

	private static class EllipsisLabel extends JLabel
	{
		private final String fullText;

		EllipsisLabel(String text)
		{
			super(text);
			fullText = text;
		}

		@Override
		public Dimension getPreferredSize()
		{
			Dimension preferred = super.getPreferredSize();
			return new Dimension(0, preferred.height);
		}

		@Override
		protected void paintComponent(Graphics graphics)
		{
			String displayText = fullText;
			FontMetrics metrics = graphics.getFontMetrics(getFont());
			int availableWidth = getWidth();
			if (metrics.stringWidth(displayText) > availableWidth)
			{
				String ellipsis = "...";
				int end = fullText.length();
				while (end > 0 && metrics.stringWidth(fullText.substring(0, end) + ellipsis) > availableWidth)
				{
					end--;
				}
				displayText = end == 0 ? ellipsis : fullText.substring(0, end) + ellipsis;
			}

			setText(displayText);
			super.paintComponent(graphics);
		}
	}

	private static class CheckBoxIcon implements Icon
	{
		private final boolean selected;

		CheckBoxIcon(boolean selected)
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
			return 12;
		}
	}
}
