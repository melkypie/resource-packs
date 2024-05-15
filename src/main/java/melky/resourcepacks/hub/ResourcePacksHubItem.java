package melky.resourcepacks.hub;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LayoutStyle;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import melky.resourcepacks.ResourcePacksManager;
import melky.resourcepacks.ResourcePacksPlugin;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.LinkBrowser;
import net.runelite.client.util.SwingUtil;

@Slf4j
public class ResourcePacksHubItem extends JPanel
{
	private static final ImageIcon MISSING_ICON;
	private static final ImageIcon HELP_ICON;
	private static final ImageIcon HELP_ICON_HOVER;
	private static final int HEIGHT = 147;
	private static final int ICON_WIDTH = 224;
	private static final int BOTTOM_LINE_HEIGHT = 16;


	public final ResourcePackManifest manifest;
	public final List<String> keywords = new ArrayList<>();

	@Getter
	private final boolean installed;


	static
	{
		BufferedImage missingIcon = ImageUtil.loadImageResource(ResourcePacksPlugin.class, "/missing.png");//missingicon @TODO
		MISSING_ICON = new ImageIcon(missingIcon);

		BufferedImage helpIcon = ImageUtil.loadImageResource(ResourcePacksPlugin.class, "/help.png");
		HELP_ICON = new ImageIcon(helpIcon);
		HELP_ICON_HOVER = new ImageIcon(ImageUtil.alphaOffset(helpIcon, -100));
	}

	ResourcePacksHubItem(ResourcePackManifest newManifest,
		Collection<ResourcePackManifest> currentManifests,
		boolean installed,
		ScheduledExecutorService executor,
		ResourcePacksClient resourcePacksClient,
		ResourcePacksManager resourcePacksManager)
	{
		ResourcePackManifest loaded = null;
		if (!currentManifests.isEmpty())
		{
			loaded = currentManifests.iterator().next();
		}

		manifest = loaded == null ? newManifest : loaded;
		this.installed = installed;

		if (manifest != null)
		{
			Collections.addAll(keywords, ResourcePacksHubPanel.SPACES.split(manifest.getDisplayName().toLowerCase()));
			Collections.addAll(keywords, manifest.getAuthor().toLowerCase());

			if (manifest.getTags() != null)
			{
				Collections.addAll(keywords, manifest.getTags());
			}
		}

		setBackground(ColorScheme.DARKER_GRAY_COLOR);
		setOpaque(true);

		GroupLayout layout = new GroupLayout(this);
		setLayout(layout);

		JLabel pluginName = new JLabel(manifest.getDisplayName());
		pluginName.setFont(FontManager.getRunescapeBoldFont());
		pluginName.setToolTipText(manifest.getDisplayName());

		JLabel author = new JLabel(manifest.getAuthor());
		author.setFont(FontManager.getRunescapeSmallFont());
		author.setToolTipText(manifest.getAuthor());

		JLabel version = new JLabel(manifest.getCompatibleVersion());
		version.setFont(FontManager.getRunescapeSmallFont());
		version.setHorizontalAlignment(JLabel.RIGHT);
		version.setToolTipText("Compatible with " + manifest.getCompatibleVersion() + " version of Resource Packs plugin");

		JLabel icon = new JLabel();
		icon.setHorizontalAlignment(JLabel.CENTER);
		icon.setIcon(MISSING_ICON);
		if (manifest.isHasIcon())
		{
			executor.submit(() ->
			{
				try
				{
					BufferedImage img = resourcePacksClient.downloadIcon(manifest);

					SwingUtilities.invokeLater(() -> icon.setIcon(new ImageIcon(img)));
				}
				catch (IOException e)
				{
					log.info("Cannot download icon for pack \"{}\"", manifest.getInternalName(), e);
				}
			});
		}

		JButton help = new JButton(HELP_ICON);
		help.setRolloverIcon(HELP_ICON_HOVER);
		SwingUtil.removeButtonDecorations(help);
		help.setBorder(null);
		if (manifest.getRepo() == null)
		{
			help.setVisible(false);
		}
		else
		{
			help.setToolTipText("See more: " + manifest.getRepo());
			help.addActionListener(ev -> LinkBrowser.browse(manifest.getRepo().toString()));
		}
		help.setBorder(null);

		boolean install = !installed;
		boolean update = loaded != null && newManifest != null && !newManifest.equals(loaded);
		boolean remove = !install && !update;
		JButton addrm = new JButton();
		if (install)
		{
			addrm.setText("Install");
			addrm.setBackground(new Color(0x28BE28));
			addrm.addActionListener(l ->
			{
				addrm.setText("Installing");
				addrm.setBackground(new Color(0xC4A800));
				resourcePacksManager.install(manifest.getInternalName());

			});
		}
		else if (remove)
		{
			addrm.setText("Remove");
			addrm.setBackground(new Color(0xBE2828));
			addrm.addActionListener(l ->
			{
				addrm.setText("Removing");
				addrm.setBackground(new Color(0xC4A800));
				resourcePacksManager.remove(manifest.getInternalName());
			});
		}
		else
		{
			addrm.setText("Update");
			addrm.setBackground(new Color(0x1F621F));
			addrm.addActionListener(l ->
			{
				addrm.setText("Updating");
				addrm.setBackground(new Color(0xC4A800));
				executor.submit(resourcePacksManager::refreshPlugins);
			});
		}
		addrm.setBorder(new LineBorder(addrm.getBackground().darker()));
		addrm.setFocusPainted(false);

		layout.setHorizontalGroup(layout.createParallelGroup()

			.addGroup(layout.createParallelGroup()
				.addGroup(layout.createSequentialGroup()
					.addGap(5)
					.addComponent(pluginName, 0, GroupLayout.PREFERRED_SIZE, 135)
					.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
					.addComponent(help, 0, 24, 24)
					.addComponent(addrm, 0, 50, GroupLayout.PREFERRED_SIZE)
					.addGap(5))
				.addGroup(layout.createSequentialGroup()
					.addGap(5)
					.addComponent(author, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
					.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.PREFERRED_SIZE, 100)
					.addComponent(version, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
					.addGap(5)))
			.addGroup(layout.createSequentialGroup()
				.addGap(2)
				.addComponent(icon, ICON_WIDTH, ICON_WIDTH, ICON_WIDTH)));

		layout.setVerticalGroup(layout.createSequentialGroup()
			.addGap(5)
			.addGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
					.addComponent(pluginName)
					.addComponent(help, BOTTOM_LINE_HEIGHT, BOTTOM_LINE_HEIGHT, BOTTOM_LINE_HEIGHT)
					.addComponent(addrm, BOTTOM_LINE_HEIGHT, BOTTOM_LINE_HEIGHT, BOTTOM_LINE_HEIGHT))
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
					.addComponent(author, BOTTOM_LINE_HEIGHT, BOTTOM_LINE_HEIGHT, BOTTOM_LINE_HEIGHT)
					.addComponent(version, BOTTOM_LINE_HEIGHT, BOTTOM_LINE_HEIGHT, BOTTOM_LINE_HEIGHT))
				.addGap(5))
			.addComponent(icon, HEIGHT, GroupLayout.DEFAULT_SIZE, HEIGHT)
			.addGap(5));
	}
}
