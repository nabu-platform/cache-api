package be.nabu.libs.cache.memory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import be.nabu.libs.cache.api.Cache;
import be.nabu.libs.cache.api.CacheProvider;
import be.nabu.libs.cache.api.CacheRefresher;
import be.nabu.libs.cache.api.CacheTimeoutManager;

public class MemoryCacheProvider implements CacheProvider {

	private Map<String, MemoryCache> caches = new HashMap<String, MemoryCache>();
	private CacheRefresher refresher;
	private CacheTimeoutManager timeoutManager;
	
	public MemoryCacheProvider(CacheRefresher refresher, CacheTimeoutManager timeoutManager) {
		this.refresher = refresher;
		this.timeoutManager = timeoutManager;
	}
	
	@Override
	public Cache get(String name) {
		if (!caches.containsKey(name)) {
			synchronized(caches) {
				if (!caches.containsKey(name)) {
					caches.put(name, new MemoryCache(refresher, timeoutManager));
				}
			}
		}
		return caches.get(name);
	}

	@Override
	public void remove(String name) throws IOException {
		synchronized(caches) {
			caches.remove(name);
		}
	}

}
