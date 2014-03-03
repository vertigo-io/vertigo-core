package io.vertigo.dynamo.impl.file.model;

import io.vertigo.dynamo.file.model.InputStreamBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

/**
 *
 * @author npiedeloup
 * @version $Id: StreamFile.java,v 1.1 2013/07/10 15:45:32 npiedeloup Exp $
 */
public final class StreamFile extends AbstractKFile {
	private static final long serialVersionUID = -4565434303879706815L;

	private final InputStreamBuilder inputStreamBuilder;

	public StreamFile(final String fileName, final String mimeType, final Date lastModified, final long length, final InputStreamBuilder inputStreamBuilder) {
		super(fileName, mimeType, lastModified, length);
		this.inputStreamBuilder = inputStreamBuilder;
	}

	/** {@inheritDoc} */
	public InputStream createInputStream() throws IOException {
		return inputStreamBuilder.createInputStream();
	}
}
