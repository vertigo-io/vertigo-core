package io.vertigo.dynamo.store.datastore.sql.vendor;

import org.hsqldb.jdbcDriver;

import io.vertigo.app.config.AppConfig;
import io.vertigo.database.impl.sql.vendor.hsql.HsqlDataBase;
import io.vertigo.dynamo.store.datastore.sql.AbstractSqlStoreManagerTest;
import io.vertigo.dynamo.store.datastore.sql.SqlDataStoreAppConfig;

/**
 * Test of sql storage in HSql DB.
 * @author mlaroche
 *
 */
public final class HSqlStoreManagerTest extends AbstractSqlStoreManagerTest {

	@Override
	protected AppConfig buildAppConfig() {
		return SqlDataStoreAppConfig.build(
				HsqlDataBase.class.getCanonicalName(),
				jdbcDriver.class.getCanonicalName(),
				"jdbc:hsqldb:mem:database");
	}
}
