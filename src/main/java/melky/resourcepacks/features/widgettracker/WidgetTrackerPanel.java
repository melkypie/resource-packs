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

import com.google.common.primitives.Ints;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.inject.Inject;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import lombok.extern.slf4j.Slf4j;
import melky.resourcepacks.features.widgettracker.event.ScriptIgnored;
import melky.resourcepacks.features.widgettracker.event.WidgetChanged;
import melky.resourcepacks.features.widgettracker.event.WidgetSelected;
import melky.resourcepacks.features.widgettracker.event.WidgetTracked;
import melky.resourcepacks.features.widgettracker.event.WidgetUntracked;
import net.runelite.api.widgets.Widget;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.SwingUtil;


@Slf4j
public class WidgetTrackerPanel extends PluginPanel
{
	private final WidgetSelector widgetSelector;
	private final EventBus eventBus;


	private JLabel selectedWidgetInfo;
	private JPanel trackedWidgetsPanel;
	private JPanel changesContainer;

	private WidgetState previewWidget = null;


	@Inject
	public WidgetTrackerPanel(
		WidgetSelector widgetSelector,
		EventBus eventBus
	)
	{
		super(false);

		this.widgetSelector = widgetSelector;
		this.eventBus = eventBus;

		setLayout(new BorderLayout());
		setBorder(new EmptyBorder(10, 10, 10, 10));

		setBackground(ColorScheme.DARK_GRAY_COLOR);

		JPanel headerPanel = createHeaderPanel();
		add(headerPanel, BorderLayout.NORTH);

		JPanel changesPanel = createChangesPanel();
		add(changesPanel, BorderLayout.CENTER);

		JPanel controlsPanel = createControlsPanel();
		add(controlsPanel, BorderLayout.SOUTH);
	}

