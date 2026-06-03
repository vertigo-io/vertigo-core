/*
 * vertigo - application development platform
 *
 * Copyright (C) 2013-2025, Vertigo.io, team@vertigo.io
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

import io.vertigo.core.lang.Assertion;

/**
 * The DateUtil provides useful methods concerning dates.
 *
 * Two types of dates are distinguished:
 *  - dates precise to the day
 *  - dates precise to the day, min, sec (ms)
 *
 * @author npiedeloup, pchretien
 */
public final class DateUtil {
	private DateUtil() {
		super();
	}

	/**
	 * Returns the date corresponding to the given expression.
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
	 * @param dateExpression Expression
	 * @param datePattern Pattern used to define a date (dd/MM/YYYY)
	 *
	 * @return date
	 */
	public static LocalDate parseToLocalDate(final String dateExpression, final String datePattern) {
		Assertion.check()
				.isNotBlank(dateExpression)
				.isNotBlank(datePattern);
		//---
		return DateQueryParserUtil.parseAsLocalDate(dateExpression, datePattern);
	}

	/**
	 * Returns the date corresponding to the given expression.
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
	 * @param dateExpression Expression
	 * @param datePattern Pattern used to define a date (dd/MM/YYYY)
	 *
	 * @return Instant
	 */
	public static Instant parseToInstant(final String dateExpression, final String datePattern) {
		Assertion.check()
				.isNotBlank(dateExpression)
				.isNotBlank(datePattern);
		//---
		return DateQueryParserUtil.parseAsInstant(dateExpression, datePattern);
	}
}
