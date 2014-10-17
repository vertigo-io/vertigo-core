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
package io.vertigo.studio.plugins.mda.search;

import io.vertigo.core.Home;
import io.vertigo.dynamo.domain.metamodel.Domain;
import io.vertigo.dynamo.domain.metamodel.DtProperty;
import io.vertigo.lang.Assertion;

import java.util.List;

import freemarker.template.SimpleScalar;
import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

/**
 * Récuperation du type de l'index du domain.
 * 
 * @author  dchallas
 */
public final class TemplateMethodIndexType implements TemplateMethodModel {

	/** {@inheritDoc}*/
	public TemplateModel exec(final List params) throws TemplateModelException {
		final String domainUrn = (String) params.get(0);
		final Domain domain = Home.getDefinitionSpace().resolve(domainUrn, Domain.class);

		// On peut préciser pour chaque domaine le type d'indexation
		String fieldType = domain.getProperties().getValue(DtProperty.INDEX_TYPE);

		// Calcul automatique  par default.
		switch (domain.getDataType()) {
			case Boolean:
				// native
			case Date:
				// native
			case Double:
				// native
			case Integer:
				// native
			case Long:
				// native
				if (fieldType == null) {
					fieldType = domain.getDataType().toString().toLowerCase();
				}
				break;
			case String:
				if (fieldType == null) {
					throw new IllegalArgumentException("## Précisez la valeur \"indexType\" dans le domain [" + domain + "].");
				}
				break;
			case DataStream:
				// IllegalArgumentException
			case BigDecimal:
				// IllegalArgumentException
			case DtObject:
				// IllegalArgumentException
			case DtList:
				// IllegalArgumentException
			default:
				// IllegalArgumentException
				throw new IllegalArgumentException("Type de donnée non pris en charge pour l'indexation [" + domain + "].");

		}
		Assertion.checkNotNull(fieldType);
		return new SimpleScalar(fieldType);

	}
}
