package be.nabu.libs.cache.api;

import java.util.Collection;

public interface ExplorableCache extends Cache {
	public Collection<CacheEntry> getEntries();
	public CacheEntry getEntry(Object key);
}
