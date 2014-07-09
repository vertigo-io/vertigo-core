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
package io.vertigo.quarto.plugins.converter.openoffice.stream;

import io.vertigo.kernel.lang.Assertion;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;


import com.sun.star.io.IOException;
import com.sun.star.io.XOutputStream;

/**
 * Impl�mentation d'une OutpuStream sp�cifique pour les appels distants de OpenOffice.
 * @author tchassagnette
 * @version $Id: OOoFileOutputStream.java,v 1.3 2013/10/22 12:23:19 pchretien Exp $
 */
public final class OOoFileOutputStream implements XOutputStream {
	private final FileOutputStream fileOutputStream;

	/**
	 * Constructeur.
	 * @param file Fichier
	 * @throws FileNotFoundException Fichier introuvable
	 */
	public OOoFileOutputStream(final File file) throws FileNotFoundException {
		Assertion.checkNotNull(file);
		//---------------------------------------------------------------------
		fileOutputStream = new FileOutputStream(file);
	}

	//
	// Implement XOutputStream
	//

	/** {@inheritDoc} */
	public void writeBytes(final byte[] values) throws IOException {
		try {
			fileOutputStream.write(values);
		} catch (final java.io.IOException e) {
			throw createSunIOException(e);
		}
	}

	/** {@inheritDoc} */
	public void closeOutput() throws IOException {
		try {
			fileOutputStream.flush();
			fileOutputStream.close();
		} catch (final java.io.IOException e) {
			throw createSunIOException(e);
		}
	}

	/** {@inheritDoc} */
	public void flush() throws IOException {
		try {
			fileOutputStream.flush();
		} catch (final java.io.IOException e) {
			throw createSunIOException(e);
		}
	}

	private IOException createSunIOException(final java.io.IOException e) {
		return new IOException(e.getMessage(), this);
	}
}
