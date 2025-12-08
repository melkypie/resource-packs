package melky.resourcepacks.event;

import java.util.List;
import lombok.Value;
import melky.resourcepacks.model.HubManifest;

@Value
public class ResourcePacksChanged
{
	List<HubManifest> newManifest;
}
