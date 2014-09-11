package io.vertigo.struts2.core;

import io.vertigo.core.lang.Assertion;
import io.vertigo.dynamo.file.model.KFile;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Builder d'envoi de Fichier.
 * 
 * @author npiedeloup
 */
public final class KFileResponseBuilder {
	private final HttpServletRequest httpRequest;
	private final HttpServletResponse httpResponse;

	/**
	 * Constructeur.
	 * @param httpRequest ServletRequest
	 * @param httpResponse ServletResponse
	 */
	public KFileResponseBuilder(final HttpServletRequest httpRequest, final HttpServletResponse httpResponse) {
		Assertion.checkNotNull(httpRequest);
		Assertion.checkNotNull(httpResponse);
		//---------------------------------------------------------------------
		this.httpRequest = httpRequest;
		this.httpResponse = httpResponse;
	}

	/**
	 * Envoi les données au client.
	 * @param kFile Fichier a envoyer
	 * @return Retour de l'action struts
	 */
	public String send(final KFile kFile) {
		try {
			doSend(kFile);
		} catch (final IOException e) {
			handleException(e);
		}
		return null;
	}

	private void doSend(final KFile kFile) throws IOException {
		final Long length = kFile.getLength();
		Assertion.checkArgument(length.longValue() < Integer.MAX_VALUE, "Le fichier est trop gros pour être envoy�. Il fait " + length.longValue() / 1024 + " Ko, mais le maximum acceptable est de " + (Integer.MAX_VALUE / 1024) + " Ko.");
		httpResponse.setContentLength(length.intValue());
		httpResponse.addHeader("Content-Disposition", encodeFileNameToContentDisposition(httpRequest, kFile.getFileName()));
		httpResponse.setDateHeader("Last-Modified", kFile.getLastModified().getTime());
		httpResponse.setContentType(kFile.getMimeType());

		try (final InputStream input = kFile.createInputStream()) {
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
		throw new RuntimeException("Impossible d'envoyer le fichier.<!-- " + exception.getMessage() + "-->", exception);
	}

	/**
	 * Encode un nom de fichier avec des % pour Content-Disposition.
	 * (US-ASCII + Encode-Word : http://www.ietf.org/rfc/rfc2183.txt, http://www.ietf.org/rfc/rfc2231.txt
	 * sauf en MS IE et Chrome qui ne supportent pas cet encodage et qui n'en ont pas besoin)
	 * @param localHttpRequest HttpServletRequest
	 * @param fileName String
	 * @return String
	 */
	private static String encodeFileNameToContentDisposition(final HttpServletRequest localHttpRequest, final String fileName) {
		if (fileName == null) {
			return "";
		}
		// on remplace par des espaces les caractères interdits dans les noms de fichiers : \ / : * ? " < > |
		final String notAllowed = "\\/:*?\"<>|";
		final int notAllowedLength = notAllowed.length();
		String file = fileName;
		for (int i = 0; i < notAllowedLength; i++) {
			file = file.replace(notAllowed.charAt(i), ' ');
		}

		final String userAgent = localHttpRequest.getHeader("user-agent");
		if (userAgent != null && (userAgent.indexOf("MSIE") != -1 || userAgent.indexOf("Chrome") != -1)) {
			return "attachment;filename=" + file;
		}
		final int length = file.length();
		final StringBuilder sb = new StringBuilder(length + length / 4);
		sb.append("attachment;filename*=\"");
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
