package io.vertigo.dynamox.search;

import io.vertigo.dynamo.collections.ListFilter;
import io.vertigo.dynamo.collections.metamodel.ListFilterBuilder;
import io.vertigo.lang.Assertion;
import io.vertigo.util.BeanUtil;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
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
public final class DefaultListFilterBuilder<C> implements ListFilterBuilder<C> {

	private static final String USER_QUERY_KEYWORD = "query";
	/** Default query : #query# .*/
	public static final String DEFAULT_QUERY = "#" + USER_QUERY_KEYWORD + "#";

	private static final Set<String> RESERVED_QUERY_KEYWORDS = new HashSet<>(Arrays.asList(new String[] { "AND", "OR", "and", "or", "And", "Or" }));

	/**
	 * Match the listFilterBuilderQuery declared in KSP.
	 * Groups regExp=>
	 *   1: index field (optional)
	 *   2: pre-expression value
	 *   3: field expression value (optional)
	 *   4: post-expression value
	 *   5: separator value
	 */
	private final static String QUERY_PATTERN_STRING = "(\\S+:)?([^\\s#]*)(?:#(\\S+)#)?([^\\s#]*)(\\s|$)+";
	private final static Pattern QUERY_PATTERN = Pattern.compile(QUERY_PATTERN_STRING);

	private final static String FULL_QUERY_PATTERN_STRING = "^(?:" + QUERY_PATTERN_STRING + ")*";
	private final static Pattern FULL_QUERY_PATTERN = Pattern.compile(FULL_QUERY_PATTERN_STRING);

	/**
	 * Match the field expression inner the listFilterBuilderQuery declared in KSP.
	 * Groups :
	 *  1 : pre-fieldName (optional, non word)
	 *  2 : fieldName (word)
	 *  3 : post-fieldName (optional,non word)
	 */
	private final static String FIELD_EXPRESSION_PATTERN_STRING = "(\\W*)(\\w+)(\\W*[0-9]*)"; //note: expressions must already match \\S non-whitespace from QUERY_PATTERN
	private final static Pattern FIELD_EXPRESSION_PATTERN = Pattern.compile(FIELD_EXPRESSION_PATTERN_STRING);

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
	private final static String CRITERIA_VALUE_OTHER_FIELD_PATTERN_STRING = "(?:(\\S+:)\\(()([^\\\"]*)()\\))"; //attention a bien avoir 4 groups
	private final static String CRITERIA_VALUE_QUOTED_PATTERN_STRING = "(?:(\\S+:)?(\\\")([^\\\"]*)(\\\"))";
	private final static String CRITERIA_VALUE_RANGE_PATTERN_STRING = "(?:(\\S+:)?([\\[\\{])([^\\]\\}]*)([\\]\\}]))";
	private final static String CRITERIA_VALUE_STAR_PATTERN_STRING = "(?:([^\\s\\*]+:)?(^|[\\s]*)(\\*)($|[\\s]+))";
	//private final static String WORD_RESERVERD_PATTERN = "\\s\\+\\-\\=\\&\\|\\>\\<\\!\\(\\)\\{\\}\\[\\]\\^\\\"\\~\\*\\?\\:\\/\\\\";
	//private final static String PREFIX_RESERVERD_PATTERN = "^\\s\\\"\\[\\{\\]\\}():,";
	//private final static String SUFFIX_RESERVERD_PATTERN = "^\\s\\\"\\[\\{\\]\\}():,";
	//\p{Punct}:  !"#$%&'()*+,-./:;<=>?@[\]^_`{|}~
	private final static String WORD_RESERVERD_PATTERN = "^\\s\\p{Punct}";
	private final static String PREFIX_RESERVERD_PATTERN = "\\+\\-\\!\\*\\?\\~\\^\\=\\>\\<";
	private final static String SUFFIX_RESERVERD_PATTERN = "\\+\\-\\!\\*\\?\\~\\^\\=\\>\\<";
	//private final static String NOT_WORD_PATTERN = "\\s\\\"\\[\\{\\]\\}():";
	//private final static String CRITERIA_VALUE_WORD_PATTERN_STRING = "(?:(\\S+:)?([^\\w" + NOT_WORD_PATTERN + "]*)([^" + NOT_WORD_PATTERN + "]+)([^\\w" + NOT_WORD_PATTERN + "]*))";
	private final static String CRITERIA_VALUE_WORD_PATTERN_STRING = "(?:(\\S+:)?([" + PREFIX_RESERVERD_PATTERN + "]*?)([" + WORD_RESERVERD_PATTERN + "]+)((?:[\\^\\~][0-9]+)|(?:[" + SUFFIX_RESERVERD_PATTERN + "]*)))";
	private final static String CRITERIA_VALUE_PATTERN_STRING = "(?:"
			+ CRITERIA_VALUE_OTHER_FIELD_PATTERN_STRING // group 1
			+ "|" + CRITERIA_VALUE_QUOTED_PATTERN_STRING
			+ "|" + CRITERIA_VALUE_RANGE_PATTERN_STRING
			+ "|" + CRITERIA_VALUE_STAR_PATTERN_STRING
			+ "|" + CRITERIA_VALUE_WORD_PATTERN_STRING
			+ ")";
	private final static Pattern CRITERIA_VALUE_PATTERN = Pattern.compile(CRITERIA_VALUE_PATTERN_STRING);

