package be.nabu.libs.cache.api;

public interface CacheTimeoutManager {
	public boolean isTimedOut(Cache cache, CacheEntry entry);
}
