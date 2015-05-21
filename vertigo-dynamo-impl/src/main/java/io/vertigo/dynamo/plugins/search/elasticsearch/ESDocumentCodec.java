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
package io.vertigo.dynamo.plugins.search.elasticsearch;

import io.vertigo.commons.codec.CodecManager;
import io.vertigo.dynamo.domain.metamodel.DataAccessor;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.model.DtSubject;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.dynamo.search.metamodel.SearchIndexDefinition;
import io.vertigo.dynamo.search.model.SearchIndex;
import io.vertigo.lang.Assertion;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.search.SearchHit;

/**
 * Traduction bi directionnelle des objets SOLR en objets logique de recherche.
 * Pseudo Codec : asymétrique par le fait que ElasticSearch gère un objet différent en écriture et lecture.
 * L'objet lu ne contient pas les données indexées non stockées !
 * @author pchretien, npiedeloup
 */
final class ESDocumentCodec {
	/** FieldName containing Full result object. */
	static final String FULL_RESULT = "FULL_RESULT";
	//public static final String URN = "URI";
	//-----
	private final CodecManager codecManager;

	/**
	 * Constructeur.
	 * @param codecManager Manager des codecs
	 */
	ESDocumentCodec(final CodecManager codecManager) {
		Assertion.checkNotNull(codecManager);
		//-----
		this.codecManager = codecManager;
	}

	private <I extends DtObject> String encode(final I dto) {
		Assertion.checkNotNull(dto);
		//-----
		//	System.out.println(">>>encode : " + dto);
		final byte[] data = codecManager.getCompressedSerializationCodec().encode(dto);
		return codecManager.getBase64Codec().encode(data);
	}

	private <R extends DtObject> R decode(final String base64Data) {
		Assertion.checkNotNull(base64Data);
		//-----
		final byte[] data = codecManager.getBase64Codec().decode(base64Data);
		return (R) codecManager.getCompressedSerializationCodec().decode(data);
	}

	/**
	 * Transformation d'un resultat ElasticSearch en un index.
	 * Les highlights sont ajoutés avant ou après (non determinable).
	 * @param <S> Type du sujet représenté par ce document
	 * @param <I> Type d'object indexé
	 * @param <R> Type d'object resultat
	 * @param indexDefinition Definition de l'index
	 * @param searchHit Resultat ElasticSearch
	 * @return Objet logique de recherche
	 */
	<S extends DtSubject, I extends DtObject> SearchIndex<S, I> searchHit2Index(final SearchIndexDefinition indexDefinition, final SearchHit searchHit) {
		/* On lit du document les données persistantes. */
		/* 1. URI */
		final String urn = searchHit.getId();
		final URI uri = io.vertigo.dynamo.domain.model.URI.fromURN(urn);

		/* 2 : Result stocké */
		final I resultDtObjectdtObject;
		if (searchHit.field(FULL_RESULT) == null) {
			resultDtObjectdtObject = decode((String) searchHit.getSource().get(FULL_RESULT));
		} else {
			resultDtObjectdtObject = decode((String) searchHit.field(FULL_RESULT).getValue());
		}
		//-----
		return SearchIndex.createIndex(indexDefinition, uri, resultDtObjectdtObject);
	}

	/**
	 * Transformation d'un index en un document ElasticSearch.
	 * @param <S> Type du sujet représenté par ce document
	 * @param <I> Type d'object indexé
	 * @param index Objet logique de recherche
	 * @return Document SOLR
	 * @throws IOException Json exception
	 */
	<S extends DtSubject, I extends DtObject> XContentBuilder index2XContentBuilder(final SearchIndex<S, I> index) throws IOException {
		Assertion.checkNotNull(index);
		//-----

		final DtDefinition dtDefinition = index.getDefinition().getIndexDtDefinition();
		final List<DtField> nonPersistentFields = getNonPersistentFields(dtDefinition);
		final I dtResult;
		if (nonPersistentFields.isEmpty()) {
			dtResult = index.getIndexDtObject();
		} else {
			dtResult = cloneDto(dtDefinition, index.getIndexDtObject(), nonPersistentFields);
		}

		final XContentBuilder xContentBuilder = XContentFactory.jsonBuilder();

		/* 1: URI */
		xContentBuilder.startObject();
		//xContentBuilder.field(URN, index.getURI().toURN());
		/* 2 : Result stocké */
		final String result = encode(dtResult);
		xContentBuilder.field(FULL_RESULT, result);

		/* 3 : Les champs du dto index */
		final DtObject dtIndex = index.getIndexDtObject();
		final DtDefinition indexDtDefinition = DtObjectUtil.findDtDefinition(dtIndex);

		for (final DtField dtField : indexDtDefinition.getFields()) {
			final Object value = dtField.getDataAccessor().getValue(dtIndex);
			if (value != null) { //les valeurs null ne sont pas indexées => conséquence : on ne peut les rechercher
				final String indexFieldName = dtField.getName();
				if (value instanceof String) {
					final String encodedValue = escapeInvalidUTF8Char((String) value);
					xContentBuilder.field(indexFieldName, encodedValue);
				} else {
					xContentBuilder.field(indexFieldName, value);
				}
			}
		}
		xContentBuilder.endObject();
		return xContentBuilder;
	}

	private List<DtField> getNonPersistentFields(final DtDefinition dtDefinition) {
		final List<DtField> nonPersistentFields = new ArrayList<>();
		for (final DtField dtField : dtDefinition.getFields()) {
			if (!dtField.isPersistent()) {
				nonPersistentFields.add(dtField);
			}
		}
		return nonPersistentFields;
	}

	private <I extends DtObject> I cloneDto(final DtDefinition dtDefinition, final I dto, final List<DtField> excludedFields) {
		final I clonedDto = (I) DtObjectUtil.createDtObject(dtDefinition);
		for (final DtField dtField : dtDefinition.getFields()) {
			if (!excludedFields.contains(dtField)) {
				final DataAccessor dataAccessor = dtField.getDataAccessor();
				dataAccessor.setValue(clonedDto, dataAccessor.getValue(dto));
			}
		}
		return clonedDto;
	}

	private static String escapeInvalidUTF8Char(final String value) {
		return value.replace('\uFFFF', ' ').replace('\uFFFE', ' '); //testé comme le plus rapide pour deux cas
	}
}
