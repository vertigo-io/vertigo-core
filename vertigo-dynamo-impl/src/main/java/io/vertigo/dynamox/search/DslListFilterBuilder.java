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
package io.vertigo.dynamox.search;

import io.vertigo.commons.parser.NotFoundException;
import io.vertigo.dynamo.collections.ListFilter;
import io.vertigo.dynamo.collections.metamodel.ListFilterBuilder;
import io.vertigo.dynamox.search.dsl.definition.DslBlockQueryDefinition;
import io.vertigo.dynamox.search.dsl.definition.DslExpressionDefinition;
import io.vertigo.dynamox.search.dsl.definition.DslFieldDefinition;
import io.vertigo.dynamox.search.dsl.definition.DslFixedQueryDefinition;
import io.vertigo.dynamox.search.dsl.definition.DslMultiExpressionDefinition;
import io.vertigo.dynamox.search.dsl.definition.DslMultiFieldDefinition;
import io.vertigo.dynamox.search.dsl.definition.DslQueryDefinition;
import io.vertigo.dynamox.search.dsl.definition.DslRangeQueryDefinition;
import io.vertigo.dynamox.search.dsl.definition.DslTermQueryDefinition;
import io.vertigo.dynamox.search.dsl.definition.DslUserCriteria;
import io.vertigo.dynamox.search.dsl.rules.DslParserUtil;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Option;
import io.vertigo.util.BeanUtil;
import io.vertigo.util.StringUtil;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

/**
 * Default builder from Criteria to ListFilter with a query pattern with DSL.
 * Pattern syntax is easy :
 * #QUERY# : criteria.toString() : use this when Criteria is a user string
 * #MY_FIELD# : criteria.myField
 * #MY_FIELD#!(myDefault) : criteria.myField!=null?criteria.myField:myDefault
 * QueryString modifier must be add into the ## and will be repeated for all word (separated by regexp \p{White_Space})
 *
 * example:
 *  "" // all result
 *  #QUERY# //directly use user's query as is
 *  code:"#code#"  //CODE equals strictly
 *  comment:#comment*#  //COMMENT contains words prefixed with criteria's comment words (all words)
 *  comment:#+comment*#  //COMMENT MUST contains all words prefixed with criteria's comment words (all words)
 *  year:[#yearMin# TO #yearMax#] //YEAR between crieteria's year_min and year_max
 *  +(addr1:#address# addr2:#address#) //criteria ADDRESS field should be in ADDR1 or ADDR2 index's fields
 *  For more info, look for ElasctiSearch QueryString Syntax
 *  @see "https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-query-string-query.html#query-string-syntax"
 *
 * If a criteria field contains OR / AND it will be use as logical operator.
 * If a criteria field contains XXX:yyyy it will be use as a specific field query and will not be transformed
 *
 * @author npiedeloup
 * @param <C> Criteria type
 */
public final class DslListFilterBuilder<C> implements ListFilterBuilder<C> {

	private static final String USER_QUERY_KEYWORD = "query";

	private static final Set<String> RESERVED_QUERY_KEYWORDS = new HashSet<>(Arrays.asList(new String[] { "AND", "OR", "and", "or", "And", "Or", "*" }));

	private static final String QUERY_RESERVERD_PATTERN = "\\+\\-\\!\\*\\?\\~\\^\\=\\>\\<\\s";

	private List<DslMultiExpressionDefinition> myBuildQuery;
	private C myCriteria;

	/**
	 * Fix query pattern.
	 * @param buildQuery Pattern (not null, could be empty)
	 * @return this builder
	 */
	@Override
	public ListFilterBuilder<C> withBuildQuery(final String buildQuery) {
		Assertion.checkNotNull(buildQuery);
		Assertion.checkState(myBuildQuery == null, "query was already set : {0}", myBuildQuery);
		//-----
		try {
			myBuildQuery = DslParserUtil.parseMultiExpression(buildQuery);
		} catch (final NotFoundException e) {
			final String message = StringUtil.format("Echec de lecture du listFilterPattern {0}\n{1}", buildQuery, e.getFullMessage());
			throw new RuntimeException(message, e);
		} catch (final Exception e) {
			final String message = StringUtil.format("Echec de lecture du listFilterPattern {0}\n{1}", buildQuery, e.getMessage());
			throw new RuntimeException(message, e);
		}
		return this;
	}

