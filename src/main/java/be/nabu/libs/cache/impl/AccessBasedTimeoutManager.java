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
