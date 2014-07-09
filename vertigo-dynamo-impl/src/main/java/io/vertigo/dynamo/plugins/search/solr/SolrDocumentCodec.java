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
package io.vertigo.dynamo.plugins.search.solr;

import io.vertigo.commons.codec.CodecManager;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.dynamo.search.IndexFieldNameResolver;
import io.vertigo.dynamo.search.metamodel.IndexDefinition;
import io.vertigo.dynamo.search.model.Index;
import io.vertigo.kernel.lang.Assertion;

import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;

/**
 * Traduction bi directionnelle des objets SOLR en objets logique de recherche.
 * Pseudo Codec : asymétrique par le fait que SOLR gère un objet différent en écriture et lecture.
 * L'objet lu ne contient pas les données indexées non stockées !
 * @author pchretien
 */
final class SolrDocumentCodec<I extends DtObject, R extends DtObject> {
	public static final String FULL_RESULT = "FULL_RESULT";
	public static final String URN = "URI";
	//-----------------------
	private final CodecManager codecManager;

	/**
	 * Constructeur.
	 * @param codecManager Manager des codecs
	 */
	SolrDocumentCodec(final CodecManager codecManager) {
		Assertion.checkNotNull(codecManager);
		//-----------------------------------------------------------------
		this.codecManager = codecManager;
	}

	private byte[] encode(final R dto) {
		Assertion.checkNotNull(dto);
		//----------------------------------------------------------------
		//	System.out.println(">>>encode : " + dto);
		return codecManager.getCompressedSerializationCodec().encode(dto);
	}

	private R decode(final byte[] data) {
		Assertion.checkNotNull(data);
		//----------------------------------------------------------------
		//	System.out.println(">>>decode  : " + codecManager.getCompressedSerializationCodec().decode(data));
		return (R) codecManager.getCompressedSerializationCodec().decode(data);
	}

	/**
	 * Transformation d'un document SOLR en un index.
	 * Les highligh sont ajoutés avant ou après (non determinable).
	 * @param indexDefinition Definition de l'index
	 * @param solrDocument Document SOLR
	 * @return Objet logique de recherche
	 */
	Index<I, R> solrDocument2Index(final IndexDefinition indexDefinition, final SolrDocument solrDocument) {
		/* On lit du document les données persistantes. */
		/* 1. URI */
		final String urn = (String) solrDocument.get(URN);

		final URI uri = io.vertigo.dynamo.domain.model.URI.fromURN(urn);

		/* 2 : Result stocké */
		final byte[] data = (byte[]) solrDocument.get(FULL_RESULT);
		final R resultDtObjectdtObject = decode(data);
		//--------------------------
		return Index.createResult(indexDefinition, uri, resultDtObjectdtObject);
	}

	/**
	 * Transformation d'un index en un document SOLR.
	 * @param index Objet logique de recherche
	 * @param indexFieldNameResolver Resolver de nom de champs d'index
	 * @return Document SOLR
	 */
	SolrInputDocument index2SolrInputDocument(final Index<I, R> index, final IndexFieldNameResolver indexFieldNameResolver) {
		Assertion.checkNotNull(index);
		Assertion.checkNotNull(indexFieldNameResolver);
		//---------------------------------------------------------------------
		final SolrInputDocument solrInputDocument = new SolrInputDocument();

		/* 1: URI */
		solrInputDocument.addField(URN, index.getURI().toURN());

		/* 2 : Result stocké */
		final byte[] result = encode(index.getResultDtObject());
		solrInputDocument.addField(FULL_RESULT, result);

		/* 3 : Les champs du dto index */
		final DtObject dtIndex = index.getIndexDtObject();
		final DtDefinition indexDtDefinition = DtObjectUtil.findDtDefinition(dtIndex);

		for (final DtField dtField : indexDtDefinition.getFields()) {
			final Object value = dtField.getDataAccessor().getValue(dtIndex);
			if (value != null) { //les valeurs null ne sont pas indexées => conséquence : on ne peut les rechercher
				//solrInputDocument.addField(dtField.getName(), value);
				final String indexFieldName = indexFieldNameResolver.obtainIndexFieldName(dtField);
				if (value instanceof String) {
					final String encodedValue = escapeInvalidUTF8Char((String) value);
					solrInputDocument.addField(indexFieldName, encodedValue);
				} else {
					solrInputDocument.addField(indexFieldName, value);
				}
			}
		}
		return solrInputDocument;
	}

	private static String escapeInvalidUTF8Char(final String value) {
		return value.replace('\uFFFF', ' ').replace('\uFFFE', ' '); //testé comme le plus rapide pour deux cas
	}
}
