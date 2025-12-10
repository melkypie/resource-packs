package melky.resourcepacks;

import com.google.inject.Binder;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import melky.resourcepacks.module.ComponentManager;
import melky.resourcepacks.module.ResourcePacksModule;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@PluginDescriptor(
	name = "Resource packs"
)
@Slf4j
public class ResourcePacksPlugin extends Plugin
{
	@Inject
	private ComponentManager componentManager;

	@Override
	public void configure(Binder binder)
	{
		binder.install(new ResourcePacksModule());
	}

	@Override
	protected void startUp() throws Exception
	{
		componentManager.onPluginStart();
	}

	@Override
	protected void shutDown()
	{
		componentManager.onPluginStop();
	}
}
