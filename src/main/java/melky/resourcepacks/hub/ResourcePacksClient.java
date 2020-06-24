package melky.resourcepacks.hub;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import static melky.resourcepacks.ResourcePacksPlugin.API_GITHUB;
import static melky.resourcepacks.ResourcePacksPlugin.GITHUB;
import static melky.resourcepacks.ResourcePacksPlugin.RAW_GITHUB;
import net.runelite.client.RuneLite;
import net.runelite.client.util.Text;
import net.runelite.http.api.RuneLiteAPI;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Slf4j
public class ResourcePacksClient
{

	private final OkHttpClient cachingClient;

	@Inject
	public ResourcePacksClient(OkHttpClient cachingClient)
	{
		this.cachingClient = cachingClient;
	}

	public List<ResourcePackManifest> downloadManifest() throws IOException
	{
		HttpUrl manifest = RAW_GITHUB
			.newBuilder()
			.addPathSegment("master")
			.addPathSegment("manifest.js")
			.build();

		try (Response res = cachingClient.newCall(new Request.Builder().url(manifest).build()).execute())
		{
			if (res.code() != 200)
			{
				throw new IOException("Non-OK response code: " + res.code());
			}

			String data = res.body().string();

			return RuneLiteAPI.GSON.fromJson(data,
				new TypeToken<List<ResourcePackManifest>>()
				{
				}.getType());
		}
		//return useOfflineManifest();
	}


	// Only run after exporting
	public List<ResourcePackManifest> useOfflineManifest() throws IOException
	{
		log.debug("Using offline manifest");
		File manifestFile = new File(RuneLite.RUNELITE_DIR.getPath() + File.separator + "manifest.js");
		try (JsonReader reader = new JsonReader(new FileReader(manifestFile)))
		{
			return RuneLiteAPI.GSON.fromJson(reader, new TypeToken<List<ResourcePackManifest>>()
			{
			}.getType());
		}
	}

	public BufferedImage downloadIcon(ResourcePackManifest plugin) throws IOException
	{
		if (!plugin.isHasIcon())
		{
			return null;
		}

		HttpUrl url = RAW_GITHUB
			.newBuilder()
			.addPathSegment(plugin.getCommit())
			.addPathSegment("icon.png")
			.build();

		try (Response res = cachingClient.newCall(new Request.Builder().url(url).build()).execute())
		{
			byte[] bytes = res.body().bytes();
			// We don't stream so the lock doesn't block the edt trying to load something at the same time
			synchronized (ImageIO.class)
			{
				return ImageIO.read(new ByteArrayInputStream(bytes));
			}
		}
	}

	// Run before using offline manifest
	public void extractPacks() throws IOException
	{

		HttpUrl url = API_GITHUB
			.newBuilder()
			.addPathSegment("branches")
			.build();

		try (Response res = cachingClient.newCall(new Request.Builder().url(url).build()).execute())
		{
			String data = res.body().string();
			// We don't stream so the lock doesn't block the edt trying to load something at the same time
			JsonParser jsonParser = new JsonParser();
			Properties properties = new Properties();
			List<ResourcePackManifest> resourcePackManifests = new ArrayList<>();
			for (JsonElement jsonElement : jsonParser.parse(data).getAsJsonArray())
			{
				JsonObject jsonObject = jsonElement.getAsJsonObject();
				String internalName = jsonObject.get("name").getAsString();
				if (internalName.startsWith("pack-"))
				{
					JsonObject commitObject = jsonObject.get("commit").getAsJsonObject();
					String commit = commitObject.get("sha").getAsString();
					log.debug("Checking: " + internalName + " : " + commit);
					HttpUrl packProeprtiesUrl = RAW_GITHUB
						.newBuilder()
						.addPathSegment(commit)
						.addPathSegment("pack.properties")
						.build();
					try (Response packPropertiesResponse = cachingClient.newCall(new Request.Builder().url(packProeprtiesUrl).build()).execute())
					{
						if (packPropertiesResponse.code() != 200)
						{
							log.debug(internalName + " doesn't have a pack.properties file - skipping");
							continue;
						}
						properties.load(packPropertiesResponse.body().byteStream());
						String displayName = properties.getProperty("displayName");
						String compatibleVersion = properties.getProperty("compatibleVersion");
						String author = properties.getProperty("author");
						String tags = properties.getProperty("tags");
						String[] tagsArray = null;
						if (tags != null && !tags.equals(""))
						{
							tagsArray = Text.fromCSV(properties.getProperty("tags")).toArray(new String[0]);
						}
						URL repoUrl = GITHUB
							.newBuilder()
							.addPathSegment("tree")
							.addPathSegment(internalName)
							.build().url();

						HttpUrl packiconUrl = RAW_GITHUB
							.newBuilder()
							.addPathSegment(commit)
							.addPathSegment("icon.png")
							.build();
						boolean hasIcon = false;
						try (Response packIconResponse = cachingClient.newCall(new Request.Builder().url(packiconUrl).build()).execute())
						{
							if (packIconResponse.code() == 200)
							{
								hasIcon = true;
							}
						}

						resourcePackManifests.add(new ResourcePackManifest(internalName, commit, displayName, compatibleVersion, author, tagsArray, repoUrl, hasIcon));
					}
				}
			}
			log.debug("Writing manifest.js");
			File manifestFile = new File(RuneLite.RUNELITE_DIR + File.separator + "manifest.js");
			if (manifestFile.exists())
			{
				manifestFile.delete();
			}
			// Write to file
			FileWriter fileWriter = new FileWriter(manifestFile);
			Gson gson = RuneLiteAPI.GSON;//new GsonBuilder().setPrettyPrinting().create();
			gson.toJson(resourcePackManifests, fileWriter);
			fileWriter.close();
		}
	}
}
