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
package io.vertigo.xml;

import io.vertigo.kernel.aop.Interceptor;
import io.vertigo.kernel.component.Plugin;
import io.vertigo.kernel.di.configurator.ComponentConfigBuilder;
import io.vertigo.kernel.di.configurator.ComponentSpaceConfigBuilder;
import io.vertigo.kernel.di.configurator.ModuleConfigBuilder;
import io.vertigo.kernel.di.configurator.PluginConfigBuilder;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.util.ClassUtil;

import java.util.Properties;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

/** 
 * @author npiedeloup, pchretien
 */
final class XMLModulesHandler extends DefaultHandler {
	private final ComponentSpaceConfigBuilder componentSpaceConfigBuilder;
	private ModuleConfigBuilder moduleConfigBuilder;
	private ComponentConfigBuilder componentConfigBuilder;
	private PluginConfigBuilder pluginConfigBuilder;
	//Global Params
	private final Properties properties;

	//---aspect
	private String annotationImplClassStr;
	private String adviceImplClassStr;

	XMLModulesHandler(final ComponentSpaceConfigBuilder componentSpaceConfigBuilder, final Properties properties) {
		Assertion.checkNotNull(componentSpaceConfigBuilder);
		Assertion.checkNotNull(properties);
		//---------------------------------------------------------------------
		this.componentSpaceConfigBuilder = componentSpaceConfigBuilder;
		this.properties = properties;
	}

	enum TagName {
		modules, module, resource, component, plugin, param, //
		aspect, advice, annotation
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
				moduleConfigBuilder = null;
				break;
			case component:
				componentConfigBuilder = null;
				break;
			case plugin:
				pluginConfigBuilder = null;
				break;
			case aspect:
				final Class<?> annotationType = ClassUtil.classForName(annotationImplClassStr);
				final Class<? extends Interceptor> adviceImplClass = ClassUtil.classForName(adviceImplClassStr, Interceptor.class);
				moduleConfigBuilder.withAspect(annotationType, adviceImplClass);
				//Reset
				annotationImplClassStr = null;
				adviceImplClassStr = null;
				break;
			case advice: //non géré
			case annotation: //non géré
			case modules: //non géré
			case param: //non géré
			case resource: //non géré
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
				moduleConfigBuilder = componentSpaceConfigBuilder.beginModule(moduleName);
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
				if (current == TagName.module) {
					moduleConfigBuilder.withResource(resourceType, evalParamValue(properties, resourcePath));
				}
				break;
			case param:
				final String paramName = attrs.getValue("name");
				final String paramValue = attrs.getValue("value");
				if (current == TagName.plugin) {
					pluginConfigBuilder.withParam(paramName, evalParamValue(properties, paramValue));
				} else {
					componentConfigBuilder.withParam(paramName, evalParamValue(properties, paramValue));
				}
				break;
			case aspect:
				//On reste dans le module
				break;
			case annotation:
				annotationImplClassStr = attrs.getValue("class");
				break;
			case advice:
				adviceImplClassStr = attrs.getValue("class");
				break;
			case modules: //non géré
			default:
		}
	}

	//-------------------------------------------------------------------------
	//----------------------------STATIC---------------------------------------
	//-------------------------------------------------------------------------

	//On recherche l'interface ayant le nom 'simpleName' dans l'arbre de la classe 'clazz'
	//Cette interface doit exister et être unique.
	private static Class<?> resolveInterface(final String simpleName, final Class<?> clazz) {
		final Class<?>[] interfaces = ClassUtil.getAllInterfaces(clazz);
		Class<?> found = null;
		for (final Class<?> interfaceClazz : interfaces) {
			if (simpleName.equals(interfaceClazz.getSimpleName())) {
				Assertion.checkState(found == null, "Many interfaces of class '{0}' have the same simpleName {1}", clazz, simpleName);
				found = interfaceClazz;
			}
		}
		Assertion.checkNotNull(found, "No interface of class '{0}' have the simpleName '{1}'", clazz, simpleName);
		return found;
	}

	private static String evalParamValue(final Properties properties, final String paramValue) {
		if (paramValue.startsWith("{") && paramValue.endsWith("}")) {
			return properties.getProperty(paramValue.substring(1, paramValue.length() - 1));
		}
		return paramValue;
	}
}
