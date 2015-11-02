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
package io.vertigo.core.plugins.param.xml;

import io.vertigo.lang.Assertion;

import java.util.HashMap;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author  pchretien
 */
final class XmlConfigHandler extends DefaultHandler {
	enum TagName {
		applicationConfig, config, property;

		static TagName valueOf2(final String value) {
			if ("application-config".equals(value)) {
				return applicationConfig;
			}
			return valueOf(value);
		}
	}

	private final Map<String, Map<String, String>> configs;
	private Map<String, String> currentConfig;

	XmlConfigHandler(final Map<String, Map<String, String>> configs) {
		Assertion.checkNotNull(configs);
		//-----
		this.configs = configs;
	}

	@Override
	public void endElement(final String namespaceURI, final String localName, final String qName) {
		String eName = localName; // Element name
		if ("".equals(eName)) {
			eName = qName;
		}
		switch (TagName.valueOf2(eName)) {
			case applicationConfig:
				break;
			case config:
				currentConfig = null;
				break;
			case property:
				break;
			default:
		}
	}

	@Override
	public void startElement(final String namespaceURI, final String localName, final String qName, final Attributes attrs) {
		String eName = localName; // Element name
		if ("".equals(eName)) {
			eName = qName;
		}
		switch (TagName.valueOf2(eName)) {
			case applicationConfig:
				break;
			case config:
				currentConfig = new HashMap<>();
				//
				final String configName = attrs.getValue("name");
				configs.put(configName, currentConfig);
				break;
			case property:
				final String propertyName = attrs.getValue("name").trim();
				final String propertyValue = attrs.getValue("value").trim();
				currentConfig.put(propertyName, propertyValue);
				break;
			default:
		}
	}
}
