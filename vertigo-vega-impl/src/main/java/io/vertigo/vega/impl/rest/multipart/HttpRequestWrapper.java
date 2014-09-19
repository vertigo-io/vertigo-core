package io.vertigo.vega.impl.rest.multipart;

import io.vertigo.core.exception.VUserException;
import io.vertigo.core.lang.Option;
import io.vertigo.dynamo.file.model.KFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.Part;

/**
 * Wrapping de la requête Http pour la gestion des requête multipart.
 * 
 * @author npiedeloup
 * @version $Id: RequestWrapper.java,v 1.6 2013/06/25 10:57:08 pchretien Exp $
 */
public final class HttpRequestWrapper extends HttpServletRequestWrapper {

	private final Map<String, String[]> parameters;
	private final Map<String, KFile> uploadedFiles;
	private final Map<String, RuntimeException> tooBigFiles;

	/**
	 * Creates a new KRequestWrapper object.
	 * 
	 * @param request Requête à gérer.
	 * @throws Exception Exception de lecture du flux
	 */
	HttpRequestWrapper(final HttpServletRequest request, final Map<String, String[]> parameters, final Map<String, KFile> uploadedFiles, final Map<String, RuntimeException> tooBigFiles) {
		super(request);
		this.parameters = parameters;
		this.uploadedFiles = uploadedFiles;
		this.tooBigFiles = tooBigFiles;
	}

	/**
	 * Donne le fichier téléchargé.
	 * 
	 * @param fileName Nom du paramètre portant le fichier dans la request
	 * @return Fichier téléchargé.
	 */
	public Option<KFile> getUploadedFile(final String fileName) {
		if (tooBigFiles.containsKey(fileName)) {
			throw (VUserException) tooBigFiles.get(fileName);
		}
		return Option.option(uploadedFiles.get(fileName));
	}

	/** {@inheritDoc} */
	@Override
	public String getParameter(final String name) {
		final String[] values = parameters.get(name);
		if (values == null) {
			return null;
		}
		// 1ère occurence.
		return values[0];
	}

	/** {@inheritDoc} */
	@Override
	public Map<String, String[]> getParameterMap() {
		return parameters;
	}

	/** {@inheritDoc} */
	@Override
	public Enumeration<String> getParameterNames() {
		return Collections.enumeration(parameters.keySet());
	}

	/** {@inheritDoc} */
	@Override
	public String[] getParameterValues(final String name) {
		return parameters.get(name);
	}

	/** {@inheritDoc} */
	@Override
	public Collection<Part> getParts() throws IOException, ServletException {
		throw new UnsupportedOperationException("Not implemented : use getPart(name)");
	}

	/** {@inheritDoc} */
	@Override
	public Part getPart(final String name) throws IOException, ServletException {
		final Option<KFile> file = getUploadedFile(name);
		return new Part() {

			public InputStream getInputStream() throws IOException {
				return file.get().createInputStream();
			}

			public String getContentType() {
				return file.get().getMimeType();
			}

			public String getName() {
				return file.get().getFileName();
			}

			public long getSize() {
				return file.get().getLength();
			}

			public void write(final String paramString) throws IOException {
				throw new UnsupportedOperationException("Not implemented");
			}

			public void delete() throws IOException {
				throw new UnsupportedOperationException("Not implemented");
			}

			public String getHeader(final String paramString) {
				throw new UnsupportedOperationException("Not implemented");
			}

			public Collection<String> getHeaders(final String paramString) {
				throw new UnsupportedOperationException("Not implemented");
			}

			public Collection<String> getHeaderNames() {
				throw new UnsupportedOperationException("Not implemented");
			}
		};
	}
}