	private String buildQuery;
	private C criteria;

	/**
	 * Fix query pattern.
	 * @param newBuildQuery Pattern (not null, could be empty)
	 * @return this builder
	 */
	@Override
	public ListFilterBuilder<C> withBuildQuery(final String newBuildQuery) {
		Assertion.checkNotNull(newBuildQuery);
		Assertion.checkState(buildQuery == null, "query was already set : {0}", buildQuery);
		Assertion.checkArgument(FULL_QUERY_PATTERN.matcher(newBuildQuery).matches(), "BuildQuery syntax error ({0}), should match : (<indexField:>#criteriaField# )+", newBuildQuery);
		//-----
		this.buildQuery = newBuildQuery;
		return this;
	}

	/**
	 * Fix criteria.
	 * @param newCriteria Criteria
	 * @return this builder
	 */
	@Override
	public ListFilterBuilder<C> withCriteria(final C newCriteria) {
		Assertion.checkNotNull(newCriteria);
		Assertion.checkState(criteria == null, "criteria was already set : {0}", criteria);
		//-----
		this.criteria = newCriteria;
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
		final Matcher queryMatcher = QUERY_PATTERN.matcher(buildQuery);
		while (queryMatcher.find()) {
			final String indexFieldName = queryMatcher.group(1);
			final String preExpression = queryMatcher.group(2);
			final String fieldExpression = queryMatcher.group(3);
			final String postExpression = queryMatcher.group(4);
			final String separator = queryMatcher.group(5);
			//On traite l'expression avant de concaténer, car si le critère est null on retire tout
			appendFieldExpression(query, indexFieldName, preExpression, fieldExpression, postExpression);
			query.append(separator);
		}
		return query.toString();
	}

	private void appendFieldExpression(final StringBuilder query, final String indexFieldName, final String preExpression, final String fieldExpression, final String postExpression) {
		if (fieldExpression != null) {
			final Matcher expressionMatcher = FIELD_EXPRESSION_PATTERN.matcher(fieldExpression);
			Assertion.checkArgument(expressionMatcher.matches(), "BuildQuery syntax error, field ({0}) in query ({1}) should match a criteria fieldName", fieldExpression, buildQuery);
			//-----
			final String preModifier = expressionMatcher.group(1);
			final String fieldName = expressionMatcher.group(2);
			final String postModifier = expressionMatcher.group(3);

			final Object value;
			if (USER_QUERY_KEYWORD.equalsIgnoreCase(fieldName)) {
				value = criteria.toString();
			} else {
				value = BeanUtil.getValue(criteria, fieldName);
			}
			if (value instanceof String) { //so not null too
				appendUserStringCriteria(query, indexFieldName, preExpression, postExpression, preModifier, postModifier, value);
			} else if (value instanceof Date) { //so not null too
				appendSimpleCriteria(query, indexFieldName, preExpression, postExpression, preModifier, postModifier, formatDate((Date) value));
			} else if (value != null) {
				appendSimpleCriteria(query, indexFieldName, preExpression, postExpression, preModifier, postModifier, value.toString());
			}
			//if value null => no criteria (TODO : CHECKME)
		} else {
			//no fieldExpression : fixed param
			appendIfNotNull(query, indexFieldName);
			appendIfNotNull(query, preExpression);
			appendIfNotNull(query, postExpression);
		}
	}

	private void appendSimpleCriteria(final StringBuilder query, final String indexFieldName, final String preExpression, final String postExpression, final String preModifier, final String postModifier, final String value) {
		appendIfNotNull(query, indexFieldName);
		appendIfNotNull(query, preExpression);
		query.append('(');
		appendIfNotNull(query, preModifier);
		appendIfNotNull(query, value);
		appendIfNotNull(query, postModifier);
		query.append(')');
		appendIfNotNull(query, postExpression);
	}

