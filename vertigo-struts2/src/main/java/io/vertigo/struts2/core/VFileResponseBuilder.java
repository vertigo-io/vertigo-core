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
package io.vertigo.struts2.core;

import io.vertigo.dynamo.file.model.VFile;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.WrappedException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Builder d'envoi de Fichier.
 *
 * @author npiedeloup
 */
public final class VFileResponseBuilder {
	private static final String NOT_ALLOWED_IN_FILENAME = "\\/:*?\"<>|;";

	private final HttpServletRequest httpRequest;
	private final HttpServletResponse httpResponse;

	/**
	 * Constructeur.
	 * @param httpRequest ServletRequest
	 * @param httpResponse ServletResponse
	 */
	public VFileResponseBuilder(final HttpServletRequest httpRequest, final HttpServletResponse httpResponse) {
		Assertion.checkNotNull(httpRequest);
		Assertion.checkNotNull(httpResponse);
		//-----
		this.httpRequest = httpRequest;
		this.httpResponse = httpResponse;
	}

	/**
	 * Envoi les données au client sous forme d'attachment.
	 * @param vFile Fichier a envoyer
	 */
	public void send(final VFile vFile) {
		send(vFile, true);
	}

	/**
	 * Envoi les données au client sous forme de stream.
	 * @param vFile Fichier a envoyer
	 */
	public void sendAsStream(final VFile vFile) {
		send(vFile, false);
	}

	private String send(final VFile vFile, final boolean attachment) {
		try {
			doSend(vFile, attachment);
		} catch (final IOException e) {
			handleException(e);
		}
		return null;
	}

	private void doSend(final VFile vFile, final boolean attachment) throws IOException {
		final Long length = vFile.getLength();
		Assertion.checkArgument(length.longValue() < Integer.MAX_VALUE, "Le fichier est trop gros pour être envoyé. Il fait " + length.longValue() / 1024 + " Ko, mais le maximum acceptable est de " + (Integer.MAX_VALUE / 1024) + " Ko.");
		httpResponse.setContentLength(length.intValue());
		httpResponse.addHeader("Content-Disposition", encodeFileNameToContentDisposition(httpRequest, vFile.getFileName(), attachment));
		httpResponse.setDateHeader("Last-Modified", vFile.getLastModified().getTime());
		httpResponse.setContentType(vFile.getMimeType());

		try (final InputStream input = vFile.createInputStream()) {
			try (final BufferedInputStream bInput = new BufferedInputStream(input)) {
				try (final OutputStream output = httpResponse.getOutputStream()) {
					copy(bInput, output);
				}
			}
		}
	}

	/**
	 * Gestion des exceptions d'export.
	 * @param exception L'exception à gérer
	 */
	private static void handleException(final Exception exception) {
		throw new WrappedException("Impossible d'envoyer le fichier.<!-- " + exception.getMessage() + "-->", exception);
	}

	/**
	 * Encode fileName according to RFC 5987.
	 * @param request HttpServletRequest
	 * @param fileName String
	 * @param isAttachment boolean is Content an attachment
	 * @return String
	 */
	private static String encodeFileNameToContentDisposition(final HttpServletRequest request, final String fileName,
			final boolean isAttachment) {
		if (fileName == null) {
			return "";
		}
		// on remplace par des espaces les caractères interdits dans les noms de fichiers : \ / : * ? " < > | ;
		final int notAllowedLength = NOT_ALLOWED_IN_FILENAME.length();
		String cleanFileName = fileName; //only accepted char
		for (int i = 0; i < notAllowedLength; i++) {
			cleanFileName = cleanFileName.replace(NOT_ALLOWED_IN_FILENAME.charAt(i), '_');
		}

		final int length = cleanFileName.length();
		final StringBuilder sb = new StringBuilder(length + length / 4);
		if (isAttachment) {
			sb.append("attachment;");
		}
		final String cleanestFileName = cleanFileName.replaceAll(" ", "%20"); //cleanest for default fileName
		sb.append("filename=" + cleanestFileName);
		byte[] utf8FileName;
		try {
			utf8FileName = cleanFileName.getBytes("utf8"); //Utf8 fileName
			sb.append(";filename*=UTF-8''");
			for (final byte c : utf8FileName) {
				if (isSimpleLetterOrDigit(c) || c == '.' || c == '-' || c == '_') {
					sb.append((char) c);
				} else {
					sb.append("%");
					sb.append(Integer.toHexString(c & 0xff)); // we want byte as a char on one byte
				}
			}
		} catch (final UnsupportedEncodingException e) {
			throw new AssertionError(e); // can't ever happen
		}
		return sb.toString();
	}

	/**
	 * Copie le contenu d'un flux d'entrée vers un flux de sortie.
	 * @param in flux d'entrée
	 * @param out flux de sortie
	 * @throws IOException Erreur d'entrée/sortie
	 */
	private static void copy(final InputStream in, final OutputStream out) throws IOException {
		final int bufferSize = 10 * 1024;
		final byte[] bytes = new byte[bufferSize];
		int read = in.read(bytes);
		while (read != -1) {
			out.write(bytes, 0, read);
			read = in.read(bytes);
		}
	}

	private static boolean isSimpleLetterOrDigit(final byte c) {
		return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '0' && c <= '9';
	}
}
