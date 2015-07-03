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
package io.vertigo.boot.xml;

import io.vertigo.core.aop.Aspect;
import io.vertigo.core.config.AppConfigBuilder;
import io.vertigo.core.config.ComponentConfigBuilder;
import io.vertigo.core.config.ModuleConfigBuilder;
import io.vertigo.core.config.PluginConfigBuilder;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Plugin;
import io.vertigo.util.ClassUtil;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author npiedeloup, pchretien
 */
final class XMLModulesHandler extends DefaultHandler {
	private ModuleConfigBuilder moduleConfigBuilder;
	private ComponentConfigBuilder componentConfigBuilder;
	private PluginConfigBuilder pluginConfigBuilder;
	//Global Params
	private final XMLModulesParams params;
	//We are populating moduleConfigs during parsing
	private final AppConfigBuilder appConfigBuilder;

	XMLModulesHandler(final AppConfigBuilder appConfigBuilder, final XMLModulesParams params) {
		Assertion.checkNotNull(appConfigBuilder);
		Assertion.checkNotNull(params);
		//-----
		this.appConfigBuilder = appConfigBuilder;
		this.params = params;
	}

	enum TagName {
		config,
		module,
		resource,
		component, plugin, param, aspect
	}

	private TagName current;

	@Override
	public void endElement(final String namespaceURI, final String localName, final String qName) {
		String eName = localName; // Element name
		if ("".equals(eName)) {
			eName = qName;
		}
		switch (TagName.valueOf(eName)) {
			case module:
				moduleConfigBuilder.endModule();
				moduleConfigBuilder = null;
				break;
			case component:
				componentConfigBuilder.endComponent();
				componentConfigBuilder = null;
				break;
			case plugin:
				pluginConfigBuilder.endPlugin();
				pluginConfigBuilder = null;
				break;
			case aspect: //non géré
			case param: //non géré
			case resource: //non géré
			case config: //non géré
			default:
		}
	}

	@Override
	public void startElement(final String namespaceURI, final String localName, final String qName, final Attributes attrs) {
		String eName = localName; // Element name
		if ("".equals(eName)) {
			eName = qName;
		}
		switch (TagName.valueOf(eName)) {
			case module:
				current = TagName.module;
				final String moduleName = attrs.getValue("name");
				final String api = attrs.getValue("api");
				final String superClass = attrs.getValue("inheritance");
				moduleConfigBuilder = appConfigBuilder.beginModule(moduleName);
				if (api != null) {
					if (!Boolean.valueOf(api)) {
						moduleConfigBuilder.withNoAPI();
					}
				}
				if (superClass != null) {
					moduleConfigBuilder.withInheritance(ClassUtil.classForName(superClass));
				}
				break;
			case component:
				current = TagName.component;
				final String componentApi = attrs.getValue("api");
				final Class<?> componentImplClass = ClassUtil.classForName(attrs.getValue("class"));
				if (componentApi != null) {
					final Class<?> componentClass = resolveInterface(componentApi, componentImplClass);
					componentConfigBuilder = moduleConfigBuilder.beginComponent(componentClass, componentImplClass);
				} else {
					componentConfigBuilder = moduleConfigBuilder.beginComponent(componentImplClass);
				}
				if (attrs.getValue("initClass") != null) {
					final Class componentInitialierClass = ClassUtil.classForName(attrs.getValue("initClass"));
					componentConfigBuilder.withInitializer(componentInitialierClass);
				}
				break;
			case plugin:
				current = TagName.plugin;
				final Class<? extends Plugin> pluginImplClass = ClassUtil.classForName(attrs.getValue("class"), Plugin.class);
				pluginConfigBuilder = componentConfigBuilder.beginPlugin(pluginImplClass);
				break;
			case resource:
				final String resourceType = attrs.getValue("type");
				final String resourcePath = attrs.getValue("path");
				moduleConfigBuilder.addResource(resourceType, evalParamValue(resourcePath));
				break;
			case param:
				final String paramName = attrs.getValue("name");
				final String paramValue = attrs.getValue("value");
				if (current == TagName.plugin) {
					pluginConfigBuilder.addParam(paramName, evalParamValue(paramValue));
				} else {
					componentConfigBuilder.addParam(paramName, evalParamValue(paramValue));
				}
				break;
			case aspect:
				final String aspectImplClassStr = attrs.getValue("class");
				final Class<? extends Aspect> aspectImplClass = ClassUtil.classForName(aspectImplClassStr, Aspect.class);
				moduleConfigBuilder.addAspect(aspectImplClass);
				break;
			case config: //non géré
			default:
		}
	}

	//On recherche l'interface ayant le nom 'simpleName' dans l'arbre de la classe 'clazz'
	//Cette interface doit exister et être unique.
	private static Class<?> resolveInterface(final String simpleName, final Class<?> clazz) {
		Class<?> found = null;
		for (final Class<?> interfaceClazz : ClassUtil.getAllInterfaces(clazz)) {
			if (simpleName.equals(interfaceClazz.getSimpleName())) {
				Assertion.checkState(found == null, "Many interfaces of class '{0}' have the same simpleName {1}", clazz, simpleName);
				found = interfaceClazz;
			}
		}
		Assertion.checkNotNull(found, "No interface of class '{0}' have the simpleName '{1}'", clazz, simpleName);
		return found;
	}

	private String evalParamValue(final String paramValue) {
		if (paramValue.startsWith("{") && paramValue.endsWith("}")) {
			return params.getParam(paramValue.substring(1, paramValue.length() - 1));
		}
		return paramValue;
	}
}
