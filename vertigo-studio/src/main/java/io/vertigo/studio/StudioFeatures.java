package io.vertigo.studio;

import io.vertigo.app.config.Feature;
import io.vertigo.app.config.Features;
import io.vertigo.core.param.Param;
import io.vertigo.studio.impl.masterdata.MasterDataManagerImpl;
import io.vertigo.studio.impl.mda.MdaManagerImpl;
import io.vertigo.studio.masterdata.MasterDataManager;
import io.vertigo.studio.mda.MdaManager;
import io.vertigo.studio.plugins.masterdata.json.JsonMasterDataValueProvider;
import io.vertigo.studio.plugins.mda.authorization.AuthorizationGeneratorPlugin;
import io.vertigo.studio.plugins.mda.domain.java.DomainGeneratorPlugin;
import io.vertigo.studio.plugins.mda.domain.js.JSGeneratorPlugin;
import io.vertigo.studio.plugins.mda.domain.sql.SqlGeneratorPlugin;
import io.vertigo.studio.plugins.mda.domain.ts.TSGeneratorPlugin;
import io.vertigo.studio.plugins.mda.file.FileInfoGeneratorPlugin;
import io.vertigo.studio.plugins.mda.task.TaskGeneratorPlugin;
import io.vertigo.studio.plugins.mda.task.test.TaskTestGeneratorPlugin;
import io.vertigo.studio.plugins.mda.webservice.WsTsGeneratorPlugin;

public class StudioFeatures extends Features<StudioFeatures> {

	public StudioFeatures() {
		super("vertigo-studio");
	}

	@Feature("mda")
	public StudioFeatures withMda(final Param... params) {
		getModuleConfigBuilder().addComponent(MdaManager.class, MdaManagerImpl.class, params);
		return this;
	}

	@Feature("mda.domain.java")
	public StudioFeatures withJavaDomainGenerator(final Param... params) {
		getModuleConfigBuilder().addPlugin(DomainGeneratorPlugin.class, params);
		return this;
	}

	@Feature("mda.domain.js")
	public StudioFeatures withJsDomainGenerator(final Param... params) {
		getModuleConfigBuilder().addPlugin(JSGeneratorPlugin.class, params);
		return this;
	}

	@Feature("mda.domain.ts")
	public StudioFeatures withTsDomainGenerator(final Param... params) {
		getModuleConfigBuilder().addPlugin(TSGeneratorPlugin.class, params);
		return this;
	}

	@Feature("mda.domain.sql")
	public StudioFeatures withSqlDomainGenerator(final Param... params) {
		getModuleConfigBuilder().addPlugin(SqlGeneratorPlugin.class, params);
		return this;
	}

	@Feature("mda.authorization")
	public StudioFeatures withAuthorizationGenerator(final Param... params) {
		getModuleConfigBuilder().addPlugin(AuthorizationGeneratorPlugin.class, params);
		return this;
	}

	@Feature("mda.file")
	public StudioFeatures withFileGenerator(final Param... params) {
		getModuleConfigBuilder().addPlugin(FileInfoGeneratorPlugin.class, params);
		return this;
	}

	@Feature("mda.task")
	public StudioFeatures withTaskGenerator(final Param... params) {
		getModuleConfigBuilder().addPlugin(TaskGeneratorPlugin.class, params);
		return this;
	}

	@Feature("mda.taskTests")
	public StudioFeatures withTaskTestsGenerator(final Param... params) {
		getModuleConfigBuilder().addPlugin(TaskTestGeneratorPlugin.class, params);
		return this;
	}

	@Feature("mda.tsWebservices")
	public StudioFeatures withTsWebServicesGenerator(final Param... params) {
		getModuleConfigBuilder().addPlugin(WsTsGeneratorPlugin.class, params);
		return this;
	}

	@Feature("masterdata")
	public StudioFeatures withMasterData() {
		getModuleConfigBuilder().addComponent(MasterDataManager.class, MasterDataManagerImpl.class);
		return this;
	}

	@Feature("masterdata.json")
	public StudioFeatures withJsonMasterDataValuesProvider(final Param... params) {
		getModuleConfigBuilder().addPlugin(JsonMasterDataValueProvider.class, params);
		return this;
	}

	@Override
	protected void buildFeatures() {
		// nothing by default

	}

}
