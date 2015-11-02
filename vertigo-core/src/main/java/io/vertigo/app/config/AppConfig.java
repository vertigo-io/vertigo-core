/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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

import io.vertigo.app.Logo;
import io.vertigo.lang.Assertion;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*
 * @author pchretien
 */
public final class AppConfig {
	private final BootConfig bootConfig;
	private final List<ModuleConfig> modules;

	AppConfig(
			final BootConfig bootConfig,
			final List<ModuleConfig> moduleConfigs) {
		Assertion.checkNotNull(bootConfig);
		Assertion.checkNotNull(moduleConfigs);
		//---
		this.bootConfig = bootConfig;
		this.modules = Collections.unmodifiableList(new ArrayList<>(moduleConfigs));
	}

	public BootConfig getBootConfig() {
		return bootConfig;
	}

	/**
	 * @return Liste des configurations de modules
	 */
	public List<ModuleConfig> getModuleConfigs() {
		return modules;
	}

	//=========================================================================
	//======================Gestion des affichages=============================
	//=========================================================================
	public void print(final PrintStream out) {
		Assertion.checkNotNull(out);
		// ---Affichage du logo et des modules---
		Logo.printCredits(out);
		doPrint(out);
	}

	/**
	 * Affiche dans la console le logo.
	 * @param out Flux de sortie des informations
	 */
	private void doPrint(final PrintStream out) {
		//	out.println("+-------------------------+------------------------+----------------------------------------------+");
		out.println("+-------------------------+------------------------+----------------------------------------------+");
		printComponent(out, "modules", "components", null);
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
			printComponent(out, moduleName, componentConfig.getImplClass().getSimpleName(), null);
			moduleName = null;
		}
		//		for (final Plugin plugin : pluginsByComponentId.get(componentId)) {
		//			printComponent(out, null, null, plugin.getClass().getSimpleName());
		//		}
		//			final ComponentDescription componentDescription = entry.getValue().getDescription();
		//final String info;
		//			if (componentDescription != null && componentDescription.getMainSummaryInfo() != null) {
		//				info = componentDescription.getMainSummaryInfo().getInfo();
		//			} else {
		//info = null;
		//}
		//		printComponent(out, componentClass.getSimpleName(), component.getClass().getSimpleName(), buffer.toString());
	}

	private static void printComponent(final PrintStream out, final String column1, final String column2, final String column3) {
		out.println("|" + truncate(column1, 24) + " | " + truncate(column2, 22) + " | " + truncate(column3, 44) + " |");
	}

	private static String truncate(final String value, final int size) {
		final String result = (value != null ? value : "") + "                                                                  ";
		return result.substring(0, size);
	}
}
