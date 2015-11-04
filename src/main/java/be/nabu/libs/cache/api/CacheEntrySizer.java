package be.nabu.libs.cache.api;

public interface CacheEntrySizer<T> {
	public long getSize(T object);
	public Class<T> getDataClass();
}
