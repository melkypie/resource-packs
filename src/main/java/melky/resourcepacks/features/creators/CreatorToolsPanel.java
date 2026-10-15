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

package melky.resourcepacks.features.creators;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.inject.Inject;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import lombok.extern.slf4j.Slf4j;
import melky.resourcepacks.event.ReloadPack;
import melky.resourcepacks.features.creators.events.SpritePicked;
import melky.resourcepacks.features.creators.events.TrackerBuilt;
import melky.resourcepacks.features.creators.skillglow.SkillGlow;
import melky.resourcepacks.features.creators.sprites.SpriteOverlay;
import melky.resourcepacks.features.creators.sprites.SpritePicker;
import melky.resourcepacks.features.creators.sprites.SpriteTrackerPanel;
import melky.resourcepacks.features.state.LocalConfig;
import net.runelite.api.Client;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.WidgetModalMode;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.JagexColors;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.materialtabs.MaterialTab;
import net.runelite.client.ui.components.materialtabs.MaterialTabGroup;
import net.runelite.client.util.ColorUtil;

@Slf4j
public class CreatorToolsPanel extends PluginPanel
{
	private static final String OVERLAY_ICON = "/overlay.png";
	private static final String VISIBILITY_ICON = "/visible.png";
	private static final String PICKER_ICON = "/picker.png";

	private final Client client;
	private final ClientThread clientThread;
	private final EventBus eventBus;
	private final LocalConfig localConfig;
	private final SpriteTrackerPanel spriteTrackerPanel;

	@Inject
	public CreatorToolsPanel(Client client, ClientThread clientThread, EventBus eventBus, LocalConfig localConfig, SpriteTrackerPanel spriteTrackerPanel)
	{
		super(false);
		this.client = client;
		this.clientThread = clientThread;
		this.eventBus = eventBus;
		this.localConfig = localConfig;
		this.spriteTrackerPanel = spriteTrackerPanel;

		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		JPanel tabDisplay = new JPanel(new BorderLayout());
		tabDisplay.setBackground(ColorScheme.DARK_GRAY_COLOR);

		MaterialTabGroup tabGroup = new MaterialTabGroup(tabDisplay);
		MaterialTab packToolsTab = new MaterialTab("Pack", tabGroup, createPackToolsPanel());
		MaterialTab spriteToolsTab = new MaterialTab("Sprites", tabGroup, createSpriteToolsPanel());
		tabGroup.addTab(packToolsTab);
		tabGroup.addTab(spriteToolsTab);
		tabGroup.select(packToolsTab);

		add(tabGroup, BorderLayout.NORTH);
		add(tabDisplay, BorderLayout.CENTER);
	}

	private JPanel createSpriteToolsPanel()
	{
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
		panel.add(createControlsPanel(), BorderLayout.NORTH);
		panel.add(spriteTrackerPanel, BorderLayout.CENTER);
		return panel;
	}

	private JPanel createPackToolsPanel()
	{
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.weightx = 1;
		constraints.anchor = GridBagConstraints.NORTH;
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		constraints.insets = new Insets(0, 0, 5, 0);

		JButton reloadPackButton = new JButton("Reload Pack");
		reloadPackButton.setToolTipText("Reload current pack");
		reloadPackButton.addActionListener(e -> eventBus.post(new ReloadPack()));
		panel.add(reloadPackButton, constraints);

		DevToolsButton skillGlowButton = new DevToolsButton("Skill Glow");
		skillGlowButton.setActive(localConfig.getState(SkillGlow.class));
		skillGlowButton.addActionListener(e -> localConfig.toggleState(SkillGlow.class));
		panel.add(skillGlowButton, constraints);

		JButton collectionLogButton = new JButton("Collection Log Popup");
		collectionLogButton.setToolTipText("Show collection log reward popup");
		collectionLogButton.addActionListener(e -> showCollectionLogPopup());
		panel.add(collectionLogButton, constraints);

		GridBagConstraints fillerConstraints = new GridBagConstraints();
		fillerConstraints.fill = GridBagConstraints.BOTH;
		fillerConstraints.weightx = 1;
		fillerConstraints.weighty = 1;
		fillerConstraints.gridwidth = GridBagConstraints.REMAINDER;
		JPanel filler = new JPanel();
		filler.setOpaque(false);
		panel.add(filler, fillerConstraints);

		return panel;
	}

	private void showCollectionLogPopup()
	{
		clientThread.invokeLater(() ->
		{
			int componentId = (client.getTopLevelInterfaceId() << 16) | (client.isResized() ? 13 : 43);
			client.openInterface(componentId, InterfaceID.NOTIFICATION_DISPLAY, WidgetModalMode.MODAL_CLICKTHROUGH);
			String title = ColorUtil.wrapWithColorTag("Collection Log", JagexColors.DARK_ORANGE_INTERFACE_TEXT);
			String description = "New collection log item:<br><br><col=ffffff>Sample reward</col>";
			client.runScript(3343, title, description, -1);
		});
	}

	private JPanel createControlsPanel()
	{
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		panel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

		JButton spriteOverlayButton = new ToolbarButton(OVERLAY_ICON);
		spriteOverlayButton.setToolTipText("Toggle sprite overlay");
		spriteOverlayButton.addActionListener(e -> toggleSpriteOverlay());

		JButton visibilityButton = new ToolbarButton(VISIBILITY_ICON);
		visibilityButton.setToolTipText("Toggle all sprites");
		visibilityButton.addActionListener(e ->
		{
			spriteTrackerPanel.toggleAllSprites();
		});

		JButton pickerButton = new ToolbarButton(PICKER_ICON);
		pickerButton.setToolTipText("Toggle sprite picker");
		pickerButton.addActionListener(e ->
		{
			localConfig.toggleState(SpritePicker.class);
		});

		panel.add(spriteOverlayButton);
		panel.add(visibilityButton);
		panel.add(pickerButton);

		return panel;
	}

	private void toggleSpriteOverlay()
	{
		localConfig.toggleState(SpriteOverlay.class);
	}


	@Subscribe
	public void onTrackerBuilt(TrackerBuilt event)
	{
		spriteTrackerPanel.rebuild();
	}

	@Subscribe
	public void onSpritePicked(SpritePicked event)
	{
		spriteTrackerPanel.refreshVisibility();
	}

}
