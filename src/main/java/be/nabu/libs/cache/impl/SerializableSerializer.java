package be.nabu.libs.cache.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

import be.nabu.libs.cache.api.DataSerializer;

public class SerializableSerializer implements DataSerializer<Serializable> {

	@Override
	public void serialize(Serializable object, OutputStream output) throws IOException {
		ObjectOutputStream serialized = new ObjectOutputStream(output);
		serialized.writeObject(object);
		serialized.flush();
	}

	@Override
	public Serializable deserialize(InputStream input) throws IOException {
		ObjectInputStream deserialized = new ObjectInputStream(input);
		try {
			return (Serializable) deserialized.readObject();
		}
		catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Class<Serializable> getDataClass() {
		return Serializable.class;
	}

}
