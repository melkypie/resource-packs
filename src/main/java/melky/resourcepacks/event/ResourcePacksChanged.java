package melky.resourcepacks.event;

import java.util.List;
import lombok.Value;
import melky.resourcepacks.hub.ResourcePackManifest;

@Value
public class ResourcePacksChanged
{
	List<ResourcePackManifest> newManifest;
}
