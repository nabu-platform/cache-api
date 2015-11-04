package be.nabu.libs.cache.api;

import java.io.IOException;

public interface CacheRefresher {
	public Object refresh(Object key) throws IOException;
}
