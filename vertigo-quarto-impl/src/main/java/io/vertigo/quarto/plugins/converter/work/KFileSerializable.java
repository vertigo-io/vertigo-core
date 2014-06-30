package io.vertigo.quarto.plugins.converter.work;

import io.vertigo.dynamo.file.model.KFile;
import io.vertigo.dynamo.file.util.FileUtil;
import io.vertigo.dynamo.file.util.TempFile;
import io.vertigo.dynamo.impl.file.model.FSFile;
import io.vertigo.kernel.lang.Assertion;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Date;

/**
 * Class d'encapsulation pour rendre un KFile Serializable.
 * @author npiedeloup
 * @version $Id: KFileSerializable.java,v 1.7 2014/02/27 10:24:53 pchretien Exp $
 */
public final class KFileSerializable implements KFile {

	private static final long serialVersionUID = -8193943440741831844L;
	private transient KFile file; //file est transient

	/**
	 * Constructeur.
	 * @param kfile fichier d'origine
	 */
	protected KFileSerializable(final KFile kfile) {
		Assertion.checkNotNull(kfile);
		Assertion.checkArgument(!(kfile instanceof KFileSerializable), "Le fichier {0} est d�j� encapsuler dans KFileSerializable", kfile.getFileName());
		//---------------------------------------------------------------------
		file = kfile;
	}

	/** {@inheritDoc} */
	public final String getFileName() {
		return file.getFileName();
	}

	/** {@inheritDoc} */
	public final String getMimeType() {
		return file.getMimeType();
	}

	/** {@inheritDoc} */
	public final Long getLength() {
		return file.getLength();
	}

	/** {@inheritDoc} */
	public final Date getLastModified() {
		return file.getLastModified();
	}

	/** {@inheritDoc} */
	public InputStream createInputStream() throws IOException {
		return file.createInputStream();
	}

	private void writeObject(final ObjectOutputStream stream) throws IOException {
		stream.defaultWriteObject();
		stream.writeUTF(file.getFileName());
		stream.writeUTF(file.getMimeType());
		try (final InputStream in = file.createInputStream()) {
			copyStream(in, stream);
		}
	}

	private void readObject(final ObjectInputStream stream) throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		file = readKFile(stream);
	}

	private KFile readKFile(final ObjectInputStream stream) throws IOException {
		final String fileName = stream.readUTF();
		final String mimeType = stream.readUTF();
		final File tempFile = new TempFile("kConverter", "." + FileUtil.getFileExtension(fileName));
		try (final OutputStream out = new FileOutputStream(tempFile)) {
			copyStream(stream, out);
		}
		//new FSFile cr�e une d�pendance � FileManager du module Commons : le FSFile est tr�s simple mais on �vite un copier/coller
		return new FSFile(fileName, mimeType, tempFile);
	}

	private void copyStream(final InputStream in, final OutputStream out) throws IOException {
		final int bufferSize = 10 * 1024;
		final byte[] bytes = new byte[bufferSize];
		int read = in.read(bytes);
		while (read != -1) {
			out.write(bytes, 0, read);
			out.flush();
			read = in.read(bytes);
		}
	}
}
