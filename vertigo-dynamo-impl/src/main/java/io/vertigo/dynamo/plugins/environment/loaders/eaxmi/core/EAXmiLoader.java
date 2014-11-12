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

import io.vertigo.dynamo.plugins.environment.loaders.TagAssociation;
import io.vertigo.dynamo.plugins.environment.loaders.TagAttribute;
import io.vertigo.dynamo.plugins.environment.loaders.TagClass;
import io.vertigo.dynamo.plugins.environment.loaders.TagId;
import io.vertigo.lang.Assertion;
import io.vertigo.util.StringUtil;

import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;

/**
 * Loader de fichier XMI version Enterprise Architect.
 * @author pforhan
 */
public final class EAXmiLoader {
	private final Map<TagId, EAXmiObject> map;

	private final Logger log = Logger.getLogger(this.getClass());

	/**
	 * Constructeur.
	 * @param xmiFileURL URL du fichier XMI
	 */
	public EAXmiLoader(final URL xmiFileURL) {
		Assertion.checkNotNull(xmiFileURL);
		//----------------------------------------------------------------------
		map = new LinkedHashMap<>();
		final EAXmiHandler handler = new EAXmiHandler(map);
		try {
			SAXParserFactory.newInstance().newSAXParser().parse(xmiFileURL.openStream(), handler);
		} catch (final Exception e) {
			throw new RuntimeException("erreur lors de la lecture du fichier xmi : " + xmiFileURL, e);
		}
	}

	/**
	 * Récupération des classes déclarées dans le XMI.
	 * @return Liste des classes
	 */
	public List<TagClass> getClasses() {
		final List<TagClass> list = new ArrayList<>();
		for (final EAXmiObject obj : map.values()) {
			log.debug("classe : " + obj.toString());
			//On ne conserve que les classes et les domaines
			if (obj.getType() == EAXmiType.Class) {
				list.add(createEAXmiClass(obj));
			}
		}
		return java.util.Collections.unmodifiableList(list);
	}

	/**
	 * Récupération des associations déclarées dans le XMI.
	 * @return Liste des associations
	 */
	public List<TagAssociation> getAssociations() {
		final List<TagAssociation> list = new ArrayList<>();
		for (final EAXmiObject obj : map.values()) {
			if (obj.getType() == EAXmiType.Association) {
				final TagAssociation associationXmi = buildDynAssociation(obj);
				if (associationXmi != null) {
					list.add(associationXmi);
				}
			}
		}
		return java.util.Collections.unmodifiableList(list);
	}

	private TagClass createEAXmiClass(final EAXmiObject obj) {
		log.debug("Creation de classe : " + obj.getName());
		//On recherche les attributs (>DtField) de cette classe(>Dt_DEFINITION)
		final String code = obj.getName().toUpperCase();
		final String packageName = obj.getParent().getPackageName();

		final List<TagAttribute> keyAttributes = new ArrayList<>();
		final List<TagAttribute> fieldAttributes = new ArrayList<>();
		for (final EAXmiObject child : obj.getChildren()) {
			if (child.getType() == EAXmiType.Attribute) {
				log.debug("Attribut = " + child.getName() + " isId = " + Boolean.toString(child.getIsId()));
				if (child.getIsId()) {
					final TagAttribute attributeXmi = createDynAttribute(child, true);
					keyAttributes.add(attributeXmi);
				} else {
					fieldAttributes.add(createDynAttribute(child, false));
				}
			}
		}
		return new TagClass(code, packageName, keyAttributes, fieldAttributes);
	}

	private static TagAttribute createDynAttribute(final EAXmiObject obj, final boolean isPK) {
		final boolean notNull;
		if (isPK) {
			//La pk est toujours notNull
			notNull = true;
		} else {
			notNull = "1..1".equals(obj.getMultiplicity());
		}

		// L'information de persistence ne peut pas être déduite du Xmi, tous les champs sont déclarés persistent de facto
		return new TagAttribute(obj.getName().toUpperCase(), obj.getLabel(), true, notNull, obj.getDomain());
	}

	/**
	 * Création d'une association.
	 * @param obj ObjectOOM
	 * @return Association
	 */
	private TagAssociation buildDynAssociation(final EAXmiObject obj) {
		log.debug("Créer association :" + obj.getName());
		final String code = obj.getName().toUpperCase();
		final String packageName = obj.getParent().getPackageName();

		final String multiplicityA = obj.getRoleAMultiplicity();
		final String multiplicityB = obj.getRoleBMultiplicity();

		//On recherche les objets référencés par l'association.
		final EAXmiObject objectB = map.get(obj.getClassB());
		final EAXmiObject objectA = map.get(obj.getClassA());

		if (objectA == null || objectB == null) {
			throw new IllegalArgumentException("Noeuds de l'association introuvables");
		}
		//Si les roles ne sont pas renseignés ont prend le nom de la table en CamelCase.
		final String roleLabelA = obj.getRoleALabel() != null ? obj.getRoleALabel() : StringUtil.constToCamelCase(objectA.getName(), true);
		final String roleLabelB = obj.getRoleBLabel() != null ? obj.getRoleBLabel() : StringUtil.constToCamelCase(objectB.getName(), true);
		// Si il n'existe pas de libelle pour un role donné alors on utilise le nom de l'objet référencé.
		//Le code du role est déduit du libellé.

		//Attention pamc inverse dans oom les déclarations des objets !!
		final String codeA = objectA.getName().toUpperCase();
		final String codeB = objectB.getName().toUpperCase();

		// associationDefinition.
		//On recherche les attributs (>DtField) de cet classe(>Dt_DEFINITION)

		// navigabilités sont optionnelles; elles sont déduites de la multiplicités quand elles ne sont pas renseignées
		final boolean navigabilityA = obj.getRoleANavigability() == null ? false : obj.getRoleANavigability();
		final boolean navigabilityB = obj.getRoleBNavigability() == null ? true : obj.getRoleBNavigability();
		return new TagAssociation(code, packageName, multiplicityA, multiplicityB, roleLabelA, roleLabelB, codeA, codeB, navigabilityA, navigabilityB);
	}

}
