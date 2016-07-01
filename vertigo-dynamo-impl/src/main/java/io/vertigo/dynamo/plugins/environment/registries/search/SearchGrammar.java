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

import static io.vertigo.core.definition.dsl.entity.EntityPropertyType.String;

import java.util.List;

import io.vertigo.core.definition.dsl.entity.Entity;
import io.vertigo.core.definition.dsl.entity.EntityBuilder;
import io.vertigo.core.definition.dsl.entity.EntityGrammar;
import io.vertigo.dynamo.plugins.environment.KspProperty;
import io.vertigo.dynamo.plugins.environment.registries.domain.DomainGrammar;
import io.vertigo.util.ListBuilder;

/**
 * @author pchretien
 */
final class SearchGrammar implements EntityGrammar {

	/** Index definition. */
	public static final Entity INDEX_DEFINITION_ENTITY;
	/** Search loader id. */
	public static final String SEARCH_LOADER_PROPERTY = "LOADER_ID";
	/** List filter class. */
	public static final String LIST_FILTER_BUILDER_CLASS = "LIST_FILTER_BUILDER_CLASS";
	/** List filter query. */
	public static final String LIST_FILTER_BUILDER_QUERY = "LIST_FILTER_BUILDER_QUERY";

	/** Index copy fields. */
	public static final Entity INDEX_COPY_ENTITY;

	/** Fieldname. */
	public static final String FIELD_NAME = "FIELD_NAME";
	/** Facet order. */
	public static final String FACET_ORDER = "ORDER";
	/** Facet definition. */
	public static final Entity FACET_DEFINITION_ENTITY;
	/** Facet range. */
	private static final Entity FACET_RANGE_ENTITY;
	/** Range filter. */
	public static final String RANGE_FILTER_PROPERTY = "FILTER";
	/** Faceted query definition. */
	public static final Entity FACETED_QUERY_DEFINITION_ENTITY;

	/** indexCopy to. */
	public static final String INDEX_COPY_TO_PROPERTY = "indexCopyTo";
	/** indexCopy from. */
	public static final String INDEX_COPY_FROM_PROPERTY = "FROM";

	/*
	 * create IndexDefinition IDX_TEST {
	    keyConcept : DT_TEST,
	    dtResult : DT_TEST,
	    dtIndex : DT_TEST,
	    indexCopyTo FIELD_TO_1 : { from: "FIELD_FROM_1,FIELD_FROM_2" }, //use field formatters
	    indexCopyTo FIELD_TO_2 : { from: "FIELD_FROM_3" }, //use field formatters

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
		INDEX_COPY_ENTITY = new EntityBuilder("indexCopyTo")
				.addField(INDEX_COPY_FROM_PROPERTY, String, true)
				.build();

		INDEX_DEFINITION_ENTITY = new EntityBuilder("IndexDefinition")
				.addField("keyConcept", DomainGrammar.DT_DEFINITION_ENTITY.getLink(), true)
				.addField("dtIndex", DomainGrammar.DT_DEFINITION_ENTITY.getLink(), true)
				.addFields(INDEX_COPY_TO_PROPERTY, INDEX_COPY_ENTITY, false) //facultative
				.addField(SEARCH_LOADER_PROPERTY, String, true)
				.build();

		FACET_RANGE_ENTITY = new EntityBuilder("range")
				.addField(RANGE_FILTER_PROPERTY, String, true)
				.addField(KspProperty.LABEL, String, true)
				.build();

		FACET_DEFINITION_ENTITY = new EntityBuilder("FacetDefinition")
				.addField("dtDefinition", DomainGrammar.DT_DEFINITION_ENTITY.getLink(), true)
				.addField(FIELD_NAME, String, true)
				.addField(KspProperty.LABEL, String, true)
				.addField(FACET_ORDER, String, false) //facultative, default to count
				.addFields("range", FACET_RANGE_ENTITY, false) //facultative
				.build();

		FACETED_QUERY_DEFINITION_ENTITY = new EntityBuilder("FacetedQueryDefinition")
				.addField("keyConcept", DomainGrammar.DT_DEFINITION_ENTITY.getLink(), true)
				.addField("domainCriteria", DomainGrammar.DOMAIN_ENTITY.getLink(), true)
				.addField(LIST_FILTER_BUILDER_CLASS, String, true)
				.addField(LIST_FILTER_BUILDER_QUERY, String, true)
				.addFields("facets", FACET_DEFINITION_ENTITY.getLink(), true)
				.build();
	}

	@Override
	public List<Entity> getEntities() {
		return new ListBuilder<Entity>()
				.add(INDEX_DEFINITION_ENTITY)
				.add(FACET_DEFINITION_ENTITY)
				.add(FACETED_QUERY_DEFINITION_ENTITY)
				.unmodifiable()
				.build();
	}
}