	private void appendUserStringCriteria(final StringBuilder query, final String indexFieldName, final String preExpression, final String postExpression, final String preModifier, final String postModifier, final Object value) {

		final String stringValue = cleanUserQuery((String) value);
		/*if ("*".equals(stringValue)) {
			appendSimpleCriteria(query, indexFieldName != null ? indexFieldName : "*", preExpression, postExpression, "", "", stringValue);
			return;
		}*/
		//split space chars to add preModifier and postModifier
		final Matcher criteriaValueMatcher = CRITERIA_VALUE_PATTERN.matcher(stringValue);
		final StringBuilder expressionValue = new StringBuilder();
		int lastIndex = 0;
		while (criteriaValueMatcher.find()) {
			final int startIndex = criteriaValueMatcher.start();
			final String preMissingPart;
			final String postMissingPart;
			if (startIndex > lastIndex) {
				final String missingPart = stringValue.substring(lastIndex, startIndex);
				if (!missingPart.startsWith(" ")) {
					preMissingPart = "";
					postMissingPart = missingPart;
				} else {
					preMissingPart = missingPart;
					postMissingPart = "";
				}
			} else {
				preMissingPart = "";
				postMissingPart = "";
			}
			lastIndex = criteriaValueMatcher.end();
			//les capturing groups matchs par group de 4, on cherche le premier qui match 4 par 4
			//on se base sur le overridedPreModifier qui n'est jamais null mais vide si match
			int foundGroup = 1;
			for (int i = 0; i < criteriaValueMatcher.groupCount() / 4; i++) {
				if (criteriaValueMatcher.group(i * 4 + 2) != null) {
					foundGroup = i * 4 + 1;
					break; //found !!
				}
			}
			final String overridedFieldName = criteriaValueMatcher.group(foundGroup);
			final String overridedPreModifier = criteriaValueMatcher.group(foundGroup + 1);
			final String criteriaValue = criteriaValueMatcher.group(foundGroup + 2);
			final String overridedPostModifier = criteriaValueMatcher.group(foundGroup + 3);
			if (criteriaValue != null && !criteriaValue.trim().isEmpty()) {
				if (overridedFieldName != null) {
					//si le field est surchargé on flush l'expression précédente

					appendMissingPart(expressionValue, query, postMissingPart);
					flushExpressionValueToQuery(query, indexFieldName, preExpression, postExpression, expressionValue);
					appendMissingPart(expressionValue, query, preMissingPart);
					//et on ajout la requete sur l'autre champs
					appendIfNotNull(query, overridedFieldName);
					query.append('(');
					appendIfNotNull(query, overridedPreModifier); //si le field est surchargé on ne prend pas les pre/postModifier du pattern
					appendIfNotNull(query, criteriaValue);
					appendIfNotNull(query, overridedPostModifier);
					query.append(')');

				} else if (RESERVED_QUERY_KEYWORDS.contains(criteriaValue)) {
					appendMissingPart(expressionValue, expressionValue, preMissingPart);
					appendMissingPart(expressionValue, expressionValue, postMissingPart);
					appendIfNotNull(expressionValue, criteriaValue);
				} else {
					appendMissingPart(expressionValue, query, preMissingPart);
					appendMissingPart(expressionValue, expressionValue, postMissingPart);
					appendIfNotNull(expressionValue, overridedPreModifier.isEmpty() ? preModifier : overridedPreModifier);
					appendIfNotNull(expressionValue, criteriaValue);
					appendIfNotNull(expressionValue, overridedPostModifier.isEmpty() ? postModifier : overridedPostModifier);
				}
			} //else no query
		}
		final String missingPart = stringValue.substring(lastIndex);
		final String preMissingPart;
		final String postMissingPart;
		if (!missingPart.startsWith(" ")) {
			preMissingPart = "";
			postMissingPart = missingPart;
		} else {
			preMissingPart = missingPart;
			postMissingPart = "";
		}

		appendMissingPart(expressionValue, query, preMissingPart);
		appendMissingPart(expressionValue, query, postMissingPart);
		flushExpressionValueToQuery(query, indexFieldName, preExpression, postExpression, expressionValue);
	}

	private String cleanUserQuery(final String value) {
		if (value.trim().isEmpty()) {
			return "*";
		}
		return value;
	}

	private void flushExpressionValueToQuery(final StringBuilder query, final String indexFieldName, final String preExpression, final String postExpression, final StringBuilder expressionValue) {
		if (expressionValue.length() > 0) {
			appendIfNotNull(query, indexFieldName);
			appendIfNotNull(query, preExpression);
			query.append('(');
			query.append(expressionValue.toString());
			query.append(')');
			appendIfNotNull(query, postExpression);
			expressionValue.setLength(0); //on la remet à 0
		}
	}

	private StringBuilder appendIfNotNull(final StringBuilder query, final String str) {
		if (str != null) {
			query.append(str);
		}
		return query;
	}

	private void appendMissingPart(final StringBuilder expressionValue, final StringBuilder query, final String missingPart) {
		if (expressionValue.length() > 0) {
			expressionValue.append(missingPart);
		} else {
			query.append(missingPart);
		}
	}

	/**
	 * Retourne la date UTC en string.
	 *
	 * @param date la date.
	 * @return la chaine de caractere formattée.
	 */
	private String formatDate(final Date date) {
		final DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
		final TimeZone tz = TimeZone.getTimeZone("UTC");
		formatter.setTimeZone(tz);
		return formatter.format(date);
	}
}
