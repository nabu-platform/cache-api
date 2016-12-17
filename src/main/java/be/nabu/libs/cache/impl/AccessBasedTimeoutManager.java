package be.nabu.libs.cache.impl;

import java.util.Date;

import be.nabu.libs.cache.api.Cache;
import be.nabu.libs.cache.api.CacheEntry;
import be.nabu.libs.cache.api.CacheTimeoutManager;

public class AccessBasedTimeoutManager implements CacheTimeoutManager {

	// setting a timeout of 0 will make it indefinite
	private long cacheTimeout;

	public AccessBasedTimeoutManager(long cacheTimeout) {
		this.cacheTimeout = cacheTimeout;
	}
	
	@Override
	public boolean isTimedOut(Cache cache, CacheEntry entry) {
		return cacheTimeout == 0 ? false : entry.getLastAccessed().before(new Date(new Date().getTime() - cacheTimeout));
	}

}
