package be.nabu.libs.cache.memory;

import java.io.IOException;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Date;

import be.nabu.libs.cache.api.CacheEntry;

public class MemoryCacheEntry implements Comparable<MemoryCacheEntry>, CacheEntry, Serializable {
	
	private static final long serialVersionUID = 5034420609980025109L;
	
	// we want an unserialized version of the key to recalculate the cache for example
	// but unserialized versions are not guaranteed to be serializable, so we skip it in serialization and instead have a manually serialized key
	private Object key;
	private Object value;
	private Date created, accessed;
	private String hash;

	public MemoryCacheEntry(Object key, Object value) throws IOException {
		this.key = key;
		this.value = value;
		this.created = new Date();
		this.accessed = this.created;
	}

	@Override
	public Object getKey() {
		return key;
	}

	public void setKey(Object key) throws IOException {
		this.key = key;
	}

	@Override
	public Object getValue() {
		this.accessed = new Date();
		return value;
	}
	public void setValue(Object value) {
		this.value = value;
	}
	
	@Override
	public int compareTo(MemoryCacheEntry o) {
		return (int) (accessed.getTime() - o.accessed.getTime());
	}
	
	public void reload(Object value) {
		this.value = value;
		this.created = new Date();
		this.accessed = this.created;
		this.hash = null;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		Object key = getKey();
		result = prime * result + (key instanceof Object[] ? Arrays.hashCode((Object[]) key) : key.hashCode());
		result = prime * result + (value instanceof Object[] ? Arrays.hashCode((Object[]) value) : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof MemoryCacheEntry)) {
			return false;
		}
		MemoryCacheEntry entry = (MemoryCacheEntry) obj;
		Object key = getKey();
		Object entryKey = entry.getKey();
		return obj instanceof MemoryCacheEntry
			&& (key instanceof Object[] && entryKey instanceof Object[] ? Arrays.equals((Object[]) key, (Object[]) entryKey) : key.equals(entryKey))
			&& (value instanceof Object[] && entry.value instanceof Object[] ? Arrays.equals((Object[]) value, (Object[]) entry.value) : value.equals(entry.value));
	}

	@Override
	public long getSize() {
		if (value instanceof byte []) {
			return ((byte[]) value).length;
		}
		else if (value instanceof String) {
			// unicode is 16 bits in java, so 2 bytes
			return ((String) value).length() * 2;
		}
		return -1;
	}

	@Override
	public Date getLastAccessed() {
		return accessed;
	}

	@Override
	public Date getLastModified() {
		return created;
	}
	
	public void accessed() {
		this.accessed = new Date();
	}
	
	public String getHash() {
		if (hash == null && (value instanceof byte[] || value instanceof String)) {
			try {
				this.hash = hash(value instanceof byte[] ? (byte[]) value : ((String) value).getBytes("UTF-8"), "MD5");
			}
			catch (IOException e) {
				// ignore...
			}
		}
		return hash;
	}
	
	public static String hash(byte[] bytes, String algorithm) throws IOException {
		try {
			MessageDigest digest = MessageDigest.getInstance(algorithm);
			digest.update(bytes, 0, bytes.length);
			byte [] hash = digest.digest();
			StringBuffer string = new StringBuffer();
			for (int i = 0; i < hash.length; ++i) {
				string.append(Integer.toHexString((hash[i] & 0xFF) | 0x100).substring(1, 3));
			}
			return string.toString();
		}
		catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}
}
