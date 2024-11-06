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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import be.nabu.libs.cache.api.AnnotatableCache;
import be.nabu.libs.cache.api.CacheAnnotater;
import be.nabu.libs.cache.api.CacheEntry;
import be.nabu.libs.cache.api.CacheRefresher;
import be.nabu.libs.cache.api.CacheTimeoutManager;
import be.nabu.libs.cache.api.CacheWithHash;
import be.nabu.libs.cache.api.DataSerializer;
import be.nabu.libs.cache.api.ExplorableCache;

public class MemoryCache implements ExplorableCache, CacheWithHash, AnnotatableCache {

	private Map<Object, Map<String, String>> annotations;
	private Map<Object, MemoryCacheEntry> entries;
	private DataSerializer<?> keySerializer, valueSerializer;
	private CacheRefresher refresher;
	private CacheTimeoutManager timeoutManager;
	// if we want to for example cluster the memory cache entries, we need to serialize the entry key in it
	// we want to be able to turn this off to avoid the overhead
	private boolean serializeEntryKey;
	private CacheAnnotater annotater;
	
	public MemoryCache(CacheRefresher cacheRefresher, CacheTimeoutManager timeoutManager, Map<Object, MemoryCacheEntry> entries, Map<Object, Map<String, String>> annotations, boolean serializeEntryKey) {
		this.refresher = cacheRefresher;
		this.timeoutManager = timeoutManager;
		this.entries = entries;
		this.annotations = annotations;
		this.serializeEntryKey = serializeEntryKey;
	}
	
	public MemoryCache(CacheRefresher cacheRefresher, CacheTimeoutManager timeoutManager) {
		this(cacheRefresher, timeoutManager, new HashMap<Object, MemoryCacheEntry>(), new HashMap<Object, Map<String, String>>(), false);
	}

	@Override
	public void clear(Object key) {
		synchronized(entries) {
			Object serializedKey = keySerializer == null ? key : serialize(key, keySerializer);
			entries.remove(serializedKey);
			annotations.remove(serializedKey);
		}
	}
	
