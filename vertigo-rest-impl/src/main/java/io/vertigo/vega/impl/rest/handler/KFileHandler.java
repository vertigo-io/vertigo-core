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
package io.vertigo.vega.impl.rest.handler;

import io.vertigo.dynamo.file.model.KFile;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.vega.rest.exception.SessionException;
import io.vertigo.vega.rest.exception.VSecurityException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import spark.Request;
import spark.Response;

/**
 * @author npiedeloup
 */
final class KFileHandler implements RouteHandler {
	private static final String NOT_ALLOWED_IN_FILENAME = "\\/:*?\"<>|";

	/** {@inheritDoc}  */
	public Object handle(final Request request, final Response response, final RouteContext routeContext, final HandlerChain chain) throws VSecurityException, SessionException {
		//TODO KFile import for download

		final Object result = chain.handle(request, response, routeContext);

		//KFile return for upload
		if (result instanceof KFile) {
			try {
				send((KFile) result, request, response);
			} catch (final IOException e) {
				throw new RuntimeException("Error while sending file. <!-- " + e.getMessage() + "-->", e);
			}
			return null; // response already send
		}
		return result;
	}

	private void send(final KFile kFile, final Request request, final Response response) throws IOException {
		final Long length = kFile.getLength();
		Assertion.checkArgument(length.longValue() < Integer.MAX_VALUE, "Too big file to be send. It's " + length.longValue() / 1024 + " Ko long, but maximum was " + (Integer.MAX_VALUE / 1024) + " Ko.");
		//response.contentLength(length.intValue());
		//response.header("Content-Length", String.valueOf(length.intValue()));
		response.header("Content-Disposition", encodeFileNameToContentDisposition(request, kFile.getFileName()));
		response.raw().addDateHeader("Last-Modified", kFile.getLastModified().getTime());
		response.type(kFile.getMimeType());

		try (final InputStream input = kFile.createInputStream()) {
			try (final OutputStream output = response.raw().getOutputStream()) {
				copy(input, output);
			}
		}
	}

	/**
	 * Encode un nom de fichier avec des % pour Content-Disposition.
	 * (US-ASCII + Encode-Word : http://www.ietf.org/rfc/rfc2183.txt, http://www.ietf.org/rfc/rfc2231.txt
	 * sauf en MS IE et Chrome qui ne supportent pas cet encodage et qui n'en ont pas besoin)
	 * @param localHttpRequest HttpServletRequest
	 * @param fileName String
	 * @return String
	 */
	private static String encodeFileNameToContentDisposition(final Request request, final String fileName) {
		if (fileName == null) {
			return "";
		}
		// on remplace par des espaces les caractères interdits dans les noms de fichiers : \ / : * ? " < > |

		final int notAllowedLength = NOT_ALLOWED_IN_FILENAME.length();
		String file = fileName;
		for (int i = 0; i < notAllowedLength; i++) {
			file = file.replace(NOT_ALLOWED_IN_FILENAME.charAt(i), ' ');
		}

		final String userAgent = request.headers("user-agent");
		if (userAgent != null && (userAgent.indexOf("MSIE") != -1 || userAgent.indexOf("Chrome") != -1)) {
			return "attachment;filename=" + file;
		}
		final int length = file.length();
		final StringBuilder sb = new StringBuilder(length + length / 4)//
				.append("attachment;filename*=\"");
		char c;
		for (int i = 0; i < length; i++) {
			c = file.charAt(i);
			if (isSimpleLetterOrDigit(c)) {
				sb.append(c);
			} else {
				sb.append('%');
				if (c < 16) {
					sb.append('0');
				}
				sb.append(Integer.toHexString(c));
			}
		}
		sb.append('\"');
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

	private static boolean isSimpleLetterOrDigit(final char c) {
		return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '0' && c <= '9';
	}

}
