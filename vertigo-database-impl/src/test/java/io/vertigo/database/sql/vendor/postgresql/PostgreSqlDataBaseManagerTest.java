package io.vertigo.database.sql.vendor.postgresql;

import io.vertigo.database.sql.AbstractSqlDataBaseManagerTest;
import io.vertigo.database.sql.vendor.SqlDialect.GenerationMode;

public final class PostgreSqlDataBaseManagerTest extends AbstractSqlDataBaseManagerTest {
	@Override
	protected String createTableMovie() {
		final String myString = "CREATE TABLE movie ( "
				+ "id 						NUMERIC(6), "
				+ "title 					VARCHAR(255), "
				+ "fps 						NUMERIC(6,3), "
				+ "income 					NUMERIC(6,3), "
				+ "color 					BOOLEAN, "
				+ "release_date 			TIMESTAMP, "
				+ "release_local_date 		DATE, "
				+ "release_zoned_date_time 	TIMESTAMP, "
				+ "icon 					BYTEA"
				+ ")";
		return myString;
	}

	@Override
	protected final String createSequenceMovie() {
		return "CREATE SEQUENCE seq_movie";
	}

	@Override
	protected GenerationMode getExpectedGenerationMode() {
		return GenerationMode.GENERATED_KEYS;
	}

	@Override
	protected boolean commitRequiredOnSchemaModification() {
		return true;
	}

}
