package io.vertigo.database.impl.sql.vendor.core;

import java.io.IOException;
import java.io.InputStream;

public final class CloseAtEoFInputStream extends InputStream {

	private final InputStream inputStream;
	private final int totalLength;
	private int position = 0;

	public CloseAtEoFInputStream(final InputStream inputStream, final int totalLength) {
		this.inputStream = inputStream;
		this.totalLength = totalLength;
	}

	@Override
	public int read() throws IOException {
		final int read = inputStream.read();
		position++;
		if (read == -1 || position >= totalLength) { //close sub inputstream when EoF is reach
			inputStream.close();
		}
		return read;
	}
}
