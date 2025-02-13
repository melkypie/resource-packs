package melky.resourcepacks;

import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.MoreFiles;
import com.google.common.io.RecursiveDeleteOption;
import com.google.gson.stream.JsonReader;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import melky.resourcepacks.ConfigKeys.InterfaceStyles;
import melky.resourcepacks.ConfigKeys.Plugins;
import melky.resourcepacks.ResourcePacksConfig.ResourcePack;
import static melky.resourcepacks.ResourcePacksPlugin.GITHUB;
import static melky.resourcepacks.ResourcePacksPlugin.OVERLAY_COLOR_CONFIG;
import melky.resourcepacks.event.ResourcePacksChanged;
import melky.resourcepacks.hub.ResourcePackManifest;
import melky.resourcepacks.hub.ResourcePacksClient;
import melky.resourcepacks.overrides.Overrides;
import melky.resourcepacks.overrides.WidgetOverride;
import net.runelite.api.Client;
import net.runelite.api.SpriteID;
import net.runelite.api.SpritePixels;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.RuneLiteConfig;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.Text;
import net.runelite.http.api.RuneLiteAPI;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Singleton
@Slf4j
public class ResourcePacksManager
{
	final List<SpritePixels> clientCrossSprites = new ArrayList<>();
	private boolean rsCrossSprites;

	@Inject
	private Client client;

	@Inject
	private ResourcePacksConfig config;

	@Inject
	private ConfigManager configManager;

	@Inject
	private SpriteManager spriteManager;

	@Inject
	private ScheduledExecutorService executor;

	@Inject
	private ResourcePacksClient resourcePacksClient;

	@Inject
	private ClientThread clientThread;

	@Inject
	private EventBus eventBus;

	@Inject
	private OkHttpClient okHttpClient;

	@Inject
	private Overrides overrides;

	public void touchFolder()
	{
		var currentFolder = getLocalPath().toFile();
		if (!currentFolder.exists())
		{
			currentFolder.mkdirs();
		}
	}

