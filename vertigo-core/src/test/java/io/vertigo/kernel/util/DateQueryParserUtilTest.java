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
package io.vertigo.kernel.util;

import io.vertigo.kernel.lang.DateBuilder;

import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.Assert;
import org.junit.Test;

public final class DateQueryParserUtilTest {
	private static final String DATE_PATTERN = "dd/MM/yy";
	private static final Long DIFF_MS = 5 * 1000L;

	private static void assertEquals(Date expectedDate, String query) {
		Date compareDate = DateUtil.parse(query, DATE_PATTERN);
		long deltaInMillis = Math.abs(expectedDate.getTime() - compareDate.getTime());
		Assert.assertTrue("expects " + expectedDate + " and finds " + compareDate, deltaInMillis < DIFF_MS);
	}

	/** Test le keyword NOW avec une erreur. */
	@Test(expected = Exception.class)
	public void testWithError0() {
		DateUtil.parse("NOW+", DATE_PATTERN);
	}

	/** Test le keyword NOW avec une erreur. */
	@Test(expected = Exception.class)
	public void testWith1() {
		//an explicit  number must be defined
		DateUtil.parse("NOW+DAY", DATE_PATTERN);
	}

	/** Test le keyword NOW avec une erreur. */
	@Test(expected = Exception.class)
	public void testWith2() {
		//Day must be in upperCase : DAY
		DateUtil.parse("NOW+1Day", DATE_PATTERN);
	}

	/** Test le keyword NOW avec une erreur. */
	@Test(expected = Exception.class)
	public void testWithError3() {
		//DAL is not a calendar unit
		DateUtil.parse("NOW+1DAL", DATE_PATTERN);
	}

	/** Test le keyword NOW avec une erreur. */
	@Test(expected = Exception.class)
	public void testWithError4() {
		//DAYST is not a calendar unit even if DAYS is valid
		DateUtil.parse("NOW+1DAYST", DATE_PATTERN);
	}

	/**
	 * Test le keyword NOW.
	 */
	@Test
	public void testNow() {
		Date expectedDate = new Date();
		assertEquals(expectedDate, "NOW");
	}

	/**
	 * Teste l'ajout d'un jour sans multiplicateur.
	 */
	@Test
	public void testAddDay() {
		Date expectedDate = new DateBuilder(new Date()).addDays(1).toDateTime();
		assertEquals(expectedDate, "NOW+1DAY");
		assertEquals(expectedDate, "NOW+1DAYS");
		assertEquals(expectedDate, "+1DAY");
		assertEquals(expectedDate, "+1DAYS");
		assertEquals(expectedDate, "+1DAY");
	}

	/**
	 * Teste l'ajout d'un jour avec multiplicateur.
	 */
	@Test
	public void testAddDays() {
		Date expectedDate = new DateBuilder(new Date()).addDays(2).toDateTime();
		assertEquals(expectedDate, "NOW+2DAY");
		assertEquals(expectedDate, "+2DAY");
		assertEquals(expectedDate, "NOW+2DAYS");
		assertEquals(expectedDate, "+2DAYS");
	}

	/**
	 * Teste la soustraction de jour avec multiplicateur.
	 */
	@Test
	public void testRemoveDays() {
		Date expectedDate = new DateBuilder(new Date()).addDays(-12).toDateTime();
		assertEquals(expectedDate, "NOW-12DAY");
		assertEquals(expectedDate, "-12DAY");
		assertEquals(expectedDate, "NOW-12DAYS");
		assertEquals(expectedDate, "-12DAYS");
	}

	/**
	 * Teste l'ajout de mois avec multiplicateur.
	 */
	@Test
	public void testAddMonths() {
		Date expectedDate = new DateBuilder(new Date()).addMonths(3).toDateTime();
		assertEquals(expectedDate, "NOW+3MONTH");
		assertEquals(expectedDate, "+3MONTH");
		assertEquals(expectedDate, "NOW+3MONTHS");
		assertEquals(expectedDate, "+3MONTHS");
	}

	/**
	 * Teste l'ajout d'années avec multiplicateur.
	 */
	@Test
	public void testAddYears() {
		Date expectedDate = new DateBuilder(new Date()).addYears(5).toDateTime();
		assertEquals(expectedDate, "NOW+5YEAR");
		assertEquals(expectedDate, "+5YEAR");
		assertEquals(expectedDate, "NOW+5YEARS");
		assertEquals(expectedDate, "+5YEARS");
	}

	/**
	 * Teste l'ajout d'heures avec multiplicateur.
	 */
	@Test
	public void testAddHours() {
		Date expectedDate = new DateBuilder(new Date()).addHours(50).toDateTime();
		assertEquals(expectedDate, "NOW+50HOUR");
		assertEquals(expectedDate, "+50HOUR");
		assertEquals(expectedDate, "NOW+50HOURS");
		assertEquals(expectedDate, "+50HOURS");
		}

	/**
	 * Teste le parsing d'une date fixe.
	 */
	@Test
	public void testFixedDate() {
		Date expectedDate = new GregorianCalendar(2014, 4, 25).getTime();
		assertEquals(expectedDate, "25/05/14");
	}
}
