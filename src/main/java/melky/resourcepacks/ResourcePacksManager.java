package melky.resourcepacks;

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
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import static melky.resourcepacks.ResourcePacksPlugin.GITHUB;
import static melky.resourcepacks.ResourcePacksPlugin.OVERLAY_COLOR_CONFIG;
import melky.resourcepacks.event.ResourcePacksChanged;
import melky.resourcepacks.hub.ResourcePackManifest;
import melky.resourcepacks.hub.ResourcePacksClient;
import net.runelite.api.Client;
import net.runelite.api.ScriptID;
import net.runelite.api.SpriteID;
import net.runelite.api.SpritePixels;
import net.runelite.api.VarClientStr;
import net.runelite.api.VarPlayer;
import net.runelite.api.Varbits;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetType;
import net.runelite.api.widgets.WidgetUtil;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.RuneLiteConfig;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.util.ColorUtil;
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
	@Getter
	private final Properties colorProperties = new Properties();

	@Getter
	private final Properties offsetProperties = new Properties();
	private SpritePixels[] defaultCrossSprites;

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

	public void refreshPlugins()
	{
		HashMap<String, ResourcePackManifest> loadedPacks = new HashMap<>();
		File[] resourcePackDirectories = ResourcePacksPlugin.RESOURCEPACKS_DIR.listFiles();
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
					File resourcePackDirectory = new File(ResourcePacksPlugin.RESOURCEPACKS_DIR.toPath() + File.separator + manifest.getInternalName());
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
				if (!keep.contains(fi) && !fi.getPath().equals(ResourcePacksPlugin.NOTICE_FILE.getPath()))
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
						String filePath = ResourcePacksPlugin.RESOURCEPACKS_DIR.getPath() + File.separator +
							(entry.getName().replaceAll("resource-packs-" + manifest.getCommit(), manifest.getInternalName()));
						if (!entry.isDirectory())
						{
							// if the entry is a file, extracts it
							BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
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
							File dir = new File(filePath);
							dir.mkdir();
						}
					}
					zipInputStream.close();
					is.close();

					File manifestFile = new File(ResourcePacksPlugin.RESOURCEPACKS_DIR.getPath() + File.separator + manifest.getInternalName() + File.separator + "manifest.js");
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
				configManager.setConfiguration(ResourcePacksConfig.GROUP_NAME, ResourcePacksConfig.HUB_RESOURCEPACKS, Text.toCSV(packs));
			}
		}

		eventBus.post(new ResourcePacksChanged(manifestList));
	}

	private ResourcePackManifest getResourcePackManifest(File resourcePackDirectory) throws IOException
	{
		File manifest = new File(resourcePackDirectory.getPath() + File.separator + "manifest.js");
		JsonReader reader = new JsonReader(new FileReader(manifest));
		ResourcePackManifest packManifest = RuneLiteAPI.GSON.fromJson(reader, ResourcePackManifest.class);
		reader.close();
		return packManifest;
	}

	public HashMultimap<String, ResourcePackManifest> getCurrentManifests() throws IOException
	{
		HashMultimap<String, ResourcePackManifest> currentManifests = HashMultimap.create();
		File[] directories = ResourcePacksPlugin.RESOURCEPACKS_DIR.listFiles();
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
			configManager.setConfiguration(ResourcePacksConfig.GROUP_NAME, "resourcePack", ResourcePacksConfig.ResourcePack.HUB);
			configManager.setConfiguration(ResourcePacksConfig.GROUP_NAME, "selectedHubPack", internalName);
			clientThread.invokeLater(this::updateAllOverrides);
		}
		else
		{
			configManager.setConfiguration(ResourcePacksConfig.GROUP_NAME, "selectedHubPack", "");
			clientThread.invokeLater(() ->
			{
				applyWidgetChanges(false);
				replaceWidgetSprites(false);
				reloadColorProperties();
				reloadOffsetProperties();
				resetLoginScreen();
				removeGameframe();
			});
		}
	}

	public List<String> getInstalledResourcePacks()
	{
		String resourcePacksString = configManager.getConfiguration(ResourcePacksConfig.GROUP_NAME, ResourcePacksConfig.HUB_RESOURCEPACKS);
		return Text.fromCSV(resourcePacksString == null ? "" : resourcePacksString);
	}

	public void install(String internalName)
	{
		Set<String> packs = new HashSet<>(getInstalledResourcePacks());
		if (packs.add(internalName))
		{
			log.debug("Installing: " + internalName);
			configManager.setConfiguration(ResourcePacksConfig.GROUP_NAME, ResourcePacksConfig.HUB_RESOURCEPACKS, Text.toCSV(packs));
			configManager.setConfiguration(ResourcePacksConfig.GROUP_NAME, "resourcePack", ResourcePacksConfig.ResourcePack.HUB);

			executor.submit(() -> {
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
			log.debug("Removing: " + internalName);
			configManager.setConfiguration(ResourcePacksConfig.GROUP_NAME, ResourcePacksConfig.HUB_RESOURCEPACKS, Text.toCSV(packs));
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

		removeGameframe();
		overrideSprites();
		reloadColorProperties();
		reloadOffsetProperties();
		applyWidgetOverrides();
		applyWidgetChanges(false);
		applyWidgetChanges(true);
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

	void applyWidgetChanges(boolean modify)
	{
		adjustWidgetDimensions(modify);
		refreshSpecialAttackText(modify);
		if (!modify)
		{
			resetChatboxNameAndInput();
		}
	}

	public void resetOffsets()
	{
		offsetProperties.clear();
		for (WidgetResize widgetResize : WidgetResize.values())
		{
			Widget widget = client.getWidget(widgetResize.getComponentId());

			if (widget != null)
			{
				widget.revalidate();
			}
		}
	}

	void adjustWidgetDimensions(boolean modify)
	{
		for (WidgetResize widgetResize : WidgetResize.values())
		{
			int xOffset = 0;
			int yOffset = 0;
			//for minimap resizing TODO
			int widthOffset = 0;
			int heightOffset = 0;
			if (offsetProperties.containsKey(widgetResize.name().toLowerCase()))
			{
				String[] property = offsetProperties.getProperty(widgetResize.name().toLowerCase()).trim().split(",");
				for (int index = 0; index < property.length; index++)
				{
					if (property[index].isEmpty())
					{
						continue;
					}

					String value = property[index].replaceAll("[^\\d-]", "");
					if (value.startsWith("0") || (value.contains("-") && !value.startsWith("-")))
					{
						continue;
					}
					if (!Objects.equals(property[index], ""))
					{
						if (index == 0)
						{
							xOffset = Integer.parseInt(value);
						}
						if (index == 1)
						{
							yOffset = Integer.parseInt(value);
						}
						//for minimap resizing TODO
						if (index == 2)
						{
							widthOffset = Integer.parseInt(value);
						}
						//for minimap resizing TODO
						if (index == 3)
						{
							heightOffset = Integer.parseInt(value);
						}
					}
				}
			}
			else
			{
				if (modify && widgetResize.isModify())
				{
					continue;
				}
			}

			Widget widget = client.getWidget(widgetResize.getComponentId());

			if (widget != null)
			{
				if (widgetResize.getChildIndex() != null)
				{
					Widget child = widget.getChild(widgetResize.getChildIndex());
					if (child != null)
					{
						widget = child;
					}
				}

				if (widgetResize.getOriginalX() != null)
				{
					widget.setOriginalX(modify ? (xOffset != 0 ? widgetResize.getOriginalX() + xOffset : widgetResize.getModifiedX()) : widgetResize.getOriginalX());
				}

				if (widgetResize.getOriginalY() != null)
				{
					widget.setOriginalY(modify ? (yOffset != 0 ? widgetResize.getOriginalY() + yOffset : widgetResize.getModifiedY()) : widgetResize.getOriginalY());
				}

				if (widgetResize.getOriginalWidth() != null)
				{
					widget.setOriginalWidth(modify ? (widthOffset != 0 ? widgetResize.getOriginalWidth() + widthOffset : widgetResize.getModifiedWidth()) : widgetResize.getOriginalWidth());
				}

				if (widgetResize.getOriginalHeight() != null)
				{
					widget.setOriginalHeight(modify ? (heightOffset != 0 ? widgetResize.getOriginalHeight() + heightOffset : widgetResize.getModifiedHeight()) : widgetResize.getOriginalHeight());
				}
				
				//for minimap resizing TODO
				if (widgetResize.getWidthModeOrig() != null)
				{
					widget.setWidthMode(modify ? (widthOffset != 0 ? widgetResize.getWidthModeMod() : widgetResize.getWidthModeOrig()) : widgetResize.getWidthModeOrig());
				}
				
				widget.revalidate();
			}
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
		for (ResourceSprites re : ResourceSprites.values())
		{
			BufferedImage image = ImageUtil.loadImageResource(getClass(), re.getFileName());
			SpritePixels sp = ImageUtil.getImageSpritePixels(image, client);
			client.getSpriteOverrides().put(re.getSpriteId(), sp);
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
				path = ResourcePacksPlugin.RESOURCEPACKS_DIR + File.separator + config.selectedHubPack();
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
				if (config.resourcePackPath().equals(""))
				{
					return false;
				}
				break;
			case SECOND:
				if (config.resourcePack2Path().equals(""))
				{
					return false;
				}
				break;
			case HUB:
				if (config.selectedHubPack().equals(""))
				{
					return false;
				}
				break;
			case THIRD:
				if (config.resourcePack3Path().equals(""))
				{
					return false;
				}
				break;
		}
		return true;
	}

	public SpritePixels getSpritePixels(SpriteOverride spriteOverride, String currentPackPath)
	{
		String folder = spriteOverride.getFolder().name().toLowerCase();
		String name = spriteOverride.name().toLowerCase();
		if (!folder.equals("other"))
		{
			name = name.replaceFirst(folder + "_", "");
		}


		File spriteFile = new File(currentPackPath + File.separator + folder + File.separator + name + ".png");
		if (!spriteFile.exists())
		{
			log.debug("Sprite doesn't exist (" + spriteFile.getPath() + "): ");
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
			log.debug("Unable to find image (" + spriteFile.getPath() + "): ");
		}
		return null;
	}

	ArrayList<Integer> dontReplaceSpriteIdList = new ArrayList<Integer>();

	void overrideSprites()
	{
		if (!dontReplaceSpriteIdList.isEmpty())
		{
			dontReplaceSpriteIdList.clear();
		}

		String currentPackPath = getCurrentPackPath();
		SpriteOverride.getOverrides().asMap().forEach((key, collection) -> {
			if (!Files.isDirectory(Paths.get(currentPackPath + File.separator + key.name().toLowerCase())) ||
				(!config.allowSpellsPrayers() && (key.name().contains("SPELL") || key.equals(SpriteOverride.Folder.PRAYER))) ||
				(!config.allowHitsplats() && key.equals(SpriteOverride.Folder.HITSPLAT)) ||
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
					//flag
					if (spriteOverride.getSpriteID() < 0) //check only custom sprite ids
					{
						dontReplaceSpriteIdList.add(spriteOverride.getSpriteID());
					}

					continue;
				}

				if (spriteOverride.getSpriteID() == SpriteID.COMPASS_TEXTURE)
				{
					client.setCompass(spritePixels);
				}
				else
				{
					if (spriteOverride.getSpriteID() < -200 && spriteOverride.getSpriteID() > -211)
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
		SpriteOverride.getOverrides().asMap().forEach((key, collection) -> {
			if (!Files.isDirectory(Paths.get(currentPackPath + File.separator + key.name().toLowerCase())))
			{
				return;
			}
			for (SpriteOverride spriteOverride : collection)
			{
				if (spriteOverride.getSpriteID() < -200 && spriteOverride.getSpriteID() > -206)
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
		loginScreenConfigChanged.setGroup("loginscreen");
		loginScreenConfigChanged.setKey("loginScreen");
		loginScreenConfigChanged.setOldValue(null);
		loginScreenConfigChanged.setNewValue("");
		eventBus.post(loginScreenConfigChanged);
	}

	void reloadColorProperties()
	{
		colorProperties.clear();
		File colorPropertiesFile = new File(getCurrentPackPath() + "/color.properties");
		try (InputStream in = Files.newInputStream(colorPropertiesFile.toPath()))
		{
			colorProperties.load(in);
		}
		catch (IOException e)
		{
			log.debug("Color properties not found");
			resetOverlayColor();
			return;
		}
		if (config.allowOverlayColor())
		{
			changeOverlayColor();
		}
		// Add more properties
	}

	void reloadOffsetProperties()
	{
		offsetProperties.clear();
		File offsetPropertiesFile = new File(getCurrentPackPath() + "/offset.properties");
		try (InputStream in = Files.newInputStream(offsetPropertiesFile.toPath()))
		{
			offsetProperties.load(in);
		}
		catch (IOException e)
		{
			log.debug("Offset properties not found");
		}
	}

	void changeOverlayColor()
	{
		if (configManager.getConfiguration(ResourcePacksConfig.GROUP_NAME, ResourcePacksConfig.ORIGINAL_OVERLAY_COLOR) == null)
		{
			configManager.setConfiguration(ResourcePacksConfig.GROUP_NAME, ResourcePacksConfig.ORIGINAL_OVERLAY_COLOR,
				configManager.getConfiguration(RuneLiteConfig.GROUP_NAME, OVERLAY_COLOR_CONFIG));
		}
		ResourcePacksPlugin.setIgnoreOverlayConfig(true);
		Color overlayColor = ColorUtil.fromHex(colorProperties.getProperty("overlay_color"));
		if (config.allowColorPack() && config.colorPack() != null && config.colorPack().getAlpha() != 0 && config.colorPackOverlay())
		{
			overlayColor = config.colorPack();
		}

		configManager.setConfiguration(RuneLiteConfig.GROUP_NAME, OVERLAY_COLOR_CONFIG, overlayColor);
		ResourcePacksPlugin.setIgnoreOverlayConfig(false);
	}

	void resetOverlayColor()
	{
		if (configManager.getConfiguration(ResourcePacksConfig.GROUP_NAME, ResourcePacksConfig.ORIGINAL_OVERLAY_COLOR) != null)
		{
			configManager.setConfiguration(RuneLiteConfig.GROUP_NAME, OVERLAY_COLOR_CONFIG,
				configManager.getConfiguration(ResourcePacksConfig.GROUP_NAME, ResourcePacksConfig.ORIGINAL_OVERLAY_COLOR));
			configManager.unsetConfiguration(ResourcePacksConfig.GROUP_NAME, ResourcePacksConfig.ORIGINAL_OVERLAY_COLOR);
		}
	}

	void changeCrossSprites()
	{
		if (!config.allowCrossSprites() || Boolean.getBoolean(configManager.getConfiguration("interfaceStyles", "rsCrossSprites")) || defaultCrossSprites != null)
		{
			return;
		}

		SpritePixels[] crossSprites = client.getCrossSprites();

		if (crossSprites == null)
		{
			return;
		}
		defaultCrossSprites = new SpritePixels[crossSprites.length];
		System.arraycopy(crossSprites, 0, defaultCrossSprites, 0, defaultCrossSprites.length);

		String currentPackPath = getCurrentPackPath();
		SpriteOverride.getOverrides().asMap().forEach((key, collection) -> {
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
		if (defaultCrossSprites == null)
		{
			return;
		}

		SpritePixels[] crossSprites = client.getCrossSprites();

		if (crossSprites != null && defaultCrossSprites.length == crossSprites.length)
		{
			System.arraycopy(defaultCrossSprites, 0, crossSprites, 0, defaultCrossSprites.length);
		}

		defaultCrossSprites = null;
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
		if (colorProperties.isEmpty())
		{
			return;
		}

		for (WidgetOverride widgetOverride : WidgetOverride.values())
		{
			addPropertyToWidget(widgetOverride);
		}
	}

	public void resetWidgetOverrides()
	{
		colorProperties.clear();

		for (WidgetOverride widgetOverride : WidgetOverride.values())
		{
			addPropertyToWidget(widgetOverride);
		}
	}

	public void addPropertyToWidget(WidgetOverride widgetOverride)
	{
		int color = widgetOverride.getDefaultColor();
		int alpha = -1;
		if (colorProperties.containsKey(widgetOverride.name().toLowerCase()))
		{
			String property = colorProperties.getProperty(widgetOverride.name().toLowerCase());
			Color hex = ColorUtil.fromHex(property);
			if (!property.isEmpty())
			{
				if (hex != null && ColorUtil.isAlphaHex(property))
				{
					color = hex.getRGB();
					alpha = hex.getAlpha();
				}
				else
				{
					color = Integer.decode(property);
				}
			}
		}
		for (Integer override : widgetOverride.getComponentId())
		{
			Widget widget = client.getWidget(override);
			if (widget == null)
			{
				continue;
			}

			if (widgetOverride.getWidgetArrayIds()[0] != -1)
			{
				for (int arrayId : widgetOverride.getWidgetArrayIds())
				{
					Widget child = widget.getChild(arrayId);
					if (child == null || child.getTextColor() == -1 || child.getTextColor() == color)
					{
						continue;
					}
					if (override == ComponentID.CHATBOX_TRANSPARENT_BACKGROUND_LINES
						&& child.getWidth() != widgetOverride.getWidth())
					{
						continue;
					}
					if ((override == WidgetOverride.ComponentId.FORESTRY_BAG_BUTTON_COMPONENT_ID
						|| override == WidgetOverride.ComponentId.FORESTRY_SHOP_BUTTON_COMPONENT_ID
						|| override == WidgetOverride.ComponentId.GIANTS_FOUNDRY_SHOP_BUTTON_COMPONENT_ID
						|| override == WidgetOverride.ComponentId.FORTIS_COLOSSEUM_MODIFIERS_COMPONENT_IDS[0]
						|| override == WidgetOverride.ComponentId.FORTIS_COLOSSEUM_MODIFIERS_COMPONENT_IDS[1]
						|| override == WidgetOverride.ComponentId.FORTIS_COLOSSEUM_MODIFIERS_COMPONENT_IDS[2]
					)
						&& child.getTextColor() != widgetOverride.getDefaultColor())
					{
						continue;
					}
					if (WidgetUtil.componentToInterface(override) == WidgetUtil.componentToInterface(WidgetOverride.ComponentId.SMITHING_COMPONENT_IDS[0])
						&& !Objects.equals(child.getText(), ""))
					{
						continue;
					}

					child.setTextColor(color);

					//may need testing, GE main window hides the borders if an offer is open
					//using && instead of || fixes it for this instance
					if (alpha == -1 && child.getOpacity() != 0)
					{
						if (child.isHidden() && widget.getType() == WidgetType.RECTANGLE)
						{
							child.setHidden(false);
						}
						continue;
					}
					int opacity = 255 - alpha;

					if (alpha == 0)
					{
						child.setHidden(true);
					}
					else
					{
						child.setOpacity(opacity);
					}
				}
			}
			else
			{
				if (widget.getTextColor() != -1 || widget.getTextColor() != color)
				{
					widget.setTextColor(color);
				}

				if (alpha != -1 && widget.getOpacity() == 0)
				{
					int opacity = 255 - alpha;
					if (alpha == 0)
					{
						widget.setHidden(true);
					}
					else
					{
						widget.setOpacity(opacity);
					}
				}
				else
				{
					if (widget.isHidden() && widget.getType() == WidgetType.RECTANGLE)
					{
						widget.setHidden(false);
					}
				}
			}
		}
	}

	public void replaceWidgetSprites(boolean modify)
	{
		for (WidgetReplace replace : WidgetReplace.values())
		{
			int id = modify ? replace.getNewSpriteId() : replace.getDefaultSpriteId();
			for (Integer override : replace.getComponentId())
			{
				Widget widget = client.getWidget(override);
				if (widget == null || widget.isHidden())
				{
					continue;
				}

				if (replace.getChildIndex()[0] != -1)
				{
					for (int arrayId : replace.getChildIndex())
					{
						Widget child = widget.getChild(arrayId);
						if (child == null || child.isHidden())
						{
							continue;
						}
						if (child.getSpriteId() == id || dontReplaceSpriteIdList.contains(id))
						{
							continue;
						}
						child.setSpriteId(id);
					}
				}
				else
				{
					if (dontReplaceSpriteIdList.contains(id))
					{
						id = replace.getDefaultSpriteId();
						if (widget.getSpriteId() != -1 && widget.getSpriteId() == id)
						{
							continue;
						}
					}
					else
					{
						if(widget.getSpriteId() != -1 && widget.getSpriteId() == id)
						{
							continue;
						}
					}

					//assigned -1 to resizable inventory backgrounds in WidgetResize
					if (id == -1)
					{
						//check which background spriteId should be assigned based on in-game settings for transparent background
						if (override == WidgetReplace.Constants.RESIZABLE_VIEWPORT_CLASSIC_COMPONENT_ID ||
							override == WidgetReplace.Constants.RESIZABLE_VIEWPORT_MODERN_COMPONENT_ID)
						{
							id = widget.getOpacity() == 0 ? 897 : 1040;
						}
					}

					if (widget.getSpriteId() != -1 || widget.getSpriteId() != id)
					{
						widget.setSpriteId(id);
					}
				}
			}
		}
	}

	void recolorChatboxNameAndInput()
	{
		Widget chatboxInput = client.getWidget(ComponentID.CHATBOX_INPUT);
		if (chatboxInput == null)
		{
			return;
		}

		final boolean isChatboxTransparent = client.isResized() && client.getVarbitValue(Varbits.TRANSPARENT_CHATBOX) == 1;
		Color inputColor = isChatboxTransparent ? config.transparentChatboxInputColor() : config.opaqueChatboxInputColor();
		Color nameColor = isChatboxTransparent ? config.transparentNameColor() : config.opaqueNameColor();

		String[] chatInput = chatboxInput.getText().split(":", 2);
		String name = chatInput[0];
		String input = chatInput[1];
		int idx = chatboxInput.getText().indexOf(':');

		if (idx != -1)
		{
			String newName = ColorUtil.wrapWithColorTag(name + ":", nameColor);
			boolean enterToChat = (
				input.equals(" Press Enter to Chat...") ||
				configManager.getConfiguration("keyremappingplus", "promptText").endsWith(input + ColorUtil.CLOSING_COLOR_TAG) ||
				!input.endsWith("*" + ColorUtil.CLOSING_COLOR_TAG)
			);
			String newInput = enterToChat ? input : ColorUtil.wrapWithColorTag(client.getVarcStrValue(VarClientStr.CHATBOX_TYPED_TEXT) + "*", inputColor);

			String newText = newName + (!enterToChat ? " " : "") + newInput;

			chatboxInput.setText(newText);
		}
	}

	void resetChatboxNameAndInput()
	{
		client.runScript(ScriptID.CHAT_PROMPT_INIT);
	}
	
	void refreshResizableMinimap()
	{
		int id = -1;
		int enum1 = -1;
		if (!client.isResized())
		{
			return;
		}

		Widget minimapClassic = client.getWidget(ComponentID.RESIZABLE_VIEWPORT_MINIMAP);
		Widget minimapModern = client.getWidget(ComponentID.RESIZABLE_VIEWPORT_BOTTOM_LINE_MINIMAP);

		Widget logoutOverlay = client.getWidget(ComponentID.RESIZABLE_VIEWPORT_BOTTOM_LINE_LOGOUT_BUTTON_OVERLAY);

		if (minimapClassic == null || minimapClassic.isHidden())
		{
			if (minimapModern != null)
			{
				id = minimapModern.getId();
				enum1 = 1131;
			}
		}
		else
		{
			id = minimapClassic.getId();
			enum1 = 1130;
		}

		if (id != -1 || enum1 != -1)
		{
			client.runScript(902, id, enum1);
			if (logoutOverlay != null && !logoutOverlay.isHidden())
			{
				logoutOverlay.revalidate();
			}
		}
	}

	final String DEFAULT_SPECIAL_ATTACK_TEXT = "Special Attack: ";
	final String RETRO_SPECIAL_ATTACK_TEXT = "S P E C I A L  A T T A C K";
	final int COMBAT_SPECIAL_BAR_BORDER = 38862885;
	final int COMBAT_SPECIAL_BAR_EMPTY = 38862886;
	final int COMBAT_SPECIAL_BAR_FILL = 38862888;
	final int COMBAT_SPECIAL_BAR_TEXT = 38862889;
	final int COMBAT_SPECIAL_BAR_OUTLINE = 38862890;
	final SpriteOverride[] SPECIAL_BAR_BORDER_SPRITES = new SpriteOverride[]{
		SpriteOverride.COMBAT_SPECIAL_BORDER_TOP_LEFT_CORNER,
		SpriteOverride.COMBAT_SPECIAL_BORDER_TOP,
		SpriteOverride.COMBAT_SPECIAL_BORDER_TOP_RIGHT_CORNER,
		SpriteOverride.COMBAT_SPECIAL_BORDER_LEFT,
		SpriteOverride.COMBAT_SPECIAL_BORDER_MIDDLE,
		SpriteOverride.COMBAT_SPECIAL_BORDER_RIGHT,
		SpriteOverride.COMBAT_SPECIAL_BORDER_BOTTOM_LEFT_CORNER,
		SpriteOverride.COMBAT_SPECIAL_BORDER_BOTTOM,
		SpriteOverride.COMBAT_SPECIAL_BORDER_BOTTOM_RIGHT_CORNER,
	};

	int specialAttackTextColor(boolean modify)
	{
		switch (client.getVarpValue(VarPlayer.SPECIAL_ATTACK_ENABLED))
		{
			case 1:
				return (modify ? config.enabledSpecialTextColor().getRGB() : 0xffff00);

			case 0:
			default:
				return (modify ? config.disableSpecialTextColor().getRGB() : 10);
		}
	}

	void refreshSpecialAttackText(boolean modify)
	{
		Widget specialAttackText = client.getWidget(COMBAT_SPECIAL_BAR_TEXT);
		if (specialAttackText != null && !specialAttackText.isHidden())
		{
			specialAttackText.setText(((!config.retroSpecialAttackText() || !modify) ? DEFAULT_SPECIAL_ATTACK_TEXT + (client.getVarpValue(VarPlayer.SPECIAL_ATTACK_PERCENT) / 10) + "%" : RETRO_SPECIAL_ATTACK_TEXT));
			specialAttackText.setTextColor(specialAttackTextColor(config.recolorSpecialAttackText()));
		}
	}

	public void setSpecialBarTo(boolean modify)
	{
		boolean bar = (
			configManager.getConfiguration(ResourcePacksConfig.GROUP_NAME, "specialBarSelection").equals("BAR")
				|| configManager.getConfiguration(ResourcePacksConfig.GROUP_NAME, "specialBarSelection").equals("BOTH")
		) && modify;

		boolean border = (
			configManager.getConfiguration(ResourcePacksConfig.GROUP_NAME, "specialBarSelection").equals("BORDER")
				|| configManager.getConfiguration(ResourcePacksConfig.GROUP_NAME, "specialBarSelection").equals("BOTH")
		) && modify;

		int id = -1;

		Widget empty = client.getWidget(COMBAT_SPECIAL_BAR_EMPTY);
		if (empty != null && !empty.isHidden())
		{
			if (modify && bar)
			{
				id = SpriteOverride.COMBAT_SPECIAL_BAR_EMPTY.getSpriteID();
			}
			empty.setType((modify && bar) ? WidgetType.GRAPHIC : WidgetType.RECTANGLE);
			empty.setSpriteId(id);
		}

		Widget fill = client.getWidget(COMBAT_SPECIAL_BAR_FILL);
		if (fill != null && !fill.isHidden())
		{
			if (modify && bar)
			{
				id = (fill.getTextColor() == 0x397d3b ? SpriteOverride.COMBAT_SPECIAL_BAR_FULL.getSpriteID() : SpriteOverride.COMBAT_SPECIAL_BAR_FILL.getSpriteID());
				fill.setSpriteTiling(true);
			}
			fill.setType((modify && bar) ? WidgetType.GRAPHIC : WidgetType.RECTANGLE);
			fill.setSpriteId(id);
		}

		Widget outline = client.getWidget(COMBAT_SPECIAL_BAR_OUTLINE);
		if (outline != null && !outline.isHidden())
		{
			if (modify && bar)
			{
				id = SpriteOverride.COMBAT_SPECIAL_BAR_BORDER.getSpriteID();
			}
			outline.setType((modify && bar) ? WidgetType.GRAPHIC : WidgetType.RECTANGLE);
			outline.setSpriteId(id);
		}

		Widget widget = client.getWidget(COMBAT_SPECIAL_BAR_BORDER);
		if (widget != null && !widget.isHidden())
		{
			for (int index = 0; index < SPECIAL_BAR_BORDER_SPRITES.length; index++)
			{
				Widget child = widget.getChild(index);
				if (child != null)
				{
					id = SPECIAL_BAR_BORDER_SPRITES[index].getSpriteID();
					if (!border && id != -1 && child.getSpriteId() == Math.abs(id))
					{
						break;
					}

					if (!border && id != -1 || dontReplaceSpriteIdList.contains(id))
					{
						id = Math.abs(id);
					}
					child.setSpriteId(id);
				}
			}
		}
	}

	public void manageBankSeparatorLines(boolean modify)
	{
		Widget widget = client.getWidget(ComponentID.BANK_ITEM_CONTAINER);
		if (widget != null)
		{
			Widget bankTitle = client.getWidget(ComponentID.BANK_TITLE_BAR);
			if (bankTitle != null)
			{
				//bank tags tab
				if (bankTitle.getText().startsWith("Tag tab <col=ff0000>"))
				{
					if (configManager.getConfiguration("banktags", "removeTabSeparators").equals("true"))
					{
						for (Widget child : widget.getDynamicChildren())
						{
							if (child.getSpriteId() == -897 && !child.isHidden())
							{
								child.setHidden(true);
							}
						}
					}
				}

				//quest helper tab
				if (bankTitle.getText().startsWith("Tab <col=ff0000>"))
				{
					for (Widget child : widget.getDynamicChildren())
					{
						if (child.getIndex() >= 1220 && child.getIndex() <= 1228)
						{
							if (!child.isHidden())
							{
								child.setHidden(modify);
							}

						}
						if (!modify)
						{
							continue;
						}
						if (child.getSpriteId() == SpriteID.RESIZEABLE_MODE_SIDE_PANEL_BACKGROUND
							|| child.getText().contains("Tab"))
						{
							child.setSpriteId(-897);
						}
					}
				}
			}
		}
	}

	public void removeEmoteTabGridLines()
	{
		Widget widget = client.getWidget(ComponentID.EMOTES_EMOTE_CONTAINER);
		if (widget != null)
		{
			if (widget.getChildren() == null)
			{
				return;
			}

			for (int index = 0; index < (widget.getChildren().length / 2); index++)
			{
				Widget child = widget.getChild(index);
				if (child != null && child.getType() == WidgetType.RECTANGLE)
				{
					child.setType(WidgetType.GRAPHIC);
				}
			}
		}
	}

	public void replaceXPLampBackground()
	{
		Widget background = client.getWidget(15728641);
		if (background != null)
		{
			if (configManager.getConfiguration(ResourcePacksConfig.GROUP_NAME, "lampBackground").
				equals("DARK"))
			{
				background.setModelId(41569);
				background.setRotationX(0);
				background.setRotationY(0);
				background.setRotationZ(0);
				background.setModelZoom(508);
			}
			if (configManager.getConfiguration(ResourcePacksConfig.GROUP_NAME, "lampBackground").
				equals("SCROLL"))
			{
				background.setModelId(21821);
				background.setRotationX(512);
				background.setRotationY(0);
				background.setRotationZ(1033);
				background.setModelZoom(612);
			}
			if (configManager.getConfiguration(ResourcePacksConfig.GROUP_NAME, "lampBackground").
				equals("DARK_BLUE"))
			{
				background.setModelId(4011);
				background.setRotationX(0);
				background.setRotationY(0);
				background.setRotationZ(0);
				background.setModelZoom(550);
			}
			//could add more, depends on if an appropriate model exists : TODO
		}
	}

}
