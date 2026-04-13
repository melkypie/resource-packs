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

package melky.resourcepacks.features.hub;

import com.google.common.base.Splitter;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javax.inject.Inject;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import lombok.extern.slf4j.Slf4j;
import melky.resourcepacks.ResourcePacksConfig;
import melky.resourcepacks.features.packs.PacksManager;
import melky.resourcepacks.model.HubManifest;
import net.runelite.client.plugins.discord.DiscordPlugin;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.DynamicGridLayout;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.IconTextField;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.LinkBrowser;
import net.runelite.client.util.Text;

@Slf4j
public class HubPanel extends PluginPanel
{
	public static final Pattern SPACES = Pattern.compile(" +");
	public static final Splitter SPLITTER = Splitter.on(" ").trimResults().omitEmptyStrings();
	private final PacksManager packsManager;
	private final HubClient hubClient;
	private final ScheduledExecutorService executor;
	private final ResourcePacksConfig config;

	private final IconTextField searchBar;
	public final JComboBox currentHubPackComboBox;
	private final JLabel refreshing;
	private final JPanel mainPanel;
	private static final ImageIcon DISCORD_ICON;
	private static final int BOTTOM_LINE_HEIGHT = 24;
	private static final int DISCORD_ICON_SIZE = 18;
	private List<HubItem> packs = null;
	private boolean ignoreSelected = false;

	static
	{
		final BufferedImage discordIcon = ImageUtil.resizeImage(ImageUtil.loadImageResource(DiscordPlugin.class, "discord.png"), DISCORD_ICON_SIZE, DISCORD_ICON_SIZE);
		DISCORD_ICON = new ImageIcon(discordIcon);
	}

