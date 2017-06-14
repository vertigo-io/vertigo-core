package io.vertigo.database.sql.vendor.h2;

import io.vertigo.database.sql.AbstractSqlDataBaseManagerTest;
import io.vertigo.database.sql.vendor.SqlDialect.GenerationMode;

public final class H2DataBaseManagerTest extends AbstractSqlDataBaseManagerTest {
	@Override
	protected String createTableMovie() {
		return "CREATE TABLE movie ( "
				+ "id 						NUMBER(6), "
				+ "title 					VARCHAR(255), "
				+ "fps 						NUMBER(6,3), "
				+ "income 					NUMBER(6,3), "
				+ "color 					BOOLEAN, "
				+ "release_date 			TIMESTAMP, "
				+ "release_local_date 		DATE, "
				+ "release_zoned_date_time 	TIMESTAMP, "
				+ "icon 					BLOB"
				+ ")";
	}

	@Override
	protected final String createSequenceMovie() {
		return "CREATE SEQUENCE seq_movie";
	}

	@Override
	protected GenerationMode getExpectedGenerationMode() {
		return GenerationMode.GENERATED_KEYS;
	}

}
