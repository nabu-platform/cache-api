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
