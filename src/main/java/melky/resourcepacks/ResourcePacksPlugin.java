package melky.resourcepacks;

import com.google.inject.Provides;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.concurrent.ScheduledExecutorService;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import melky.resourcepacks.ConfigKeys.InterfaceStyles;
import melky.resourcepacks.event.ResourcePacksChanged;
import melky.resourcepacks.hub.ResourcePacksHubPanel;
import melky.resourcepacks.overrides.Overrides;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.BeforeRender;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.client.RuneLite;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.RuneLiteConfig;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.events.ProfileChanged;
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
	public static final Path PACKS_BASE_DIR = Path.of(RuneLite.RUNELITE_DIR.getPath(), "resource-packs-repository");
	public static final String BRANCH = "github-actions";
	public static final String OVERLAY_COLOR_CONFIG = "overlayBackgroundColor";
	public static final HttpUrl GITHUB = HttpUrl.parse("https://github.com/melkypie/resource-packs");
	public static final HttpUrl RAW_GITHUB = HttpUrl.parse("https://raw.githubusercontent.com/melkypie/resource-packs");
	public static final HttpUrl API_GITHUB = HttpUrl.parse("https://api.github.com/repos/melkypie/resource-packs");

	@Setter
	private static boolean ignoreOverlayConfig = false;

	@Inject
	private ChatMessageManager chatMessageManager;

	@Inject
	private Client client;

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

	@Inject
	private Overrides overrides;

	private ResourcePacksHubPanel resourcePacksHubPanel;
	private NavigationButton navButton;
	private long currentProfile = Long.MIN_VALUE;
	private GameState lastGameState;

	@Provides
	ResourcePacksConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ResourcePacksConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		var packsDir = PACKS_BASE_DIR.toFile();
		if (!packsDir.exists())
		{
			packsDir.mkdirs();
		}

		var noticeFile = Path.of(PACKS_BASE_DIR + "", "DO_NOT_EDIT_CHANGES_WILL_BE_OVERWRITTEN").toFile();
		if (!noticeFile.exists())
		{
			noticeFile.createNewFile();
		}

		resourcePacksManager.touchFolder();

		if (client.getGameState() == GameState.LOGGED_IN &&
			configManager.getConfiguration(InterfaceStyles.GROUP_NAME, InterfaceStyles.gameframe, Skin.class) != Skin.DEFAULT &&
			!config.disableInterfaceStylesPrompt())
		{
			setInterfaceStylesGameframeOption();
		}

		executor.submit(() ->
		{
			resourcePacksManager.refreshPlugins();
			queueUpdateAllOverrides();
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

		currentProfile = configManager.getProfile().getId();
	}

	@Override
	protected void shutDown()
	{
		clientThread.invokeLater(() ->
		{
			resourcePacksManager.adjustWidgetDimensions(false);
			resourcePacksManager.removeGameframe();
			resourcePacksManager.resetWidgetOverrides();
			resourcePacksManager.resetCrossSprites();
			resourcePacksManager.clientCrossSprites.clear();
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

	@Subscribe(priority = Float.MIN_VALUE)
	public void onConfigChanged(ConfigChanged event)
	{
		if (currentProfile != configManager.getProfile().getId())
		{
			return;
		}

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
			config.originalOverlayColor(event.getNewValue());

			if (config.displayWarnings())
			{
				sendWarning("Your overlay color will be overwritten by your resource pack. You can disable this feature by turning off 'Allow overlay color to be changed'.");
			}
		}
		else if (shouldReset(event))
		{
			// lazy reset to try and be after other plugins
			clientThread.invokeLater(() -> clientThread.invokeLater(resourcePacksManager::updateAllOverrides));
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

	@Subscribe(priority = Float.MIN_VALUE)
	public void onProfileChanged(ProfileChanged event)
	{
		currentProfile = configManager.getProfile().getId();
		resourcePacksManager.touchFolder();

		executor.submit(() ->
		{
			resourcePacksManager.refreshPlugins();
			clientThread.invokeLater(resourcePacksManager::updateAllOverrides);
		});
	}

	@Subscribe(priority = Float.MIN_VALUE)
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGIN_SCREEN)
		{
			resourcePacksManager.changeCrossSprites();

			if (lastGameState == GameState.STARTING)
			{
				queueUpdateAllOverrides();
			}
		}

		if (client.getGameState() == GameState.LOGGED_IN &&
			configManager.getConfiguration(InterfaceStyles.GROUP_NAME, InterfaceStyles.gameframe, Skin.class) != Skin.DEFAULT &&
			!config.disableInterfaceStylesPrompt())
		{
			setInterfaceStylesGameframeOption();
			clientThread.invokeLater(resourcePacksManager::updateAllOverrides);
		}

		lastGameState = gameStateChanged.getGameState();
	}

	@Subscribe
	public void onScriptPostFired(ScriptPostFired event)
	{
		if (!overrides.isEmpty() && overrides.contains(event.getScriptId()))
		{
			for (var widgetOverride : overrides.get(event.getScriptId()))
			{
				resourcePacksManager.addPropertyToWidget(widgetOverride, false);
			}
		}
	}

	private void queueUpdateAllOverrides()
	{
		clientThread.invokeLater(() ->
		{
			if (client.getGameState().getState() < GameState.LOGIN_SCREEN.getState())
			{
				return false;
			}

			resourcePacksManager.updateAllOverrides();
			return true;
		});
	}

	private static boolean shouldReset(ConfigChanged event)
	{
		return event.getGroup().equals(InterfaceStyles.GROUP_NAME) ||
			(event.getGroup().equals(RuneLiteConfig.GROUP_NAME) && ConfigKeys.Plugins.interfacestylesplugin.equals(event.getKey()));
	}

	private void toggleSidePanelButton()
	{
		if (config.hideSidePanelButton())
		{
			clientToolbar.removeNavigation(navButton);
		}
		else
		{
			clientToolbar.addNavigation(navButton);
		}
	}

	private void setInterfaceStylesGameframeOption()
	{
		if (config.displayWarnings())
		{
			sendWarning("Your interface styles gameframe option was set to default to fix interfaces being misaligned. You can disable Resource packs changing it to default inside it's config");
		}

		configManager.setConfiguration(InterfaceStyles.GROUP_NAME, InterfaceStyles.gameframe, Skin.DEFAULT);
	}

	private void sendWarning(String msg)
	{
		String message = new ChatMessageBuilder()
			.append(ChatColorType.NORMAL)
			.append("[")
			.append(ChatColorType.HIGHLIGHT)
			.append("Resource Packs")
			.append(ChatColorType.NORMAL)
			.append("] " + msg)
			.build();

		chatMessageManager.queue(QueuedMessage.builder()
			.type(ChatMessageType.CONSOLE)
			.runeLiteFormattedMessage(message)
			.build());
	}
}
