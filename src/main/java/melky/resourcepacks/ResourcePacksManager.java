package melky.resourcepacks;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;
import com.google.common.io.MoreFiles;
import com.google.common.io.RecursiveDeleteOption;
import com.google.gson.stream.JsonReader;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import static melky.resourcepacks.ResourcePacksPlugin.GITHUB;
import melky.resourcepacks.event.ResourcePacksChanged;
import melky.resourcepacks.hub.ResourcePackManifest;
import melky.resourcepacks.hub.ResourcePacksClient;
import net.runelite.api.Client;
import net.runelite.api.SpriteID;
import net.runelite.api.SpritePixels;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.Text;
import net.runelite.http.api.RuneLiteAPI;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;

@Singleton
@Slf4j
public class ResourcePacksManager
{
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

				try (Response res = RuneLiteAPI.CLIENT.newCall(new Request.Builder().url(url).build()).execute())
				{
					BufferedInputStream is = new BufferedInputStream(res.body().byteStream());
					ZipInputStream zipInputStream = new ZipInputStream(is);
					ZipEntry entry;
					while ((entry = zipInputStream.getNextEntry()) != null)
					{
						String filePath = ResourcePacksPlugin.RESOURCEPACKS_DIR.getPath() + File.separator +
							(entry.getName().replaceAll("resource-packs-" + manifest.getCommit(), manifest.getInternalName()));
						if (!entry.isDirectory()) {
							// if the entry is a file, extracts it
							BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
							byte[] bytesIn = new byte[2048];
							int read;
							while ((read = zipInputStream.read(bytesIn)) != -1) {
								bos.write(bytesIn, 0, read);
							}
							bos.close();
						} else {
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
				adjustWidgetDimensions(false);
				removeGameframe();
			});
		}
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
		if (!spriteFile.exists()) {
			log.debug("Sprite doesn't exist (" + spriteFile.getPath() + "): ");
			return null;
		}
		try
		{
			BufferedImage image = ImageIO.read(spriteFile);
			return ImageUtil.getImageSpritePixels(image, client);
		}
		catch (RuntimeException | IOException ex)
		{
			log.debug("Unable to find image (" + spriteFile.getPath() + "): ");
		}
		return null;
	}

	public String getCurrentPackPath() {
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
		adjustWidgetDimensions(false);
		adjustWidgetDimensions(true);
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
			Widget widget = client.getWidget(widgetResize.getGroup(), widgetResize.getChild());

			if (widget != null)
			{
				if (widgetResize.getOriginalX() != null)
				{
					if (modify)
					{
						widget.setOriginalX(widgetResize.getModifiedX());
					}
					else
					{
						widget.setOriginalX(widgetResize.getOriginalX());
					}
				}

				if (widgetResize.getOriginalY() != null)
				{
					if (modify)
					{
						widget.setOriginalY(widgetResize.getModifiedY());
					}
					else
					{
						widget.setOriginalY(widgetResize.getOriginalY());
					}
				}

				if (widgetResize.getOriginalWidth() != null)
				{
					if (modify)
					{
						widget.setOriginalWidth(widgetResize.getModifiedWidth());
					}
					else
					{
						widget.setOriginalWidth(widgetResize.getOriginalWidth());
					}
				}

				if (widgetResize.getOriginalHeight() != null)
				{
					if (modify)
					{
						widget.setOriginalWidth(widgetResize.getModifiedHeight());
					}
					else
					{
						widget.setOriginalWidth(widgetResize.getOriginalHeight());
					}
				}
			}
			if (widget != null)
			{
				widget.revalidate();
			}
		}
	}

	void restoreSprites()
	{
		client.getWidgetSpriteCache().reset();

		for (SpriteOverride spriteOverride : SpriteOverride.values())
		{
			client.getSpriteOverrides().remove(spriteOverride.getSpriteID());
		}
		for (TabSprites tabSprite : TabSprites.values())
		{
			BufferedImage image = ImageUtil.getResourceStreamFromClass(getClass(), tabSprite.getFileName());
			SpritePixels sp = ImageUtil.getImageSpritePixels(image, client);
			client.getSpriteOverrides().put(tabSprite.getSpriteId(), sp);
		}
	}

	void overrideSprites()
	{
		String currentPackPath = getCurrentPackPath();
		for (SpriteOverride spriteOverride : SpriteOverride.values())
		{
			SpritePixels spritePixels = getSpritePixels(spriteOverride, currentPackPath);
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
	}
}