	public void refreshPlugins()
	{
		HashMap<String, ResourcePackManifest> loadedPacks = new HashMap<>();
		File[] resourcePackDirectories = getLocalPath().toFile().listFiles();
		if (resourcePackDirectories != null)
		{
			for (File resourcePackDirectory : resourcePackDirectories)
			{
				if (resourcePackDirectory.isDirectory())
				{
					try
					{
						ResourcePackManifest man = getResourcePackManifest(resourcePackDirectory);
						if (man != null)
						{
							loadedPacks.put(man.getInternalName(), man);
						}
					}
					catch (IOException ignored)
					{
					}
				}
			}
		}

		List<String> installedIDs = getInstalledResourcePacks();
		if (installedIDs.isEmpty() && loadedPacks.isEmpty())
		{
			return;
		}

		Set<ResourcePackManifest> resourcePacks = new HashSet<>();
		List<ResourcePackManifest> manifestList;
		try
		{
			manifestList = resourcePacksClient.downloadManifest();
			Map<String, ResourcePackManifest> manifests = manifestList
				.stream().collect(ImmutableMap.toImmutableMap(ResourcePackManifest::getInternalName, Function.identity()));

			Set<ResourcePackManifest> needsDownload = new HashSet<>();
			Set<File> keep = new HashSet<>();
			assert resourcePackDirectories != null;
			List<File> resourcePackDirectoryList = Arrays.asList(resourcePackDirectories);

			// Check for changed commits and packs that need to be downloaded
			for (String name : installedIDs)
			{
				ResourcePackManifest manifest = manifests.get(name);
				if (manifest != null)
				{
					resourcePacks.add(manifest);
					ResourcePackManifest loadedResourcePack = loadedPacks.get(manifest.getInternalName());
					File resourcePackDirectory = getLocalPath(manifest.getInternalName()).toFile();
					if (loadedResourcePack == null || !loadedResourcePack.equals(manifest))
					{
						needsDownload.add(manifest);
					}
					else if (loadedResourcePack.getCommit().equals(manifest.getCommit()) && resourcePackDirectoryList.contains(resourcePackDirectory))
					{
						keep.add(resourcePackDirectory);
					}
				}
			}

			// delete old packs
			for (File fi : resourcePackDirectoryList)
			{
				if (!keep.contains(fi))
				{
					MoreFiles.deleteRecursively(fi.toPath(), RecursiveDeleteOption.ALLOW_INSECURE);
				}
			}

			// Download packs that need updates/install
			for (ResourcePackManifest manifest : needsDownload)
			{
				HttpUrl url = GITHUB.newBuilder()
					.addPathSegment("archive")
					.addPathSegment(manifest.getCommit() + ".zip")
					.build();

				try (Response res = okHttpClient.newCall(new Request.Builder().url(url).build()).execute())
				{
					BufferedInputStream is = new BufferedInputStream(res.body().byteStream());
					ZipInputStream zipInputStream = new ZipInputStream(is);
					ZipEntry entry;
					while ((entry = zipInputStream.getNextEntry()) != null)
					{
						var filePath = getLocalPath(entry.getName().replaceAll("resource-packs-" + manifest.getCommit(), manifest.getInternalName()));
						if (!entry.isDirectory())
						{
							// if the entry is a file, extracts it
							BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath + ""));
							byte[] bytesIn = new byte[2048];
							int read;
							while ((read = zipInputStream.read(bytesIn)) != -1)
							{
								bos.write(bytesIn, 0, read);
							}
							bos.close();
						}
						else
						{
							// if the entry is a directory, make the directory
							File dir = filePath.toFile();
							dir.mkdir();
						}
					}
					zipInputStream.close();
					is.close();

					File manifestFile = getLocalPath(manifest.getInternalName(), "manifest.js").toFile();
					FileWriter manifestWriter = new FileWriter(manifestFile);
					RuneLiteAPI.GSON.toJson(manifest, manifestWriter);
					manifestWriter.close();
					// In case of total resource folder nuke
					if (config.selectedHubPack().equals(manifest.getInternalName()))
					{
						clientThread.invokeLater(this::updateAllOverrides);
					}
				}
				catch (IOException e)
				{
					resourcePacks.remove(manifest);
					log.error("Unable to download resource pack \"{}\"", manifest.getInternalName(), e);
				}
			}
		}
		catch (IOException e)
		{
			log.error("Unable to download resource packs", e);
			return;
		}
		for (ResourcePackManifest ex : resourcePacks)
		{
			loadedPacks.remove(ex.getInternalName());
		}

		// list of installed packs that aren't in the manifest
		Collection<ResourcePackManifest> remove = loadedPacks.values();
		for (ResourcePackManifest rem : remove)
		{
			log.info("Removing pack \"{}\"", rem.getInternalName());
			Set<String> packs = new HashSet<>(getInstalledResourcePacks());
			if (packs.remove(rem.getInternalName()))
			{
				config.selectedHubPack(Text.toCSV(packs));
			}
		}

		eventBus.post(new ResourcePacksChanged(manifestList));
	}

	private ResourcePackManifest getResourcePackManifest(File resourcePackDirectory) throws IOException
	{
		File manifest = Path.of(resourcePackDirectory.getPath(), "manifest.js").toFile();
		JsonReader reader = new JsonReader(new FileReader(manifest));
		ResourcePackManifest packManifest = RuneLiteAPI.GSON.fromJson(reader, ResourcePackManifest.class);
		reader.close();
		return packManifest;
	}

	public HashMultimap<String, ResourcePackManifest> getCurrentManifests() throws IOException
	{
		HashMultimap<String, ResourcePackManifest> currentManifests = HashMultimap.create();
		File[] directories = getLocalPath().toFile().listFiles();
		if (directories != null)
		{
			for (File resourcePackDirectory : directories)
			{
				if (!resourcePackDirectory.isDirectory())
				{
					continue;
				}
				ResourcePackManifest resourcePackManifest = getResourcePackManifest(resourcePackDirectory);
				currentManifests.put(resourcePackManifest.getInternalName(), resourcePackManifest);
			}
		}
		return currentManifests;
	}

	public void setSelectedHubPack(String internalName)
	{
		if (!internalName.equals("None"))
		{
			config.resourcePack(ResourcePack.HUB);
			config.selectedHubPack(internalName);
			clientThread.invokeLater(this::updateAllOverrides);
		}
		else
		{
			config.selectedHubPack("");
			clientThread.invokeLater(() ->
			{
				adjustWidgetDimensions(false);
				resetWidgetOverrides();
				resetLoginScreen();
				removeGameframe();
				resetCrossSprites();
				resetOverlayColor();
			});
		}
	}

	public List<String> getInstalledResourcePacks()
	{
		String resourcePacksString = config.hubPacks();
		return Text.fromCSV(resourcePacksString == null ? "" : resourcePacksString);
	}

	public void install(String internalName)
	{
		Set<String> packs = new HashSet<>(getInstalledResourcePacks());
		if (packs.add(internalName))
		{
			log.debug("Installing: {}", internalName);
			config.hubPacks(Text.toCSV(packs));
			config.resourcePack(ResourcePack.HUB);

			executor.submit(() ->
			{
				refreshPlugins();
				setSelectedHubPack(internalName);
			});
		}
	}

	public void remove(String internalName)
	{
		Set<String> packs = new HashSet<>(getInstalledResourcePacks());
		if (packs.remove(internalName))
		{
			log.debug("Removing: {}", internalName);
			config.hubPacks(Text.toCSV(packs));
			if (config.selectedHubPack() != null && config.selectedHubPack().equals(internalName))
			{
				setSelectedHubPack("None");
			}
			executor.submit(this::refreshPlugins);
		}
	}

	void updateAllOverrides()
	{
		if (!checkIfResourcePackPathIsNotEmpty())
		{
			return;
		}

		log.debug("updating all overrides");

		removeGameframe();
		overrideSprites();
		reloadWidgetOverrides();

		applyWidgetOverrides();
		adjustWidgetDimensions(false);
		adjustWidgetDimensions(true);

		saveClientSprites();
		resetCrossSprites();
		changeCrossSprites();
	}

	void removeGameframe()
	{
		restoreSprites();

		BufferedImage compassImage = spriteManager.getSprite(SpriteID.COMPASS_TEXTURE, 0);

		if (compassImage != null)
		{
			SpritePixels compass = ImageUtil.getImageSpritePixels(compassImage, client);
			client.setCompass(compass);
		}
	}

	void adjustWidgetDimensions(boolean modify)
	{
		for (WidgetResize widgetResize : WidgetResize.values())
		{
			Widget widget = client.getWidget(widgetResize.getComponent());

			if (widget != null)
			{
				if (widgetResize.getOriginalX() != null)
				{
					widget.setOriginalX(modify ? widgetResize.getModifiedX() : widgetResize.getOriginalX());
				}

				if (widgetResize.getOriginalY() != null)
				{
					widget.setOriginalY(modify ? widgetResize.getModifiedY() : widgetResize.getOriginalY());
				}

				if (widgetResize.getOriginalWidth() != null)
				{
					widget.setOriginalWidth(modify ? widgetResize.getModifiedWidth() : widgetResize.getOriginalWidth());
				}

				if (widgetResize.getOriginalHeight() != null)
				{
					widget.setOriginalHeight(modify ? widgetResize.getModifiedHeight() : widgetResize.getOriginalHeight());
				}

				widget.revalidate();
			}
		}
	}

	public void saveClientSprites()
	{
		boolean interfaceStylesEnabled = configManager.getConfiguration(RuneLiteConfig.GROUP_NAME, Plugins.interfacestylesplugin, Boolean.class);
		boolean isRs3 = configManager.getConfiguration(InterfaceStyles.GROUP_NAME, InterfaceStyles.rsCrossSprites, Boolean.class);
		isRs3 &= interfaceStylesEnabled;
		if (!clientCrossSprites.isEmpty() && rsCrossSprites == isRs3)
		{
			return;
		}

		clientCrossSprites.clear();
		rsCrossSprites = isRs3;

		String path = "/cross_sprites/" + (rsCrossSprites ? "rs3" : "osrs") + "/";

		SpritePixels[] crossSprites = client.getCrossSprites();
		if (crossSprites == null)
		{
			return;
		}

		int frames = crossSprites.length;
		for (int i = 0; i < frames; i++)
		{
			SpritePixels newSprite = loadResourceSprite(path + i + ".png");
			if (newSprite == null)
			{
				continue;
			}

			clientCrossSprites.add(newSprite);
		}
	}

	void restoreSprites()
	{
		client.getWidgetSpriteCache().reset();

		for (SpriteOverride spriteOverride : SpriteOverride.values())
		{
			if (spriteOverride.equals(SpriteOverride.LOGIN_SCREEN_BACKGROUND))
			{
				continue;
			}
			client.getSpriteOverrides().remove(spriteOverride.getSpriteID());
		}
		for (TabSprites tabSprite : TabSprites.values())
		{
			BufferedImage image = ImageUtil.loadImageResource(getClass(), tabSprite.getFileName());
			SpritePixels sp = ImageUtil.getImageSpritePixels(image, client);
			client.getSpriteOverrides().put(tabSprite.getSpriteId(), sp);
		}
	}

	public String getCurrentPackPath()
	{
		String path;
		switch (config.resourcePack())
		{
			case SECOND:
				path = config.resourcePack2Path();
				break;
			case THIRD:
				path = config.resourcePack3Path();
				break;
			case HUB:
				path = getLocalPath(config.selectedHubPack()) + "";
				break;
			case FIRST:
			default:
				path = config.resourcePackPath();
				break;
		}
		return path;
	}

	public boolean checkIfResourcePackPathIsNotEmpty()
	{
		switch (config.resourcePack())
		{
			case FIRST:
				if (config.resourcePackPath().isEmpty())
				{
					return false;
				}
				break;
			case SECOND:
				if (config.resourcePack2Path().isEmpty())
				{
					return false;
				}
				break;
			case HUB:
				if (config.selectedHubPack().isEmpty())
				{
					return false;
				}
				break;
			case THIRD:
				if (config.resourcePack3Path().isEmpty())
				{
					return false;
				}
				break;
		}
		return true;
	}

	public SpritePixels loadResourceSprite(String path)
	{
		try
		{
			BufferedImage image = ImageUtil.loadImageResource(ResourcePacksManager.class, path);
			return ImageUtil.getImageSpritePixels(image, client);
		}
		catch (RuntimeException e)
		{
			log.debug("Unable to find resource ({}): ", path);
		}

		return null;
	}

	public SpritePixels getSpritePixels(SpriteOverride spriteOverride, String currentPackPath)
	{
		String folder = spriteOverride.getFolder().name().toLowerCase();
		String name = spriteOverride.name().toLowerCase();
		if (!folder.equals("other"))
		{
			name = name.replaceFirst(folder + "_", "");
		}

		File spriteFile = Path.of(currentPackPath, folder, name + ".png").toFile();
		if (!spriteFile.exists())
		{
//			log.debug("Sprite doesn't exist ({}): ", spriteFile.getPath());
			return null;
		}
		try
		{
			BufferedImage image = ImageIO.read(spriteFile);
			if (config.allowColorPack() && config.colorPack() != null)
			{
				image = dye(image, config.colorPack());
			}
			return ImageUtil.getImageSpritePixels(image, client);
		}
		catch (RuntimeException | IOException ex)
		{
			log.debug("Unable to find image ({}): ", spriteFile.getPath());
		}
		return null;
	}

	void overrideSprites()
	{
		String currentPackPath = getCurrentPackPath();
		SpriteOverride.getOverrides().asMap().forEach((key, collection) ->
		{
			if (!Files.isDirectory(Paths.get(currentPackPath + File.separator + key.name().toLowerCase())) ||
				(!config.allowSpellsPrayers() && (key.name().contains("SPELL") || key.equals(SpriteOverride.Folder.PRAYER))) ||
				key == SpriteOverride.Folder.CROSS_SPRITES)
			{
				return;
			}

			for (SpriteOverride spriteOverride : collection)
			{

				SpritePixels spritePixels = getSpritePixels(spriteOverride, currentPackPath);
				if (config.allowLoginScreen() && spriteOverride == SpriteOverride.LOGIN_SCREEN_BACKGROUND)
				{
					if (spritePixels != null)
					{
						client.setLoginScreen(spritePixels);
					}
					else
					{
						resetLoginScreen();
					}
				}
				if (spritePixels == null)
				{
					continue;
				}

				if (spriteOverride.getSpriteID() == SpriteID.COMPASS_TEXTURE)
				{
					client.setCompass(spritePixels);
				}
				else
				{
					if (spriteOverride.getSpriteID() < -200)
					{
						client.getSpriteOverrides().remove(spriteOverride.getSpriteID());
					}
					client.getSpriteOverrides().put(spriteOverride.getSpriteID(), spritePixels);
				}
			}
		});
	}

	void reloadBankTagSprites()
	{
		String currentPackPath = getCurrentPackPath();
		SpriteOverride.getOverrides().asMap().forEach((key, collection) ->
		{
			if (!Files.isDirectory(Paths.get(currentPackPath + File.separator + key.name().toLowerCase())))
			{
				return;
			}
			for (SpriteOverride spriteOverride : collection)
			{
				if (spriteOverride.getSpriteID() < -200)
				{
					SpritePixels spritePixels = getSpritePixels(spriteOverride, currentPackPath);
					client.getSpriteOverrides().remove(spriteOverride.getSpriteID());
					client.getSpriteOverrides().put(spriteOverride.getSpriteID(), spritePixels);
				}
			}
		});
	}

	void resetLoginScreen()
	{
		ConfigChanged loginScreenConfigChanged = new ConfigChanged();
		loginScreenConfigChanged.setGroup(ConfigKeys.LoginScreen.GROUP_NAME);
		loginScreenConfigChanged.setKey("doesn't matter");
		loginScreenConfigChanged.setOldValue(null);
		loginScreenConfigChanged.setNewValue("");
		eventBus.post(loginScreenConfigChanged);
	}

	void resetInterfaceStyles()
	{
		ConfigChanged loginScreenConfigChanged = new ConfigChanged();
		loginScreenConfigChanged.setGroup(InterfaceStyles.GROUP_NAME);
		loginScreenConfigChanged.setKey("doesn't matter");
		loginScreenConfigChanged.setOldValue(null);
		loginScreenConfigChanged.setNewValue("");
		eventBus.post(loginScreenConfigChanged);
	}

	void reloadWidgetOverrides()
	{
		try
		{
			File overridesFile = Path.of(getCurrentPackPath(), "overrides.toml").toFile();
			if (overridesFile.exists())
			{
				var data = com.google.common.io.Files.asCharSource(overridesFile, Charset.defaultCharset()).read();
				overrides.buildOverrides(data);
			}
			else
			{
				log.debug("overrides.toml not found, trying color.properties");
				var backwardsMap = new Properties();
				var properties = new Properties();

				File propertiesFile = Path.of(getCurrentPackPath(), "color.properties").toFile();
				try (var is = new FileInputStream(propertiesFile); var is2 = ResourcePacksManager.class.getResourceAsStream("/overrides/backwards-map.properties"))
				{
					properties.load(is);
					backwardsMap.load(is2);
				}

				var lines = new ArrayList<String>();
				for (var entry : backwardsMap.entrySet())
				{
					if (properties.containsKey(entry.getValue()))
					{
						lines.add("[" + entry.getKey() + "]");
						lines.add("color=" + properties.get(entry.getValue()));
					}
				}

				log.debug("built {}", String.join("\n", lines));
				overrides.buildOverrides(String.join("\n", lines));
			}
		}
		catch (IOException e)
		{
			log.debug("error loading color overrides", e);


			overrides.buildOverrides("");
			resetOverlayColor();

			return;
		}

		if (config.allowOverlayColor())
		{
			changeOverlayColor();
		}
	}

	void changeOverlayColor()
	{
		if (Strings.isNullOrEmpty(config.originalOverlayColor()))
		{
			config.originalOverlayColor(configManager.getConfiguration(RuneLiteConfig.GROUP_NAME, OVERLAY_COLOR_CONFIG));
		}

		ResourcePacksPlugin.setIgnoreOverlayConfig(true);
		Color overlayColor = overrides.getOverlayColor();
		if (config.allowColorPack() && config.colorPack() != null && config.colorPack().getAlpha() != 0 && config.colorPackOverlay())
		{
			overlayColor = config.colorPack();
		}

		configManager.setConfiguration(RuneLiteConfig.GROUP_NAME, OVERLAY_COLOR_CONFIG, overlayColor);
		ResourcePacksPlugin.setIgnoreOverlayConfig(false);
	}

	void resetOverlayColor()
	{
		if (!Strings.isNullOrEmpty(config.originalOverlayColor()))
		{
			configManager.setConfiguration(RuneLiteConfig.GROUP_NAME, OVERLAY_COLOR_CONFIG, config.originalOverlayColor());
			configManager.unsetConfiguration(ResourcePacksConfig.GROUP_NAME, ResourcePacksConfig.ORIGINAL_OVERLAY_COLOR);
		}
	}

	void changeCrossSprites()
	{
		if (!config.allowCrossSprites())
		{
			return;
		}

		SpritePixels[] crossSprites = client.getCrossSprites();
		if (crossSprites == null)
		{
			return;
		}

		String currentPackPath = getCurrentPackPath();
		SpriteOverride.getOverrides().asMap().forEach((key, collection) ->
		{
			if (key != SpriteOverride.Folder.CROSS_SPRITES || !Files.isDirectory(Paths.get(currentPackPath + File.separator + key.name().toLowerCase())))
			{
				return;
			}

			for (SpriteOverride spriteOverride : collection)
			{
				SpritePixels spritePixels = getSpritePixels(spriteOverride, currentPackPath);
				if (spritePixels == null)
				{
					continue;
				}
				crossSprites[spriteOverride.getFrameID()] = spritePixels;
			}
		});
	}

	void resetCrossSprites()
	{
		SpritePixels[] crossSprites = client.getCrossSprites();

		if (crossSprites != null && clientCrossSprites.size() == crossSprites.length)
		{
			System.arraycopy(clientCrossSprites.toArray(new SpritePixels[0]), 0, crossSprites, 0, crossSprites.length);
		}
	}

	private BufferedImage dye(BufferedImage image, Color color)
	{
		int w = image.getWidth();
		int h = image.getHeight();
		BufferedImage dyed = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = dyed.createGraphics();
		g.drawImage(image, 0, 0, null);
		g.setComposite(AlphaComposite.SrcAtop);
		g.setColor(color);
		g.fillRect(0, 0, w, h);
		g.dispose();
		return dyed;
	}

	private void applyWidgetOverrides()
	{
		if (overrides.isEmpty())
		{
			return;
		}

		for (WidgetOverride widgetOverride : overrides.values())
		{
			addPropertyToWidget(widgetOverride, false);
		}
	}

	public void resetWidgetOverrides()
	{
		log.debug("resetting widget overrides");

		for (WidgetOverride widgetOverride : overrides.values())
		{
			addPropertyToWidget(widgetOverride, true);
		}

		overrides.clear();
	}

	public void addPropertyToWidget(WidgetOverride widgetOverride, boolean reset)
	{
		Widget widgetToOverride = client.getWidget(widgetOverride.getInterfaceId(), widgetOverride.getChildId());
		if (widgetToOverride == null)
		{
			return;
		}

		if (!widgetOverride.getDynamicChildren().isEmpty())
		{
			for (var arrayId : widgetOverride.getDynamicChildren())
			{
				Widget arrayWidget = widgetToOverride.getChild(arrayId);
				if (arrayWidget == null)
				{
					continue;
				}

				applyWidgetProperties(arrayWidget, widgetOverride, reset);
			}
		}
		else if (widgetOverride.isAllChildren())
		{
			for (var widget : widgetToOverride.getDynamicChildren())
			{
				if (widget == null)
				{
					continue;
				}

				applyWidgetProperties(widget, widgetOverride, reset);
			}
		}
		else
		{
			applyWidgetProperties(widgetToOverride, widgetOverride, reset);
		}
	}

	private void applyWidgetProperties(Widget widget, WidgetOverride widgetOverride, boolean reset)
	{
		if (widget == null || widget.getTextColor() == -1)
		{
			return;
		}

		int oldColor = widgetOverride.getColor();
		int newColor = widgetOverride.getNewColor();

		if (reset)
		{
			widget.setTextColor(oldColor);

			if (widget.getType() == widgetOverride.getNewType())
			{
				widget.setType(widgetOverride.getType());
			}

			if (widgetOverride.getOpacity() > -1)
			{
				widget.setOpacity(widgetOverride.getOpacity());
			}

			return;
		}

		if (widget.getTextColor() == newColor ||
			!widgetOverride.checkVarbit(client) ||
			typeCompare(widgetOverride, widget) ||
			explicitCompare(widgetOverride, widget))
		{
			return;
		}

		if (widgetOverride.isActiveWidget())
		{
			var w = client.getScriptActiveWidget();
			if (w == null || w.getId() != widget.getId() || w.getTextColor() != widgetOverride.getColor())
			{
				return;
			}

			widget = w;
		}

		widget.setTextColor(widgetOverride.getNewColor());

		if (widgetOverride.getNewOpacity() > -1)
		{
			widget.setOpacity(widgetOverride.getNewOpacity());
		}

		if (widgetOverride.getNewType() > -1)
		{
			log.debug("{} overriding widget type {} to {}", widget.getId(), widget.getType(), widgetOverride.getNewType());
			widget.setType(widgetOverride.getNewType());
			if (widgetOverride.getNewType() == 3)
			{
				widget.setFilled(true);
			}
		}
	}

	private static boolean typeCompare(WidgetOverride widgetOverride, Widget widget)
	{
		return widgetOverride.getType() > -1 && (widget.getType() != widgetOverride.getType() && widgetOverride.getNewType() != widget.getType());
	}

	private static boolean explicitCompare(WidgetOverride widgetOverride, Widget widget)
	{
		return widgetOverride.isExplicit() &&
			(widget.getTextColor() != widgetOverride.getColor() ||
				(widgetOverride.getType() > -1 && widget.getType() != widgetOverride.getType())
			);
	}

	public Path getLocalPath(String... path)
	{
		var p = Path.of(ResourcePacksPlugin.PACKS_BASE_DIR + "", configManager.getProfile().getId() + "");
		return Path.of(p + "", path);
	}
}
