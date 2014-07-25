package io.vertigo.dynamo.collections.functions.filter;

import io.vertigo.AbstractTestCaseJU4;
import io.vertigo.dynamo.collections.facet.metamodel.FacetedQueryDefinition;
import io.vertigo.dynamo.impl.collections.functions.filter.DateQueryParser;
import io.vertigo.dynamock.domain.car.CarDataBase;
import io.vertigo.dynamock.facet.CarFacetInitializer;
import io.vertigo.kernel.Home;
import io.vertigo.kernel.util.DateUtil;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.Assert;
import org.junit.Test;

public class DateQueryParserTest extends AbstractTestCaseJU4{
	
	public static final Long diffMs = 5*1000L;
	
	/**{@inheritDoc}*/
	@Override
	protected void doSetUp() {
		// On a rien a faire
	}
	
	/**
	 * Test le keyword NOW.
	 */
	@Test
	public void testNowKeyWord() {
		Date dateReference =DateUtil.newDateTime();
		Date parsedDate = DateQueryParser.parseDateQuery("NOW");
		Assert.assertTrue(testDifferenceCorrecte(dateReference, parsedDate));
		
	}
	
	
	
	/**
	 * Teste l'ajout d'un jour sans multiplicateur.
	 */
	@Test
	public void testAddDay() {
		Calendar calendarReference = new GregorianCalendar();
		calendarReference.add(Calendar.DAY_OF_YEAR, 1);
		Date parsedDate = DateQueryParser.parseDateQuery("NOW+DAY");
		Assert.assertTrue(testDifferenceCorrecte(calendarReference.getTime(), parsedDate));
		
	}
	
	/**
	 * Teste l'ajout d'un jour avec multiplicateur.
	 */
	@Test
	public void testAddDays() {
		Calendar calendarReference = new GregorianCalendar();
		calendarReference.add(Calendar.DAY_OF_YEAR, 2);
		Date parsedDate = DateQueryParser.parseDateQuery("NOW+2DAY");
		Assert.assertTrue(testDifferenceCorrecte(calendarReference.getTime(), parsedDate));
		
	}
	
	
	
	/**
	 * Teste la soustraction de jour avec multiplicateur.
	 */
	@Test
	public void testRemoveDays() {
		Calendar calendarReference = new GregorianCalendar();
		calendarReference.add(Calendar.DAY_OF_YEAR, -12);
		Date parsedDate = DateQueryParser.parseDateQuery("NOW-12DAY");
		Assert.assertTrue(testDifferenceCorrecte(calendarReference.getTime(), parsedDate));
		
	}
	
	/**
	 * Teste l'ajout de mois avec multiplicateur.
	 */
	@Test
	public void testAddMonths() {
		Calendar calendarReference = new GregorianCalendar();
		calendarReference.add(Calendar.MONTH, 3);
		Date parsedDate = DateQueryParser.parseDateQuery("NOW+3MONTH");
		Assert.assertTrue(testDifferenceCorrecte(calendarReference.getTime(), parsedDate));
		
	}
	
	/**
	 * Teste l'ajout d'années avec multiplicateur.
	 */
	@Test
	public void testAddYears() {
		Calendar calendarReference = new GregorianCalendar();
		calendarReference.add(Calendar.YEAR, 5);
		Date parsedDate = DateQueryParser.parseDateQuery("NOW+5YEAR");
		Assert.assertTrue(testDifferenceCorrecte(calendarReference.getTime(), parsedDate));
		
	}
	
	/**
	 * Teste l'ajout d'heures avec multiplicateur.
	 */
	@Test
	public void testAddHours() {
		Calendar calendarReference = new GregorianCalendar();
		calendarReference.add(Calendar.HOUR_OF_DAY, 50);
		Date parsedDate = DateQueryParser.parseDateQuery("NOW+50HOUR");
		Assert.assertTrue(testDifferenceCorrecte(calendarReference.getTime(), parsedDate));
		
	}
	
	/**
	 * Teste le parsing d'une date fixe.
	 */
	@Test
	public void testFixedDate() {
		Calendar calendarReference = new GregorianCalendar(2014,4,25);// Careful MonthIndex starts at 0
		Date parsedDate = DateQueryParser.parseDateQuery("25/05/14");
		Assert.assertTrue(testDifferenceCorrecte(calendarReference.getTime(), parsedDate));
		
	}
	
	
	private boolean testDifferenceCorrecte(final Date dateReference, final Date dateToTest){
		return Math.abs(dateReference.getTime() - dateToTest.getTime()) < diffMs;		
	} 
	
}