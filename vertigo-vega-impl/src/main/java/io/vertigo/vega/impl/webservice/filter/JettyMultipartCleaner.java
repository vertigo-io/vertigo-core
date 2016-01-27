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
package io.vertigo.vega.impl.webservice.filter;

import org.eclipse.jetty.util.MultiException;
import org.eclipse.jetty.util.MultiPartInputStreamParser;

import spark.Filter;
import spark.Request;
import spark.Response;
import spark.utils.SparkUtils;

/**
 * Filter to configure MultipartConfigElement for Jetty Request.
 * @author npiedeloup
 */
public final class JettyMultipartCleaner extends Filter {
	private static final String JETTY_MULTIPART_INPUT_STREAM = org.eclipse.jetty.server.Request.__MULTIPART_INPUT_STREAM;//"org.eclipse.multipartConfig";

	/**
	 * Constructor.
	 */
	public JettyMultipartCleaner() {
		super(SparkUtils.ALL_PATHS, "*/*");
	}

	/** {@inheritDoc} */
	@Override
	public void handle(final Request request, final Response response) {
		final MultiPartInputStreamParser multipartInputStream = (MultiPartInputStreamParser) request.raw()
				.getAttribute(JETTY_MULTIPART_INPUT_STREAM);
		if (multipartInputStream != null) {
			try {
				// a multipart request to a servlet will have the parts cleaned up correctly, but
				// the repeated call to deleteParts() here will safely do nothing.
				multipartInputStream.deleteParts();
			} catch (final MultiException e) {
				//	            LOG.error("Error while deleting multipart request parts", e);
			}
		}
	}
}
