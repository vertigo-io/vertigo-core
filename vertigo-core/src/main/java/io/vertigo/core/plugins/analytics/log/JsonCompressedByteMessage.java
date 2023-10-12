package io.vertigo.core.plugins.analytics.log;

import java.nio.charset.StandardCharsets;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import org.apache.logging.log4j.message.Message;

import io.vertigo.core.lang.WrappedException;

public class JsonCompressedByteMessage implements Message {
	private static final long serialVersionUID = 4914949261782429975L;
	private static final Inflater inflater = new Inflater();
	private static final Deflater deflater = new Deflater(1);
	private static final byte[] compressedObject = new byte[5 * 1024 * 1024];
	private transient String formattedMessage;
	private transient boolean keepFormattedMessageNull; //true on source, false on dest => don't send formattedMessage

	private final int length;
	private final byte[] compressedData;

	public JsonCompressedByteMessage(final String jsonEvent) {
		keepFormattedMessageNull = true;
		final byte[] uncompressedData = jsonEvent.getBytes(StandardCharsets.UTF_8);
		length = uncompressedData.length;
		int compressedSize = 0;
		synchronized (deflater) { //deflater n'est pas multi-thread
			deflater.reset();
			deflater.setInput(uncompressedData);
			deflater.finish();
			deflater.deflate(compressedObject);
			compressedSize = deflater.getTotalOut();
		}
		compressedData = new byte[compressedSize];
		System.arraycopy(compressedObject, 0, compressedData, 0, compressedSize);

		//compressedData = LZFEncoder.encode(uncompressedData);
	}

	@Override
	public String getFormattedMessage() {
		if (!keepFormattedMessageNull && formattedMessage == null) {
			final byte[] uncompressedData = new byte[length];
			try {
				synchronized (inflater) { //inflater n'est pas multi-thread
					inflater.reset();
					inflater.setInput(compressedData, 0, compressedData.length);
					inflater.inflate(uncompressedData);
				}
			} catch (final DataFormatException e) {
				throw WrappedException.wrap(e);
			}
			/*try {
				LZFDecoder.decode(compressedData, uncompressedData);
			} catch (final LZFException e) {
				throw WrappedException.wrap(e);
			}*/
			formattedMessage = new String(uncompressedData, StandardCharsets.UTF_8);
		}
		return formattedMessage;
	}

	@Override
	public String getFormat() {
		return null;
	}

	@Override
	public Object[] getParameters() {
		return new Object[0];
	}

	@Override
	public Throwable getThrowable() {
		return null;
	}
}
