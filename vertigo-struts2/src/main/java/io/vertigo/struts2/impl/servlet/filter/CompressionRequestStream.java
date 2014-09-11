package io.vertigo.struts2.impl.servlet.filter;

import java.io.IOException;
import java.util.zip.GZIPInputStream;

import javax.servlet.http.HttpServletRequest;

/**
 * Implémentation de ServletInputStream qui fonctionne avec le CompressionServletRequestWrapper.
 * Il est suppos� qu'il est utilisé avec un flux compressé (Content-Encoding = gzip).
 * @author Emeric Vernat
 */
class CompressionRequestStream extends FilterServletInputStream {
	private final HttpServletRequest request;

	/**
	 * Construit un servlet input stream associé avec la request sp�cifi�e.
	 * @param request javax.servlet.http.HttpServletRequest
	 * @throws java.io.IOException   Erreur d'entrée/sortie
	 */
	CompressionRequestStream(final HttpServletRequest request) throws IOException {
		super(new GZIPInputStream(request.getInputStream()));
		this.request = request;
	}

	/**
	 * Ferme cet input stream.
	 * @throws java.io.IOException   Erreur d'entrée/sortie
	 */
	@Override
	public void close() throws IOException {
		super.close();
		request.getInputStream().close();
	}

	/**
	 * Teste si cet input stream supporte les m�thodes <code>mark</code> et <code>reset</code>.
	 * @return boolean
	 * @see java.io.InputStream#mark(int)
	 * @see java.io.InputStream#reset()
	 */
	@Override
	public boolean markSupported() {
		return false; // Assume that mark is NO good for a gzipInputStream
	}
}
