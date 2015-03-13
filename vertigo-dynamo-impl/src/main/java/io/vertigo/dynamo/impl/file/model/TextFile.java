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
package io.vertigo.dynamo.impl.file.model;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;

/**
 * Fichier construit à partir d'une chaine.
 * Ce TextFile fournit un fichier de type text.
 *
 * @author npiedeloup
 */
public final class TextFile extends AbstractVFile {
	private static final String TEXT_CHARSET = "UTF8";
	private static final long serialVersionUID = 1L;
	private final byte[] contentAsBytes;

	/**
	 * Constructeur.
	 * Associe un fichier à des méta-données
	 * @param fileName Nom d'origine du fichier
	 * @param content Contenu en lui même	 */
	public TextFile(final String fileName, final String content) {
		this(fileName, "text/plain", content);
	}

	/**
	 * Constructeur.
	 * Associe un fichier à des méta-données
	 * @param fileName Nom d'origine du fichier
	 * @param mimeType Type mime du fichier
	 * @param content Contenu en lui même (non null)
	 */
	public TextFile(final String fileName, final String mimeType, final String content) {
		//le content ne doit pas être null
		super(fileName, mimeType, new Date(), Long.valueOf(convertContentAsByte(content).length));
		//-----
		contentAsBytes = convertContentAsByte(content);
	}

	/** {@inheritDoc} */
	@Override
	public InputStream createInputStream() {
		return new java.io.ByteArrayInputStream(contentAsBytes);
	}

	private static byte[] convertContentAsByte(final String content) {
		try {
			return content.getBytes(TEXT_CHARSET);
		} catch (final UnsupportedEncodingException e) {
			//Just rethrow this "utf8 charset not found" error
			throw new RuntimeException(e);
		}
	}
}
