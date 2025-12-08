package melky.resourcepacks.event;

import java.util.List;
import lombok.Value;
import melky.resourcepacks.features.hub.HubManifest;

@Value
public class ResourcePacksChanged
{
	List<HubManifest> newManifest;
}
