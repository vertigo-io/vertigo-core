/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2017, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.dynamo.domain.formatter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.Assert;
import org.junit.Test;

import io.vertigo.AbstractTestCaseJU4;
import io.vertigo.dynamo.domain.metamodel.DataType;
import io.vertigo.dynamo.domain.metamodel.FormatterException;
import io.vertigo.dynamox.domain.formatter.FormatterDate;

/**
 * Test de l'implémentation standard.
 *
 * @author pchretien
 */
public class DateFormatterTest extends AbstractTestCaseJU4 {
	private final FormatterDate formatterDate = new FormatterDate("yyyy-MM-dd");
	private final FormatterDate formatterDateZDT = new FormatterDate("yyyy-MM-dd' 'HH:mm:ss");

	@Test
	public void testFormatter() throws FormatterException {
		final Date date = new GregorianCalendar(2003, Calendar.SEPTEMBER, 15).getTime();
		Assert.assertEquals("2003-09-15", formatterDate.valueToString(date, DataType.Date));
		Assert.assertEquals(date, formatterDate.stringToValue("2003-09-15", DataType.Date));
	}

	@Test
	public void testLocalDateFormatter() throws FormatterException {
		final LocalDate localDate = LocalDate.of(2000, 12, 25);
		Assert.assertEquals("2000-12-25", formatterDate.valueToString(localDate, DataType.LocalDate));
		Assert.assertEquals(localDate, formatterDate.stringToValue("2000-12-25", DataType.LocalDate));
	}

	@Test
	public void testZonedDateTimeFormatter() throws FormatterException {
		final ZonedDateTime zonedDateTime = ZonedDateTime.of(LocalDateTime.of(2009, 2, 23, 16, 30), ZoneId.of("UTC"));
		Assert.assertEquals("2009-02-23 16:30:00", formatterDateZDT.valueToString(zonedDateTime, DataType.ZonedDateTime));
		Assert.assertEquals(zonedDateTime, formatterDateZDT.stringToValue("2009-02-23 16:30:00", DataType.ZonedDateTime));
	}

	@Test(expected = FormatterException.class)
	public void testFormatterErrorDate() throws FormatterException {
		final Date date = new GregorianCalendar(2003, Calendar.SEPTEMBER, 15).getTime();
		Assert.assertEquals(date, formatterDate.stringToValue("2003/09/15", DataType.Date));
	}

	@Test(expected = FormatterException.class)
	public void testFormatterErrorLocalDate() throws FormatterException {
		final LocalDate localDate = LocalDate.of(2000, 12, 25);
		Assert.assertEquals(localDate, formatterDate.stringToValue("2003/09/15", DataType.LocalDate));
	}

	@Test(expected = FormatterException.class)
	public void testFormatterErrorZonedDateTime() throws FormatterException {
		final ZonedDateTime zonedDateTime = ZonedDateTime.of(LocalDateTime.of(2009, 2, 23, 16, 30), ZoneId.of("UTC"));
		Assert.assertEquals(zonedDateTime, formatterDate.stringToValue("2003/09/15 16:30:00", DataType.ZonedDateTime));
	}

}
