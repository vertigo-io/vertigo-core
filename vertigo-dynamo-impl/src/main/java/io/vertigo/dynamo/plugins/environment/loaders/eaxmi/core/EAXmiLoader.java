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
package io.vertigo.dynamo.plugins.environment.loaders.eaxmi.core;

import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;

import io.vertigo.dynamo.plugins.environment.loaders.xml.XmlAssociation;
import io.vertigo.dynamo.plugins.environment.loaders.xml.XmlAttribute;
import io.vertigo.dynamo.plugins.environment.loaders.xml.XmlClass;
import io.vertigo.dynamo.plugins.environment.loaders.xml.XmlId;
import io.vertigo.dynamo.plugins.environment.loaders.xml.XmlLoader;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.WrappedException;
import io.vertigo.util.ListBuilder;
import io.vertigo.util.StringUtil;

/**
 * Loader de fichier XMI version Enterprise Architect.
 * @author pforhan
 */
public final class EAXmiLoader implements XmlLoader {
	private final Map<XmlId, EAXmiObject> map;

	private static final Logger LOG = Logger.getLogger(EAXmiLoader.class);

	/**
	 * Constructeur.
	 * @param xmiFileURL URL du fichier XMI
	 */
	public EAXmiLoader(final URL xmiFileURL) {
		Assertion.checkNotNull(xmiFileURL);
		//-----
		map = new LinkedHashMap<>();
		final EAXmiHandler handler = new EAXmiHandler(map);
		try {
			final SAXParserFactory factory = SAXParserFactory.newInstance();
			factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

			final SAXParser saxParser = factory.newSAXParser();
			saxParser.parse(xmiFileURL.openStream(), handler);
		} catch (final Exception e) {
			throw new WrappedException("erreur lors de la lecture du fichier xmi : " + xmiFileURL, e);
		}
	}

	/**
	 * Récupération des classes déclarées dans le XMI.
	 * @return Liste des classes
	 */
	@Override
	public List<XmlClass> getClasses() {
		final ListBuilder<XmlClass> listBuilder = new ListBuilder<>();
		for (final EAXmiObject obj : map.values()) {
			LOG.debug("classe : " + obj);
			//On ne conserve que les classes et les domaines
			if (obj.getType() == EAXmiType.Class) {
				listBuilder.add(createClass(obj));
			}
		}
		return listBuilder.unmodifiable().build();
	}

	/**
	 * Récupération des associations déclarées dans le XMI.
	 * @return Liste des associations
	 */
	@Override
	public List<XmlAssociation> getAssociations() {
		final ListBuilder<XmlAssociation> listBuilder = new ListBuilder<>();
		for (final EAXmiObject obj : map.values()) {
			if (obj.getType() == EAXmiType.Association) {
				final XmlAssociation associationXmi = createAssociation(obj);
				if (associationXmi != null) {
					listBuilder.add(associationXmi);
				}
			}
		}
		return listBuilder.unmodifiable().build();
	}

	private static XmlClass createClass(final EAXmiObject obj) {
		LOG.debug("Creation de classe : " + obj.getName());
		//On recherche les attributs (>DtField) de cette classe(>Dt_DEFINITION)
		final String code = obj.getName().toUpperCase();
		final String packageName = obj.getParent().getPackageName();
		final String stereotype = obj.getStereotype();

		final List<XmlAttribute> keyAttributes = new ArrayList<>();
		final List<XmlAttribute> fieldAttributes = new ArrayList<>();
		for (final EAXmiObject child : obj.getChildren()) {
			if (child.getType() == EAXmiType.Attribute) {
				LOG.debug("Attribut = " + child.getName() + " isId = " + Boolean.toString(child.getIsId()));
				if (child.getIsId()) {
					final XmlAttribute attributeXmi = createAttribute(child, true);
					keyAttributes.add(attributeXmi);
				} else {
					fieldAttributes.add(createAttribute(child, false));
				}
			}
		}
		return new XmlClass(code, packageName, stereotype, keyAttributes, fieldAttributes);
	}

	private static XmlAttribute createAttribute(final EAXmiObject obj, final boolean isPK) {
		final String code = obj.getName().toUpperCase();
		final String label = obj.getLabel();
		final boolean persistent = true;

		final boolean notNull;
		if (isPK) {
			//La pk est toujours notNull
			notNull = true;
		} else {
			notNull = "1..1".equals(obj.getMultiplicity());
		}

		// L'information de persistence ne peut pas être déduite du Xmi, tous les champs sont déclarés persistent de facto
		return new XmlAttribute(code, label, persistent, notNull, obj.getDomain());
	}

	/**
	 * Création d'une association.
	 * @param obj ObjectOOM
	 * @return Association
	 */
	private XmlAssociation createAssociation(final EAXmiObject obj) {
		LOG.debug("Créer association :" + obj.getName());
		//On recherche les objets référencés par l'association.
		final EAXmiObject objectB = map.get(obj.getClassB());
		final EAXmiObject objectA = map.get(obj.getClassA());

		if (objectA == null || objectB == null) {
			throw new IllegalArgumentException("Noeuds de l'association introuvables");
		}

		final String code = obj.getName().toUpperCase();
		final String packageName = obj.getParent().getPackageName();

		final String multiplicityA = obj.getRoleAMultiplicity();
		final String multiplicityB = obj.getRoleBMultiplicity();

		//Si les roles ne sont pas renseignés ont prend le nom de la table en CamelCase.
		final String roleLabelA = obj.getRoleALabel() != null ? obj.getRoleALabel() : StringUtil.constToUpperCamelCase(objectA.getName());
		final String roleLabelB = obj.getRoleBLabel() != null ? obj.getRoleBLabel() : StringUtil.constToUpperCamelCase(objectB.getName());
		// Si il n'existe pas de libelle pour un role donné alors on utilise le nom de l'objet référencé.
		//Le code du role est déduit du libellé.

		//Attention pamc inverse dans oom les déclarations des objets !!
		final String codeA = objectA.getName().toUpperCase();
		final String codeB = objectB.getName().toUpperCase();

		// associationDefinition.
		//On recherche les attributs (>DtField) de cet classe(>Dt_DEFINITION)
		final boolean navigabilityA = obj.getRoleANavigability();
		final boolean navigabilityB = obj.getRoleBNavigability();
		return new XmlAssociation(code, packageName, multiplicityA, multiplicityB, roleLabelA, roleLabelB, codeA, codeB, navigabilityA, navigabilityB);
	}

}
