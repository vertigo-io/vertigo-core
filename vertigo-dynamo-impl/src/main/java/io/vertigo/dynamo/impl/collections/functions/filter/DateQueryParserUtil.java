package io.vertigo.dynamo.impl.collections.functions.filter;

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
 * @author mlaroche
 */
public final class DateQueryParserUtil {
	private static final Map<String, Integer> CALENDAR_UNITS = makeUnitsMap();
	private static final String DATE_PATTERN = "dd/MM/yy";
	private final static Pattern pattern = Pattern.compile("([0-9]{1,})([A-Z]{1,})");
	private final static String NOW = "NOW";

	private static Map<String, Integer> makeUnitsMap() {
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
	 * @param stringValue
	 * @return
	 */
	public static Date parseDateQuery(final String stringValue) {
		// ---
		final Calendar calendar = new GregorianCalendar();
		if (NOW.equals(stringValue)) {
			//today is gonna be the day 
		} else if (stringValue.startsWith(NOW)) {
			final Integer index = stringValue.indexOf(NOW) + NOW.length();
			final String operator = stringValue.substring(index, index + 1);
			String secondOperand = stringValue.substring(index + 1);
			Integer multiplicator = 1;
			if (!CALENDAR_UNITS.containsKey(secondOperand)) {
				final Matcher matcher = pattern.matcher(secondOperand);
				Assertion.checkState(matcher.matches(), "Le second operande ne respecte pas le pattern {0}", pattern.toString());
				//---
				multiplicator = Integer.valueOf(matcher.group(1));
				secondOperand = matcher.group(2);
			}
			if (!CALENDAR_UNITS.containsKey(secondOperand)) {
				throw new RuntimeException("L'unité " + secondOperand + " n'est pas correcte");
			}
			// ---
			if ("+".equals(operator)) {
				//Nothing to do
			} else if ("-".equals(operator)) {
				multiplicator = 0 - multiplicator;
			} else {
				throw new RuntimeException();
			}
			calendar.add(CALENDAR_UNITS.get(secondOperand), multiplicator);
		} else {
			//We are expecting a date respectig pattern 
			final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_PATTERN);
			try {
				calendar.setTime(simpleDateFormat.parse(stringValue));
			} catch (final ParseException e) {
				throw new RuntimeException("La date " + stringValue + " ne respecte pas le pattern : " + simpleDateFormat.toPattern().toString());
			}
		}

		return calendar.getTime();
	}
}
