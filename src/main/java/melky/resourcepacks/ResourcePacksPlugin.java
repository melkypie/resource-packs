package melky.resourcepacks;

import com.google.inject.Provides;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import melky.resourcepacks.event.ResourcePacksChanged;
import melky.resourcepacks.hub.ResourcePacksClient;
import melky.resourcepacks.hub.ResourcePacksHubPanel;
import net.runelite.api.events.BeforeRender;
import net.runelite.client.RuneLite;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.events.SessionClose;
import net.runelite.client.events.SessionOpen;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;
import okhttp3.HttpUrl;

@PluginDescriptor(
	name = "Resource packs"
)
public class ResourcePacksPlugin extends Plugin
{
	public static final File RESOURCEPACKS_DIR = new File(RuneLite.RUNELITE_DIR.getPath() + File.separator + "resource-packs-repository");
	public static final File NOTICE_FILE = new File(RESOURCEPACKS_DIR.getPath() + File.separator + "DO_NOT_EDIT_CHANGES_WILL_BE_OVERWRITTEN");
	public static final String BRANCH = "github-actions";
	public static final HttpUrl GITHUB = HttpUrl.parse("https://github.com/melkypie/resource-packs");
	public static final HttpUrl RAW_GITHUB = HttpUrl.parse("https://raw.githubusercontent.com/melkypie/resource-packs");
	public static final HttpUrl API_GITHUB = HttpUrl.parse("https://api.github.com/repos/melkypie/resource-packs");

	@Inject
	private ClientThread clientThread;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private ResourcePacksManager resourcePacksManager;

	@Inject
	private ScheduledExecutorService executor;

	private ResourcePacksHubPanel resourcePacksHubPanel;

	private NavigationButton navButton;

	@Provides
	ResourcePacksConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ResourcePacksConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		clientThread.invokeLater(resourcePacksManager::updateAllOverrides);

		resourcePacksHubPanel = injector.getInstance(ResourcePacksHubPanel.class);
		final BufferedImage icon = ImageUtil.getResourceStreamFromClass(getClass(), "/panel.png");

		navButton = NavigationButton.builder()
			.tooltip("Resource packs hub")
			.icon(icon)
			.priority(10)
			.panel(resourcePacksHubPanel)
			.build();

		clientToolbar.addNavigation(navButton);

		if (!RESOURCEPACKS_DIR.exists())
		{
			RESOURCEPACKS_DIR.mkdirs();
		}

		if (!NOTICE_FILE.exists())
		{
			NOTICE_FILE.createNewFile();
		}

		executor.submit(resourcePacksManager::refreshPlugins);
	}

	@Override
	protected void shutDown() throws Exception
	{
		clientThread.invokeLater(() ->
		{
			resourcePacksManager.adjustWidgetDimensions(false);
			resourcePacksManager.removeGameframe();
		});
		clientToolbar.removeNavigation(navButton);
	}

	@Subscribe
	public void onBeforeRender(BeforeRender event)
	{
		resourcePacksManager.adjustWidgetDimensions(true);
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (event.getGroup().equals(ResourcePacksConfig.GROUP_NAME) && event.getKey().equals("resourcePack"))
		{
			clientThread.invokeLater(resourcePacksManager::updateAllOverrides);
		}
		else if (event.getGroup().equals("banktags") && event.getKey().equals("useTabs"))
		{
			clientThread.invoke(resourcePacksManager::updateAllOverrides);
		}
	}

	@Subscribe
	public void onResourcePacksChanged(ResourcePacksChanged packsChanged)
	{
		SwingUtilities.invokeLater(() -> resourcePacksHubPanel.reloadResourcePackList(packsChanged.getNewManifest()));
	}

	@Subscribe
	public void onSessionOpen(SessionOpen event)
	{
		executor.submit(resourcePacksManager::refreshPlugins);
	}

	@Subscribe
	public void onSessionClose(SessionClose event)
	{
		executor.submit(resourcePacksManager::refreshPlugins);
	}
}
