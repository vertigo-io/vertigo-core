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

import io.vertigo.lang.Option;

import javax.inject.Inject;
import javax.inject.Named;

import spark.Filter;
import spark.Request;
import spark.Response;
import spark.utils.SparkUtils;

/**
 * Handler of Cross-Origin Resource Sharing (CORS).
 * @author npiedeloup
 */
public final class CorsAllowerFilter extends Filter {
	private static final String DEFAULT_ORIGINE_CORS_FILTER = "*";
	private static final String DEFAULT_METHODS_CORS_FILTER = "GET, POST, DELETE, PUT";//"*";
	private static final String DEFAULT_HEADERS_CORS_FILTER = "Content-Type";//"*";

	private final String originCORSFilter;

	@Inject
	public CorsAllowerFilter(@Named("originCORSFilter") final Option<String> originCORSFilter) {
		super(SparkUtils.ALL_PATHS, "*/*");
		this.originCORSFilter = originCORSFilter.getOrElse(DEFAULT_ORIGINE_CORS_FILTER);
	}

	/** {@inheritDoc} */
	@Override
	public void handle(final Request request, final Response response) {
		//TODO
		//check if OPTION : Origin not match originCORSFilter : throw Forbidden ?

		response.header("Access-Control-Allow-Origin", originCORSFilter);
		response.header("Access-Control-Request-Method", DEFAULT_METHODS_CORS_FILTER);
		response.header("Access-Control-Allow-Headers", DEFAULT_HEADERS_CORS_FILTER);
	}

}
