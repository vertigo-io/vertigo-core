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
package io.vertigo.vega.plugins.rest.handler;

import io.vertigo.core.Home;
import io.vertigo.dynamo.file.FileManager;
import io.vertigo.dynamo.file.model.InputStreamBuilder;
import io.vertigo.dynamo.file.model.VFile;
import io.vertigo.lang.Assertion;
import io.vertigo.vega.rest.metamodel.EndPointParam;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.Part;

import org.apache.log4j.Logger;

import spark.Request;
import spark.Response;

/**
 * @author npiedeloup
 */
final class VFileUtil {

	private static final Logger LOG = Logger.getLogger(VFileUtil.class);
	private static final String NOT_ALLOWED_IN_FILENAME = "\\/:*?\"<>|;";

	private VFileUtil() {
		//nothing
	}

	/**
	 * @param request Request
	 * @return If this request is multipart
	 */
	static boolean isMultipartRequest(final Request request) {
		final String contentType = request.contentType();
		return "POST".equalsIgnoreCase(request.raw().getMethod()) && contentType != null && contentType.startsWith("multipart/form-data");
	}

	/**
	 * @param endPointParam EndPoint param
	 * @return If this is a VFile param
	 */
	static boolean isVFileParam(final EndPointParam endPointParam) {
		return VFile.class.isAssignableFrom(endPointParam.getType());
	}

	/**
	 * @param request Request
	 * @param endPointParam EndPoint param
	 * @return All VFile informations
	 */
	static VFile readVFileParam(final Request request, final EndPointParam endPointParam) {
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

	/**
	 * @param result WebService result
	 * @return if result is a VFile
	 */
	static boolean isVFileResult(final Object result) {
		return result instanceof VFile;
	}

	/**
	 * @param result WebService result
	 * @param request Request
	 * @param response Response
	 */
	static void sendVFile(final Object result, final Request request, final Response response) {
		sendVFile((VFile) result, true, response);
	}

	private static void sendVFile(final VFile result, final boolean attachment, final Response response) {
		try {
			send(result, attachment, response);
		} catch (final IOException e) {
			throw new RuntimeException("Error while sending file. <!-- " + e.getMessage() + "-->", e);
		}
		// response already send
	}

	private static VFile readQueryFile(final Request request, final EndPointParam endPointParam) {
		try {
			Assertion.checkArgument(request.contentType().contains("multipart/form-data"), "File {0} not found. Request contentType isn't \"multipart/form-data\"", endPointParam.getName());
			Assertion.checkArgument(!request.raw().getParts().isEmpty(), "File {0} not found. Request is multipart but there is no Parts. : Check you have defined MultipartConfig (example for Tomcat set allowCasualMultipartParsing=\"true\" on context tag in your context definition, for Jetty use JettyMultipartConfig)", endPointParam.getName());
			final Part file = request.raw().getPart(endPointParam.getName());
			if (file == null) {
				final StringBuilder sb = new StringBuilder();
				String sep = "";
				for (final Part part : request.raw().getParts()) {
					sb.append(sep).append(part.getName());
					sep = ", ";
				}
				throw new IllegalArgumentException("File " + endPointParam.getName() + " not found. Parts sent : " + sb.toString());
			}
			return createVFile(file);
		} catch (IOException | ServletException e) {
			throw new RuntimeException(e);
		}
	}

	private static void send(final VFile vFile, final boolean isAttachment, final Response response)
			throws IOException {
		final Long length = vFile.getLength();
		Assertion.checkArgument(length.longValue() < Integer.MAX_VALUE, "Too big file to be send. It's "
				+ length.longValue() / 1024 + " Ko long, but maximum was " + (Integer.MAX_VALUE / 1024)
				+ " Ko.");
		response.header("Content-Length", String.valueOf(length.intValue()));
		response.header("Content-Disposition",
				encodeFileNameToContentDisposition(vFile.getFileName(), isAttachment));
		response.raw().addDateHeader("Last-Modified", vFile.getLastModified().getTime());
		response.type(vFile.getMimeType());

		try (final InputStream input = vFile.createInputStream()) {
			try (final OutputStream output = response.raw().getOutputStream()) {
				copy(input, output);
			}
		}
	}

	/**
	 * Encode fileName according to RFC 5987.
	 * @param fileName String
	 * @param isAttachment boolean is Content an attachment
	 * @return String
	 */
	private static String encodeFileNameToContentDisposition(final String fileName,
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
			LOG.warn("UnsupportedEncodingException UTF-8", e);
			//utf-8 unsupported we only use the filename= header
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

	private static VFile createVFile(final Part file) {
		final String fileName = getSubmittedFileName(file);
		String mimeType = file.getContentType();
		if (mimeType == null) {
			mimeType = "application/octet-stream";
		}
		final FileManager fileManager = Home.getComponentSpace().resolve(FileManager.class);
		return fileManager.createFile(fileName, mimeType, new Date(), file.getSize(), new FileInputStreamBuilder(file));
	}

	private static String getSubmittedFileName(final Part filePart) {
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

	private static final class FileInputStreamBuilder implements InputStreamBuilder {
		private final Part file;

		FileInputStreamBuilder(final Part file) {
			this.file = file;
		}

		/** {@inheritDoc} */
		@Override
		public InputStream createInputStream() throws IOException {
			return file.getInputStream();
		}
	}

}
