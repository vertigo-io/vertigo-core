package io.vertigo.struts2.impl.servlet.filter;

import java.io.IOException;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * Implémentation de HttpServletRequestWrapper qui fonctionne avec le CompressionServletRequestStream.
 * @author Emeric Vernat
 */
class CompressionServletRequestWrapper extends HttpServletRequestWrapper {
	private ServletInputStream stream;

	/**
	 * Constructeur qui crée un adapteur de ServletRequest wrappant la request sp�cifi�e.
	 * @param request javax.servlet.HttpServletRequest
	 */
	CompressionServletRequestWrapper(final HttpServletRequest request) {
		super(request);
	}

	/**
	 * Crée et retourne un ServletInputStream pour lire le flux associé avec cette request.
	 * @return javax.servlet.ServletInputStream
	 * @throws java.io.IOException   Erreur d'entrée/sortie
	 */
	public ServletInputStream createInputStream() throws IOException {
		return new CompressionRequestStream((HttpServletRequest) getRequest());
	}

	/**
	 * Retourne le servlet input stream associé avec cette request.
	 * @return javax.servlet.ServletInputStream
	 * @throws java.io.IOException   Erreur d'entrée/sortie
	 */
	@Override
	public ServletInputStream getInputStream() throws IOException {
		if (stream == null) {
			stream = createInputStream();
		}

		return stream;
	}
}
