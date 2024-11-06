/*
* Copyright (C) 2015 Alexander Verbruggen
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with this program. If not, see <https://www.gnu.org/licenses/>.
*/

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
