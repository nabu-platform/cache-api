package be.nabu.libs.cache.api;

import java.util.Date;

public interface CacheEntry {
	public long getSize();
	public Date getLastAccessed();
	public Date getLastModified();
}
