/*
 * vertigo - application development platform
 *
 * Copyright (C) 2013-2024, Vertigo.io, team@vertigo.io
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
package io.vertigo.core.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.VSystemException;

/**
 * Implements parsing of a date expression.
 * y=year, M=month, w=week
 * d=day, h=hour, m=minute, s= second
 * Mind the UpperCase : 'M'onth and 'm'inute !
 * now+1d
 * now-6d
 * now+2w
 * now-12M
 * now-2y
 * "06/12/2003", "dd/MM/yyyy"
 *
 * @author mlaroche
 */
final class DateQueryParserUtil {
	private static final Map<String, Integer> CALENDAR_UNITS_INSTANT = createCalendarUnitsInstant();
	private static final Map<String, Integer> CALENDAR_UNITS_LOCAL_DATE = createCalendarUnitsLocalDate();
	private static final Pattern PATTERN_INSTANT = Pattern.compile("([0-9]{1,})([y,M, w,d,h,m,s]{1})");
	private static final Pattern PATTERN_LOCAL_DATE = Pattern.compile("([0-9]{1,})([y,M, w,d]{1})");
	private static final String NOW = "now";

	private DateQueryParserUtil() {
		//private
	}

	private static Map<String, Integer> createCalendarUnitsInstant() {
		return Map.of(
				"y", Calendar.YEAR,
				"M", Calendar.MONTH,
				"w", Calendar.WEEK_OF_YEAR,
				"d", Calendar.DAY_OF_YEAR,
				"h", Calendar.HOUR_OF_DAY,
				"m", Calendar.MINUTE,
				"s", Calendar.SECOND);
	}

	private static Map<String, Integer> createCalendarUnitsLocalDate() {
		return Map.of(
				"y", Calendar.YEAR,
				"M", Calendar.MONTH,
				"w", Calendar.WEEK_OF_YEAR,
				"d", Calendar.DAY_OF_YEAR);
	}

	/**
	 * Retourne la date correspondant à l'expression passée en parametre.
	 * La syntaxe est de type now((+/-)eeeUNIT) ou une date heure GMT au format dd/MM/yy hh:mm:ss
	 *
	 * @param dateExpression Expression
	 * @param datePattern Pattern used to define a date (dd/MM/YYYY hh:mm:ss)
	 * @return date
	 */
	static Instant parseAsInstant(final String dateExpression, final String datePattern) {
		Assertion.check()
				.isNotBlank(dateExpression)
				.isNotBlank(datePattern, "you must define a valid datePattern such as dd/MM/yyyy hh:mm or MM/dd/yy hh:mm:ss")
				.isTrue(dateExpression.startsWith(NOW), "Instant evaluation is always relative to now");
		//---
		if (NOW.equals(dateExpression)) {
			//today is gonna be the day
			return Instant.now();
		}
		if (dateExpression.startsWith(NOW)) {
			final int index = NOW.length();
			final char operator = dateExpression.charAt(index);
			//---
			//operand = 21d
			final String operand = dateExpression.substring(index + 1);
			//NOW+21DAY or NOW-12MONTH
			final Matcher matcher = PATTERN_INSTANT.matcher(operand);
			Assertion.check()
					.isTrue(matcher.matches(), "Le second operande ne respecte pas le pattern {0}", PATTERN_INSTANT.toString());
			//---
			final String calendarUnit = matcher.group(2);
			//We check that we have found a real unit Calendar and not 'NOW+15DAL'
			if (!CALENDAR_UNITS_INSTANT.containsKey(calendarUnit)) {
				throw new VSystemException("unit '" + calendarUnit + "' is not allowed. You must use a unit among : " + CALENDAR_UNITS_INSTANT.keySet());
			}
			//---
			final Calendar calendar = new GregorianCalendar();
			final int sign = buildSign(dateExpression, operator);
			final int unitCount = sign * Integer.parseInt(matcher.group(1));
			calendar.add(CALENDAR_UNITS_INSTANT.get(calendarUnit), unitCount);
			return calendar.toInstant();
		}
		return LocalDateTime.parse(dateExpression, DateTimeFormatter.ofPattern(datePattern)).toInstant(ZoneOffset.UTC);
	}

	static LocalDate parseAsLocalDate(final String dateExpression, final String datePattern) {
		Assertion.check()
				.isNotBlank(dateExpression)
				.isNotBlank(datePattern, "you must define a valid datePattern such as dd/MM/yyyy or MM/dd/yy")
				.isFalse(datePattern.contains("H") || datePattern.contains("m"), "LocalDate evaluation cannot contain HH ou mm in the pattern");
		//---
		if (NOW.equals(dateExpression)) {
			//today is gonna be the day
			return LocalDate.now();
		}
		if (dateExpression.startsWith(NOW)) {
			final int index = NOW.length();
			final char operator = dateExpression.charAt(index);
			//---
			//operand = 21d
			final String operand = dateExpression.substring(index + 1);
			//NOW+21DAY or NOW-12MONTH
			final Matcher matcher = PATTERN_LOCAL_DATE.matcher(operand);
			Assertion.check()
					.isTrue(matcher.matches(), "Le second operande ne respecte pas le pattern {0}", PATTERN_LOCAL_DATE.toString());
			//---
			final String calendarUnit = matcher.group(2);
			//We check that we have found a real unit Calendar and not 'NOW+15DAL'
			if (!CALENDAR_UNITS_LOCAL_DATE.containsKey(calendarUnit)) {
				throw new VSystemException("unit '" + calendarUnit + "' is not allowed. You must use a unit among : " + CALENDAR_UNITS_LOCAL_DATE.keySet());
			}
			//---
			final Calendar calendar = new GregorianCalendar();
			final int sign = buildSign(dateExpression, operator);
			final int unitCount = sign * Integer.parseInt(matcher.group(1));
			calendar.add(CALENDAR_UNITS_LOCAL_DATE.get(calendarUnit), unitCount);
			return LocalDate.of(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
		}
		return LocalDate.parse(dateExpression, DateTimeFormatter.ofPattern(datePattern));
	}

	private static int buildSign(final String dateExpression, final char operator) {
		return switch (operator) {
			case '+' -> 1;
			case '-' -> -1;
			default -> throw new VSystemException("a valid operator (+ or -) is expected :'{0}' on {1}", operator, dateExpression);
		};
	}
}
