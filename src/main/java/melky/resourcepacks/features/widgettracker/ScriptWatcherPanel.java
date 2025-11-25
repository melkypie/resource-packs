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

import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import javax.inject.Inject;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.widgets.Widget;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.util.ColorUtil;

@Slf4j
public class ScriptWatcherPanel extends PluginPanel
{
	private final Client client;
	private final EventBus eventBus;

	private final Multimap<Integer, ScriptWatcher> scriptWatchers = ArrayListMultimap.create();
	private final JPanel watchersContainer = new JPanel();

	@Inject
	public ScriptWatcherPanel(Client client, EventBus eventBus)
	{
		super(false);
		this.client = client;
		this.eventBus = eventBus;

		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		add(createInputPanel(), BorderLayout.NORTH);
		add(createWatchersPanel(), BorderLayout.CENTER);
	}

	private JPanel createInputPanel()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

		JTextField scriptIdField = new JTextField();
		JTextField interfaceIdField = new JTextField();
		JTextField childIdField = new JTextField();
		JTextField childIndexField = new JTextField();
		JTextField colorField = new JTextField();

		panel.add(createLabeledField("Script ID:", scriptIdField));
		panel.add(createLabeledField("Interface ID:", interfaceIdField));
		panel.add(createLabeledField("Child ID:", childIdField));
		panel.add(createLabeledField("Child Index:", childIndexField));
		panel.add(createLabeledField("Color:", colorField));

		JButton addButton = new JButton("Add Watcher");
		addButton.setFocusPainted(false);
		addButton.addActionListener(e ->
		{
			try
			{
				int scriptId = Integer.parseInt(scriptIdField.getText());
				int interfaceId = Integer.parseInt(interfaceIdField.getText());
				int childId = Integer.parseInt(childIdField.getText());
				int childIndex = Strings.isNullOrEmpty(childIndexField.getText()) ? -1 : Integer.parseInt(childIndexField.getText());
				int color = ColorUtil.fromHex(colorField.getText()).getRGB();

				ScriptWatcher watcher = new ScriptWatcher(scriptId, interfaceId, childId, childIndex, color);
				addScriptWatcher(watcher);

				scriptIdField.setText("");
				interfaceIdField.setText("");
				childIdField.setText("");
				childIndexField.setText("");
				colorField.setText("");
			}
			catch (NumberFormatException ex)
			{
				log.warn("Invalid input for script watcher");
			}
		});

		panel.add(Box.createRigidArea(new Dimension(0, 5)));
		panel.add(addButton);

		return panel;
	}

	private JPanel createWatchersPanel()
	{
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBackground(ColorScheme.DARK_GRAY_COLOR);

		JLabel titleLabel = new JLabel("Active Watchers:");
		titleLabel.setForeground(Color.WHITE);
		titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));

		watchersContainer.setLayout(new BoxLayout(watchersContainer, BoxLayout.Y_AXIS));
		watchersContainer.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		JScrollPane scrollPane = new JScrollPane(watchersContainer);
		scrollPane.setPreferredSize(new Dimension(0, 200));

		panel.add(titleLabel, BorderLayout.NORTH);
		panel.add(scrollPane, BorderLayout.CENTER);

		return panel;
	}

	private JPanel createLabeledField(String label, JTextField field)
	{
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBackground(ColorScheme.DARK_GRAY_COLOR);

		JLabel jLabel = new JLabel(label);
		jLabel.setForeground(Color.WHITE);
		jLabel.setPreferredSize(new Dimension(80, 20));

		panel.add(jLabel, BorderLayout.WEST);
		panel.add(field, BorderLayout.CENTER);
		panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));

		return panel;
	}

	private void addScriptWatcher(ScriptWatcher watcher)
	{
		scriptWatchers.put(watcher.getScriptId(), watcher);

		JPanel watcherPanel = new JPanel(new BorderLayout());
		watcherPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		watcherPanel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));

		String info = String.format("Script: %d, Interface: %d, Child: %d, Index: %d, Color: 0x%06X",
			watcher.getScriptId(), watcher.getInterfaceId(), watcher.getChildId(),
			watcher.getChildIndex(), watcher.getColor());

		JTextArea infoArea = new JTextArea(info);
		infoArea.setEditable(false);
		infoArea.setWrapStyleWord(true);
		infoArea.setLineWrap(true);
		infoArea.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		infoArea.setForeground(Color.WHITE);
		infoArea.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));

		JButton removeButton = new JButton("X");
		removeButton.setPreferredSize(new Dimension(30, 20));
		removeButton.setFocusPainted(false);
		removeButton.addActionListener(e ->
		{
			scriptWatchers.remove(watcher.getScriptId(), watcher);
			watchersContainer.remove(watcherPanel);
			watchersContainer.revalidate();
			watchersContainer.repaint();
		});

		watcherPanel.add(infoArea, BorderLayout.CENTER);
		watcherPanel.add(removeButton, BorderLayout.EAST);

		watchersContainer.add(watcherPanel);
		watchersContainer.revalidate();
		watchersContainer.repaint();
	}

	@Subscribe
	public void onScriptPostFired(ScriptPostFired event)
	{
		if (scriptWatchers.isEmpty())
		{
			return;
		}

		int scriptId = event.getScriptId();
		if (scriptWatchers.containsKey(scriptId))
		{
			for (ScriptWatcher watcher : scriptWatchers.get(scriptId))
			{
				Widget widget = client.getWidget(watcher.getInterfaceId(), watcher.getChildId());
				if (widget != null)
				{
					if (watcher.getChildIndex() > -1)
					{
						widget = widget.getChild(watcher.getChildIndex());
					}

					if (widget == null)
					{
						continue;
					}

					log.debug("Script {} fired - Widget: {} at index {}", scriptId, watcher.getChildId(), watcher.getChildIndex());
					widget.setTextColor(watcher.getColor());
				}
			}
		}
	}
}
