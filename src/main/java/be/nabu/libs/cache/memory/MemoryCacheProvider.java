/*
* Copyright (C) 2015 Alexander Verbruggen
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with this program. If not, see <https://www.gnu.org/licenses/>.
*/

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