	/**
	 * Fix criteria.
	 * @param criteria Criteria
	 * @return this builder
	 */
	@Override
	public ListFilterBuilder<C> withCriteria(final C criteria) {
		Assertion.checkNotNull(criteria);
		Assertion.checkState(myCriteria == null, "criteria was already set : {0}", myCriteria);
		//-----
		this.myCriteria = criteria;
		return this;

	}

	/** {@inheritDoc} */
	@Override
	public ListFilter build() {
		final String query = buildQueryString();
		return new ListFilter(query);
	}

	private String buildQueryString() {
		final StringBuilder query = new StringBuilder();
		for (final DslMultiExpressionDefinition multiExpressionDefinition : myBuildQuery) {
			appendMultiExpression(query, multiExpressionDefinition);
		}
		return query.toString();
	}

	private void appendMultiExpression(final StringBuilder query, final DslMultiExpressionDefinition multiExpressionDefinition) {
		final StringBuilder multiExpressionQuery = new StringBuilder();
		for (final DslExpressionDefinition expression : multiExpressionDefinition.getExpressions()) {
			appendExpression(multiExpressionQuery, expression);
		}
		for (final DslMultiExpressionDefinition multiExpression : multiExpressionDefinition.getMultiExpressions()) {
			appendMultiExpression(multiExpressionQuery, multiExpression);
		}
		flushSubQueryToQuery(query, multiExpressionDefinition.getPreBody(), multiExpressionDefinition.getPostBody(), multiExpressionDefinition.isBlock(), multiExpressionQuery);
	}

	private void appendExpression(final StringBuilder query, final DslExpressionDefinition expressionDefinition) {
		final StringBuilder expressionQuery = new StringBuilder();
		final DslQueryDefinition dslQueryDefinition = expressionDefinition.getQuery();
		appendQuery(query, expressionDefinition, expressionQuery, dslQueryDefinition);
		flushExpressionToQuery(query, expressionDefinition, expressionQuery);
	}

	private static void flushExpressionToQuery(final StringBuilder query, final DslExpressionDefinition expressionDefinition, final StringBuilder expressionQuery) {
		if (expressionQuery.length() > 0) {
			final String[] trimedExpression = splitTrimedSubQueryToQuery(expressionQuery.toString());
			query.append(trimedExpression[0]);
			query.append(expressionDefinition.getPreBody());
			if (expressionDefinition.getField().isDefined()) {
				appendField(query, expressionDefinition.getField().get());
			}
			final boolean useBlock = mayUseBlock(trimedExpression[1]);

			query.append(useBlock ? "(" : "")
					.append(trimedExpression[1])
					.append(useBlock ? ")" : "")
					.append(expressionDefinition.getPostBody());
			expressionQuery.setLength(0);
		}
	}

	private static boolean mayUseBlock(final String trimedExpression) {
		//on place des parenthèses s'il n'y a pas encore de block, ou des caractères interdits
		return !trimedExpression.matches("((\\(.*\\))|(\\[.*\\])|(\\\".*\\\")|\\*)(\\^[0-9]+)?")//not : (...) or [...] or "..." but may finished by ^2
				&& trimedExpression.matches(".*[" + QUERY_RESERVERD_PATTERN + "].*"); //contains any reserved char +-!*?~^=>< or any spaces

	}

	private static void flushSubQueryToQuery(final StringBuilder query, final String preExpression, final String postExpression, final boolean useBlock, final StringBuilder subQuery) {
		if (subQuery.length() > 0) {
			final String[] trimedQuery = splitTrimedSubQueryToQuery(subQuery.toString());
			query.append(trimedQuery[0])
					.append(preExpression)
					.append(useBlock ? "(" : "")
					.append(trimedQuery[1])
					.append(useBlock ? ")" : "")
					.append(postExpression);
		}
	}

