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
package io.vertigo.vega.impl.rest.catalog;

import io.vertigo.core.Home;
import io.vertigo.vega.rest.RestfulService;
import io.vertigo.vega.rest.metamodel.EndPointDefinition;
import io.vertigo.vega.rest.metamodel.EndPointParam;
import io.vertigo.vega.rest.stereotype.AnonymousAccessAllowed;
import io.vertigo.vega.rest.stereotype.GET;
import io.vertigo.vega.rest.stereotype.SessionLess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Default RestService to list services published.
 * @author npiedeloup (22 juil. 2014 11:12:02)
 */
public final class CatalogRestServices implements RestfulService {

	@SessionLess
	@AnonymousAccessAllowed
	@GET("/catalog")
	public List<String> publishCatalog() {
		final Collection<EndPointDefinition> endPointDefCollection = Home.getDefinitionSpace().getAll(EndPointDefinition.class);
		final List<String> result = new ArrayList<>();

		final StringBuilder sb = new StringBuilder();
		for (final EndPointDefinition endPointDefinition : endPointDefCollection) {
			final String doc = endPointDefinition.getDoc();
			sb.append(endPointDefinition.getVerb().name()).append(":");
			sb.append(endPointDefinition.getPath());
			sb.append("(");
			String sep = "";
			for (final EndPointParam endPointParam : endPointDefinition.getEndPointParams()) {
				sb.append(sep);
				sb.append(endPointParam);
				sep = ", ";
			}
			sb.append(")");
			if (!doc.isEmpty()) {
				sb.append(" /*");
				sb.append(endPointDefinition.getDoc());
				sb.append("*/");
			}
			result.add(sb.toString());
			sb.setLength(0);
		}
		return result;
	}
}
