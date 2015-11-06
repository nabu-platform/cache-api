package be.nabu.libs.cache.impl;

import java.util.Date;

import be.nabu.libs.cache.api.Cache;
import be.nabu.libs.cache.api.CacheEntry;
import be.nabu.libs.cache.api.CacheTimeoutManager;

public class LastModifiedTimeoutManager implements CacheTimeoutManager {

	private long cacheTimeout;

	public LastModifiedTimeoutManager(long cacheTimeout) {
		this.cacheTimeout = cacheTimeout;
	}
	
	@Override
	public boolean isTimedOut(Cache cache, CacheEntry entry) {
		return entry.getLastModified().before(new Date(new Date().getTime() - cacheTimeout));
	}

}