	private static String[] splitTrimedSubQueryToQuery(final String subQueryStr) {
		final String[] result = new String[2];
		if (!subQueryStr.isEmpty()) {
			final String trimSubQueryStr = subQueryStr.replaceFirst("^\\s*", "");
			final String preTrimSubQueryStr = subQueryStr.substring(0, subQueryStr.length() - trimSubQueryStr.length());
			result[0] = preTrimSubQueryStr;
			result[1] = trimSubQueryStr;
		}
		return result;
	}

	private static void appendField(final StringBuilder query, final DslFieldDefinition dslFieldDefinition) {
		query.append(dslFieldDefinition.getPreBody())
				.append(dslFieldDefinition.getFieldName())
				.append(dslFieldDefinition.getPostBody())
				.append(":");
	}

	private void appendMultiQuery(final StringBuilder query, final DslBlockQueryDefinition dslMultiQueryDefinition, final DslExpressionDefinition expressionDefinition, final StringBuilder parentQuery) {
		final StringBuilder expressionMultiQuery = new StringBuilder();
		for (final DslQueryDefinition dslQueryDefinition : dslMultiQueryDefinition.getQueries()) {
			appendQuery(parentQuery, expressionDefinition, expressionMultiQuery, dslQueryDefinition);
		}
		flushSubQueryToQuery(query, dslMultiQueryDefinition.getPreBody(), dslMultiQueryDefinition.getPostBody(), true, expressionMultiQuery);
	}

	private void appendQuery(final StringBuilder query, final DslExpressionDefinition expressionDefinition, final StringBuilder expressionQuery, final DslQueryDefinition dslQueryDefinition) {
		if (dslQueryDefinition instanceof DslTermQueryDefinition) {
			appendTermQuery(expressionQuery, (DslTermQueryDefinition) dslQueryDefinition, expressionDefinition, query);
		} else if (dslQueryDefinition instanceof DslBlockQueryDefinition) {
			appendMultiQuery(expressionQuery, (DslBlockQueryDefinition) dslQueryDefinition, expressionDefinition, query);
		} else if (dslQueryDefinition instanceof DslRangeQueryDefinition) {
			appendRangeQuery(expressionQuery, (DslRangeQueryDefinition) dslQueryDefinition, expressionDefinition);
		} else if (dslQueryDefinition instanceof DslFixedQueryDefinition) {
			appendFixedQuery(expressionQuery, (DslFixedQueryDefinition) dslQueryDefinition);
		}
	}

	private void appendTermQuery(final StringBuilder query, final DslTermQueryDefinition dslQueryDefinition, final DslExpressionDefinition expressionDefinition, final StringBuilder outExpressionQuery) {
		final String fieldName = dslQueryDefinition.getTermField();
		final Object value;
		if (USER_QUERY_KEYWORD.equalsIgnoreCase(fieldName)) {
			value = cleanUserCriteria(myCriteria.toString());
		} else {
			value = cleanUserCriteria(BeanUtil.getValue(myCriteria, fieldName));
		}
		appendTermQueryWithValue(value, query, dslQueryDefinition, expressionDefinition, outExpressionQuery);
	}

	private static <O> O cleanUserCriteria(final O value) {
		if (value instanceof String && ((String) value).trim().isEmpty()) { //so not null too
			return (O) "*";
		}
		return value;
	}

