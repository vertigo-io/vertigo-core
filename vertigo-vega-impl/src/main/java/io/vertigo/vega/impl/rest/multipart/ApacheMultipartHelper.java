package io.vertigo.vega.impl.rest.multipart;

import io.vertigo.commons.codec.CodecManager;
import io.vertigo.core.Home;
import io.vertigo.core.exception.VUserException;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.MessageText;
import io.vertigo.dynamo.file.FileManager;
import io.vertigo.dynamo.file.model.InputStreamBuilder;
import io.vertigo.dynamo.file.model.KFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.MultipartConfigElement;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemHeaders;
import org.apache.commons.fileupload.FileItemHeadersSupport;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadBase.FileSizeLimitExceededException;
import org.apache.commons.fileupload.FileUploadBase.FileUploadIOException;
import org.apache.commons.fileupload.FileUploadBase.IOFileUploadException;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;

import spark.Request;

/**
 * Plugin d'upload de fichier, par la librairie org.apache.commons.upload.
 * 
 * @author npiedeloup
 * @version $Id: ApacheFileUploadPlugin.java,v 1.11 2013/06/25 10:57:08 pchretien Exp $
 */
public final class ApacheMultipartHelper {
	/** MultipartConfig in request's attributes. */
	public static final String MULTIPART_CONFIG_ELEMENT = "io.vertigo.vega.rest.multipartConfig";

	/**
	 * @param request Spark Request
	 * @return If Request is multipart (may have files).
	 */
	public static boolean isMultipart(final Request request) {
		final String contentType = request.contentType();
		return "POST".equalsIgnoreCase(request.raw().getMethod()) && contentType != null && contentType.startsWith("multipart/form-data");
	}

	/**
	 * @param request Spark Request
	 * @return Wrapped Spark Request which support Multipart
	 */
	public static Request createRequestWrapper(final Request request) {
		final Map<String, List<String>> parameters = new HashMap<>();
		final Map<String, KFile> uploadedFiles = new HashMap<>();
		final Map<String, RuntimeException> tooBigFiles = new HashMap<>();
		try {
			wrapParameters(request.raw(), parameters, uploadedFiles, tooBigFiles);
		} catch (FileUploadException | IOException e) {
			throw new RuntimeException("FileUpload error", e);
		}
		return new RequestWrapper(request, asMapArray(parameters), uploadedFiles, tooBigFiles);

	}

	private static Map<String, String[]> asMapArray(final Map<String, List<String>> map) {
		final Map<String, String[]> result = new HashMap<>();
		for (final Map.Entry<String, List<String>> entry : map.entrySet()) {
			result.put(entry.getKey(), entry.getValue().toArray(new String[entry.getValue().size()]));
		}
		return result;
	}

	private static void wrapParameters(final HttpServletRequest httpServletRequest, final Map<String, List<String>> parameters, final Map<String, KFile> uploadedFiles, final Map<String, RuntimeException> tooBigFiles) throws FileUploadException, IOException {
		final MultipartConfigElement multipartConfigElement = (MultipartConfigElement) httpServletRequest.getAttribute(MULTIPART_CONFIG_ELEMENT);
		Assertion.checkNotNull(multipartConfigElement, "No MultipartConfigElement found. Set it as request.attibute({0}", MULTIPART_CONFIG_ELEMENT);
		//---------------------------------------------------------------------
		// Create a factory for disk-based file items
		final DiskFileItemFactory factory = new DiskFileItemFactory(multipartConfigElement.getFileSizeThreshold(), new File(multipartConfigElement.getLocation()));
		// Create a new file upload handler
		final ServletFileUpload upload = new ServletFileUpload(factory);
		// Set overall request size constraint
		upload.setSizeMax(multipartConfigElement.getMaxRequestSize());
		// Paramétrage de la limite par fichier (celle sur la taille de la request n'est pas interceptable)
		upload.setFileSizeMax(multipartConfigElement.getMaxFileSize());

		// Parse the request

		final FileItemIterator iterator = upload.getItemIterator(httpServletRequest);
		while (iterator.hasNext()) {
			final FileItemStream item = iterator.next();
			final FileItem fileItem = factory.createItem(item.getFieldName(), item.getContentType(), item.isFormField(), getName(item));
			boolean sizeExcedeed = false;
			try {
				fillFileItem(item, fileItem);
			} catch (final FileSizeLimitExceededException e) {
				sizeExcedeed = true;
			}
			if (fileItem.isFormField()) {
				processFormField(parameters, fileItem);
			} else if (fileItem.getName().length() > 0) { // Si le nom est vide, le champ n'était pas renseigné.
				// Si le fichier est vide alors soit le nom ne correspond pas à un fichier, soit il était vide coté coté
				// client
				if (fileItem.getSize() > 0) {
					processUploadedFile(fileItem, uploadedFiles, tooBigFiles, sizeExcedeed, upload.getFileSizeMax());
				} else {
					final MessageText msg = new MessageText("Empty file", null, fileItem.getName());
					tooBigFiles.put(fileItem.getFieldName(), new VUserException(msg));
				}
			}
		}
	}

