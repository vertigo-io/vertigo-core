package io.vertigo.kernel.util;

import io.vertigo.kernel.lang.Assertion;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implements parsing of a date expression.
 *  NOW+DAY //you can omit 1
 * NOW-DAY //you can omit 1 
 * NOW+1DAY
 * NOW-12MONTH
 * NOW-2YEAR
 * "06/12/2003", "dd/MM/yyyy"
 * 
 * @author mlaroche
 */
final class DateQueryParserUtil {
	private static final Map<String, Integer> CALENDAR_UNITS = createCalendarUnits();
	private final static Pattern PATTERN = Pattern.compile("([0-9]{1,})([A-Z]{1,})");
	private final static String NOW = "NOW";

	private static Map<String, Integer> createCalendarUnits() {
		final Map<String, Integer> units = new HashMap<>(5);
		units.put("YEAR", Calendar.YEAR);
		units.put("MONTH", Calendar.MONTH);
		units.put("DAY", Calendar.DAY_OF_YEAR);
		units.put("HOUR", Calendar.HOUR_OF_DAY);
		units.put("MINUTE", Calendar.MINUTE);
		return units;
	}

	/**
	 * Retourne la date correspondant à l'expression passée en parametre.
	 * La syntaxe est de type NOW((+/-)eeeUNIT) ou une date au format dd/MM/yy
	 * 
	 * @param dateQuery Expression
	 * @param datePattern Pattern used to define a date (dd/MM/YYYY)
	 * @return date
	 */
	static Date parse(final String dateQuery, String datePattern) {
		Assertion.checkArgNotEmpty(dateQuery);
		Assertion.checkArgNotEmpty(datePattern, "you must define a valid datePattern such as dd/MM/yyyy or MM/dd/yy");
		// ---
		final Calendar calendar = new GregorianCalendar();
		if (NOW.equals(dateQuery)) {
			//today is gonna be the day 
		} else if (dateQuery.startsWith(NOW)) {
			final Integer index = NOW.length();
			//---
			final char operator = dateQuery.charAt(index);
			final int sign;
			if ('+' == operator) {
				sign = 1;
			} else if ('-' == operator) {
				sign = -1;
			} else {
				throw new RuntimeException();
			}
			//---
			String operand = dateQuery.substring(index + 1);
			final String calendarUnit;
			final int unitCount;
			if (CALENDAR_UNITS.containsKey(operand)) {
				//NOW+DAY or NOW-MONTH without any number between NOW and calendar Unit. 
				unitCount = sign * 1;
				calendarUnit = operand;
			} else {
				//NOW+21DAY or NOW-12MONTH 
				final Matcher matcher = PATTERN.matcher(operand);
				Assertion.checkState(matcher.matches(), "Le second operande ne respecte pas le pattern {0}", PATTERN.toString());
				//---
				unitCount = sign * Integer.valueOf(matcher.group(1));
				calendarUnit = matcher.group(2);
				//We check that we have found a real unit Calendar and not 'NOW+15DAL'
				if (!CALENDAR_UNITS.containsKey(calendarUnit)) {
					throw new RuntimeException("unit '" + calendarUnit + "' is not allowed. You must use a unit among : " + CALENDAR_UNITS.keySet());
				}
			}
			calendar.add(CALENDAR_UNITS.get(calendarUnit), unitCount);
		} else {
			//We are expecting a date respectig pattern 
			final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(datePattern);
			try {
				calendar.setTime(simpleDateFormat.parse(dateQuery));
			} catch (final ParseException e) {
				throw new RuntimeException("La date " + dateQuery + " ne respecte pas le pattern : " + simpleDateFormat.toPattern().toString());
			}
		}

		return calendar.getTime();
	}
}
