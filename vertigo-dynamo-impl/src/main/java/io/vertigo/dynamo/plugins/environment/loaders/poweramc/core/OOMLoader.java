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
package io.vertigo.dynamo.plugins.environment.loaders.poweramc.core;

import io.vertigo.dynamo.plugins.environment.loaders.xml.XmlAssociation;
import io.vertigo.dynamo.plugins.environment.loaders.xml.XmlAttribute;
import io.vertigo.dynamo.plugins.environment.loaders.xml.XmlClass;
import io.vertigo.dynamo.plugins.environment.loaders.xml.XmlId;
import io.vertigo.dynamo.plugins.environment.loaders.xml.XmlLoader;
import io.vertigo.lang.Assertion;
import io.vertigo.util.StringUtil;

import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.SAXParserFactory;

/**
 * Chargement d'un fichier OOM.
 * Seules les classes et leurs attributs ainsi que les associations sont extraites.
 * @author pchretien
 */
public final class OOMLoader implements XmlLoader {
	private final Map<XmlId, OOMObject> map;

	/**
	 * Constructeur.
	 * @param powerAMCURL URL du fichier PowerAMC
	 */
	public OOMLoader(final URL powerAMCURL) {
		Assertion.checkNotNull(powerAMCURL);
		//-----
		map = new LinkedHashMap<>();
		final OOMHandler handler = new OOMHandler(map);
		try {
			SAXParserFactory.newInstance().newSAXParser().parse(powerAMCURL.openStream(), handler);
		} catch (final Exception e) {
			throw new RuntimeException("erreur lors de la lecture du fichier oom : " + powerAMCURL, e);
		}
	}

	@Override
	public List<XmlClass> getClasses() {
		final List<XmlClass> list = new ArrayList<>();
		for (final OOMObject obj : map.values()) {
			//On ne conserve que les classes et les domaines
			if (obj.getType() == OOMType.Class) {
				list.add(createClass(obj));
			}
		}
		return java.util.Collections.unmodifiableList(list);
	}

	@Override
	public List<XmlAssociation> getAssociations() {
		final List<XmlAssociation> list = new ArrayList<>();
		for (final OOMObject obj : map.values()) {
			if (obj.getType() == OOMType.Association) {
				final XmlAssociation associationOOM = createAssociation(obj);
				if (associationOOM != null) {
					list.add(associationOOM);
				}
			}
		}
		return java.util.Collections.unmodifiableList(list);
	}

	private XmlClass createClass(final OOMObject obj) {
		//On recherche les attributs (>DtField) de cet classe(>Dt_DEFINITION)
		final String code = obj.getCode();
		final String packageName = obj.getParent().getPackageName();
		//On recherche les PrimaryIdentifiers :
		//La class possède
		//- une liste des identifiers qui référencent des champs
		//- un bloc <c:PrimaryIdentifier> (non parsé) qui référence les primaryIdentifiers
		//C'est pourquoi on a une double redirection
		final List<XmlId> pkList = new ArrayList<>();
		for (final XmlId ref : obj.getRefList()) {
			final OOMObject childRef = map.get(ref); //On recherche les references vers identifiers (ceux dans PrimaryIdentifier)
			if (childRef != null && childRef.getType() == OOMType.Identifier) {
				pkList.addAll(childRef.getRefList()); //On recherche les champs pointé par l'identifier
			}
		}

		final List<XmlAttribute> keyAttributes = new ArrayList<>();
		final List<XmlAttribute> fieldAttributes = new ArrayList<>();
		for (final OOMObject child : obj.getChildren()) {
			if (child.getType() == OOMType.Attribute) {
				if (pkList.contains(child.getId())) {
					final XmlAttribute attributeOOm = createAttribute(child, true);
					keyAttributes.add(attributeOOm);
				} else {
					fieldAttributes.add(createAttribute(child, false));
				}
			}
		}
		return new XmlClass(code, packageName, keyAttributes, fieldAttributes);
	}

	private XmlAttribute createAttribute(final OOMObject obj, final boolean isPK) {
		final String code = obj.getCode();
		final String label = obj.getLabel();
		final boolean persistent = !"0".equals(obj.getPersistent());

		final boolean notNull;
		if (isPK) {
			//La pk est toujours notNull
			notNull = true;
		} else {
			notNull = "1..1".equals(obj.getMultiplicity());
		}

		//Domain
		String domain = null;
		for (final XmlId ref : obj.getRefList()) {
			final OOMObject childRef = map.get(ref);
			if (childRef != null && childRef.getType() == OOMType.Domain) {
				Assertion.checkState(domain == null, "domain deja affecté");
				domain = childRef.getCode();
			}
		}
		return new XmlAttribute(code, label, persistent, notNull, domain);
	}

	/**
	 * Création d'une association.
	 * @param obj ObjectOOM
	 * @return Association
	 */
	private XmlAssociation createAssociation(final OOMObject obj) {
		final String code = obj.getCode();
		final String packageName = obj.getParent().getPackageName();

		final String multiplicityA = obj.getRoleAMultiplicity();
		final String multiplicityB = obj.getRoleBMultiplicity();

		//On recherche les objets référencés par l'association.
		OOMObject objectB = null;
		OOMObject objectA = null;
		for (final XmlId ref : obj.getRefList()) {
			final OOMObject childRef = map.get(ref);
			if (childRef != null && (childRef.getType() == OOMType.Class || childRef.getType() == OOMType.Shortcut)) {
				if (objectB == null) {
					objectB = childRef;
				} else if (objectA == null) {
					objectA = childRef;
				} else {
					throw new IllegalStateException("objectB and objectA can't be null at the same time.");
				}
			}
		}

		if (objectA == null || objectB == null) {
			throw new IllegalArgumentException("Noeuds de l'association introuvables");
		}
		//Si les roles ne sont pas renseignés ont prend le nom de la table en CamelCase.
		final String roleLabelA = obj.getRoleALabel() != null ? obj.getRoleALabel() : StringUtil.constToCamelCase(objectA.getName(), true);
		final String roleLabelB = obj.getRoleBLabel() != null ? obj.getRoleBLabel() : StringUtil.constToCamelCase(objectB.getName(), true);
		// Si il n'existe pas de libelle pour un role donné alors on utilise le nom de l'objet référencé.
		//Le code du role est déduit du libellé.

		//Attention pamc inverse dans oom les déclarations des objets !!
		final String codeA = objectA.getCode();
		final String codeB = objectB.getCode();

		// associationDefinition.
		//On recherche les attributs (>DtField) de cet classe(>Dt_DEFINITION)

		final boolean navigabilityA = obj.getRoleANavigability();
		final boolean navigabilityB = obj.getRoleBNavigability();

		return new XmlAssociation(code, packageName, multiplicityA, multiplicityB, roleLabelA, roleLabelB, codeA, codeB, navigabilityA, navigabilityB);
	}
}
