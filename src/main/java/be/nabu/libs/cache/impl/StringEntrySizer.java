package be.nabu.libs.cache.impl;

import be.nabu.libs.cache.api.CacheEntrySizer;

public class StringEntrySizer implements CacheEntrySizer<String> {

	@Override
	public long getSize(String object) {
		return object == null ? 0 : object.length();
	}

	@Override
	public Class<String> getDataClass() {
		return String.class;
	}

}
