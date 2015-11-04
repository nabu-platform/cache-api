package be.nabu.libs.cache;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import be.nabu.libs.cache.api.DataSerializer;

public class DataSerializationFactory {

	private static DataSerializationFactory instance;
	
	private List<DataSerializer<?>> serializers = new ArrayList<DataSerializer<?>>();
	
	public static DataSerializationFactory getInstance() {
		if (instance == null) {
			synchronized(DataSerializationFactory.class) {
				if (instance == null) {
					instance = new DataSerializationFactory();
				}
			}
		}
		return instance;
	}
	
	public void addSerializer(DataSerializer<?> serializer) {
		serializers.add(serializer);
	}
	
	public void removeSerializer(DataSerializer<?> serializer) {
		serializers.remove(serializer);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private List<DataSerializer<?>> getSerializers() {
		if (serializers.isEmpty()) {
			try {
				// let's try this with custom service loading based on a configuration
				Class<?> clazz = getClass().getClassLoader().loadClass("be.nabu.utils.services.ServiceLoader");
				Method declaredMethod = clazz.getDeclaredMethod("load", Class.class);
				serializers.addAll((List<DataSerializer<?>>) declaredMethod.invoke(null, DataSerializer.class));
			}
			catch (ClassNotFoundException e) {
				// ignore, the framework is not present
			}
			catch (NoSuchMethodException e) {
				// corrupt framework?
				throw new RuntimeException(e);
			}
			catch (SecurityException e) {
				throw new RuntimeException(e);
			}
			catch (IllegalAccessException e) {
				// ignore
			}
			catch (InvocationTargetException e) {
				// ignore
			}
			// if there are still no instances, fall back to SPI
			// it is actually possible that you _are_ using the custom service loader, but 
			if (serializers.isEmpty()) {
				List<DataSerializer<?>> serializers = new ArrayList<DataSerializer<?>>();
				ServiceLoader<DataSerializer> serviceLoader = ServiceLoader.load(DataSerializer.class);
				for (DataSerializer<?> serializer : serviceLoader) {
					serializers.add(serializer);
				}
				this.serializers.addAll(serializers);
			}
		}
		return serializers;
	}
	
	@SuppressWarnings("unused")
	private void activate() {
		instance = this;
	}
	@SuppressWarnings("unused")
	private void deactivate() {
		instance = null;
	}
	
	@SuppressWarnings("unchecked")
	public <T> DataSerializer<T> getSerializer(Class<T> clazz) {
		DataSerializer<T> closest = null;
		for (DataSerializer<?> serializer : getSerializers()) {
			if (serializer.getDataClass().isAssignableFrom(clazz)) {
				if (closest == null || closest.getDataClass().isAssignableFrom(serializer.getDataClass())) {
					closest = (DataSerializer<T>) serializer;
				}
			}
		}
		return closest;
	}
}