	private JPanel createHeaderPanel()
	{
		JPanel headerPanel = new JPanel();
		headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
		headerPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));


		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());
		buttonPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

		JButton readdPicker = new JButton();
		var img = ImageUtil.resizeImage(ImageUtil.loadImageResource(EventBus.class, "/util/reset.png"), 18, 16);
		readdPicker.setIcon(new ImageIcon(img));
		readdPicker.setBackground(ColorScheme.DARK_GRAY_COLOR);
		readdPicker.addActionListener((ev) -> widgetSelector.startUp());
		readdPicker.setFocusPainted(false);

		var trackSelected = new JButton("Track Widget");
		trackSelected.setFocusPainted(false);
		trackSelected.setAlignmentX(Component.CENTER_ALIGNMENT);
		trackSelected.addActionListener(e ->
		{
			if (previewWidget != null)
			{
				eventBus.post(new WidgetTracked(previewWidget));
			}
			previewWidget(null);
		});

		var cancelSelection = new JButton("Cancel");
		cancelSelection.setFocusPainted(false);
		cancelSelection.setAlignmentX(Component.CENTER_ALIGNMENT);
		cancelSelection.addActionListener(e ->
		{
			previewWidget(null);
		});

		buttonPanel.add(readdPicker);
		buttonPanel.add(trackSelected);
		buttonPanel.add(cancelSelection);

		selectedWidgetInfo = new JLabel("No widget selected");
		selectedWidgetInfo.setForeground(Color.LIGHT_GRAY);
		selectedWidgetInfo.setAlignmentX(Component.CENTER_ALIGNMENT);

		headerPanel.add(buttonPanel);
		headerPanel.add(Box.createRigidArea(new Dimension(0, 5)));
		headerPanel.add(selectedWidgetInfo);

		headerPanel.add(Box.createRigidArea(new Dimension(0, 10)));

		headerPanel.add(createTrackedPanel());

		headerPanel.add(Box.createRigidArea(new Dimension(0, 10)));

		return headerPanel;
	}

	private JPanel createTrackedPanel()
	{
		JPanel trackedPanel = new JPanel();
		trackedPanel.setLayout(new BorderLayout());
		trackedPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

		JLabel trackedLabel = new JLabel("Tracked Widgets:");
		trackedLabel.setForeground(Color.WHITE);
		trackedLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
		trackedPanel.add(trackedLabel, BorderLayout.NORTH);

		trackedWidgetsPanel = new JPanel();
		trackedWidgetsPanel.setLayout(new BoxLayout(trackedWidgetsPanel, BoxLayout.Y_AXIS));
		trackedWidgetsPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		JScrollPane trackedScrollPane = new JScrollPane(trackedWidgetsPanel);
		trackedScrollPane.setPreferredSize(new Dimension(0, 120));
		trackedPanel.add(trackedScrollPane, BorderLayout.CENTER);

		return trackedPanel;
	}

	private JPanel createChangesPanel()
	{
		JPanel changesPanel = new JPanel(new BorderLayout());
		changesPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

		JLabel titleLabel = new JLabel("Widget Changes:");
		titleLabel.setForeground(Color.WHITE);
		titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));

		changesContainer = new JPanel();
		changesContainer.setLayout(new BoxLayout(changesContainer, BoxLayout.Y_AXIS));
		changesContainer.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		JScrollPane scrollPane = new JScrollPane(changesContainer);
		scrollPane.setBackground(ColorScheme.DARK_GRAY_COLOR);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		changesPanel.add(titleLabel, BorderLayout.NORTH);
		changesPanel.add(scrollPane, BorderLayout.CENTER);

		return changesPanel;
	}

	private JPanel createControlsPanel()
	{
		JPanel controlsPanel = new JPanel();
		controlsPanel.setLayout(new BoxLayout(controlsPanel, BoxLayout.Y_AXIS));
		controlsPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		controlsPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());
		buttonPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

		JButton clearChangesButton = new JButton("Clear");
		clearChangesButton.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		clearChangesButton.setForeground(Color.WHITE);
		clearChangesButton.setFocusPainted(false);
		clearChangesButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		clearChangesButton.addActionListener(e -> SwingUtil.fastRemoveAll(changesContainer));

		JTextField ignoreScriptField = new JTextField();
		ignoreScriptField.addKeyListener(new KeyAdapter()
		{
			@Override
			public void keyPressed(KeyEvent e)
			{
				if (e.getKeyCode() == KeyEvent.VK_ENTER)
				{
					Integer value = Ints.tryParse(ignoreScriptField.getText());
					if (value != null)
					{
						eventBus.post(new ScriptIgnored(value));
						ignoreScriptField.setText("");
					}
				}
			}

		});

		buttonPanel.add(clearChangesButton);
		buttonPanel.add(ignoreScriptField);

		controlsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
		controlsPanel.add(buttonPanel);

		return controlsPanel;
	}

	private void previewWidget(Widget widget)
	{
		if (widget == null)
		{
			selectedWidgetInfo.setText("No widget selected");
			previewWidget = null;
		}
		else
		{
			previewWidget = new WidgetState(widget.getId(), widget);
			selectedWidgetInfo.setText(generatePreview(previewWidget));
		}

		log.debug("Preview {}", previewWidget);
	}

	private String generatePreview(WidgetState widgetState)
	{
		return "<html>" +
			"<table width='100%'>" +
			"<tr><td>id: " + widgetState.getName() + "</td><td>color: 0x" + ColorUtil.colorToHexCode(new Color(widgetState.getColor())) + "</td></tr>" +
			"<tr><td>type: " + widgetState.getType() + "</td><td>opacity: " + widgetState.getOpacity() + "</td></tr>" +
			"<tr><td>spriteId: " + widgetState.getSpriteId() + "</td><td>hidden: " + widgetState.isHidden() + "</td></tr>" +
			"</table></html>";
	}

	@Subscribe
	public void onWidgetSelected(WidgetSelected event)
	{
		previewWidget(event.getWidget());
	}

	@Subscribe
	public void onWidgetChanged(WidgetChanged event)
	{
		SwingUtilities.invokeLater(() ->
		{
			JPanel changePanel = createChangePanel(event);
			changesContainer.add(changePanel);
			changesContainer.revalidate();
			changesContainer.repaint();

			SwingUtilities.invokeLater(() ->
			{
				JScrollPane scrollPane = (JScrollPane) changesContainer.getParent().getParent();
				JScrollBar vertical = scrollPane.getVerticalScrollBar();
				vertical.setValue(vertical.getMaximum());
			});
		});
	}

	private JPanel createChangePanel(WidgetChanged event)
	{
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBackground(changesContainer.getComponentCount() % 2 == 0 ?
			ColorScheme.DARKER_GRAY_COLOR : new Color(45, 45, 45));
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		String timestamp = String.format("[%tH:%tM:%tS]", event.getTimestamp(), event.getTimestamp(), event.getTimestamp());
		JLabel headerLabel = new JLabel(timestamp + " Script: " + event.getScriptId());
		headerLabel.setForeground(Color.WHITE);

		JTextArea changesText = new JTextArea(String.join("\n", event.getDiff()));
		changesText.setEditable(false);
		changesText.setWrapStyleWord(true);
		changesText.setLineWrap(true);
		changesText.setBackground(panel.getBackground());
		changesText.setForeground(Color.LIGHT_GRAY);
		changesText.setBorder(BorderFactory.createEmptyBorder(2, 10, 0, 0));

		panel.add(headerLabel, BorderLayout.NORTH);
		panel.add(changesText, BorderLayout.CENTER);

		return panel;
	}

	@Subscribe
	public void onWidgetTracked(WidgetTracked event)
	{
		var trackedWidget = event.getWidget();

		for (Component comp : trackedWidgetsPanel.getComponents())
		{
			if (comp instanceof JPanel)
			{
				JPanel existingPanel = (JPanel) comp;
				Component[] children = existingPanel.getComponents();
				if (children.length > 0 && children[0] instanceof JLabel)
				{
					JLabel label = (JLabel) children[0];
					if (label.getText().equals("Widget: " + trackedWidget.getName()))
					{
						// Widget already tracked, don't add again
						return;
					}
				}
			}
		}

		JPanel widgetPanel = new JPanel(new BorderLayout());
		widgetPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		widgetPanel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
		widgetPanel.setPreferredSize(new Dimension(0, 25));
		widgetPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));

		JLabel widgetLabel = new JLabel("Widget: " + trackedWidget.getName());
		widgetLabel.setForeground(Color.WHITE);

		JButton stopButton = new JButton("X");
		stopButton.setPreferredSize(new Dimension(30, 20));
		stopButton.setFocusPainted(false);
		stopButton.addActionListener(e ->
		{
			eventBus.post(new WidgetUntracked(trackedWidget));

			trackedWidgetsPanel.remove(widgetPanel);
			trackedWidgetsPanel.revalidate();
			trackedWidgetsPanel.repaint();
		});

		widgetPanel.add(widgetLabel, BorderLayout.CENTER);
		widgetPanel.add(stopButton, BorderLayout.EAST);

		trackedWidgetsPanel.add(widgetPanel);
	}

	@Override
	public void onActivate()
	{
		widgetSelector.setEnabled(true);
	}

	@Override
	public void onDeactivate()
	{
		widgetSelector.setEnabled(false);
	}
}