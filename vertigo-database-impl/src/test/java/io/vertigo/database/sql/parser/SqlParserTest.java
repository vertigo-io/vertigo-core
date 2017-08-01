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
