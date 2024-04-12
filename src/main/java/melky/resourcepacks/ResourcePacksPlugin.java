package melky.resourcepacks;

import com.google.inject.Provides;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.ScheduledExecutorService;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import melky.resourcepacks.event.ResourcePacksChanged;
import melky.resourcepacks.hub.ResourcePacksHubPanel;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.ScriptID;
import net.runelite.api.VarClientInt;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.PostClientTick;
import net.runelite.api.events.ScriptCallbackEvent;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.WidgetUtil;
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

		if (client.getGameState() == GameState.LOGGED_IN && !configManager.getConfiguration("interfaceStyles", "gameframe", Skin.DEFAULT.getDeclaringClass()).equals(Skin.DEFAULT) &&
			!config.disableInterfaceStylesPrompt())
		{
			setInterfaceStylesGameframeOption();
		}

		executor.submit(() -> {
			resourcePacksManager.refreshPlugins();
			clientThread.invokeLater(resourcePacksManager::updateAllOverrides);
			clientThread.invokeLater(resourcePacksManager::refreshResizableMinimap);
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

	}

	@Override
	protected void shutDown() throws Exception
	{
		clientThread.invokeLater(() ->
		{
			resourcePacksManager.resetWidgetDimensions(true);
			resourcePacksManager.resetWidgetSpriteIds();
			resourcePacksManager.removeGameframe();
			resourcePacksManager.resetWidgetOverrides();
			resourcePacksManager.resetCrossSprites();
			resourcePacksManager.resetChatboxNameAndInput();
			resourcePacksManager.setSpecialAttackText(false);
			resourcePacksManager.setSpecialBarTo(false);
			resourcePacksManager.manageBankSeparatorLines(false);
			resourcePacksManager.refreshResizableMinimap();

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

	boolean updateWidgetSpriteIds = false;

	@Subscribe
	public void onPostClientTick(PostClientTick event)
	{
		resourcePacksManager.applyWidgetDimensions();

		if (!resourcePacksManager.checkIfResourcePackPathIsNotEmpty() || !config.allowCustomSpriteOverrides())
		{
			if (updateWidgetSpriteIds)
			{
				resourcePacksManager.resetWidgetSpriteIds();
			}
			updateWidgetSpriteIds = false;
			return;
		}

		resourcePacksManager.applyNewWidgetSpriteId();
		updateWidgetSpriteIds = true;
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
				case "selectedHubPack": //didn't update properly in some cases
					clientThread.invokeLater(resourcePacksManager::updateAllOverrides);
					if (event.getKey().equals("resourcePack") || event.getKey().equals("selectedHubPack"))
					{
						updateWidgetSpriteIds = true;
						clientThread.invokeLater(resourcePacksManager::refreshResizableMinimap);
					}
					break;

				case "allowHitsplats":
					clientThread.invokeLater(resourcePacksManager::updateAllOverrides);
					String hitsplatWarning = new ChatMessageBuilder()
						.append(ChatColorType.NORMAL)
						.append("[")
						.append(ChatColorType.HIGHLIGHT)
						.append("Resource Packs")
						.append(ChatColorType.NORMAL)
						.append("] Hitsplat changes will take effect the next time you logout or restart.")
						.build();

					chatMessageManager.queue(QueuedMessage.builder()
						.type(ChatMessageType.CONSOLE)
						.runeLiteFormattedMessage(hitsplatWarning)
						.build());
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

				case "allowCustomSpriteOverrides":
					updateWidgetSpriteIds = true;
					if (config.allowSpecialBarChanges())
					{
						clientThread.invokeLater(() -> resourcePacksManager.setSpecialBarTo(config.allowCustomSpriteOverrides()));
					}
					break;

				case "allowSpecialBarChanges":
				case "specialBarSelection":
					clientThread.invokeLater(() -> resourcePacksManager.setSpecialBarTo(
						(config.allowSpecialBarChanges() && config.allowCustomSpriteOverrides()))
					);
					break;

				case "retroSpecialAttackText":
				case "specialAttackTextShadowed":
				case "recolorSpecialAttackText":
				case "disableSpecialTextColor":
				case "enabledSpecialTextColor":
					clientThread.invokeLater(resourcePacksManager::resetSpecialAttackText);
					clientThread.invokeLater(() -> resourcePacksManager.setSpecialAttackText(true));
					break;

				case "allowChatboxNameRecolor":
				case "opaqueNameColor":
				case "opaqueChatboxInputColor":
				case "transparentNameColor":
				case "transparentChatboxInputColor":
					clientThread.invokeLater(resourcePacksManager::resetChatboxNameAndInput);
					if (config.allowChatboxNameRecolor())
					{
						clientThread.invokeLater(resourcePacksManager::recolorChatboxNameAndInput);
					}
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
			if(resourcePacksManager.checkIfResourcePackPathIsNotEmpty())
			{
				String warn = new ChatMessageBuilder()
					.append(ChatColorType.NORMAL)
					.append("[")
					.append(ChatColorType.HIGHLIGHT)
					.append("Resource Packs")
					.append(ChatColorType.NORMAL)
					.append("] Current pack will override the Overlay Color changes made. To disable, uncheck ")
					.append(ChatColorType.HIGHLIGHT)
					.append("'Allow overlay color to be changed' ")
					.append(ChatColorType.NORMAL)
					.append("in the config settings.")
					.build();

				chatMessageManager.queue(QueuedMessage.builder()
					.type(ChatMessageType.CONSOLE)
					.runeLiteFormattedMessage(warn)
					.build());
			}

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
		if (gameStateChanged.getGameState() == GameState.LOGIN_SCREEN)
		{
			resourcePacksManager.changeCrossSprites();
		}

		if (client.getGameState() == GameState.LOGGED_IN
			&& !configManager.getConfiguration("interfaceStyles", "gameframe", Skin.DEFAULT.getDeclaringClass()).equals(Skin.DEFAULT) &&
			!config.disableInterfaceStylesPrompt())
		{
			setInterfaceStylesGameframeOption();
			clientThread.invokeLater(resourcePacksManager::updateAllOverrides);
		}
	}
	
	@Subscribe
	public void onWidgetLoaded(WidgetLoaded event)
	{
		if (event.getGroupId() == WidgetUtil.componentToInterface(ComponentID.RESIZABLE_VIEWPORT_BOTTOM_LINE_LOGOUT_BUTTON_OVERLAY))
		{
			//on initial load, make sure everything is aligned
			clientThread.invokeLater(resourcePacksManager::refreshResizableMinimap);
		}
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

		//open tab via (keybind || click)
		boolean setSidebarTabIdScript = (event.getScriptId() == 905 || event.getScriptId() == 914);

		if (event.getScriptId() == 3805)
		{
			resourcePacksManager.replaceXPLampBackground();
		}

		//special bar (redraw || updatebar)
		if (event.getScriptId() == 186 || event.getScriptId() == 188 || setSidebarTabIdScript)
		{
			if (client.getVarcIntValue(VarClientInt.INVENTORY_TAB) == 0)
			{
				resourcePacksManager.setSpecialAttackText(true);
				resourcePacksManager.setSpecialBarTo((config.allowSpecialBarChanges() && config.allowCustomSpriteOverrides()));
			}
		}

		if (setSidebarTabIdScript && client.getVarcIntValue(VarClientInt.INVENTORY_TAB) == 12)
		{
			resourcePacksManager.removeEmoteTabGridLines();
		}

		if (event.getScriptId() == ScriptID.BANKMAIN_SIZE_CHECK)
		{
			resourcePacksManager.manageBankSeparatorLines(config.allowCustomSpriteOverrides());
		}
	}

	@Subscribe(priority = -0.1F)
	public void onScriptCallbackEvent(ScriptCallbackEvent event)
	{
		if (!config.allowChatboxNameRecolor())
		{
			return;
		}

		if (client.getGameState() == GameState.LOGIN_SCREEN)
		{
			return;
		}

		if (event.getEventName().equals("setChatboxInput"))
		{
			resourcePacksManager.recolorChatboxNameAndInput();
		}
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
		String message = new ChatMessageBuilder()
			.append(ChatColorType.NORMAL)
			.append("[")
			.append(ChatColorType.HIGHLIGHT)
			.append("Resource Packs")
			.append(ChatColorType.NORMAL)
			.append("] Your interface styles gameframe option was set to default to fix interfaces being misaligned. You can disable Resource packs changing it to default inside it's config")
			.build();

		chatMessageManager.queue(QueuedMessage.builder()
			.type(ChatMessageType.CONSOLE)
			.runeLiteFormattedMessage(message)
			.build());

		configManager.setConfiguration("interfaceStyles", "gameframe", Skin.DEFAULT);
	}
}