	@Inject
	HubPanel(
		PacksManager packsManager,
		HubClient hubClient,
		ScheduledExecutorService executor,
		ResourcePacksConfig config)
	{
		super(false);
		this.packsManager = packsManager;
		this.hubClient = hubClient;
		this.executor = executor;
		this.config = config;

		{
			Object refresh = "this could just be a lambda, but no, it has to be abstracted";
			getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), refresh);
			getActionMap().put(refresh, new AbstractAction()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					reloadResourcePackList();
				}
			});
		}

		GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		searchBar = new IconTextField();
		searchBar.setIcon(IconTextField.Icon.SEARCH);
		searchBar.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		searchBar.setHoverBackgroundColor(ColorScheme.DARK_GRAY_HOVER_COLOR);
		searchBar.getDocument().addDocumentListener(new DocumentListener()
		{
			@Override
			public void insertUpdate(DocumentEvent e)
			{
				filter();
			}

			@Override
			public void removeUpdate(DocumentEvent e)
			{
				filter();
			}

			@Override
			public void changedUpdate(DocumentEvent e)
			{
				filter();
			}
		});

		JButton discordButton = new JButton();
		discordButton.setIcon(DISCORD_ICON);
		discordButton.setBackground(ColorScheme.DARK_GRAY_COLOR);
		discordButton.addActionListener((ev) -> LinkBrowser.browse("https://discord.gg/DsDhUz4NNN"));
		discordButton.setToolTipText("Hang out with pack creators and ask for any help");
		discordButton.addChangeListener(ev ->
		{
			if (discordButton.getModel().isPressed())
			{
				discordButton.setBackground(ColorScheme.DARKER_GRAY_COLOR);
			}
			else if (discordButton.getModel().isRollover())
			{
				discordButton.setBackground(ColorScheme.DARK_GRAY_HOVER_COLOR);
			}
			else
			{
				discordButton.setBackground(ColorScheme.DARK_GRAY_COLOR);
			}
		});

		currentHubPackComboBox = new JComboBox();
		currentHubPackComboBox.setPrototypeDisplayValue("XXXXXXXXXXX");
		currentHubPackComboBox.addItemListener(e ->
		{
			if (e.getStateChange() == ItemEvent.SELECTED && !ignoreSelected)
			{
				if (e.getItem() instanceof HubManifest)
				{
					HubManifest hubManifest = (HubManifest) e.getItem();
					packsManager.setSelectedHubPack(hubManifest.getInternalName());
				}
				else
				{
					packsManager.setSelectedHubPack("None");
				}
			}
		});
		JLabel currentHubPackLabel = new JLabel("Selected pack: ");

		mainPanel = new JPanel();
		mainPanel.setBorder(BorderFactory.createEmptyBorder(0, 7, 15, 7));
		mainPanel.setLayout(new DynamicGridLayout(0, 1, 0, 5));
		mainPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

		refreshing = new JLabel("Loading...");
		refreshing.setHorizontalAlignment(JLabel.CENTER);

		JPanel mainPanelWrapper = new JPanel();
		mainPanelWrapper.setLayout(new BorderLayout());
		mainPanelWrapper.add(mainPanel, BorderLayout.NORTH);
		mainPanelWrapper.add(refreshing, BorderLayout.CENTER);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		// Can't use Short.MAX_VALUE like the docs say because of JDK-8079640
		scrollPane.setPreferredSize(new Dimension(0x7000, 0x7000));
		scrollPane.setViewportView(mainPanelWrapper);

		layout.setVerticalGroup(layout.createSequentialGroup()
			.addGap(5)
			.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				.addComponent(searchBar, BOTTOM_LINE_HEIGHT, BOTTOM_LINE_HEIGHT, BOTTOM_LINE_HEIGHT)
				.addComponent(discordButton, BOTTOM_LINE_HEIGHT, BOTTOM_LINE_HEIGHT, BOTTOM_LINE_HEIGHT))
			.addGap(5)
			.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				.addComponent(currentHubPackLabel)
				.addComponent(currentHubPackComboBox))
			.addGap(10)
			.addComponent(scrollPane));

		layout.setHorizontalGroup(layout.createParallelGroup()
			.addGroup(layout.createSequentialGroup()
				.addGap(7)
				.addComponent(searchBar)
				.addGap(3)
				.addComponent(discordButton, 0, 24, 24)
				.addGap(7))
			.addGroup(layout.createSequentialGroup()
				.addGap(7)
				.addComponent(currentHubPackLabel)
				.addComponent(currentHubPackComboBox)
				.addGap(7))
			.addComponent(scrollPane));

		revalidate();

		refreshing.setVisible(false);
		reloadResourcePackList();
	}

	private void reloadResourcePackList()
	{
		if (refreshing.isVisible())
		{
			return;
		}

		refreshing.setVisible(true);
		mainPanel.removeAll();

		executor.submit(() ->
		{
			List<HubManifest> manifest;
			try
			{

				manifest = hubClient.downloadManifest();
			}
			catch (IOException e)
			{
				log.error("", e);
				SwingUtilities.invokeLater(() ->
				{
					refreshing.setVisible(false);
					mainPanel.add(new JLabel("Downloading the pack manifest failed"));

					JButton retry = new JButton("Retry");
					retry.addActionListener(l -> reloadResourcePackList());
					mainPanel.add(retry);
				});
				return;
			}

			reloadResourcePackList(manifest);
		});
	}

	public void reloadResourcePackList(List<HubManifest> manifest)
	{
		Map<String, HubManifest> downloadedManifests = manifest.stream()
			.collect(ImmutableMap.toImmutableMap(HubManifest::getInternalName, Function.identity()));

		try
		{
			HashMultimap<String, HubManifest> currentManifests = packsManager.getCurrentManifests();
			Set<String> installed = new HashSet<>(packsManager.getInstalledResourcePacks());
			HashMap<String, HubManifest> installedPacks = new HashMap<>();

			for (String pack : installed)
			{
				HubManifest packManifest = downloadedManifests.get(pack);
				if (packManifest != null)
				{
					installedPacks.put(pack, packManifest);
				}
			}

			SwingUtilities.invokeLater(() ->
			{
				ignoreSelected = true;
				currentHubPackComboBox.removeAllItems();
				currentHubPackComboBox.addItem("None");
				installed.forEach(internal ->
				{
					HubManifest toAddManifest = installedPacks.get(internal);
					if (toAddManifest == null)
					{
						log.warn("pack missing from manifest: {}", internal);
						return;
					}

					if (((DefaultComboBoxModel) currentHubPackComboBox.getModel()).getIndexOf(toAddManifest) == -1)
					{
						currentHubPackComboBox.addItem(toAddManifest);
						if (config.selectedHubPack().equals(toAddManifest.getInternalName()))
						{
							currentHubPackComboBox.setSelectedItem(toAddManifest);
						}
					}
				});
				ignoreSelected = false;
				List<HubItem> list = new ArrayList<>();
				for (String id : downloadedManifests.keySet())
				{
					HubItem resourcePacksHubItem = new HubItem(downloadedManifests.get(id), currentManifests.get(id), installed.contains(id), executor, hubClient, packsManager);
					list.add(resourcePacksHubItem);
				}
				packs = list;

				refreshing.setVisible(false);
				filter();
			});
		}
		catch (IOException e)
		{
			log.error("", e);
		}

	}

	void filter()
	{
		if (refreshing.isVisible())
		{
			return;
		}

		mainPanel.removeAll();

		Stream<HubItem> stream = packs.stream();

		String search = searchBar.getText();
		boolean isSearching = search != null && !search.trim().isEmpty();
		if (isSearching)
		{
			stream = stream
				.filter(p -> Text.matchesSearchTerms(SPLITTER.split(search.toLowerCase()), p.keywords))
				.sorted(Comparator.comparing(p -> p.manifest.getDisplayName()));
		}
		else
		{
			stream = stream
				.sorted(Comparator.comparing(HubItem::isInstalled).thenComparing(p -> p.manifest.getDisplayName()));
		}

		stream.forEach(mainPanel::add);
		mainPanel.revalidate();
	}

	@Override
	public void onActivate()
	{
		revalidate();
		searchBar.setText("");
		reloadResourcePackList();
		searchBar.requestFocusInWindow();
	}
}
