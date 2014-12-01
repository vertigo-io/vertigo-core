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
package io.vertigo.vega.impl.rest.filter;

import javax.servlet.MultipartConfigElement;

import spark.Filter;
import spark.Request;
import spark.Response;

/**
 * Filter to configure MultipartConfigElement for Jetty Request.
 * @author npiedeloup
 */
public final class JettyMultipartConfig extends Filter {
	private static final String JETTY_CONFIG_ATTRIBUTE = org.eclipse.jetty.server.Request.__MULTIPART_CONFIG_ELEMENT;//"org.eclipse.multipartConfig";
	private final MultipartConfigElement multipartConfigElement;

	/**
	 * Constructor.
	 * @param tempPath path for uploaded tempfiles
	 */
	public JettyMultipartConfig(final String tempPath) {
		multipartConfigElement = new MultipartConfigElement(tempPath, 30 * 1024 * 1024L, 5 * 30 * 1024 * 1024L, 50 * 1024);
	}

	/** {@inheritDoc} */
	@Override
	public void handle(final Request request, final Response response) {
		request.raw().setAttribute(JETTY_CONFIG_ATTRIBUTE, multipartConfigElement);

	}

}
