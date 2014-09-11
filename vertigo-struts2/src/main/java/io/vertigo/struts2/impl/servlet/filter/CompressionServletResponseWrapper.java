package io.vertigo.struts2.impl.servlet.filter;

import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

/**
 * Implémentation de HttpServletResponseWrapper qui fonctionne avec le CompressionServletResponseStream.
 * @author Amy Roh, Dmitri Valdin (Apache Software Foundation)
 */
class CompressionServletResponseWrapper extends AbstractHttpServletResponseWrapper {
	private final int compressionThreshold;

	/**
	 * Constructeur qui crée un adapteur de ServletResponse wrappant la response sp�cifi�e.
	 * @param response javax.servlet.HttpServletResponse
	 * @param compressionThreshold int
	 */
	CompressionServletResponseWrapper(final HttpServletResponse response, final int compressionThreshold) {
		super(response);
		this.compressionThreshold = compressionThreshold;
	}

	/**
	 * Crée et retourne un ServletOutputStream pour �crire le contenu dans la response associée.
	 * @return javax.servlet.ServletOutputStream
	 */
	@Override
	public ServletOutputStream createOutputStream() {
		return new CompressionResponseStream((HttpServletResponse) getResponse(), compressionThreshold);
	}

	/**
	 * Termine et ferme la response.
	 */
	public void finishResponse() {
		try {
			close();
		} catch (final IOException e) {
			Logger.getRootLogger().trace(e.getMessage(), e);
		}
	}

	/**
	 * Ne fait rien
	 * @param length int
	 */
	@Override
	public void setContentLength(final int length) {
		// ne fait rien
	}
}
