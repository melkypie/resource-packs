package melky.resourcepacks;

import com.google.inject.Provides;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.ScheduledExecutorService;
import javax.inject.Inject;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import melky.resourcepacks.event.ResourcePacksChanged;
import melky.resourcepacks.hub.ResourcePacksHubPanel;
import net.runelite.api.GameState;
import net.runelite.api.events.BeforeRender;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.client.RuneLite;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.RuneLiteConfig;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.events.SessionClose;
import net.runelite.client.events.SessionOpen;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.interfacestyles.Skin;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;
import okhttp3.HttpUrl;

@PluginDescriptor(
	name = "Resource packs"
)
@Slf4j
public class ResourcePacksPlugin extends Plugin
{
	public static final File RESOURCEPACKS_DIR = new File(RuneLite.RUNELITE_DIR.getPath() + File.separator + "resource-packs-repository");
	public static final File NOTICE_FILE = new File(RESOURCEPACKS_DIR.getPath() + File.separator + "DO_NOT_EDIT_CHANGES_WILL_BE_OVERWRITTEN");
	public static final String BRANCH = "github-actions";
	public static final String OVERLAY_COLOR_CONFIG = "overlayBackgroundColor";
	public static final HttpUrl GITHUB = HttpUrl.parse("https://github.com/melkypie/resource-packs");
	public static final HttpUrl RAW_GITHUB = HttpUrl.parse("https://raw.githubusercontent.com/melkypie/resource-packs");
	public static final HttpUrl API_GITHUB = HttpUrl.parse("https://api.github.com/repos/melkypie/resource-packs");

	@Setter
	private static boolean ignoreOverlayConfig = false;

	@Inject
	private ClientThread clientThread;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private ResourcePacksConfig config;

	@Inject
	private ConfigManager configManager;

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
		if (!RESOURCEPACKS_DIR.exists())
		{
			RESOURCEPACKS_DIR.mkdirs();
		}

		if (!NOTICE_FILE.exists())
		{
			NOTICE_FILE.createNewFile();
		}

		executor.submit(() -> {
			resourcePacksManager.refreshPlugins();
			clientThread.invokeLater(resourcePacksManager::updateAllOverrides);
		});

		resourcePacksHubPanel = injector.getInstance(ResourcePacksHubPanel.class);
		final BufferedImage icon = ImageUtil.loadImageResource(getClass(), "/panel.png");

		navButton = NavigationButton.builder()
			.tooltip("Resource packs hub")
			.icon(icon)
			.priority(10)
			.panel(resourcePacksHubPanel)
			.build();

		if (!config.hideSidePanelButton())
		{
			clientToolbar.addNavigation(navButton);
		}

		if (configManager.getConfiguration("interfaceStyles", "gameframe", Skin.DEFAULT.getDeclaringClass()).equals(Skin.DEFAULT) ||
			config.disableInterfaceStylesPrompt())
		{
			return;
		}

		SwingUtilities.invokeLater(() -> {
			String[] options = new String[] {"Yes", "No", "No, Don't ask again"};
			int result = JOptionPane.showOptionDialog(
				null,
				"<html><p>Your Interface Styles gameframe option is not set to Default<br/> This can cause some interfaces to be misaligned</p><br/><strong>Would you like it to be set to Default?</strong></html>",
				"Interface Styles conflict",
				JOptionPane.DEFAULT_OPTION,
				JOptionPane.WARNING_MESSAGE,
				null, options, options[0]);
			switch (options[result])
			{
				case "Yes":
					configManager.setConfiguration("interfaceStyles", "gameframe", Skin.DEFAULT);
					clientThread.invokeLater(resourcePacksManager::updateAllOverrides);
					break;
				case "No":
					break;
				case "No, Don't ask again":
					configManager.setConfiguration(ResourcePacksConfig.GROUP_NAME, "disableInterfaceStylesPrompt", true);
					break;
			}
		});
	}

	@Override
	protected void shutDown() throws Exception
	{
		clientThread.invokeLater(() ->
		{
			resourcePacksManager.adjustWidgetDimensions(false);
			resourcePacksManager.removeGameframe();
			resourcePacksManager.resetWidgetOverrides();
			resourcePacksManager.resetCrossSprites();
		});
		if (config.allowLoginScreen())
		{
			resourcePacksManager.resetLoginScreen();
		}
		if (config.allowOverlayColor())
		{
			resourcePacksManager.resetOverlayColor();
		}

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
		if (event.getGroup().equals(ResourcePacksConfig.GROUP_NAME))
		{
			switch (event.getKey())
			{
				case "allowSpellsPrayers":
				case "allowColorPack":
				case "colorPackOverlay":
				case "colorPack":
				case "resourcePack":
					clientThread.invokeLater(resourcePacksManager::updateAllOverrides);
					break;
				case "allowOverlayColor":
					if (config.allowOverlayColor())
					{
						clientThread.invokeLater(resourcePacksManager::updateAllOverrides);
					}
					else
					{
						resourcePacksManager.resetOverlayColor();
					}
					break;
				case "allowCrossSprites":
					if (config.allowCrossSprites())
					{
						clientThread.invokeLater(resourcePacksManager::changeCrossSprites);
					}
					else
					{
						resourcePacksManager.resetCrossSprites();
					}
					break;
				case "allowLoginScreen":
					if (config.allowLoginScreen())
					{
						clientThread.invokeLater(resourcePacksManager::updateAllOverrides);
					}
					else
					{
						resourcePacksManager.resetLoginScreen();
					}
					break;
				case "hideSidePanelButton":
					clientThread.invokeLater(this::toggleSidePanelButton);
					break;
			}
		}
		else if (event.getGroup().equals("banktags") && event.getKey().equals("useTabs"))
		{
			clientThread.invoke(resourcePacksManager::reloadBankTagSprites);
		}
		else if (config.allowOverlayColor() && !ignoreOverlayConfig &&
			event.getGroup().equals(RuneLiteConfig.GROUP_NAME) && event.getKey().equals(OVERLAY_COLOR_CONFIG))
		{
			configManager.setConfiguration(ResourcePacksConfig.GROUP_NAME, ResourcePacksConfig.ORIGINAL_OVERLAY_COLOR,
				event.getNewValue());
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

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() != GameState.LOGIN_SCREEN)
		{
			return;
		}

		resourcePacksManager.changeCrossSprites();
	}

	@Subscribe
	public void onScriptPostFired(ScriptPostFired event)
	{
		if (!resourcePacksManager.getColorProperties().isEmpty() && WidgetOverride.scriptWidgetOverrides.containsKey(event.getScriptId()))
		{
			for (WidgetOverride widgetOverride : WidgetOverride.scriptWidgetOverrides.get(event.getScriptId()))
			{
				resourcePacksManager.addPropertyToWidget(widgetOverride);
			}
		}
	}

	private void toggleSidePanelButton()
	{
		if (config.hideSidePanelButton())
		{
			clientToolbar.removeNavigation(navButton);
			navButton.setSelected(false);
		}
		else
		{
			clientToolbar.addNavigation(navButton);
		}
	}
}
