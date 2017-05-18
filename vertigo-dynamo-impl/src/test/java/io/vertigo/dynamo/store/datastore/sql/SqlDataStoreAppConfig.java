package io.vertigo.dynamo.store.datastore.sql;

import io.vertigo.app.config.AppConfig;
import io.vertigo.app.config.DefinitionProviderConfig;
import io.vertigo.app.config.ModuleConfig;
import io.vertigo.commons.impl.CommonsFeatures;
import io.vertigo.commons.plugins.cache.memory.MemoryCachePlugin;
import io.vertigo.core.param.Param;
import io.vertigo.core.plugins.resource.classpath.ClassPathResourceResolverPlugin;
import io.vertigo.dynamo.impl.DynamoFeatures;
import io.vertigo.dynamo.plugins.database.connection.c3p0.C3p0ConnectionProviderPlugin;
import io.vertigo.dynamo.plugins.environment.DynamoDefinitionProvider;
import io.vertigo.dynamo.plugins.store.datastore.sql.SqlDataStorePlugin;
import io.vertigo.dynamo.plugins.store.filestore.db.DbFileStorePlugin;
import io.vertigo.dynamo.store.StoreManagerInitializer;

/**
 * AppConfig builder for SqlStore tests. (Params for db specificities)
 * @author mlaroche
 *
 */
public class SqlDataStoreAppConfig {

	public static AppConfig build(final String dataBaseClass, final String jdbcDriver, final String jdbcUrl) {
		return AppConfig.builder()
				.beginBoot()
				.withLocales("fr_FR")
				.addPlugin(ClassPathResourceResolverPlugin.class)
				.endBoot()
				.addModule(new CommonsFeatures()
						.withScript()
						.withCache(MemoryCachePlugin.class)
						.build())
				.addModule(new DynamoFeatures()
						.withStore()
						.withSqlDataBase()
						.addDataStorePlugin(SqlDataStorePlugin.class)
						.addSqlConnectionProviderPlugin(C3p0ConnectionProviderPlugin.class,
								Param.of("dataBaseClass", dataBaseClass),
								Param.of("jdbcDriver", jdbcDriver),
								Param.of("jdbcUrl", jdbcUrl))
						.addFileStorePlugin(DbFileStorePlugin.class,
								Param.of("storeDtName", "DT_VX_FILE_INFO"))
						.build())
				.addModule(ModuleConfig.builder("definition").addDefinitionProvider(DefinitionProviderConfig.builder(DynamoDefinitionProvider.class)
						.addDefinitionResource("kpr", "io/vertigo/dynamo/store/data/executionWfileinfo.kpr")
						.addDefinitionResource("classes", "io.vertigo.dynamo.store.data.DtDefinitions")
						.build())
						.build())
				.addInitializer(StoreManagerInitializer.class)
				.build();

	}

}
