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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;

import com.sun.star.io.IOException;
import com.sun.star.io.XInputStream;
import com.sun.star.io.XSeekable;

/**
 * InputStream de lecture de fichier pour l'envoyer vers le serveur OpenOffice.
 * 
 * @author tchassagnette
 * @version $Id: OOoFileInputStream.java,v 1.2 2014/01/13 13:42:44 npiedeloup Exp $
 */
public final class OOoFileInputStream implements XInputStream, XSeekable {
	private final RandomAccessFile randomAccessFile;

	/**
	 * Constructeur.
	 * @param file Fichier
	 * @throws FileNotFoundException Fichier introuvable
	 */
	public OOoFileInputStream(final File file) throws FileNotFoundException {
		randomAccessFile = new RandomAccessFile(file, "r");
	}

	/**
	 * Constructeur prenant en param�tre un chemin vers un fichier.
	 * @param filePath Chemin vers le fichier.
	 * @throws FileNotFoundException Fichier introuvable
	 */
	public OOoFileInputStream(final String filePath) throws FileNotFoundException {
		randomAccessFile = new RandomAccessFile(filePath, "r");
	}

	/** {@inheritDoc} */
	public long getLength() throws IOException {
		try {
			return randomAccessFile.length();
		} catch (final java.io.IOException e) {
			throw createSunIOException(e);
		}
	}

	/** {@inheritDoc} */
	public long getPosition() throws IOException {
		try {
			return randomAccessFile.getFilePointer();
		} catch (final java.io.IOException e) {
			throw createSunIOException(e);
		}
	}

	/** {@inheritDoc} */
	public void seek(final long arg0) throws IOException {
		try {
			randomAccessFile.seek(arg0);
		} catch (final java.io.IOException e) {
			throw createSunIOException(e);
		}
	}

	/** {@inheritDoc} */
	public int available() throws IOException {
		//retourne le nombre d'octet readable ou skipable sans blocage
		try {
			return (int) (randomAccessFile.length() - randomAccessFile.getFilePointer());
		} catch (final java.io.IOException e) {
			throw createSunIOException(e);
		}
	}

	/** {@inheritDoc} */
	public void closeInput() throws IOException {
		try {
			randomAccessFile.close();
		} catch (final java.io.IOException e) {
			throw createSunIOException(e);
		}
	}

	/** {@inheritDoc} */
	public int readBytes(final byte[][] buffer, final int bufferSize) throws IOException {
		int numberOfReadBytes;
		try {
			byte[] bytes = new byte[bufferSize];
			numberOfReadBytes = randomAccessFile.read(bytes);
			if (numberOfReadBytes > 0) {
				if (numberOfReadBytes < bufferSize) {
					final byte[] smallerBuffer = new byte[numberOfReadBytes];
					System.arraycopy(bytes, 0, smallerBuffer, 0, numberOfReadBytes);
					bytes = smallerBuffer;
				}
			} else {
				bytes = new byte[0];
				numberOfReadBytes = 0;
			}

			buffer[0] = bytes;
			return numberOfReadBytes;
		} catch (final java.io.IOException e) {
			throw createSunIOException(e);
		}
	}

	/** {@inheritDoc} */
	public int readSomeBytes(final byte[][] buffer, final int bufferSize) throws IOException {
		return readBytes(buffer, bufferSize);
	}

	/** {@inheritDoc} */
	public void skipBytes(final int arg0) throws IOException {
		try {
			randomAccessFile.skipBytes(arg0);
		} catch (final java.io.IOException e) {
			throw createSunIOException(e);
		}
	}

	private IOException createSunIOException(final java.io.IOException e) {
		return new IOException(e.getMessage(), this);
	}
}
