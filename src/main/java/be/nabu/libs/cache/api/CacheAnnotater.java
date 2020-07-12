package be.nabu.libs.cache.api;

import java.util.Map;

import javax.jws.WebParam;
import javax.jws.WebResult;

public interface CacheAnnotater {
	@WebResult(name = "annotations")
	public Map<String, String> annotate(@WebParam(name = "key") Object key, @WebParam(name = "value") Object value);
}
