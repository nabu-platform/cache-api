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

import java.lang.instrument.Instrumentation;

import be.nabu.libs.cache.CacheEntrySizerFactory;
import be.nabu.libs.cache.api.CacheEntrySizer;

/**
 * This class uses the instrumentation logic to determine the size of objects
 * For all JVMs it works this way:
 * - add to MANIFEST.MF: Premain-Class: be.nabu.libs.cache.impl.ObjectEntrySizer
 * - add a premain(String, Instrumentation) to the class in question
 * - put the resulting jar on the classpath
 * - start java with: java -javaagent:resulting.jar class
 * 
 * For some JVMs you can also request a new instance of the instrumentation logic at runtime
 */
public class ObjectEntrySizer implements CacheEntrySizer<Object> {

	private static Instrumentation instrumentation;
	
	public static void premain(String args, Instrumentation instrumentation) {
		ObjectEntrySizer.instrumentation = instrumentation;
		CacheEntrySizerFactory.getInstance().addSerializer(new ObjectEntrySizer());
	}
	
	private ObjectEntrySizer() {
		// hide constructor
	}
	 
	@Override
	public long getSize(Object object) {
		return object == null ? 0 : instrumentation.getObjectSize(object);
	}

	@Override
	public Class<Object> getDataClass() {
		return Object.class;
	}

}
