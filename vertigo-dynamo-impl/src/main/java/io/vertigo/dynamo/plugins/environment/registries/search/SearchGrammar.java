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
package io.vertigo.dynamo.plugins.environment.registries.search;

import io.vertigo.dynamo.impl.environment.kernel.meta.Entity;
import io.vertigo.dynamo.impl.environment.kernel.meta.EntityBuilder;
import io.vertigo.dynamo.impl.environment.kernel.meta.EntityProperty;
import io.vertigo.dynamo.impl.environment.kernel.meta.EntityPropertyType;
import io.vertigo.dynamo.impl.environment.kernel.meta.Grammar;
import io.vertigo.dynamo.plugins.environment.KspProperty;
import io.vertigo.dynamo.plugins.environment.registries.domain.DomainGrammar;

/**
 * @author pchretien
 */
final class SearchGrammar {

	public static final Entity INDEX_DEFINITION_ENTITY;

	public static final EntityProperty SEARCH_LOADER_PROPERTY = new EntityProperty("LOADER_ID", EntityPropertyType.String);

	public static final EntityProperty FIELD_NAME = new EntityProperty("FIELD_NAME", EntityPropertyType.String);

	public static final Grammar GRAMMAR;

	public static final Entity FACET_DEFINITION_ENTITY;

	public static final Entity FACET_RANGE_ENTITY;
	public static final EntityProperty RANGE_FILTER_PROPERTY = new EntityProperty("FILTER", EntityPropertyType.String);

	public static final Entity FACETED_QUERY_DEFINITION_ENTITY;
	public static final EntityProperty LIST_FILTER_BUILDER_CLASS = new EntityProperty("LIST_FILTER_BUILDER_CLASS", EntityPropertyType.String);

	public static final EntityProperty LIST_FILTER_BUILDER_QUERY = new EntityProperty("LIST_FILTER_BUILDER_QUERY", EntityPropertyType.String);

	/*
	 * create IndexDefinition IDX_TEST {
	    keyConcept : DT_TEST,
	    dtResult : DT_TEST,
	    dtIndex : DT_TEST,
	    searchLoader : com.project.domain.search.dao.SearchLoaderPeople
	}

	create FacetDefinition FCT_MOVIE_GENRE {
		dtDefinition : DT_TEST, fieldName : "GENRE", label : "Par genre"
	}

	create FacetDefinition FCT_MOVIE_ANNEE {
		dtDefinition : DT_TEST, fieldName : "YEAR", label : "Par année",
	 	range R1 { filter : "YEAR:[* TO 2000]", label : "avant 2000"}, //TODO : fieldName in filter too ?
	 	range R2 { filter : "YEAR:[2000 TO 2005]", label : "2000-2005"},
	 	range R3 { filter : "YEAR:[2005 TO *]", label : "après 2005"}
	}

	create FacetedQueryDefinition QRY_MOVIE {
		facet FCT_MOVIE_GENRE,
		facet FCT_MOVIE_ANNEE,
	}
	*/

	static {
		INDEX_DEFINITION_ENTITY = new EntityBuilder("IndexDefinition")
				.withAttribute("keyConcept", DomainGrammar.DT_DEFINITION_ENTITY, true)
				.withAttribute("dtIndex", DomainGrammar.DT_DEFINITION_ENTITY, true)
				.withProperty(SEARCH_LOADER_PROPERTY, true)
				.build();

		FACET_RANGE_ENTITY = new EntityBuilder("range")
				.withProperty(RANGE_FILTER_PROPERTY, true)
				.withProperty(KspProperty.LABEL, true)
				.build();

		FACET_DEFINITION_ENTITY = new EntityBuilder("FacetDefinition")
				.withAttribute("dtDefinition", DomainGrammar.DT_DEFINITION_ENTITY, true)
				.withProperty(FIELD_NAME, true)
				.withProperty(KspProperty.LABEL, true)
				.withAttributes("range", FACET_RANGE_ENTITY, false)// facultative
				.build();

		FACETED_QUERY_DEFINITION_ENTITY = new EntityBuilder("FacetedQueryDefinition")
				.withAttribute("keyConcept", DomainGrammar.DT_DEFINITION_ENTITY, true)
				.withAttribute("domainCriteria", DomainGrammar.DOMAIN_ENTITY, true)
				.withProperty(LIST_FILTER_BUILDER_CLASS, true)
				.withProperty(LIST_FILTER_BUILDER_QUERY, true)
				.withAttributes("facets", FACET_DEFINITION_ENTITY, true)
				.build();

		GRAMMAR = new Grammar(INDEX_DEFINITION_ENTITY, FACET_DEFINITION_ENTITY, FACETED_QUERY_DEFINITION_ENTITY);
	}

}