	private void appendTermQueryWithValue(final Object value, final StringBuilder query, final DslTermQueryDefinition dslQueryDefinition, final DslExpressionDefinition expressionDefinition, final StringBuilder outExpressionQuery) {
		final boolean useBlock;
		final StringBuilder queryPart = new StringBuilder();
		if (value instanceof String) { //so not null too
			useBlock = appendUserStringCriteria(queryPart, dslQueryDefinition, expressionDefinition, (String) value, outExpressionQuery);
		} else if (value instanceof Date) { //so not null too
			useBlock = appendSimpleCriteria(queryPart, dslQueryDefinition, formatDate((Date) value));
		} else if (value != null) {
			useBlock = appendSimpleCriteria(queryPart, dslQueryDefinition, value.toString());
		} else if (dslQueryDefinition.getDefaultValue().isDefined()) { //if value null => defaultValue
			useBlock = appendSimpleCriteria(queryPart, dslQueryDefinition, dslQueryDefinition.getDefaultValue().get());
		} else {
			useBlock = false;
		}
		flushSubQueryToQuery(query, dslQueryDefinition.getPreBody(), dslQueryDefinition.getPostBody(), useBlock, queryPart);
		//if defaultValue null => no criteria
	}

	private void appendRangeQuery(final StringBuilder query, final DslRangeQueryDefinition dslQueryDefinition, final DslExpressionDefinition expressionDefinition) {
		final DslQueryDefinition startQueryDefinition = dslQueryDefinition.getStartQueryDefinitions();
		final DslQueryDefinition endQueryDefinition = dslQueryDefinition.getEndQueryDefinitions();
		final StringBuilder startRangeQuery = new StringBuilder();
		if (startQueryDefinition instanceof DslTermQueryDefinition) {
			appendTermQuery(startRangeQuery, (DslTermQueryDefinition) startQueryDefinition, expressionDefinition, null); //null because, can't use upper output
		} else if (startQueryDefinition instanceof DslFixedQueryDefinition) {
			appendFixedQuery(startRangeQuery, (DslFixedQueryDefinition) startQueryDefinition);
		}
		final StringBuilder endRangeQuery = new StringBuilder();
		if (endQueryDefinition instanceof DslTermQueryDefinition) {
			appendTermQuery(endRangeQuery, (DslTermQueryDefinition) endQueryDefinition, expressionDefinition, null); //null because, can't use upper output
		} else if (endQueryDefinition instanceof DslFixedQueryDefinition) {
			appendFixedQuery(endRangeQuery, (DslFixedQueryDefinition) endQueryDefinition);
		}

		//flush Range Query
		final String startRangeStr = startRangeQuery.length() > 0 ? startRangeQuery.toString() : "*";
		final String endRangeStr = endRangeQuery.length() > 0 ? endRangeQuery.toString() : "*";

		if (!"*".equals(startRangeStr) || !"*".equals(endRangeStr)) {
			query.append(dslQueryDefinition.getPreBody())
					.append('[')
					.append(startRangeStr)
					.append(" to ")
					.append(endRangeStr)
					.append(']')
					.append(dslQueryDefinition.getPostBody());
		}
	}

	private static void appendFixedQuery(final StringBuilder query, final DslFixedQueryDefinition dslQueryDefinition) {
		query.append(dslQueryDefinition.getFixedQuery());
	}

	private static boolean appendSimpleCriteria(final StringBuilder query, final DslTermQueryDefinition dslTermDefinition, final String value) {
		query.append(dslTermDefinition.getPreTerm())
				.append(value)
				.append(dslTermDefinition.getPostTerm());
		return false; //never use block
	}

