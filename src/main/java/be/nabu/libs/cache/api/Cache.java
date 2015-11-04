package be.nabu.libs.cache.api;

import java.io.IOException;

public interface Cache {
	public boolean put(Object key, Object value) throws IOException;
	public Object get(Object key) throws IOException;
	public void clear(Object key) throws IOException;
	public void clear() throws IOException;
	public long getSize();
	public void prune() throws IOException;
	public void refresh() throws IOException;
}
