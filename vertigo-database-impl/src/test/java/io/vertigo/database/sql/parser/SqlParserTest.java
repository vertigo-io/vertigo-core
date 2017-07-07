package io.vertigo.database.sql.parser;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;

import io.vertigo.AbstractTestCaseJU4;
import io.vertigo.database.sql.SqlDataBaseManager;

public class SqlParserTest extends AbstractTestCaseJU4 {
	@Inject
	private SqlDataBaseManager sqlDataBaseManager;

	@Test
	public void testEscape() {
		//Si le séparateur est un car.
		//il suffit de double le séparateur pour l'échapper.
		final String script = "select...where #price#...##...";
		final String sql = sqlDataBaseManager.parseQuery(script).getVal1();
		Assert.assertEquals("select...where ?...#...", sql);
	}

	@Test(expected = Exception.class)
	public void testMissingTag() {
		final String script = "select...where  #price";
		final String sql = sqlDataBaseManager.parseQuery(script).getVal1();
		nop(sql);
		Assert.fail();
	}

}
