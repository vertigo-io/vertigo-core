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

import io.vertigo.core.Home;
import io.vertigo.dynamo.file.FileManager;
import io.vertigo.dynamo.file.model.InputStreamBuilder;
import io.vertigo.dynamo.file.model.KFile;
import io.vertigo.lang.Assertion;
import io.vertigo.vega.rest.metamodel.EndPointParam;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.Part;

import spark.Request;
import spark.Response;

/**
 * @author npiedeloup
 */
final class KFileUtil {
	private static final String NOT_ALLOWED_IN_FILENAME = "\\/:*?\"<>|";

	private KFileUtil() {
		//nothing
	}

	static boolean isMultipartRequest(final Request request) {
		final String contentType = request.contentType();
		return "POST".equalsIgnoreCase(request.raw().getMethod()) && contentType != null && contentType.startsWith("multipart/form-data");
	}

	static boolean isKFileParam(final EndPointParam endPointParam) {
		return KFile.class.isAssignableFrom(endPointParam.getType());
	}

	static KFile readKFileParam(final Request request, final EndPointParam endPointParam) {
		switch (endPointParam.getParamType()) {
			case Query:
				return readQueryFile(request, endPointParam);
			case Body:
			case Header:
			case InnerBody:
			case Path:
			case Implicit:
			default:
				throw new IllegalArgumentException("Files are only read from a Multipart Form parameter (use @QueryParam) : " + endPointParam.getFullName());
		}
	}

	static boolean isKFileResult(final Object result) {
		return result instanceof KFile;
	}

	static void sendKFile(final Object result, final Request request, final Response response) {
		try {
			send((KFile) result, request, response);
		} catch (final IOException e) {
			throw new RuntimeException("Error while sending file. <!-- " + e.getMessage() + "-->", e);
		}
		// response already send
	}

	private static KFile readQueryFile(final Request request, final EndPointParam endPointParam) {
		try {
			final Part file = request.raw().getPart(endPointParam.getName());
			return createKFile(file);
		} catch (IOException | ServletException e) {
			throw new RuntimeException(e);
		}
	}

	private static void send(final KFile kFile, final Request request, final Response response) throws IOException {
		final Long length = kFile.getLength();
		Assertion.checkArgument(length.longValue() < Integer.MAX_VALUE, "Too big file to be send. It's " + length.longValue() / 1024 + " Ko long, but maximum was " + (Integer.MAX_VALUE / 1024) + " Ko.");
		//response.contentLength(length.intValue());
		response.header("Content-Length", String.valueOf(length.intValue()));
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
	 * @param request HttpServletRequest
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
		final StringBuilder sb = new StringBuilder(length + length / 4)
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

	private static KFile createKFile(final Part file) {
		final String fileName = getSubmittedFileName(file);
		String mimeType = file.getContentType();
		if (mimeType == null) {
			mimeType = "application/octet-stream";
		}
		final FileManager fileManager = Home.getComponentSpace().resolve(FileManager.class);
		return fileManager.createFile(fileName, mimeType, new Date(), file.getSize(), new InputStreamBuilder() {
			public InputStream createInputStream() throws IOException {
				return file.getInputStream();
			}
		});
	}

	private static String getSubmittedFileName(final Part filePart) {
		//final String fileName = Home.getComponentSpace().resolve(CodecManager.class).getHtmlCodec().decode(file.getName());
		//TODO : check if encoded fileName ?
		final String header = filePart.getHeader("content-disposition");
		if (header == null) {
			return null;
		}
		for (final String headerPart : header.split(";")) {
			if (headerPart.trim().startsWith("filename")) {
				return headerPart.substring(headerPart.indexOf('=') + 1).trim().replace("\"", "");
			}
		}
		return null;
	}

}
