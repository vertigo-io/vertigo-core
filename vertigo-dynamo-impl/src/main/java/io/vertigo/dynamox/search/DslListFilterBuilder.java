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

import io.vertigo.commons.parser.ManyRule;
import io.vertigo.commons.parser.NotFoundException;
import io.vertigo.commons.parser.Parser;
import io.vertigo.commons.parser.Rule;
import io.vertigo.dynamo.collections.ListFilter;
import io.vertigo.dynamo.collections.metamodel.ListFilterBuilder;
import io.vertigo.dynamox.search.dsl.definition.DslExpressionDefinition;
import io.vertigo.dynamox.search.dsl.definition.DslFieldDefinition;
import io.vertigo.dynamox.search.dsl.definition.DslFixedQueryDefinition;
import io.vertigo.dynamox.search.dsl.definition.DslMultiExpressionDefinition;
import io.vertigo.dynamox.search.dsl.definition.DslMultiFieldDefinition;
import io.vertigo.dynamox.search.dsl.definition.DslMultiQueryDefinition;
import io.vertigo.dynamox.search.dsl.definition.DslQueryDefinition;
import io.vertigo.dynamox.search.dsl.definition.DslRangeQueryDefinition;
import io.vertigo.dynamox.search.dsl.definition.DslTermDefinition;
import io.vertigo.dynamox.search.dsl.definition.DslTermQueryDefinition;
import io.vertigo.dynamox.search.dsl.definition.DslUserCriteria;
import io.vertigo.dynamox.search.dsl.rules.DslMultiExpressionRule;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	/** Default query : #query# .*/
	public static final String DEFAULT_QUERY = "#" + USER_QUERY_KEYWORD + "#";

	private static final Set<String> RESERVED_QUERY_KEYWORDS = new HashSet<>(Arrays.asList(new String[] { "AND", "OR", "and", "or", "And", "Or", "*" }));

	/**
	 * Regexp to parse USER query string.
	 * User can use some elaticSearch syntax + - * ? ~ or and ( ) field: [ to ] { to }
	 * Any use of these commands but and or ( ) desactivate process of the word
	 * Like : Harry~ (Potter or Poter) -azkaban year:1998
	 *
	 * Multiple regexp match a set of 4 groups :
	 *  1 : index field (optional)
	 *  2 : pre-word (optional, non word, non space, non "[{( )
	 *  3 : user word (non space, non "[]{}() )
	 *  4 : post-word (optional, non word, non space, non "]}) )
	 *  OR
	 *  1 : index field (optional)
	 *  2 : "
	 *  3 : anythings
	 *  4 : "
	 *  OR
	 *  1 : index field (optional)
	 *  2 : [ or {
	 *  3 : anythings
	 *  4 : ] or }
	 *  OR
	 *  1 : index field (mandatory)
	 *  2 : (
	 *  3 : anythings
	 *  4 : )
	 */
	private final static String CRITERIA_VALUE_OTHER_FIELD_PATTERN_STRING = "(?:(\\S+:)(\\()([^\\\"]*)(\\)))"; //attention a bien avoir 4 groups
	private final static String CRITERIA_VALUE_QUOTED_PATTERN_STRING = "(?:(\\S+:)?(\\\")([^\\\"]*)(\\\"))";
	private final static String CRITERIA_VALUE_RANGE_PATTERN_STRING = "(?:(\\S+:)?([\\[\\{])([^\\]\\}]*)([\\]\\}]))";
	private final static String CRITERIA_VALUE_STAR_PATTERN_STRING = "(?:(\\S+:)?(^|[\\s]*)(\\*)($|[\\s]+))";
	//private final static String WORD_RESERVERD_PATTERN = "\\s\\+\\-\\=\\&\\|\\>\\<\\!\\(\\)\\{\\}\\[\\]\\^\\\"\\~\\*\\?\\:\\/\\\\";
	//private final static String PREFIX_RESERVERD_PATTERN = "^\\s\\\"\\[\\{\\]\\}():,";
	//private final static String SUFFIX_RESERVERD_PATTERN = "^\\s\\\"\\[\\{\\]\\}():,";
	//\p{Punct}:  !"#$%&'()*+,-./:;<=>?@[\]^_`{|}~
	private final static String WORD_RESERVERD_PATTERN = "^\\s!\"#$%&'()*+,-./:;<=>?@[\\\\]^`{|}~"; //Punct sauf _
	private final static String PREFIX_RESERVERD_PATTERN = "\\+\\-\\!\\*\\?\\~\\^\\=\\>\\<";
	private final static String SUFFIX_RESERVERD_PATTERN = "\\+\\-\\!\\*\\?\\~\\^\\=\\>\\<";
	//private final static String NOT_WORD_PATTERN = "\\s\\\"\\[\\{\\]\\}():";
	//private final static String CRITERIA_VALUE_WORD_PATTERN_STRING = "(?:(\\S+:)?([^\\w" + NOT_WORD_PATTERN + "]*)([^" + NOT_WORD_PATTERN + "]+)([^\\w" + NOT_WORD_PATTERN + "]*))";
	private final static String CRITERIA_VALUE_WORD_PATTERN_STRING = "(?:(\\S+:)?([" + PREFIX_RESERVERD_PATTERN + "]*?)([" + WORD_RESERVERD_PATTERN + "]+)((?:[\\^\\~][0-9]+)|(?:[" + SUFFIX_RESERVERD_PATTERN + "]*)))";
	private final static String CRITERIA_VALUE_PATTERN_STRING = "(?:((?:\\s|^).*?)?)(?:" //group 1
			+ CRITERIA_VALUE_OTHER_FIELD_PATTERN_STRING // group 2-5
			+ "|" + CRITERIA_VALUE_QUOTED_PATTERN_STRING // group 6-9
			+ "|" + CRITERIA_VALUE_RANGE_PATTERN_STRING // group 10-13
			+ "|" + CRITERIA_VALUE_STAR_PATTERN_STRING // group 14-17
			+ "|" + CRITERIA_VALUE_WORD_PATTERN_STRING // group 18-21
			+ ")(\\S*)"; // group 22
	private final static Pattern CRITERIA_VALUE_PATTERN = Pattern.compile(CRITERIA_VALUE_PATTERN_STRING);
	private static final String QUERY_RESERVERD_PATTERN = "\\+\\-\\!\\*\\?\\~\\^\\=\\>\\<\\s";

	private List<DslMultiExpressionDefinition> myBuildQuery;
	//private DslExpressionDefinition myBuildQuery;
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
			final Rule<DslMultiExpressionDefinition> expressionsRule = new DslMultiExpressionRule(0);
			final ManyRule<DslMultiExpressionDefinition> many = new ManyRule<>(expressionsRule, false, true); //repeat true => on veut tout la chaine
			final Parser<List<DslMultiExpressionDefinition>> parser = many.createParser();
			parser.parse(buildQuery, 0);
			myBuildQuery = parser.get();
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
		//appendExpression(query, myBuildQuery);
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
		flushSubQueryToQuery(query, multiExpressionDefinition.getPreMultiExpression(), multiExpressionDefinition.getPostMultiExpression(), multiExpressionDefinition.isBlock(), multiExpressionQuery);
	}

	private void appendExpression(final StringBuilder query, final DslExpressionDefinition expressionDefinition) {
		final StringBuilder expressionQuery = new StringBuilder();
		//		if(expressionDefinition.getMultiField().isDefined()) {
		//			final DslMultiFieldDefinition multiFieldDefinition = expressionDefinition.getMultiField().get();
		//			final DslExpressionDefinition multiFieldExpressionDefinition = new DslExpressionDefinition(expressionDefinition.getPreExpression()+multiFieldDefinition.getPreMultiField(), )
		//			final DslFieldDefinition monoFieldDefinition = new DslFieldDefinition(
		//					firstNotEmpty(dslFieldDefinition.getPreField(), dslMultiFieldDefinition.getPreMultiField()),
		//					dslFieldDefinition.getFieldName(),
		//					firstNotEmpty(dslFieldDefinition.getPostField(), dslMultiFieldDefinition.getPostMultiField()));
		//			final DslExpressionDefinition monoFieldExpressionDefinition = new DslExpressionDefinition(
		//					null,
		//					Option.some(monoFieldDefinition), Option.<DslMultiFieldDefinition> none(), null,
		//					null);
		//
		//			final DslMultiExpressionDefinition monoFieldMultiExpressionDefinition = new DslMultiExpressionDefinition(
		//					firstNotEmpty(userCriteria.getOverridedPreModifier(), dslTermDefinition.getPreTerm()), true,
		//					Collections.singletonList(monoFieldExpressionDefinition), Collections.<DslMultiExpressionDefinition> emptyList(),
		//					firstNotEmpty(userCriteria.getOverridedPostModifier(), dslTermDefinition.getPreTerm()));
		//		} else {
		final DslQueryDefinition dslQueryDefinition = expressionDefinition.getQuery();
		appendQuery(query, expressionDefinition, expressionQuery, dslQueryDefinition);
		flushExpressionToQuery(query, expressionDefinition, expressionQuery);
		//		}
	}

	private static void flushExpressionToQuery(final StringBuilder query, final DslExpressionDefinition expressionDefinition, final StringBuilder expressionQuery) {
		if (expressionQuery.length() > 0) {
			final String[] trimedExpression = splitTrimedSubQueryToQuery(expressionQuery.toString());
			appendIfNotNull(query, trimedExpression[0]);
			appendIfNotNull(query, expressionDefinition.getPreExpression());
			if (expressionDefinition.getField().isDefined()) {
				appendField(query, expressionDefinition.getField().get());
			}
			final boolean useBlock = mayUseBlock(trimedExpression[1]);

			query.append(useBlock ? "(" : "");
			query.append(trimedExpression[1]);
			query.append(useBlock ? ")" : "");

			appendIfNotNull(query, expressionDefinition.getPostExpression());
			expressionQuery.setLength(0);
		}
	}

	private static boolean mayUseBlock(final String trimedExpression) {
		//on place des parenthèses s'il n'y a pas encore de block, ou des caractères interdits
		return !trimedExpression.matches("((\\(.*\\))|(\\[.*\\])|(\\\".*\\\")|\\*)(\\^[0-9]+)?")
				&& trimedExpression.matches(".*[" + QUERY_RESERVERD_PATTERN + "].*");

	}

	private static void flushSubQueryToQuery(final StringBuilder query, final String preExpression, final String postExpression, final boolean useBlock, final StringBuilder subQuery) {
		if (subQuery.length() > 0) {
			final String[] trimedQuery = splitTrimedSubQueryToQuery(subQuery.toString());
			query.append(trimedQuery[0]);
			appendIfNotNull(query, preExpression);
			query.append(useBlock ? "(" : "");
			query.append(trimedQuery[1]);
			query.append(useBlock ? ")" : "");
			appendIfNotNull(query, postExpression);
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
		appendIfNotNull(query, dslFieldDefinition.getPreField());
		query.append(dslFieldDefinition.getFieldName());
		appendIfNotNull(query, dslFieldDefinition.getPostField());
		query.append(":");
	}

	private void appendMultiQuery(final StringBuilder query, final DslMultiQueryDefinition dslMultiQueryDefinition, final DslExpressionDefinition expressionDefinition, final StringBuilder parentQuery) {
		final StringBuilder expressionMultiQuery = new StringBuilder();
		for (final DslQueryDefinition dslQueryDefinition : dslMultiQueryDefinition.getQueries()) {
			appendQuery(parentQuery, expressionDefinition, expressionMultiQuery, dslQueryDefinition);
		}
		flushSubQueryToQuery(query, dslMultiQueryDefinition.getPreMultiQuery(), dslMultiQueryDefinition.getPostMultiQuery(), true, expressionMultiQuery);
	}

	private void appendQuery(final StringBuilder query, final DslExpressionDefinition expressionDefinition, final StringBuilder expressionQuery, final DslQueryDefinition dslQueryDefinition) {
		if (dslQueryDefinition instanceof DslTermQueryDefinition) {
			appendTermQuery(expressionQuery, (DslTermQueryDefinition) dslQueryDefinition, expressionDefinition, query);
		} else if (dslQueryDefinition instanceof DslMultiQueryDefinition) {
			appendMultiQuery(expressionQuery, (DslMultiQueryDefinition) dslQueryDefinition, expressionDefinition, query);
		} else if (dslQueryDefinition instanceof DslRangeQueryDefinition) {
			appendRangeQuery(expressionQuery, (DslRangeQueryDefinition) dslQueryDefinition, expressionDefinition);
		} else if (dslQueryDefinition instanceof DslFixedQueryDefinition) {
			appendFixedQuery(expressionQuery, (DslFixedQueryDefinition) dslQueryDefinition);
		}
	}

	private void appendTermQuery(final StringBuilder query, final DslTermQueryDefinition dslQueryDefinition, final DslExpressionDefinition expressionDefinition, final StringBuilder outExpressionQuery) {
		final String fieldName = dslQueryDefinition.getTerm().getTermField();
		final Object value;
		if (USER_QUERY_KEYWORD.equalsIgnoreCase(fieldName)) {
			value = cleanUserCriteria(myCriteria.toString());
		} else {
			value = cleanUserCriteria(BeanUtil.getValue(myCriteria, fieldName));
		}
		appendTermQueryWithValue(value, query, dslQueryDefinition, expressionDefinition, outExpressionQuery);
	}

	private void appendTermQueryWithValue(final Object value, final StringBuilder query, final DslTermQueryDefinition dslQueryDefinition, final DslExpressionDefinition expressionDefinition, final StringBuilder outExpressionQuery) {
		final boolean useBlock;
		final StringBuilder queryPart = new StringBuilder();
		if (value instanceof String) { //so not null too
			useBlock = appendUserStringCriteria(queryPart, dslQueryDefinition.getTerm(), dslQueryDefinition, expressionDefinition, (String) value, outExpressionQuery);
		} else if (value instanceof Date) { //so not null too
			useBlock = appendSimpleCriteria(queryPart, dslQueryDefinition.getTerm(), formatDate((Date) value));
		} else if (value != null) {
			useBlock = appendSimpleCriteria(queryPart, dslQueryDefinition.getTerm(), value.toString());
		} else if (dslQueryDefinition.getTerm().getDefaultValue().isDefined()) { //if value null => defaultValue
			useBlock = appendSimpleCriteria(queryPart, dslQueryDefinition.getTerm(), dslQueryDefinition.getTerm().getDefaultValue().get());
		} else {
			useBlock = false;
		}
		flushSubQueryToQuery(query, dslQueryDefinition.getPreQuery(), dslQueryDefinition.getPostQuery(), useBlock, queryPart);
		//if defaultValue null => no criteria
	}

	private void appendFixedQuery(final StringBuilder query, final DslFixedQueryDefinition dslQueryDefinition) {
		appendIfNotNull(query, dslQueryDefinition.getFixedQuery());
	}

	private void appendRangeQuery(final StringBuilder query, final DslRangeQueryDefinition dslQueryDefinition, final DslExpressionDefinition expressionDefinition) {
		final DslQueryDefinition startQueryDefinition = dslQueryDefinition.getStartQueryDefinitions();
		final DslQueryDefinition endQueryDefinition = dslQueryDefinition.getEndQueryDefinitions();
		final StringBuilder startRangeQuery = new StringBuilder();
		if (startQueryDefinition instanceof DslTermQueryDefinition) {
			appendTermQuery(startRangeQuery, (DslTermQueryDefinition) startQueryDefinition, expressionDefinition, null);
		} else if (startQueryDefinition instanceof DslFixedQueryDefinition) {
			appendFixedQuery(startRangeQuery, (DslFixedQueryDefinition) startQueryDefinition);
		}
		final StringBuilder endRangeQuery = new StringBuilder();
		if (endQueryDefinition instanceof DslTermQueryDefinition) {
			appendTermQuery(endRangeQuery, (DslTermQueryDefinition) endQueryDefinition, expressionDefinition, null);
		} else if (endQueryDefinition instanceof DslFixedQueryDefinition) {
			appendFixedQuery(endRangeQuery, (DslFixedQueryDefinition) endQueryDefinition);
		}

		//flush Range Query
		final String startRangeStr = startRangeQuery.length() > 0 ? startRangeQuery.toString() : "*";
		final String endRangeStr = endRangeQuery.length() > 0 ? endRangeQuery.toString() : "*";

		if (!"*".equals(startRangeStr) || !"*".equals(endRangeStr)) {
			appendIfNotNull(query, dslQueryDefinition.getPreRangeQuery());
			query.append('[');
			query.append(startRangeStr);
			query.append(" to ");
			query.append(endRangeStr);
			query.append(']');
			appendIfNotNull(query, dslQueryDefinition.getPostRangeQuery());
		}
	}

	private <O> O cleanUserCriteria(final O value) {
		if (value instanceof String && ((String) value).trim().isEmpty()) { //so not null too
			return (O) "*";
		}
		return value;
	}

	/*private void appendRangeExpression(final StringBuilder query, final String indexFieldName, final String preExpression, final String fieldExpression, final String postExpression, final String defaultValue, final String rangeEndFieldExpression, final String rangeDefaultValue) {
		if (defaultValue == null && (fieldExpression == null || getFieldValue(fieldExpression) == null)
				&& rangeDefaultValue == null && (rangeEndFieldExpression == null || getFieldValue(rangeEndFieldExpression) == null)) {
			//if nothing null => no criteria
			return;
		}
		appendFieldExpression(query, indexFieldName, preExpression, fieldExpression, null, defaultValue != null ? defaultValue : "*");
		appendFieldExpression(query, " to ", null, rangeEndFieldExpression, postExpression, rangeDefaultValue != null ? rangeDefaultValue : "*");
	}

	private void appendFieldExpression(final StringBuilder query, final String indexFieldName, final String preExpression, final String fieldExpression, final String postExpression, final String defaultValue) {
		if (fieldExpression != null) {
			final Matcher expressionMatcher = FIELD_EXPRESSION_PATTERN.matcher(fieldExpression);
			Assertion.checkArgument(expressionMatcher.matches(), "BuildQuery syntax error, field ({0}) in query ({1}) should match a criteria fieldName", fieldExpression, myBuildQuery);
			//-----
			final String preFieldModifier = expressionMatcher.group(1);
			final String fieldName = expressionMatcher.group(2);
			final String postFieldModifier = expressionMatcher.group(3);

			final Object value;
			if (USER_QUERY_KEYWORD.equalsIgnoreCase(fieldName)) {
				value = myCriteria.toString();
			} else {
				value = BeanUtil.getValue(myCriteria, fieldName);
			}
			if (value instanceof String) { //so not null too
				appendUserStringCriteria(query, indexFieldName, preExpression, postExpression, preFieldModifier, postFieldModifier, (String) value, defaultValue);
			} else if (value instanceof Date) { //so not null too
				appendSimpleCriteria(query, indexFieldName, preExpression, postExpression, preFieldModifier, postFieldModifier, formatDate((Date) value));
			} else if (value != null) {
				appendSimpleCriteria(query, indexFieldName, preExpression, postExpression, preFieldModifier, postFieldModifier, value.toString());
			} else if (defaultValue != null) { //if value null => defaultValue
				appendSimpleCriteria(query, indexFieldName, preExpression, postExpression, null, null, defaultValue);
			}
			//if defaultValue null => no criteria
		} else {
			//no fieldExpression : fixed param
			appendIfNotNull(query, indexFieldName);
			appendIfNotNull(query, preExpression);
			appendIfNotNull(query, postExpression);
		}
	}

	private Object getFieldValue(final String fieldExpression) {
		final Matcher expressionMatcher = FIELD_EXPRESSION_PATTERN.matcher(fieldExpression);
		Assertion.checkArgument(expressionMatcher.matches(), "BuildQuery syntax error, field ({0}) in query ({1}) should match a criteria fieldName", fieldExpression, myBuildQuery);
		//-----
		final String fieldName = expressionMatcher.group(2);
		if (USER_QUERY_KEYWORD.equalsIgnoreCase(fieldName)) {
			return myCriteria.toString();
		}
		return BeanUtil.getValue(myCriteria, fieldName);
	}*/

	private boolean appendSimpleCriteria(final StringBuilder query, final DslTermDefinition dslTermDefinition, final String value) {
		appendIfNotNull(query, dslTermDefinition.getPreTerm());
		appendIfNotNull(query, value);
		appendIfNotNull(query, dslTermDefinition.getPostTerm());
		return false; //never use block
	}

	private boolean appendUserStringCriteria(final StringBuilder query, final DslTermDefinition dslTermDefinition, final DslTermQueryDefinition dslQueryDefinition, final DslExpressionDefinition expressionDefinition, final String userString, final StringBuilder outExpressionQuery) {
		final List<DslUserCriteria> userCriteriaList = parseUserCriteria(userString);

		int criteriaOnDefinitionField = 0; //On compte les fields sur le field de la definition. Si >1 on mettra des ( )
		for (final DslUserCriteria userCriteria : userCriteriaList) {
			final String criteriaValue = userCriteria.getCriteriaWord();
			if (criteriaValue != null) {
				/*if (expressionDefinition.getMultiField().isDefined()) {
					final DslMultiFieldDefinition dslMultiFieldDefinition = expressionDefinition.getMultiField().get();
					final StringBuilder wordCriteriaQuery = new StringBuilder();
					appendIfNotNull(wordCriteriaQuery, userCriteria.getPreMissingPart());
					String sep = "";
					for (final DslFieldDefinition dslFieldDefinition : dslMultiFieldDefinition.getFields()) {
						final StringBuilder monoFieldQuery = new StringBuilder();
						appendIfNotNull(wordCriteriaQuery, sep);
						if (RESERVED_QUERY_KEYWORDS.contains(criteriaValue)) {
							appendIfNotNull(monoFieldQuery, criteriaValue);
						} else {
							//appendIfNotNull(monoFieldQuery, userCriteria.getOverridedPreModifier().isEmpty() ? dslTermDefinition.getPreTerm() : userCriteria.getOverridedPreModifier());
							appendIfNotNull(monoFieldQuery, criteriaValue);
							appendIfNotNull(monoFieldQuery, userCriteria.getOverridedPostModifier().isEmpty() ? dslTermDefinition.getPostTerm() : userCriteria.getOverridedPostModifier());
						}
						flushSubQueryToQuery(wordCriteriaQuery, dslFieldDefinition.getFieldName() + ":", dslFieldDefinition.getPostField(), mayUseBlock(monoFieldQuery.toString()), monoFieldQuery);
						sep = " ";
					}
					appendIfNotNull(outExpressionQuery, userCriteria.getPostMissingPart());
					flushSubQueryToQuery(outExpressionQuery,
							userCriteria.getOverridedPreModifier().isEmpty() ? dslTermDefinition.getPreTerm() : userCriteria.getOverridedPreModifier(),
							dslQueryDefinition.getPostQuery(), true, wordCriteriaQuery); */
				if (userCriteria.getOverridedFieldName() != null) {
					//si le field est surchargé on flush l'expression précédente
					flushExpressionToQuery(outExpressionQuery, expressionDefinition, query);
					criteriaOnDefinitionField = 0;
					//et on ajout la requete sur l'autre champs
					appendIfNotNull(outExpressionQuery, userCriteria.getPreMissingPart());
					outExpressionQuery.append(userCriteria.getOverridedFieldName());
					appendIfNotNull(outExpressionQuery, userCriteria.getOverridedPreModifier());
					appendIfNotNull(outExpressionQuery, criteriaValue);
					appendIfNotNull(outExpressionQuery, userCriteria.getOverridedPostModifier());
					appendIfNotNull(outExpressionQuery, expressionDefinition.getPostExpression());
					appendIfNotNull(outExpressionQuery, userCriteria.getPostMissingPart());
				} else if (expressionDefinition.getMultiField().isDefined()) {
					criteriaOnDefinitionField++;
					final DslMultiFieldDefinition dslMultiFieldDefinition = expressionDefinition.getMultiField().get();
					appendIfNotNull(query, userCriteria.getPreMissingPart());
					final List<DslExpressionDefinition> monoFieldExpressionDefinitions = new ArrayList<>();
					for (final DslFieldDefinition dslFieldDefinition : dslMultiFieldDefinition.getFields()) {
						final DslFieldDefinition monoFieldDefinition = new DslFieldDefinition(
								firstNotEmpty(dslFieldDefinition.getPreField(), dslMultiFieldDefinition.getPreMultiField()),
								dslFieldDefinition.getFieldName(),
								"");
						final DslExpressionDefinition monoFieldExpressionDefinition = new DslExpressionDefinition(
								monoFieldExpressionDefinitions.size() == 0 ? "" : " ",
								Option.some(monoFieldDefinition), Option.<DslMultiFieldDefinition> none(),
								new DslFixedQueryDefinition(concat(criteriaValue, firstNotEmpty(userCriteria.getOverridedPostModifier(), dslTermDefinition.getPostTerm()))),
								firstNotEmpty(dslFieldDefinition.getPostField(), dslMultiFieldDefinition.getPostMultiField()));
						monoFieldExpressionDefinitions.add(monoFieldExpressionDefinition);
					}
					final DslMultiExpressionDefinition monoFieldMultiExpressionDefinition = new DslMultiExpressionDefinition(
							firstNotEmpty(userCriteria.getOverridedPreModifier(), dslTermDefinition.getPreTerm()), true,
							monoFieldExpressionDefinitions, Collections.<DslMultiExpressionDefinition> emptyList(),
							"");

					appendMultiExpression(query, monoFieldMultiExpressionDefinition);
					appendIfNotNull(query, userCriteria.getPostMissingPart());

				} else {
					criteriaOnDefinitionField++;
					appendIfNotNull(query, userCriteria.getPreMissingPart());
					if (RESERVED_QUERY_KEYWORDS.contains(criteriaValue)) {
						appendIfNotNull(query, criteriaValue);
					} else {
						appendIfNotNull(query, userCriteria.getOverridedPreModifier().isEmpty() ? dslTermDefinition.getPreTerm() : userCriteria.getOverridedPreModifier());
						appendIfNotNull(query, criteriaValue);
						appendIfNotNull(query, userCriteria.getOverridedPostModifier().isEmpty() ? dslTermDefinition.getPostTerm() : userCriteria.getOverridedPostModifier());
					}
					appendIfNotNull(query, userCriteria.getPostMissingPart());
				}
			}
		}
		return criteriaOnDefinitionField > 1; //useBlock if more than 1 criteria
	}

	private static String firstNotEmpty(final String... elements) {
		for (final String element : elements) {
			if (element != null && !element.isEmpty()) {
				return element;
			}
		}
		return null;
	}

	/**
	 * Concat nullable elements.
	 * @param elements Nullable elements
	 * @return Concat string
	 */
	static String concat(final String... elements) {
		boolean allNull = true;
		final StringBuilder sb = new StringBuilder();
		for (final String element : elements) {
			if (element != null) {
				allNull = false;
				sb.append(element);
			}
		}
		return allNull ? null : sb.toString();
	}

	private static List<DslUserCriteria> parseUserCriteria(final String userString) {
		final List<DslUserCriteria> userCriteria = new ArrayList<>();
		//split space chars to add preModifier and postModifier
		final Matcher criteriaValueMatcher = CRITERIA_VALUE_PATTERN.matcher(userString);
		while (criteriaValueMatcher.find()) {
			final String preMissingPart = criteriaValueMatcher.group(1);
			final String postMissingPart = criteriaValueMatcher.group(22);
			//les capturing groups matchs par group de 4, on cherche le premier qui match 4 par 4
			//on se base sur le overridedPreModifier qui n'est jamais null mais vide si match
			int foundGroup = 2;
			for (int i = 0; i < criteriaValueMatcher.groupCount() / 4; i++) {
				if (criteriaValueMatcher.group(i * 4 + 3) != null) {
					foundGroup = i * 4 + 2;
					break; //found !!
				}
			}
			final String overridedFieldName = criteriaValueMatcher.group(foundGroup);
			final String overridedPreModifier = criteriaValueMatcher.group(foundGroup + 1);
			final String criteriaValue = criteriaValueMatcher.group(foundGroup + 2);
			final String overridedPostModifier = criteriaValueMatcher.group(foundGroup + 3);
			userCriteria.add(new DslUserCriteria(preMissingPart, overridedFieldName, overridedPreModifier, criteriaValue, overridedPostModifier, postMissingPart));
		}
		return userCriteria;
	}

	/*private static void appendTermValue(final StringBuilder query, final StringBuilder queryPart, final String criteriaValue,
			final DslTermDefinition dslTermDefinition, final DslTermQueryDefinition dslQueryDefinition, final DslExpressionDefinition expressionDefinition,
			final String overridedFieldName, final String overridedPreModifier, final String overridedPostModifier,
			final String preMissingPart, final String postMissingPart, final int foundGroup) {
		if (overridedFieldName != null) {
			//si le field est surchargé on flush l'expression précédente
			appendMissingPart(queryPart, query, postMissingPart);
			flushSubQueryToQuery(query, dslQueryDefinition.getPreQuery(), dslQueryDefinition.getPostQuery(), false, queryPart);
			appendMissingPart(queryPart, query, preMissingPart);
			//et on ajout la requete sur l'autre champs
			flushExpressionValueToQuery(query, overridedFieldName, overridedPreModifier, overridedPostModifier, new StringBuilder(criteriaValue));
		} else if (RESERVED_QUERY_KEYWORDS.contains(criteriaValue)) {
			appendMissingPart(queryPart, queryPart, preMissingPart);
			appendMissingPart(queryPart, queryPart, postMissingPart);
			appendIfNotNull(queryPart, criteriaValue);
		} else if (foundGroup == 13 && !"*:".equals(postMissingPart)) { //case of *:* and *, maybe better tested..
			appendMissingPart(queryPart, query, preMissingPart);
			appendMissingPart(queryPart, queryPart, postMissingPart);
			appendIfNotNull(queryPart, criteriaValue);
		} else {
			appendMissingPart(queryPart, query, preMissingPart);
			appendMissingPart(queryPart, queryPart, postMissingPart);
			appendIfNotNull(queryPart, overridedPreModifier.isEmpty() ? dslTermDefinition.getPreTerm() : overridedPreModifier);
			appendIfNotNull(queryPart, criteriaValue);
			appendIfNotNull(queryPart, overridedPostModifier.isEmpty() ? dslTermDefinition.getPostTerm() : overridedPostModifier);
		}
	}*/

	/*private static String cleanUserQuery(final String value, final String defaultValue) {
		if (value.trim().isEmpty()) {
			return defaultValue;
		}
		return value;
	}*/

	/*private static void flushExpressionValueToQuery(final StringBuilder query, final String indexFieldName, final String preExpression, final String postExpression, final StringBuilder expressionValue) {
		if (expressionValue.length() > 0) {
			final boolean useParenthesis = expressionValue.length() > 1 && (
					(indexFieldName != null && !indexFieldName.isEmpty())
							|| (preExpression != null && !preExpression.isEmpty())
							|| (postExpression != null && !postExpression.isEmpty()));
			appendIfNotNull(query, indexFieldName);
			if (indexFieldName != null) {
				query.append(':');
			}
			appendIfNotNull(query, preExpression);
			if (useParenthesis) {
				query.append('(');
			}
			query.append(expressionValue.toString());
			if (useParenthesis) {
				query.append(')');
			}
			appendIfNotNull(query, postExpression);
			expressionValue.setLength(0); //on la remet à 0
		}
	}*/

	private static StringBuilder appendIfNotNull(final StringBuilder query, final String str) {
		if (str != null) {
			query.append(str);
		}
		return query;
	}

	/*private static void appendMissingPart(final StringBuilder queryPart, final StringBuilder query, final String missingPart) {
		if (missingPart != null && !missingPart.isEmpty()) {
			if (queryPart.length() > 0) {
				queryPart.append(missingPart);
			} else {
				query.append(missingPart);
			}
		}
	}*/

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
