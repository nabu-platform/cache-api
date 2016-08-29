package be.nabu.libs.cache.memory;

import java.util.Arrays;
import java.util.Date;

import be.nabu.libs.cache.api.CacheEntry;

public class MemoryCacheEntry implements Comparable<MemoryCacheEntry>, CacheEntry {
	
	private Object key;
	private Object value;
	private Date created, accessed;

	public MemoryCacheEntry(Object key, Object value) {
		this.key = key;
		this.value = value;
	}

	public Object getKey() {
		return key;
	}
	public void setKey(Object key) {
		this.key = key;
	}

	public Object getValue() {
		this.accessed = new Date();
		return value;
	}
	public void setValue(Object value) {
		this.value = value;
	}
	
	@Override
	public int compareTo(MemoryCacheEntry o) {
		return (int) (accessed.getTime() - o.accessed.getTime());
	}
	
	public void reload(Object value) {
		this.value = value;
		this.created = new Date();
		this.accessed = this.created;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (key instanceof Object[] ? Arrays.hashCode((Object[]) key) : key.hashCode());
		result = prime * result + (value instanceof Object[] ? Arrays.hashCode((Object[]) value) : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof MemoryCacheEntry)) {
			return false;
		}
		MemoryCacheEntry entry = (MemoryCacheEntry) obj;
		return obj instanceof MemoryCacheEntry
			&& (key instanceof Object[] && entry.key instanceof Object[] ? Arrays.equals((Object[]) key, (Object[]) entry.key) : key.equals(entry.key))
			&& (value instanceof Object[] && entry.value instanceof Object[] ? Arrays.equals((Object[]) value, (Object[]) entry.value) : value.equals(entry.value));
	}

	@Override
	public long getSize() {
		return -1;
	}

	@Override
	public Date getLastAccessed() {
		return accessed;
	}

	@Override
	public Date getLastModified() {
		return created;
	}
}
