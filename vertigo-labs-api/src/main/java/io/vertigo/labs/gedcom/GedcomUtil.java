package io.vertigo.labs.gedcom;

public final class GedcomUtil {

	private GedcomUtil() {
		//
	}

	public static Integer findYear(String birthDate) {
		if (birthDate == null) {
			return null;
		}
		try {
			return Integer.valueOf(birthDate.trim());
		} catch (Exception e) {
			String[] markers = "JAN,FEB,MAR,APR,MAY,JUN,JUI,AUG,SEP,OCT,NOV,DEC".split(",");
			Integer year = findYear(birthDate, markers);
			if (year != null) {
				return year;
			}
			markers = "ABT".split(",");
			year = findYear(birthDate, markers);
			if (year != null) {
				return year;
			}
		}
		return null;
	}

	private static Integer findYear(String birthDate, String[] markers) {
		for (String marker : markers) {
			int idx = birthDate.indexOf(marker);
			String right = birthDate.substring(idx + marker.length()).trim();
			if (idx >= 0) {
				return Integer.valueOf(right);
			}
		}
		return null;
	}
}
