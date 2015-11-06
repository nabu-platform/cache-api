package be.nabu.libs.cache.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

import be.nabu.libs.cache.api.DataSerializer;

public class StringSerializer implements DataSerializer<String> {

	private Charset charset;

	public StringSerializer(Charset charset) {
		this.charset = charset;
	}
	
	public StringSerializer() {
		this(Charset.forName("UTF-8"));
	}
	
	@Override
	public void serialize(String object, OutputStream output) throws IOException {
		output.write(object.getBytes(charset));
	}

	@Override
	public String deserialize(InputStream input) throws IOException {
		int read = 0;
		byte [] buffer = new byte[4096];
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		while ((read = input.read(buffer)) > 0) {
			output.write(buffer, 0, read);
		}
		return new String(output.toByteArray(), charset);
	}

	@Override
	public Class<String> getDataClass() {
		return String.class;
	}

}
