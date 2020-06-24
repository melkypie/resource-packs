package melky.resourcepacks.hub;

import java.net.URL;
import javax.annotation.Nullable;
import lombok.Data;

@Data
public class ResourcePackManifest
{
	private final String internalName;
	private final String commit;

	private final String displayName;
	private final String compatibleVersion;
	private final String author;
	@Nullable
	private final String[] tags;
	private final URL repo;
	private final boolean hasIcon;

	@Override
	public String toString()
	{
		return displayName;
	}
}
