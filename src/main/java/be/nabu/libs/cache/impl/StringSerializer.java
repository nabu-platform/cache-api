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
