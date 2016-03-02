package be.nabu.libs.cache.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import be.nabu.libs.cache.api.DataSerializer;

public class ByteSerializer implements DataSerializer<byte[]> {

	@Override
	public void serialize(byte[] object, OutputStream output) throws IOException {
		output.write(object);
	}

	@Override
	public byte[] deserialize(InputStream input) throws IOException {
		int read = 0;
		byte [] buffer = new byte[4096];
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		while ((read = input.read(buffer)) > 0) {
			output.write(buffer, 0, read);
		}
		return output.toByteArray();
	}

	@Override
	public Class<byte[]> getDataClass() {
		return byte[].class;
	}

}
