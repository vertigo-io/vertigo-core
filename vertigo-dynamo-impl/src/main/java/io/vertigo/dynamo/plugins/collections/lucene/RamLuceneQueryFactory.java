/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2016, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.dynamo.plugins.collections.lucene;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Collection;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;

import io.vertigo.dynamo.collections.ListFilter;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Option;
import io.vertigo.lang.WrappedException;
import io.vertigo.util.StringUtil;

final class RamLuceneQueryFactory {
	private final Analyzer queryAnalyzer;

	RamLuceneQueryFactory(final Analyzer queryAnalyzer) {
		Assertion.checkNotNull(queryAnalyzer);
		//-----
		this.queryAnalyzer = queryAnalyzer;
	}

	Query createFilterQuery(final String keywords, final Collection<DtField> searchedFields, final List<ListFilter> listFilters, final Option<DtField> boostedField) throws IOException {
		final Query filteredQuery;
		final Query keywordsQuery = createKeywordQuery(queryAnalyzer, keywords, searchedFields, boostedField);
		if (!listFilters.isEmpty()) {
			filteredQuery = createFilteredQuery(queryAnalyzer, keywordsQuery, listFilters);
		} else {
			filteredQuery = keywordsQuery;
		}
		return filteredQuery;
	}

	private static Query createKeywordQuery(final Analyzer queryAnalyser, final String keywords, final Collection<DtField> searchedFieldList, final Option<DtField> boostedField) throws IOException {
		if (StringUtil.isEmpty(keywords)) {
			return new MatchAllDocsQuery();
		}
		//-----
		final BooleanQuery query = new BooleanQuery();
		for (final DtField dtField : searchedFieldList) {
			final Query queryWord = createParsedKeywordsQuery(queryAnalyser, dtField.name(), keywords);
			if (boostedField.isPresent() && dtField.equals(boostedField.get())) {
				queryWord.setBoost(4);
			}
			query.add(queryWord, BooleanClause.Occur.SHOULD);
		}
		return query;
	}

	private static Query createFilteredQuery(final Analyzer queryAnalyser, final Query keywordsQuery, final List<ListFilter> filters) {
		final BooleanQuery query = new BooleanQuery();
		query.add(keywordsQuery, BooleanClause.Occur.MUST);

		for (final ListFilter filter : filters) {
			final StandardQueryParser queryParser = new StandardQueryParser(queryAnalyser);
			try {
				query.add(queryParser.parse(filter.getFilterValue(), null), isExclusion(filter) ? BooleanClause.Occur.MUST_NOT : BooleanClause.Occur.MUST);
			} catch (final QueryNodeException e) {
				throw new WrappedException("Erreur lors de la création du filtrage de la requete", e);
			}
		}
		return query;
	}

	private static boolean isExclusion(final ListFilter listFilter) {
		final String listFilterValue = listFilter.getFilterValue().trim();
		return listFilterValue.startsWith("-");
	}

	private static Query createParsedKeywordsQuery(final Analyzer queryAnalyser, final String fieldName, final String keywords) throws IOException {
		final BooleanQuery query = new BooleanQuery();
		final Reader reader = new StringReader(keywords);
		try (final TokenStream tokenStream = queryAnalyser.tokenStream(fieldName, reader)) {
			tokenStream.reset();
			try {
				final CharTermAttribute termAttribute = tokenStream.getAttribute(CharTermAttribute.class);
				while (tokenStream.incrementToken()) {
					final String term = new String(termAttribute.buffer(), 0, termAttribute.length());
					final PrefixQuery termQuery = new PrefixQuery(new Term(fieldName, term));
					query.add(termQuery, BooleanClause.Occur.MUST);
				}
			} finally {
				reader.reset();
				tokenStream.end();
			}
		}
		return query;
	}
}
