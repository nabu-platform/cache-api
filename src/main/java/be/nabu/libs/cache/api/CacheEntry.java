package be.nabu.libs.cache.api;

import java.io.IOException;
import java.util.Date;

public interface CacheEntry {
	public long getSize();
	public Date getLastAccessed();
	public Date getLastModified();
	public Object getKey() throws IOException;
	public Object getValue() throws IOException;
}
