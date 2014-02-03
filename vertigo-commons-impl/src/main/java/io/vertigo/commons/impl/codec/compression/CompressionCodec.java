package io.vertigo.commons.impl.codec.compression;

import io.vertigo.commons.codec.Codec;
import io.vertigo.kernel.component.ComponentInfo;
import io.vertigo.kernel.component.Describable;
import io.vertigo.kernel.exception.VRuntimeException;
import io.vertigo.kernel.lang.Assertion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * Impl�mentation standard ThreadSafe g�rant les m�canismes permettant de compresser/d�compresser un format binaire (byte[]) en un binaire.
 * 
 * @author pchretien
 * @version $Id: CompressionCodec.java,v 1.7 2013/11/15 15:27:29 pchretien Exp $
 */
public final class CompressionCodec implements Codec<byte[], byte[]>, Describable {
	/**
	 * Seuil exprim� en octets en de�a duquel on ne compresse pas les donn�es.
	 */
	public static final int MIN_SIZE_FOR_COMPRESSION = 100;

	/**
	 * Seuil maximal autoris� pour la compression, ce seuil est exprim� en octets. 
	 */
	public static final int MAX_SIZE_FOR_COMPRESSION = 20 * 1024 * 1024; //au dela de 20Mo, on ne compresse pas en m�moire : le codec est inadapt�

	/**
	 * Niveau de compression de 0(pas de compression) � 9 (max compression). se
	 * r�f�rer au classes impl�mentant zip pour des pr�cisions sur le niveau de
	 * compression.
	 */
	public static final int COMPRESSION_LEVEL = 1;

	private static final byte[] COMPRESS_KEY = { 'C', 'O', 'M', 'P' };

	private final Deflater deflater = new Deflater(COMPRESSION_LEVEL);

	private final Inflater inflater = new Inflater();

	/**
	 * Compression d'un objet.
	 * @param unCompressedObject Objet non compress�
	 * @return Objet Compress�
	 */
	public byte[] encode(final byte[] unCompressedObject) {
		Assertion.checkNotNull(unCompressedObject);
		checkMaxSize(unCompressedObject.length);
		//---------------------------------------------------------------------
		if (unCompressedObject.length < MIN_SIZE_FOR_COMPRESSION) {
			return unCompressedObject;
		}

		//long time = System.currentTimeMillis();
		final int nonCompressedLength = unCompressedObject.length;
		final byte[] compressedObject = new byte[nonCompressedLength + 8];
		final int compressedSize;
		synchronized (deflater) { //deflater n'est pas multi-thread
			deflater.reset();
			deflater.setInput(unCompressedObject);
			deflater.finish();
			deflater.deflate(compressedObject);
			compressedSize = deflater.getTotalOut();
		}
		final byte[] newCompressedObject = new byte[compressedSize + COMPRESS_KEY.length + 4];
		System.arraycopy(COMPRESS_KEY, 0, newCompressedObject, 0, COMPRESS_KEY.length);
		newCompressedObject[COMPRESS_KEY.length] = (byte) (nonCompressedLength >>> 24 & 0xFF);
		newCompressedObject[COMPRESS_KEY.length + 1] = (byte) (nonCompressedLength >>> 16 & 0xFF);
		newCompressedObject[COMPRESS_KEY.length + 2] = (byte) (nonCompressedLength >>> 8 & 0xFF);
		newCompressedObject[COMPRESS_KEY.length + 3] = (byte) (nonCompressedLength & 0xFF);

		System.arraycopy(compressedObject, 0, newCompressedObject, COMPRESS_KEY.length + 4, compressedSize);
		//        if (Logger.getRootLogger().isTraceEnabled()) {
		//            Logger.getRootLogger().trace(
		//                    "Compress notification de " + nonCompressedLength + " octets � " + newCompressedObject.length + " octets ("
		//                    + (((newCompressedObject.length * 10000) / nonCompressedLength) / 100d) + "%) en " + (System.currentTimeMillis() - time) + " ms");
		//        }
		return newCompressedObject;
	}

	private static void checkMaxSize(final int length) {
		if (length >= MAX_SIZE_FOR_COMPRESSION) {
			throw new VRuntimeException("L''objet est trop gros pour �tre compress� en m�moire ({0} Mo)", null, length / (1024 * 1024));
		}
	}

	/**
	 * D�compression d'un objet.
	 * @param compressedObject Objet compress�
	 * @return Objet d�compress�
	 */
	public byte[] decode(final byte[] compressedObject) {
		Assertion.checkNotNull(compressedObject);
		//---------------------------------------------------------------------
		byte[] uncompressedObject = compressedObject;
		final byte[] compressHeader = new byte[COMPRESS_KEY.length];
		if (compressedObject.length > COMPRESS_KEY.length + 4) {
			System.arraycopy(compressedObject, 0, compressHeader, 0, COMPRESS_KEY.length);
			if (Arrays.equals(COMPRESS_KEY, compressHeader)) {
				final int ch1 = compressedObject[COMPRESS_KEY.length] & 0xff;
				// le & 0xff est necessaire pour avoir un int de 0 � 255, sinon
				// on a un int sign� de -127 � 128

				final int ch2 = compressedObject[COMPRESS_KEY.length + 1] & 0xff;
				final int ch3 = compressedObject[COMPRESS_KEY.length + 2] & 0xff;
				final int ch4 = compressedObject[COMPRESS_KEY.length + 3] & 0xff;
				final int unCompressedLength = ch4 + (ch3 << 8) + (ch2 << 16) + (ch1 << 24);
				checkMaxSize(unCompressedLength);

				try {
					synchronized (inflater) { //inflater n'est pas multi-thread
						inflater.reset();
						inflater.setInput(compressedObject, COMPRESS_KEY.length + 4, compressedObject.length - (COMPRESS_KEY.length + 4));
						uncompressedObject = new byte[unCompressedLength];
						inflater.inflate(uncompressedObject);
					}
				} catch (final DataFormatException e) {
					throw new VRuntimeException("d�compression", e);
				}
			}
		}
		return uncompressedObject;
	}

	/** {@inheritDoc} */
	public List<ComponentInfo> getInfos() {
		final List<ComponentInfo> componentInfos = new ArrayList<>();
		//---
		componentInfos.add(new ComponentInfo("compression.minSize(bytes)", MIN_SIZE_FOR_COMPRESSION));
		componentInfos.add(new ComponentInfo("compression.compressionLevel", COMPRESSION_LEVEL));
		//---
		return componentInfos;

	}
}
