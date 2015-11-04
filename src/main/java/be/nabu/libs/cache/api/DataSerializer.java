package be.nabu.libs.cache.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface DataSerializer<T> {
	public void serialize(T object, OutputStream output) throws IOException;
	public T deserialize(InputStream input) throws IOException;
	public Class<T> getDataClass();
}