	private boolean appendUserStringCriteria(final StringBuilder query, final DslTermQueryDefinition dslTermDefinition, final DslExpressionDefinition expressionDefinition, final String userString, final StringBuilder outExpressionQuery) {
		final List<DslUserCriteria> userCriteriaList = DslParserUtil.parseUserCriteria(userString);

		int criteriaOnDefinitionField = 0; //On compte les fields sur le field de la definition. Si >1 on mettra des ( )
		for (final DslUserCriteria userCriteria : userCriteriaList) {
			final String criteriaValue = userCriteria.getCriteriaWord();
			if (!userCriteria.getOverridedFieldName().isEmpty()) {
				//si le field est surchargé on flush l'expression précédente
				flushExpressionToQuery(outExpressionQuery, expressionDefinition, query);
				criteriaOnDefinitionField = 0;
				//et on ajout la requete sur l'autre champs
				outExpressionQuery.append(userCriteria.getPreMissingPart())
						.append(userCriteria.getOverridedFieldName())
						.append(userCriteria.getOverridedPreModifier())
						.append(criteriaValue)
						.append(userCriteria.getOverridedPostModifier())
						.append(expressionDefinition.getPostBody())
						.append(userCriteria.getPostMissingPart());

			} else if (expressionDefinition.getMultiField().isDefined()) {
				criteriaOnDefinitionField++;
				final DslMultiFieldDefinition dslMultiFieldDefinition = expressionDefinition.getMultiField().get();
				query.append(userCriteria.getPreMissingPart());
				final List<DslExpressionDefinition> monoFieldExpressionDefinitions = new ArrayList<>();
				for (final DslFieldDefinition dslFieldDefinition : dslMultiFieldDefinition.getFields()) {
					final DslFieldDefinition monoFieldDefinition = new DslFieldDefinition(
							firstNotEmpty(dslFieldDefinition.getPreBody(), dslMultiFieldDefinition.getPreBody()),
							dslFieldDefinition.getFieldName(),
							"");
					final DslExpressionDefinition monoFieldExpressionDefinition = new DslExpressionDefinition(
							monoFieldExpressionDefinitions.isEmpty() ? "" : " ",
							Option.some(monoFieldDefinition), Option.<DslMultiFieldDefinition> none(),
							new DslFixedQueryDefinition(concat(criteriaValue, firstNotEmpty(userCriteria.getOverridedPostModifier(), dslTermDefinition.getPostTerm()))),
							firstNotEmpty(dslFieldDefinition.getPostBody(), dslMultiFieldDefinition.getPostBody()));
					monoFieldExpressionDefinitions.add(monoFieldExpressionDefinition);
				}
				final DslMultiExpressionDefinition monoFieldMultiExpressionDefinition = new DslMultiExpressionDefinition(
						firstNotEmpty(userCriteria.getOverridedPreModifier(), dslTermDefinition.getPreTerm()), true,
						monoFieldExpressionDefinitions, Collections.<DslMultiExpressionDefinition> emptyList(),
						"");

				appendMultiExpression(query, monoFieldMultiExpressionDefinition);
				query.append(userCriteria.getPostMissingPart());

			} else {
				criteriaOnDefinitionField++;
				query.append(userCriteria.getPreMissingPart());
				if (RESERVED_QUERY_KEYWORDS.contains(criteriaValue)) {
					query.append(criteriaValue);
				} else {
					query.append(userCriteria.getOverridedPreModifier().isEmpty() ? dslTermDefinition.getPreTerm() : userCriteria.getOverridedPreModifier())
							.append(criteriaValue)
							.append(userCriteria.getOverridedPostModifier().isEmpty() ? dslTermDefinition.getPostTerm() : userCriteria.getOverridedPostModifier());
				}
				query.append(userCriteria.getPostMissingPart());
			}
		}
		return criteriaOnDefinitionField > 1; //useBlock if more than 1 criteria
	}

	private static String firstNotEmpty(final String... elements) {
		for (final String element : elements) {
			if (!element.isEmpty()) {
				return element;
			}
		}
		return "";
	}

	/**
	 * Concat string elements.
	 * @param elements Nullable elements
	 * @return Concat string
	 */
	static String concat(final String... elements) {
		final StringBuilder sb = new StringBuilder();
		for (final String element : elements) {
			sb.append(element);
		}
		return sb.toString();
	}

	/**
	 * Retourne la date UTC en string.
	 *
	 * @param date la date.
	 * @return la chaine de caractere formattée.
	 */
	private static String formatDate(final Date date) {
		final DateFormat formatter = new SimpleDateFormat("\"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'\"", Locale.getDefault());
		final TimeZone tz = TimeZone.getTimeZone("UTC");
		formatter.setTimeZone(tz);
		return formatter.format(date);
	}
}
