package io.vertigo.commons.impl.codec;

import io.vertigo.commons.codec.Codec;
import io.vertigo.commons.codec.CodecManager;
import io.vertigo.commons.codec.Encoder;
import io.vertigo.commons.impl.codec.base64.Base64Codec;
import io.vertigo.commons.impl.codec.compressedSerialization.CompressedSerializationCodec;
import io.vertigo.commons.impl.codec.compression.CompressionCodec;
import io.vertigo.commons.impl.codec.crypto.CryptoCodec;
import io.vertigo.commons.impl.codec.csv.CsvEncoder;
import io.vertigo.commons.impl.codec.hash.HashEncoder;
import io.vertigo.commons.impl.codec.hex.HexEncoder;
import io.vertigo.commons.impl.codec.html.HtmlCodec;
import io.vertigo.commons.impl.codec.serialization.SerializationCodec;
import io.vertigo.kernel.component.ComponentInfo;
import io.vertigo.kernel.component.Describable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Impl�mentation standard de CodecManager.
 *
 * @author pchretien
 * @version $Id: CodecManagerImpl.java,v 1.7 2013/11/15 15:27:29 pchretien Exp $
 */
public final class CodecManagerImpl implements CodecManager, Describable {
	/** Codage/d�codage HTML de String > String. */
	private final Codec<String, String> htmlCodec;

	/** Crypto de byte[] > byte[]. */
	private final Codec<byte[], byte[]> tripleDESCodec;

	/** Crypto de byte[] > byte[]. */
	private final Codec<byte[], byte[]> aes128Codec;

	/** MD5 de byte[] > String. */
	private final Encoder<byte[], byte[]> md5Encoder;

	/** SHA-1 de byte[] > String. */
	private final Encoder<byte[], byte[]> sha1Encoder;

	/** SHA-256 de byte[] > String. */
	private final Encoder<byte[], byte[]> sha256Encoder;

	/** S�rialisation de byte[] > String. */
	private final Codec<byte[], String> base64Codec;

	/** S�rialisation de byte[] > String. */
	private final Encoder<byte[], String> hexEncoder;

	/** Compression de byte[] > byte[]. */
	private final Codec<byte[], byte[]> compressionCodec;

	/** S�rialisation de Serializable > byte[]. */
	private final Codec<Serializable, byte[]> serializationCodec;

	/** S�rialisation de Serializable > byte[]. */
	private final Codec<Serializable, byte[]> compressedSerializationCodec;

	/** CSV de String > String. */
	private final Encoder<String, String> csvEncoder;

	private final List<ComponentInfo> componentInfos = new ArrayList<>();

	/**
	 * Constructeur.
	 */
	public CodecManagerImpl() {
		super();
		htmlCodec = new HtmlCodec();
		//---
		final CryptoCodec tmpTripleDESCodec = new CryptoCodec(CryptoCodec.Crypto.TripleDES);
		tripleDESCodec = new NullCodec<>(tmpTripleDESCodec);
		componentInfos.addAll(tmpTripleDESCodec.getInfos());
		//---
		final CryptoCodec tmpAes128Codec = new CryptoCodec(CryptoCodec.Crypto.AES);
		aes128Codec = new NullCodec<>(tmpAes128Codec);
		componentInfos.addAll(tmpAes128Codec.getInfos());
		//---
		md5Encoder = new HashEncoder(HashEncoder.Hash.MD5);
		sha1Encoder = new HashEncoder(HashEncoder.Hash.SHA1);
		sha256Encoder = new HashEncoder(HashEncoder.Hash.SHA256);

		base64Codec = new NullCodec<>(new Base64Codec());
		hexEncoder = new HexEncoder();
		//---
		final CompressionCodec tmpCompressionCodec = new CompressionCodec();
		compressionCodec = new NullCodec<>(tmpCompressionCodec);
		componentInfos.addAll(tmpCompressionCodec.getInfos());

		//---
		serializationCodec = new NullCodec<>(new SerializationCodec());
		compressedSerializationCodec = new NullCodec<>(new CompressedSerializationCodec(serializationCodec, compressionCodec));
		csvEncoder = new CsvEncoder();
	}

	/** {@inheritDoc} */
	public Codec<String, String> getHtmlCodec() {
		return htmlCodec;
	}

	/** {@inheritDoc} */
	public Encoder<byte[], byte[]> getMD5Encoder() {
		return md5Encoder;
	}

	/** {@inheritDoc} */
	public Encoder<byte[], byte[]> getSha256Encoder() {
		return sha256Encoder;
	}

	/** {@inheritDoc} */
	public Encoder<byte[], byte[]> getSha1Encoder() {
		return sha1Encoder;
	}

	/** {@inheritDoc} */
	public Codec<byte[], String> getBase64Codec() {
		return base64Codec;
	}

	/** {@inheritDoc} */
	public Encoder<byte[], String> getHexEncoder() {
		return hexEncoder;
	}

	/** {@inheritDoc} */
	public Codec<byte[], byte[]> getTripleDESCodec() {
		return tripleDESCodec;
	}

	/** {@inheritDoc} */
	public Codec<byte[], byte[]> getAES128Codec() {
		return aes128Codec;
	}

	/** {@inheritDoc} */
	public Codec<byte[], byte[]> getCompressionCodec() {
		return compressionCodec;
	}

	/** {@inheritDoc} */
	public Codec<Serializable, byte[]> getCompressedSerializationCodec() {
		return compressedSerializationCodec;
	}

	/** {@inheritDoc} */
	public Codec<Serializable, byte[]> getSerializationCodec() {
		return serializationCodec;
	}

	/** {@inheritDoc} */
	public Encoder<String, String> getCsvEncoder() {
		return csvEncoder;
	}

	/** {@inheritDoc} */
	public List<ComponentInfo> getInfos() {
		return componentInfos;
	}
}
