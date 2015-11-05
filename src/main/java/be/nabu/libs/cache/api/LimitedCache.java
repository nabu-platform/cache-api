package be.nabu.libs.cache.api;

public interface LimitedCache extends Cache {
	public long getCurrentSize();
	public long getMaxEntrySize();
	public long getMaxTotalSize();
}
