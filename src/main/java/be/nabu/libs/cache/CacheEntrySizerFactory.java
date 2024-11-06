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

package be.nabu.libs.cache;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import be.nabu.libs.cache.api.CacheEntrySizer;

public class CacheEntrySizerFactory {

	private static CacheEntrySizerFactory instance;
	
	private List<CacheEntrySizer<?>> entrySizers = new ArrayList<CacheEntrySizer<?>>();
	
	public static CacheEntrySizerFactory getInstance() {
		if (instance == null) {
			synchronized(CacheEntrySizerFactory.class) {
				if (instance == null) {
					instance = new CacheEntrySizerFactory();
				}
			}
		}
		return instance;
	}
	
	public void addSerializer(CacheEntrySizer<?> serializer) {
		entrySizers.add(serializer);
	}
	
	public void removeSerializer(CacheEntrySizer<?> serializer) {
		entrySizers.remove(serializer);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private List<CacheEntrySizer<?>> getSerializers() {
		if (entrySizers.isEmpty()) {
			try {
				// let's try this with custom service loading based on a configuration
				Class<?> clazz = getClass().getClassLoader().loadClass("be.nabu.utils.services.ServiceLoader");
				Method declaredMethod = clazz.getDeclaredMethod("load", Class.class);
				entrySizers.addAll((List<CacheEntrySizer<?>>) declaredMethod.invoke(null, CacheEntrySizer.class));
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
			if (entrySizers.isEmpty()) {
				List<CacheEntrySizer<?>> serializers = new ArrayList<CacheEntrySizer<?>>();
				ServiceLoader<CacheEntrySizer> serviceLoader = ServiceLoader.load(CacheEntrySizer.class);
				for (CacheEntrySizer<?> cacheEntrySizer : serviceLoader) {
					serializers.add(cacheEntrySizer);
				}
				this.entrySizers.addAll(serializers);
			}
		}
		return entrySizers;
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
	public <T> CacheEntrySizer<T> getEntrySizer(Class<T> clazz) {
		CacheEntrySizer<T> closest = null;
		for (CacheEntrySizer<?> serializer : getSerializers()) {
			if (serializer.getDataClass().isAssignableFrom(clazz)) {
				if (closest == null || closest.getDataClass().isAssignableFrom(serializer.getDataClass())) {
					closest = (CacheEntrySizer<T>) serializer;
				}
			}
		}
		return closest;
	}
}
