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