	private void serializedClear(Object serializedKey) {
		synchronized(entries) {
			entries.remove(serializedKey);
			annotations.remove(serializedKey);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private String serialize(Object object, DataSerializer serializer) {
		try {
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			serializer.serialize(object, output);
			return javax.xml.bind.DatatypeConverter.printHexBinary(output.toByteArray());
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private Object deserialize(Object object, DataSerializer<?> serializer) {
		try {
			return serializer.deserialize(new ByteArrayInputStream(javax.xml.bind.DatatypeConverter.parseHexBinary((String) object)));
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void clear() {
		synchronized(entries) {
			entries.clear();
		}
	}
	
	public Map<Object, MemoryCacheEntry> getEntryMap() {
		return entries;
	}

	@Override
	public boolean put(Object key, Object value) throws IOException {
		boolean annotateError = false;
		try {
			Object serializedKey = keySerializer == null ? key : serialize(key, keySerializer);
			MemoryCacheEntry entry = new MemoryCacheEntry(serializeEntryKey ? serializedKey : key, valueSerializer == null ? value : serialize(value, valueSerializer));
			// load the annotations _before_ setting the entry, if annotating fails, we don't store the entry. if you are expecting annotated caches and they fail, this can be dangerous as you can't reset the cache as you would expect (which is the primary reason for annotations)
			Map<String, String> annotations = null;
			if (annotater != null) {
				annotateError = true;
				annotations = annotater.annotate(key, value);
				annotateError = false;
			}
			synchronized(entries) {
				entries.put(serializedKey, entry);
				if (annotations != null) {
					serializedAnnotate(serializedKey, annotations);
				}
			}
			return true;
		}
		catch (Exception e) {
			if (!annotateError) {
				e.printStackTrace();
			}
			return false;
		}
	}

	@Override
	public void prune() throws IOException {
		if (timeoutManager != null) {
			synchronized(entries) {
				Iterator<MemoryCacheEntry> iterator = entries.values().iterator();
				while(iterator.hasNext()) {
					MemoryCacheEntry entry = iterator.next();
					if (timeoutManager.isTimedOut(this, entry)) {
						iterator.remove();
					}
				}
			}
		}
	}

	@Override
	public void refresh(Object key) throws IOException {
		if (refresher != null) {
			synchronized(entries) {
				Object unserializedKey = null;
				// see description in get(key) for dirty-ness
				MemoryCacheEntry memoryCacheEntry = key instanceof String ? entries.get(key) : null;
				if (memoryCacheEntry == null && keySerializer != null) {
					unserializedKey = key;
					key = serialize(key, keySerializer);
					memoryCacheEntry = entries.get(key);
				}
				if (memoryCacheEntry != null) {
					if (unserializedKey == null) {
						unserializedKey = keySerializer != null ? deserialize(key, keySerializer) : key;
					}
					Object refreshed = refresher.refresh(unserializedKey);
					if (refreshed == null) {
						if (timeoutManager != null && timeoutManager.isTimedOut(this, memoryCacheEntry)) {
							entries.remove(key);
						}
					}
					else {
						memoryCacheEntry.reload(valueSerializer == null ? refreshed : serialize(refreshed, valueSerializer));
						// if annotation fails, we retain original annotations
						if (annotater != null) {
							try {
								serializedAnnotate(key, annotater.annotate(unserializedKey, refreshed));
							}
							catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
		}
	}
	
	@Override
	public void refresh() throws IOException {
		if (refresher != null) {
			synchronized(entries) {
				Iterator<Entry<Object, MemoryCacheEntry>> iterator = entries.entrySet().iterator();
				// in case of a cluster, modifying the entry directly will not be picked up by the clustering logic
				// we need to remerge the updated values to reflect the changes in all the nodes
				Map<Object, MemoryCacheEntry> remerge = new HashMap<Object, MemoryCacheEntry>();
				while(iterator.hasNext()) {
					Entry<Object, MemoryCacheEntry> next = iterator.next();
					MemoryCacheEntry entry = next.getValue();
					Object key = entry.getKey();
					// if the key has been serialized, deserialize it for refreshing
					if (serializeEntryKey && keySerializer != null) {
						key = deserialize(key, keySerializer);
					}
					Object refreshed = refresher.refresh(key);
					// if we can not refresh it _and_ the current value has outlived its usefulness, remove it
					// otherwise the old value will remain until it is no longer valid
					if (refreshed == null) {
						if (timeoutManager != null && timeoutManager.isTimedOut(this, next.getValue())) {
							iterator.remove();
						}
					}
					else {
						entry.reload(valueSerializer == null ? refreshed : serialize(refreshed, valueSerializer));
						remerge.put(next.getKey(), entry);
						// if annotation fails, we retain original annotations
						if (annotater != null) {
							try {
								serializedAnnotate(key, annotater.annotate(key, refreshed));
							}
							catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}
				entries.putAll(remerge);
			}
		}
	}

	@Override
	public Object get(Object key) throws IOException {
		// bit of a hack: we want memory cache entries to be serializable, so we send in serialized keys
		// that means anyone who gets a hold of the key from the entry, actually has the serialized version
		// the hazelcast map will not do an equals to find a match but instead serialize the incoming key to check it against existing keys
		// this means we can only request the instance if it has indeed been serialized
		// note that the serialization routine here creates a string, not a byte array
		MemoryCacheEntry memoryCacheEntry = key instanceof String ? entries.get(key) : null;
		if (memoryCacheEntry == null && keySerializer != null) {
			key = serialize(key, keySerializer);
			memoryCacheEntry = entries.get(key);
		}
		if (memoryCacheEntry != null) {
			if (timeoutManager != null && timeoutManager.isTimedOut(this, memoryCacheEntry)) {
				synchronized(entries) {
					entries.remove(key);
				}
			}
			else {
				Object value = memoryCacheEntry.getValue();
				return value == null || valueSerializer == null ? value : deserialize(value, valueSerializer);
			}
		}
		return null;
	}

	public DataSerializer<?> getKeySerializer() {
		return keySerializer;
	}
	public void setKeySerializer(DataSerializer<?> keySerializer) {
		this.keySerializer = keySerializer;
	}

	public DataSerializer<?> getValueSerializer() {
		return valueSerializer;
	}
	public void setValueSerializer(DataSerializer<?> valueSerializer) {
		this.valueSerializer = valueSerializer;
	}

	@Override
	public Collection<CacheEntry> getEntries() {
		synchronized(entries) {
			return new ArrayList<CacheEntry>(entries.values());
		}
	}

	@Override
	public CacheEntry getEntry(Object key) {
		// check logic above in get()
		MemoryCacheEntry memoryCacheEntry = key instanceof String ? entries.get(key) : null;
		if (memoryCacheEntry == null && keySerializer != null) {
			key = serialize(key, keySerializer);
			memoryCacheEntry = entries.get(key);
		}
		if (memoryCacheEntry != null) {
			if (timeoutManager != null && timeoutManager.isTimedOut(this, memoryCacheEntry)) {
				synchronized(entries) {
					entries.remove(key);
				}
			}
			else {
				memoryCacheEntry.accessed();
				return memoryCacheEntry;
			}
		}
		return null;
	}

	@Override
	public String hash(Object key) {
		CacheEntry entry = getEntry(key);
		return entry == null ? null : ((MemoryCacheEntry) entry).getHash();
	}

	@Override
	public void annotate(Object key, Map<String, String> annotations) {
		if (annotations != null && !annotations.isEmpty()) {
			Object serializedKey = keySerializer == null ? key : serialize(key, keySerializer);
			serializedAnnotate(serializedKey, annotations);
		}
	}
	
	private void serializedAnnotate(Object serializedKey, Map<String, String> annotations) {
		if (annotations != null && !annotations.isEmpty()) {
			synchronized(this.annotations) {
				if (!this.annotations.containsKey(serializedKey)) {
					this.annotations.put(serializedKey, new HashMap<String, String>(annotations));
				}
				else {
					this.annotations.get(serializedKey).putAll(annotations);
				}
			}
		}
	}

	@Override
	public void clear(Map<String, String> annotations) {
		if (annotations != null && !annotations.isEmpty()) {
			List<Object> clear = new ArrayList<Object>();
			for (Map.Entry<Object, Map<String, String>> entry : this.annotations.entrySet()) {
				Map<String, String> entryAnnotations = entry.getValue();
				boolean match = true;
				for (Map.Entry<String, String> annotation : annotations.entrySet()) {
					String sourceValue = entryAnnotations.get(annotation.getKey());
					String targetValue = annotation.getValue();
					if (targetValue == null && sourceValue != null) {
						match = false;
					}
					else if (targetValue != null && sourceValue == null) {
						match = false;
					}
					else if (targetValue != null && !targetValue.equals(sourceValue)) {
						match = false;
					}
					if (!match) {
						break;
					}
				}
				if (match) {
					clear.add(entry.getKey());
				}
			}
			for (Object single : clear) {
				serializedClear(single);
			}
		}
	}

	public CacheAnnotater getAnnotater() {
		return annotater;
	}
	public void setAnnotater(CacheAnnotater annotater) {
		this.annotater = annotater;
	}
}
