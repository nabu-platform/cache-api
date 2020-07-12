package be.nabu.libs.cache.api;

import java.util.Map;

public interface AnnotatableCache {
	// annotations have to be strings so we are sure they can be easily serialized
	public void annotate(Object cacheKey, Map<String, String> annotations);
	// clear the caches matching all the given annotations (and operation). if you want an "or" operation, at this point you need to call the clear multiple times
	public void clear(Map<String, String> annotations);
}