	private static void fillFileItem(final FileItemStream item, final FileItem fileItem) throws FileUploadException {
		try {
			Streams.copy(item.openStream(), fileItem.getOutputStream(), true);
		} catch (final FileUploadIOException e) {
			throw (FileUploadException) e.getCause();
		} catch (final IOException e) {
			throw new IOFileUploadException("Processing of multipart/form-data request failed. " + e.getMessage(), e);
		}

		final FileItemHeaders fih = item.getHeaders();
		((FileItemHeadersSupport) fileItem).setHeaders(fih);
	}

	private static void processFormField(final Map<String, List<String>> parameters, final FileItem fileItem) {
		addValue(parameters, fileItem.getFieldName(), fileItem.getString()/* convertToString(fileItem) */);
	}

	private static void processUploadedFile(final FileItem fileItem, final Map<String, KFile> uploadedFiles, final Map<String, RuntimeException> tooBigFiles, final boolean sizeExcedeed, final long maxFileSize) throws IOException {
		final String fileName;
		try {
			fileName = Home.getComponentSpace().resolve(CodecManager.class).getHtmlCodec().decode(fileItem.getName());
		} catch (final RuntimeException e) {
			throw new IOException("Erreur encodage", e);
		}

		if (!sizeExcedeed) {
			final KFile file = createKFIle(fileName, fileItem);
			uploadedFiles.put(fileItem.getFieldName(), file);
		} else {
			final MessageText msg = new MessageText("Too big file, {0} must be smaller than {1} bytes", null, fileName, maxFileSize / 1024d / 1024d);
			tooBigFiles.put(fileItem.getFieldName(), new VUserException(msg));
		}
	}

	private static void addValue(final Map<String, List<String>> parameters, final String name, final String value) {
		List<String> values = parameters.get(name);
		if (values == null) {
			values = new ArrayList<>();
		}
		values.add(value);
		parameters.put(name, values);
	}

	private static String getName(final FileItemStream fileItemStream) {
		final String fileName = fileItemStream.getName();
		// correction IE envoi le chemin complet du fichier, et pas seulement le nom
		if (fileName != null && fileName.lastIndexOf('\\') != -1) {
			return fileName.substring(fileName.lastIndexOf('\\') + 1); // +1 pour retirer le \
		}
		return fileName;
	}

	/**
	 * Création d'un KFile à partir d'un FileItem.
	 * 
	 * @param fileName Nom du fichier
	 * @param fileItem Fichier
	 * @return FileInfo crée
	 */
	private static KFile createKFIle(final String fileName, final FileItem fileItem) {
		String contentType = fileItem.getContentType();
		if (contentType == null) {
			contentType = "application/octet-stream";
		}

		final FileManager fileManager = Home.getComponentSpace().resolve(FileManager.class);
		return fileManager.createFile(fileName, contentType, new Date(), fileItem.getSize(), new InputStreamBuilder() {

			public InputStream createInputStream() throws IOException {
				return fileItem.getInputStream();
			}
		});
	}
}
