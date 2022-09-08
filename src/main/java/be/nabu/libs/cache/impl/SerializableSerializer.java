package be.nabu.libs.cache.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
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
		ObjectInputStream deserialized = new ObjectInputStream(input) {
			@Override
			protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
				Class<?> loadClass = null;
				try {
					loadClass = Thread.currentThread().getContextClassLoader().loadClass(desc.getName());
				}
				catch (Exception e) {
					// ignore
				}
				return loadClass == null ? super.resolveClass(desc) : loadClass;
			}
		};
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
