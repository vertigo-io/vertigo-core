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
package io.vertigo.studio.plugins.mda.webservice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import io.vertigo.app.Home;
import io.vertigo.lang.Assertion;
import io.vertigo.studio.impl.mda.GeneratorPlugin;
import io.vertigo.studio.mda.MdaResultBuilder;
import io.vertigo.studio.plugins.mda.FileGenerator;
import io.vertigo.studio.plugins.mda.FileGeneratorConfig;
import io.vertigo.studio.plugins.mda.webservice.model.WebServiceDefinitionModel;
import io.vertigo.util.MapBuilder;
import io.vertigo.vega.webservice.metamodel.WebServiceDefinition;

/**
 * Generation des objets relatifs au module Vega.
 * @author npiedeloup
 */
public final class WsJsGeneratorPlugin implements GeneratorPlugin {

	private final String targetSubDir;

	/**
	 * Constructeur.
	 * @param targetSubDir Repertoire de generation des fichiers de ce plugin
	 */
	@Inject
	public WsJsGeneratorPlugin(@Named("targetSubDir") final String targetSubDir) {
		//-----
		this.targetSubDir = targetSubDir;
	}

	/** {@inheritDoc} */
	@Override
	public void generate(final FileGeneratorConfig fileGeneratorConfig, final MdaResultBuilder mdaResultBuilder) {
		Assertion.checkNotNull(fileGeneratorConfig);
		Assertion.checkNotNull(mdaResultBuilder);
		//-----
		generateRoute(targetSubDir, fileGeneratorConfig, mdaResultBuilder);
	}

	private static Collection<WebServiceDefinition> getWebServiceDefinitions() {
		return Home.getApp().getDefinitionSpace().getAll(WebServiceDefinition.class);
	}

	private static void generateRoute(final String targetSubDir, final FileGeneratorConfig fileGeneratorConfig, final MdaResultBuilder mdaResultBuilder) {
		final Collection<WebServiceDefinition> webServiceDefinitions = getWebServiceDefinitions();
		if (!webServiceDefinitions.isEmpty()) {
			final Map<String, List<WebServiceDefinitionModel>> webServicesPerFacades = new HashMap<>();
			for (final WebServiceDefinition webServiceDefinition : webServiceDefinitions) {
				final String facadeName = webServiceDefinition.getMethod().getDeclaringClass().getSimpleName().replaceAll("WebServices", "");
				List<WebServiceDefinitionModel> facadeWebServiceDefinitions = webServicesPerFacades.get(facadeName);
				if (facadeWebServiceDefinitions == null) {
					facadeWebServiceDefinitions = new ArrayList<>();
					webServicesPerFacades.put(facadeName, facadeWebServiceDefinitions);
				}
				facadeWebServiceDefinitions.add(new WebServiceDefinitionModel(webServiceDefinition));
			}

			for (final Map.Entry<String, List<WebServiceDefinitionModel>> entry : webServicesPerFacades.entrySet()) {
				final String simpleClassName = entry.getKey();
				final String javaFacadeName = entry.getValue().get(0).getJavaFacadeName();
				final String prefix = entry.getValue().get(0).getPathPrefix();
				final String root = prefix != null ? prefix : "/";
				final Map<String, Object> model = new MapBuilder<String, Object>()
						.put("facade", javaFacadeName)
						.put("root", root)
						.put("routes", entry.getValue())
						.build();

				FileGenerator.builder(fileGeneratorConfig)
						.withModel(model)
						.withFileName(simpleClassName + ".js")
						.withGenSubDir(targetSubDir)
						.withPackageName("")
						.withTemplateName("webservice/template/routejs.ftl")
						.build()
						.generateFile(mdaResultBuilder);
			}

		}
	}
}
