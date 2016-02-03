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
package io.vertigo.vega.plugins.webservice.loaders;

import io.vertigo.app.Home;
import io.vertigo.core.definition.dsl.dynamic.DynamicDefinitionRepository;
import io.vertigo.core.definition.loader.LoaderPlugin;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.WrappedException;
import io.vertigo.vega.plugins.webservice.scanner.annotations.AnnotationsWebServiceScannerUtil;
import io.vertigo.vega.webservice.WebServices;
import io.vertigo.vega.webservice.metamodel.WebServiceDefinition;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.reflections.Reflections;

/**
 * Lecture des webServices par annotations présentes sur les objets du classPath.
 * @author npiedeloup
 */
public final class ClassPathWebServiceLoaderPlugin implements LoaderPlugin {

	/** {@inheritDoc} */
	@Override
	public String getType() {
		return "webservice";
	}

	/** {@inheritDoc} */
	@Override
	public void load(final String classPathPrefix, final DynamicDefinitionRepository dynamicModelrepository) {
		Assertion.checkArgNotEmpty(classPathPrefix);
		//-----
		final Iterable<Class<? extends WebServices>> classes = getWebServicesClasses(classPathPrefix);
		//--Enregistrement des fichiers java annotés
		for (final Class<? extends WebServices> webServicesClass : classes) {
			scanWebServices(webServicesClass);
		}
	}

	/**
	 * @return Liste des fichiers Java représentant des webServices.
	 */
	private static Set<Class<? extends WebServices>> getWebServicesClasses(final String classPathPrefix) {
		if (classPathPrefix.endsWith("*")) {
			final Set<Class<? extends WebServices>> webServicesClasses = new Reflections(classPathPrefix.substring(0, classPathPrefix.length() - 1)).getSubTypesOf(WebServices.class);
			Assertion.checkArgument(webServicesClasses.size() > 0, "No webServices found with prefix {0}", classPathPrefix);
			return webServicesClasses;
		}
		try {
			final Class oneClass = Class.forName(classPathPrefix);
			Assertion.checkArgument(WebServices.class.isAssignableFrom(oneClass), "ClassPathWebServiceLoaderPlugin needs a full className or a classPath prefix endsWith * : {0}", classPathPrefix);
			return (Set) Collections.singleton(oneClass);
		} catch (final ClassNotFoundException e) {
			throw new WrappedException("ClassPathWebServiceLoaderPlugin needs a full className or a classPath prefix endsWith * : " + classPathPrefix, e);
		}
	}

	private static void scanWebServices(final Class<? extends WebServices> webServicesClass) {
		final List<WebServiceDefinition> webServiceDefinitions = AnnotationsWebServiceScannerUtil.scanWebService(webServicesClass);
		for (final WebServiceDefinition webServiceDefinition : webServiceDefinitions) {
			Home.getApp().getDefinitionSpace().put(webServiceDefinition);
		}
	}

}
