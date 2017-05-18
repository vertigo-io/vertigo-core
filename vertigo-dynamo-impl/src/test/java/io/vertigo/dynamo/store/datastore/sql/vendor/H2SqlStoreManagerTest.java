package io.vertigo.dynamo.store.datastore.sql.vendor;

import org.h2.Driver;

import io.vertigo.app.config.AppConfig;
import io.vertigo.dynamo.impl.database.vendor.h2.H2DataBase;
import io.vertigo.dynamo.store.datastore.sql.AbstractSqlStoreManagerTest;
import io.vertigo.dynamo.store.datastore.sql.SqlDataStoreAppConfig;

/**
 * Test of sql storage in H2 DB.
 * @author mlaroche
 *
 */
public final class H2SqlStoreManagerTest extends AbstractSqlStoreManagerTest {

	@Override
	protected AppConfig buildAppConfig() {
		return SqlDataStoreAppConfig.build(
				H2DataBase.class.getCanonicalName(),
				Driver.class.getCanonicalName(),
				"jdbc:h2:mem:database");
	}

}
