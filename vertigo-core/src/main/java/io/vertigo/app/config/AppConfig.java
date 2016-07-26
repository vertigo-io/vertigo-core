/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2016, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.app.config;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.vertigo.app.Logo;
import io.vertigo.lang.Assertion;

/**
 * The AppConfig class defines the config.
 * The app is built from this config.
 *
 * AppConfig must be created using the AppConfigBuilder.
 * @author pchretien
 */
public final class AppConfig {
	private final BootConfig bootConfig;
	private final List<ModuleConfig> modules;
	private final List<ComponentInitializerConfig> initializers;

	AppConfig(
			final BootConfig bootConfig,
			final List<ModuleConfig> moduleConfigs,
			final List<ComponentInitializerConfig> componentInitializerConfigs) {
		Assertion.checkNotNull(bootConfig);
		Assertion.checkNotNull(moduleConfigs);
		Assertion.checkNotNull(componentInitializerConfigs);
		//---
		this.bootConfig = bootConfig;
		modules = Collections.unmodifiableList(new ArrayList<>(moduleConfigs));
		initializers = Collections.unmodifiableList(new ArrayList<>(componentInitializerConfigs));
	}

	/**
	 *
	 * @return the config of the boot
	 */
	public BootConfig getBootConfig() {
		return bootConfig;
	}

	/**
	 * @return list of the configs of the modules
	 */
	public List<ModuleConfig> getModuleConfigs() {
		return modules;
	}

	/**
	 *
	 * @return List of the config of the initializers
	 */
	public List<ComponentInitializerConfig> getComponentInitializerConfigs() {
		return initializers;
	}

	//=========================================================================
	//======================Gestion des affichages=============================
	//=========================================================================
	/**
	 * Allows to print a short description of the config.
	 * @param out Out
	 */
	public void print(final PrintStream out) {
		Assertion.checkNotNull(out);
		Logo.printCredits(out);
		doPrint(out);
	}

	/**
	 * Affiche dans la console le logo.
	 * @param out Flux de sortie des informations
	 */
	private void doPrint(final PrintStream out) {
		out.println("+-------------------------+------------------------+----------------------------------------------+");
		printComponent(out, "modules", "components", "plugins");
		out.println("+-------------------------+------------------------+----------------------------------------------+");
		printModule(out, bootConfig.getBootModuleConfig());
		for (final ModuleConfig moduleConfig : modules) {
			out.println("+-------------------------+------------------------+----------------------------------------------+");
			printModule(out, moduleConfig);
		}
		out.println("+-------------------------+------------------------+----------------------------------------------+");
	}

	private static void printModule(final PrintStream out, final ModuleConfig moduleConfig) {
		String moduleName = moduleConfig.getName();
		for (final ComponentConfig componentConfig : moduleConfig.getComponentConfigs()) {
			final String componentClassName = (componentConfig.getApiClass().isPresent() ? componentConfig.getApiClass().get() : componentConfig.getImplClass()).getSimpleName();
			printComponent(out, moduleName, componentClassName, null);
			moduleName = null;
		}
		for (final PluginConfig pluginConfig : moduleConfig.getPluginConfigs()) {
			final String pluginClassName = pluginConfig.getImplClass().getSimpleName();
			printComponent(out, moduleName, null, pluginClassName);
		}
	}

	private static void printComponent(final PrintStream out, final String column1, final String column2, final String column3) {
		out.println("|" + truncate(column1, 24) + " | " + truncate(column2, 22) + " | " + truncate(column3, 44) + " |");
	}

	private static String truncate(final String value, final int size) {
		final String result = (value != null ? value : "") + "                                                                  ";
		return result.substring(0, size);
	}
}
