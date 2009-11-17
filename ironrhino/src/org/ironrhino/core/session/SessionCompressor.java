package org.ironrhino.core.session;

public interface SessionCompressor<T> {

	public boolean supportsKey(String key);

	public String compress(T value);

	public T uncompress(String string);

}
