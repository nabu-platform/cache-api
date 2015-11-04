package be.nabu.libs.cache.impl;

import be.nabu.libs.cache.api.CacheEntrySizer;

public class ByteEntrySizer implements CacheEntrySizer<byte[]> {

	@Override
	public long getSize(byte[] object) {
		return object == null ? 0 : object.length;
	}

	@Override
	public Class<byte[]> getDataClass() {
		return byte[].class;
	}

}
