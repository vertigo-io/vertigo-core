package io.vertigo.dynamo.collections.functions.filter;

import io.vertigo.AbstractTestCaseJU4;
import io.vertigo.dynamo.impl.collections.functions.filter.DateQueryParserUtil;
import io.vertigo.kernel.lang.DateBuilder;
import io.vertigo.kernel.util.DateUtil;

import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.Assert;
import org.junit.Test;

public final class DateQueryParserUtilTest extends AbstractTestCaseJU4 {
	private static final Long DIFF_MS = 5 * 1000L;

	private static void assertEquals(Date expectedDate, Date compareDate) {
		long deltaInMillis = Math.abs(expectedDate.getTime() - compareDate.getTime());
		Assert.assertTrue(deltaInMillis < DIFF_MS);
	}

	/**
	 * Test le keyword NOW.
	 */
	@Test
	public void testNowKeyWord() {
		Date expectedDate = DateUtil.newDateTime();
		Date parsedDate = DateQueryParserUtil.parseDateQuery("NOW");
		//---
		assertEquals(expectedDate, parsedDate);
	}

	/**
	 * Teste l'ajout d'un jour sans multiplicateur.
	 */
	@Test
	public void testAddDay() {
		Date expectedDate = new DateBuilder(new Date()).addDays(1).toDateTime();
		Date parsedDate = DateQueryParserUtil.parseDateQuery("NOW+DAY");
		//---
		assertEquals(expectedDate, parsedDate);
	}

	/**
	 * Teste l'ajout d'un jour avec multiplicateur.
	 */
	@Test
	public void testAddDays() {
		Date expectedDate = new DateBuilder(new Date()).addDays(2).toDateTime();
		Date parsedDate = DateQueryParserUtil.parseDateQuery("NOW+2DAY");
		//---
		assertEquals(expectedDate, parsedDate);
	}

	/**
	 * Teste la soustraction de jour avec multiplicateur.
	 */
	@Test
	public void testRemoveDays() {
		Date expectedDate = new DateBuilder(new Date()).addDays(-12).toDateTime();
		Date parsedDate = DateQueryParserUtil.parseDateQuery("NOW-12DAY");
		//---
		assertEquals(expectedDate, parsedDate);
	}

	/**
	 * Teste l'ajout de mois avec multiplicateur.
	 */
	@Test
	public void testAddMonths() {
		Date expectedDate = new DateBuilder(new Date()).addMonths(3).toDateTime();
		Date parsedDate = DateQueryParserUtil.parseDateQuery("NOW+3MONTH");
		//---
		assertEquals(expectedDate, parsedDate);
	}

	/**
	 * Teste l'ajout d'années avec multiplicateur.
	 */
	@Test
	public void testAddYears() {
		Date expectedDate = new DateBuilder(new Date()).addYears(5).toDateTime();
		Date parsedDate = DateQueryParserUtil.parseDateQuery("NOW+5YEAR");
		//---
		assertEquals(expectedDate, parsedDate);
	}

	/**
	 * Teste l'ajout d'heures avec multiplicateur.
	 */
	@Test
	public void testAddHours() {
		Date expectedDate = new DateBuilder(new Date()).addHours(50).toDateTime();
		Date parsedDate = DateQueryParserUtil.parseDateQuery("NOW+50HOUR");
		//---
		assertEquals(expectedDate, parsedDate);
	}

	/**
	 * Teste le parsing d'une date fixe.
	 */
	@Test
	public void testFixedDate() {
		Date expectedDate = new GregorianCalendar(2014, 4, 25).getTime();
		Date parsedDate = DateQueryParserUtil.parseDateQuery("25/05/14");
		//---
		assertEquals(expectedDate, parsedDate);

	}
}
