/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2016, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.util;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

@RunWith(JUnitPlatform.class)
public final class DateQueryParserUtilTest {
	private static final String DATE_PATTERN = "dd/MM/yy";
	private static final Long DIFF_MS = 5 * 1000L;

	private static void assertEquals(final Date expectedDate, final String query) {
		final Date compareDate = DateUtil.parse(query, DATE_PATTERN);
		final long deltaInMillis = Math.abs(expectedDate.getTime() - compareDate.getTime());
		assertTrue(deltaInMillis < DIFF_MS, "expects " + expectedDate + " and finds " + compareDate);
	}

	/** Test le keyword now avec une erreur. */
	@Test
	public void testWithError0() {
		Assertions.assertThrows(Exception.class,
				() -> DateUtil.parse("now+", DATE_PATTERN));
	}

	/** Test le keyword now avec une erreur. */
	@Test
	public void testWithError1() {
		Assertions.assertThrows(Exception.class,
				//an explicit  number must be defined
				() -> DateUtil.parse("now+DAY", DATE_PATTERN));
	}

	/** Test le keyword now avec une erreur. */
	@Test
	public void testWithError2() {
		Assertions.assertThrows(Exception.class,
				//Day must be in upperCase : DAY
				() -> DateUtil.parse("now+1Day", DATE_PATTERN));
	}

	/** Test le keyword now avec une erreur. */
	@Test
	public void testWithError3() {
		Assertions.assertThrows(Exception.class,
				//day is not a calendar unit
				() -> DateUtil.parse("now+1day", DATE_PATTERN));
	}

	/** Test le keyword now avec une erreur. */
	@Test
	public void testWithError4() {
		Assertions.assertThrows(Exception.class,
				//D is not a calendar unit even if d is valid
				() -> DateUtil.parse("now+1D", DATE_PATTERN));
	}

	/**
	 * Test le keyword now.
	 */
	@Test
	public void testNow() {
		final Date expectedDate = new Date();
		assertEquals(expectedDate, "now");
	}

	/**
	 * Teste l'ajout d'un jour sans multiplicateur.
	 */
	@Test
	public void testAddDay() {
		final Date expectedDate = new DateBuilder(new Date()).addDays(1).toDateTime();
		assertEquals(expectedDate, "now+1d");
	}

	/**
	 * Add 5 weeks.
	 */
	@Test
	public void testAddWeek() {
		final Date expectedDate = new DateBuilder(new Date()).addWeeks(5).toDateTime();
		assertEquals(expectedDate, "now+5w");
	}

	/**
	 * Teste l'ajout d'un jour avec multiplicateur.
	 */
	@Test
	public void testAddDays() {
		final Date expectedDate = new DateBuilder(new Date()).addDays(2).toDateTime();
		assertEquals(expectedDate, "now+2d");
	}

	/**
	 * Teste la soustraction de jour avec multiplicateur.
	 */
	@Test
	public void testRemoveDays() {
		final Date expectedDate = new DateBuilder(new Date()).addDays(-12).toDateTime();
		assertEquals(expectedDate, "now-12d");
	}

	/**
	 * Teste l'ajout de mois avec multiplicateur.
	 */
	@Test
	public void testAddMonths() {
		final Date expectedDate = new DateBuilder(new Date()).addMonths(3).toDateTime();
		assertEquals(expectedDate, "now+3M");
	}

	/**
	 * Teste l'ajout d'années avec multiplicateur.
	 */
	@Test
	public void testAddYears() {
		final Date expectedDate = new DateBuilder(new Date()).addYears(5).toDateTime();
		assertEquals(expectedDate, "now+5y");
	}

	/**
	 * Teste l'ajout d'heures avec multiplicateur.
	 */
	@Test
	public void testAddHours() {
		final Date expectedDate = new DateBuilder(new Date()).addHours(50).toDateTime();
		assertEquals(expectedDate, "now+50h");
	}

	/**
	 * Teste le parsing d'une date fixe.
	 */
	@Test
	public void testFixedDate() {
		final Date expectedDate = new GregorianCalendar(2014, 4, 25).getTime();
		assertEquals(expectedDate, "25/05/14");
	}
}
