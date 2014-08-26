/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidiere - BP 159 - 92357 Le Plessis Robinson Cedex - France
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
		Assertion.checkArgument(!(kfile instanceof KFileSerializable), "Le fichier {0} est déjà encapsuler dans KFileSerializable", kfile.getFileName());
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
		//new FSFile crée une dépendance à FileManager du module Commons : le FSFile est très simple mais on évite un copier/coller
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
