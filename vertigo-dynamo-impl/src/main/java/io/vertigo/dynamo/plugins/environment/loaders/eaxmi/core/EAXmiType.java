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
package io.vertigo.dynamo.plugins.environment.loaders.eaxmi.core;

/**
 * Type d'objets du XMI g�r�.
 * @author pforhan
 */
public enum EAXmiType {
	/**
	 * Objet Xmi d�crivant un Package >>Package.
	 */
	Package("uml:Package"),
	/**
	 * Objet Xmi d�crivant une Classe >> DtDefinition.
	 */
	Class("uml:Class"),
	/**
	 * Objet Xmi d�crivant un Attibut d'une Classe >> DtField.
	 */
	Attribute("uml:Property"),
	/**
	 * Objet Xmi d�crivant une Association >> Association.
	 */
	Association("uml:Association"),
	/**
	 * Tag Xmi pour un �l�ment � traiter.
	 */
	PackageElement("packageElement"),
	/**
	 * Tag Xmi pour un attribut de classe.
	 */
	OwnedAttribute("ownedAttribute"),
	/**
	 * Tag d'extension EA pour un attribut.
	 */
	ClassAttribute("attribute"),
	/**
	 * Tag d'extension EA pour une association.
	 */
	Connector("connector");

	private final String code;

	private EAXmiType(final String code) {
		this.code = code;
	}

	private String getCode() {
		return code;
	}

	static EAXmiType getType(final String name) {
		final EAXmiType type;
		if (Package.getCode().equals(name)) {
			type = Package;
		} else if (Class.getCode().equals(name)) {
			type = Class;
		} else if (Attribute.getCode().equals(name)) {
			type = Attribute;
		} else if (Association.getCode().equals(name)) {
			type = Association;
		} else if (ClassAttribute.getCode().equals(name)) {
			type = ClassAttribute;
		} else if (Connector.getCode().equals(name)) {
			type = Association;
		} else {
			//rien trouv�
			type = null;
		}
		return type;
	}

	static boolean isNodeByRef(final String name) {
		boolean ok = false;
		ok = ok || Attribute.getCode().equals(name);
		ok = ok || Class.getCode().equals(name);
		ok = ok || ClassAttribute.getCode().equals(name);
		ok = ok || Package.getCode().equals(name);
		ok = ok || Connector.getCode().equals(name);
		return ok;
	}

	static boolean isObjet(final String type, final String tagName) {
		boolean ok = false;
		ok = ok || Attribute.getCode().equals(type) && OwnedAttribute.getCode().equals(tagName);
		ok = ok || Class.getCode().equals(type);
		ok = ok || Package.getCode().equals(type);
		ok = ok || Association.getCode().equals(type);
		return ok;
	}

	boolean isAttribute() {
		boolean ok = false;
		ok = ok || this == Attribute;
		return ok;
	}

	boolean isClass() {
		boolean ok = false;
		ok = ok || this == Class;
		ok = ok || this == Association;
		return ok;
	}
}
