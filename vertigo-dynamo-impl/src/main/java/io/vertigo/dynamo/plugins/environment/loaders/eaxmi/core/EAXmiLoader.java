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

import io.vertigo.kernel.exception.VRuntimeException;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.util.StringUtil;

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
	private final Map<EAXmiId, EAXmiObject> map;

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
			throw new VRuntimeException("erreur lors de la lecture du fichier xmi : " + xmiFileURL, e);
		}
	}

	/**
	 * R�cup�ration des classes d�clar�es dans le XMI.
	 * @return Liste des classes
	 */
	public List<EAXmiClass> getClassList() {
		final List<EAXmiClass> list = new ArrayList<>();
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
	 * R�cup�ration des associations d�clar�es dans le XMI.
	 * @return Liste des associations
	 */
	public List<EAXmiAssociation> getAssociationList() {
		final List<EAXmiAssociation> list = new ArrayList<>();
		for (final EAXmiObject obj : map.values()) {
			if (obj.getType() == EAXmiType.Association) {
				final EAXmiAssociation associationXmi = buildDynAssociation(obj);
				if (associationXmi != null) {
					list.add(associationXmi);
				}
			}
		}
		return java.util.Collections.unmodifiableList(list);
	}

	private EAXmiClass createEAXmiClass(final EAXmiObject obj) {
		log.debug("Creation de classe : " + obj.getName());
		//On recherche les attributs (>DtField) de cette classe(>Dt_DEFINITION)
		final String code = obj.getName().toUpperCase();
		final String packageName = obj.getParent().getPackageName();

		final List<EAXmiAttribute> keyAttributes = new ArrayList<>();
		final List<EAXmiAttribute> fieldAttributes = new ArrayList<>();
		for (final EAXmiObject child : obj.getChildList()) {
			if (child.getType() == EAXmiType.Attribute) {
				log.debug("Attribut = " + child.getName() + " isId = " + Boolean.toString(child.getIsId()));
				if (child.getIsId()) {
					final EAXmiAttribute attributeXmi = createDynAttribute(child, true);
					keyAttributes.add(attributeXmi);
				} else {
					fieldAttributes.add(createDynAttribute(child, false));
				}
			}
		}
		return new EAXmiClass(code, packageName, keyAttributes, fieldAttributes);
	}

	private static EAXmiAttribute createDynAttribute(final EAXmiObject obj, final boolean isPK) {
		final boolean notNull;
		if (isPK) {
			//La pk est toujours notNull
			notNull = true;
		} else {
			notNull = "1..1".equals(obj.getMultiplicity());
		}

		return new EAXmiAttribute(obj.getName().toUpperCase(), obj.getLabel(), notNull, obj.getDomain());
	}

	/**
	 * Création d'une association.
	 * @param obj ObjectOOM
	 * @return Association 
	 */
	private EAXmiAssociation buildDynAssociation(final EAXmiObject obj) {
		log.debug("Cr�er association :" + obj.getName());
		final String code = obj.getName().toUpperCase();
		final String packageName = obj.getParent().getPackageName();

		final String multiplicityA = obj.getRoleAMultiplicity();
		final String multiplicityB = obj.getRoleBMultiplicity();

		//On recherche les objets r�f�renc�s par l'association.
		EAXmiObject objectB = map.get(obj.getClassB());
		EAXmiObject objectA = map.get(obj.getClassA());

		if (objectA == null || objectB == null) {
			throw new IllegalArgumentException("Noeuds de l'association introuvables");
		}
		//Si les roles ne sont pas renseign�s ont prend le nom de la table en CamelCase.
		final String roleLabelA = obj.getRoleALabel() != null ? obj.getRoleALabel() : StringUtil.constToCamelCase(objectA.getName(), true);
		final String roleLabelB = obj.getRoleBLabel() != null ? obj.getRoleBLabel() : StringUtil.constToCamelCase(objectB.getName(), true);
		// Si il n'existe pas de libelle pour un role donn� alors on utilise le nom de l'objet r�f�renc�.
		//Le code du role est d�duit du libell�.

		//Attention pamc inverse dans oom les d�clarations des objets !!
		final String codeA = objectA.getName().toUpperCase();
		final String codeB = objectB.getName().toUpperCase();

		// associationDefinition.
		//On recherche les attributs (>DtField) de cet classe(>Dt_DEFINITION)

		// navigabilit�s sont optionnelles; elles sont d�duites de la multiplicit�s quand elles ne sont pas renseignées
		final boolean navigabilityA = obj.getRoleANavigability() == null ? false : obj.getRoleANavigability();
		final boolean navigabilityB = obj.getRoleBNavigability() == null ? true : obj.getRoleBNavigability();
		return new EAXmiAssociation(code, packageName, multiplicityA, multiplicityB, roleLabelA, roleLabelB, codeA, codeB, navigabilityA, navigabilityB);
	}

}
