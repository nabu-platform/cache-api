package be.nabu.libs.cache.memory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import be.nabu.libs.cache.api.Cache;
import be.nabu.libs.cache.api.CacheRefresher;
import be.nabu.libs.cache.api.CacheTimeoutManager;
import be.nabu.libs.cache.api.DataSerializer;

public class MemoryCache implements Cache {

	private Map<Object, MemoryCacheEntry> entries = new HashMap<Object, MemoryCacheEntry>();
	private DataSerializer<?> keySerializer, valueSerializer;
	private CacheRefresher refresher;
	private CacheTimeoutManager timeoutManager;

	public MemoryCache(CacheRefresher cacheRefresher, CacheTimeoutManager timeoutManager) {
		this.refresher = cacheRefresher;
		this.timeoutManager = timeoutManager;
	}

	@Override
	public void clear(Object key) {
		synchronized(entries) {
			entries.remove(keySerializer == null ? key : serialize(key, keySerializer));
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

	@Override
	public boolean put(Object key, Object value) throws IOException {
		MemoryCacheEntry entry = new MemoryCacheEntry(key, valueSerializer == null ? value : serialize(value, valueSerializer));
		synchronized(entries) {
			entries.put(keySerializer == null ? key : serialize(key, keySerializer), entry);
		}
		return true;
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
	public void refresh() throws IOException {
		if (refresher != null) {
			synchronized(entries) {
				Iterator<MemoryCacheEntry> iterator = entries.values().iterator();
				while(iterator.hasNext()) {
					MemoryCacheEntry entry = iterator.next();
					Object refreshed = refresher.refresh(entry.getKey());
					if (refreshed == null) {
						iterator.remove();
					}
					else {
						entry.reload(valueSerializer == null ? refreshed : serialize(refreshed, valueSerializer));
					}
				}
			}
		}
	}

	@Override
	public Object get(Object key) throws IOException {
		MemoryCacheEntry memoryCacheEntry = entries.get(keySerializer == null ? key : serialize(key, keySerializer));
		Object value = memoryCacheEntry == null ? null : memoryCacheEntry.getValue();
		return value == null || valueSerializer == null ? value : deserialize(value, valueSerializer);
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

}
