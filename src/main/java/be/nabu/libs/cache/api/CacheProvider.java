package be.nabu.libs.cache.api;

import java.io.IOException;

public interface CacheProvider {
	public Cache get(String name) throws IOException;
	public void remove(String name) throws IOException;
}
