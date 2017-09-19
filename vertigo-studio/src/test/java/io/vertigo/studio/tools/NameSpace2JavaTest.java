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
package io.vertigo.studio.tools;

import org.junit.Test;

import io.vertigo.app.App;
import io.vertigo.app.AutoCloseableApp;
import io.vertigo.core.resource.ResourceManager;
import io.vertigo.database.sql.SqlDataBaseManager;
import io.vertigo.database.sql.connection.SqlConnection;

/**
 * Test la génération à partir des oom et ksp.
 * @author dchallas
 */
public class NameSpace2JavaTest {

	/**
	 * Lancement du test.
	 */
	@Test
	public void testGenerate() {
		NameSpace2Java.main(new String[] { "data/test.properties" });
	}

	/**
	 * Lancement du test.
	 */
	@Test
	public void testGenerateMasterData() {
		NameSpace2Java.main(new String[] { "data/testMasterdata.properties" });
	}

	/**
	 * Lancement du test.
	 */
	@Test
	public void testGenerateAuthorization() {
		NameSpace2Java.main(new String[] { "data/testAuthorization.properties" });
	}

	/**
	 * Lancement du test.
	 */
	@Test
	public void testGenerateFile() {
		NameSpace2Java.main(new String[] { "data/testFile.properties" });
	}

	/**
	 * Lancement du test.
	 */
	@Test
	public void testGenerateSql() {
		NameSpace2Java.main(new String[] { "data/testSql.properties" });
		try (AutoCloseableApp app = new AutoCloseableApp(SqlTestConfigurator.config())) {
			execSqlScript("target/databasegenh2/crebas.sql", app);
		}

	}

	private void execSqlScript(final String sqlScript, final App app) {
		final ResourceManager resourceManager = app.getComponentSpace().resolve(ResourceManager.class);
		final SqlDataBaseManager sqlDataBaseManager = app.getComponentSpace().resolve(SqlDataBaseManager.class);

		final SqlConnection connection = sqlDataBaseManager.getConnectionProvider(SqlDataBaseManager.MAIN_CONNECTION_PROVIDER_NAME).obtainConnection();
		DataBaseScriptUtil.execSqlScript(connection, sqlScript, resourceManager, sqlDataBaseManager);
	}

	/**
	 * Lancement du test.
	 */
	@Test
	public void testGenerateSqlServer() {
		NameSpace2Java.main(new String[] { "data/testSqlServer.properties" });
	}

	/**
	 * Lancement du test.
	 */
	@Test
	public void testGenerateDtDefinitions() {
		NameSpace2Java.main(new String[] { "data/testDtDefinitions.properties" });
	}

	/**
	 * Lancement du test.
	 */
	@Test
	public void testGenerateWsRoute() {
		NameSpace2Java.main(new String[] { "data/testWsRoute.properties" });
	}
}
