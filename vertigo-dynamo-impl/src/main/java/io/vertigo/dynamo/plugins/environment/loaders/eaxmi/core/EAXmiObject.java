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

import io.vertigo.dynamo.plugins.environment.loaders.TagId;
import io.vertigo.lang.Assertion;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;

/**
 * Objets de l'arbre Xmi.
 * Les extensions correspondent à Enterprise Architect.
 * @author pforhan
 */
final class EAXmiObject {
	// Propriétés des attributs
	private static final String PROPERTY_NAME = "name";
	//Le libellé est dans une des deux propriétés suivantes s'il a été défini.
	// Style est prioritaire sur documentation.
	private static final String PROPERTY_COMMENT = "documentation";
	private static final String PROPERTY_ALIAS = "style";
	// Nom de la variable qui contient la valeur dans les attributs
	private static final String PROPERTY_ALIAS_NAME = "value";
	//Définition du domaine
	private static final String PROPERTY_DOMAIN = "properties";
	private static final String PROPERTY_DOMAIN_NAME = "type";

	// Identification de l'Id.
	private static final String PROPERTY_ID = "xrefs";
	private static final String PROPERTY_ID_NAME = "$DES=@PROP=@NAME=isID@ENDNAME;@TYPE=Boolean@ENDTYPE;@VALU=1@ENDVALU;";

	private static final String PROPERTY_MULTIPLICITY = "bounds";
	private static final String PROPERTY_MULTIPLICITY_LOWER_NAME = "lower";
	private static final String PROPERTY_MULTIPLICITY_UPPER_NAME = "upper";

	private static final String PROPERTY_CLASSE_A = "source";
	private static final String PROPERTY_CLASSE_B = "target";
	// On stocke les ids, les noms des classes seront misesp lus tard.
	private static final String PROPERTY_CLASSE_NAME = "xmi:idref";

	// On évite de chercher les libellés/multiplicité dans les extrémités, on prend plutôt le résumé
	private static final String PROPERTY_ROLE_MULTIPLICITY = "labels";
	private static final String PROPERTY_ROLE_A_MULTIPLICITY = "lb";
	private static final String PROPERTY_ROLE_B_MULTIPLICITY = "rb";
	private static final String PROPERTY_ROLE_A_NAME = "lt";
	private static final String PROPERTY_ROLE_B_NAME = "rt";

	private static final String PROPERTY_ROLE_NAVIGABILITY_NAME = "direction";
	private static final String PROPERTY_NAVIGABILITY_NONE = "Unspecified";
	private static final String PROPERTY_NAVIGABILITY_BI = "Bi-Directional";
	private static final String PROPERTY_NAVIGABILITY_AB = "Source -> Destination";
	private static final String PROPERTY_NAVIGABILITY_BA = "Destination -> Source";

	private final TagId id;
	private final EAXmiObject parent;
	private final EAXmiType type;
	private final List<EAXmiObject> children = new ArrayList<>();

	//Données spécifiques
	private String name;

	//=========Gestion des attributes============================
	private String label;
	private String multiplicity;
	private boolean isId;
	private String domain;
	//===========================================================

	//=========Gestion des associations==========================
	private String roleALabel;
	private String roleBLabel;

	private TagId classA;
	private TagId classB;

	private String roleAMultiplicity;
	private String roleBMultiplicity;

	private Boolean roleANavigability;
	private Boolean roleBNavigability;

	//============================================================
	private final EAXmiObject root;

	private final Logger log = Logger.getLogger(this.getClass());

	/**
	 * Constructeur de la racine
	 */
	private EAXmiObject() {
		id = null;
		type = null;
		parent = null;
		root = this;
	}

	private EAXmiObject(final EAXmiObject parent, final TagId id, final EAXmiType type) {
		Assertion.checkNotNull(parent);
		Assertion.checkNotNull(id);
		Assertion.checkNotNull(type);
		//------------------------------------------------------------------
		this.parent = parent;
		this.id = id;
		this.type = type;
		root = parent.root;
	}

	static EAXmiObject createdRoot() {
		return new EAXmiObject();
	}

	EAXmiObject createEAXmiObject(final TagId newId, final EAXmiType newType, final String leNom) {
		log.debug("Le père : " + name + " le fils " + leNom + " Le Type " + newType.name());
		final EAXmiObject created = new EAXmiObject(this, newId, newType);
		if (leNom != null) {
			created.setName(leNom);
		}
		children.add(created);
		return created;
	}

	List<EAXmiObject> getChildren() {
		return children;
	}

	EAXmiType getType() {
		return type;
	}

	EAXmiObject getParent() {
		return parent;
	}

	TagId getId() {
		return id;
	}

	boolean getIsId() {
		return isId;
	}

	String getMultiplicity() {
		return multiplicity;
	}

	String getName() {
		return name != null ? name : id.toString();
	}

	void setName(final String name) {
		this.name = name;
	}

	String getLabel() {
		return label != null ? label : getName();
	}

	String getDomain() {
		return this.domain;
	}

	//================================ASSOCIATIONS==============================

	/**
	 * @return Libellé du role du noeud A, null si non defini
	 */
	String getRoleALabel() {
		return roleALabel;
	}

	/**
	 * @return Libellé du role du noeud B, null si non defini
	 */
	String getRoleBLabel() {
		return roleBLabel;
	}

	/**
	 * @return Multiplicité du noeud A
	 */
	String getRoleAMultiplicity() {
		return roleAMultiplicity;
	}

	/**
	 * @return Multiplicité du noeud B
	 */
	String getRoleBMultiplicity() {
		return roleBMultiplicity;
	}

	/**
	 * @return Navigabilité du noeud A
	 */
	Boolean getRoleANavigability() {
		return roleANavigability;
	}

	/**
	 * @return Navigabilité du noeud B
	 */
	Boolean getRoleBNavigability() {
		return roleBNavigability;
	}

	/**
	 * @return La classe d'origine
	 */
	public TagId getClassA() {
		return classA;
	}

	/**
	 * Setter.
	 */
	public void setClassA(final TagId classA) {
		this.classA = classA;
	}

	/**
	 * @return La classe de destination
	 */
	public TagId getClassB() {
		return classB;
	}

	// Gestion des propriétés

	void setProperty(final String propertyName, final Attributes attributes) {
		Assertion.checkNotNull(propertyName);
		//----------------------------------------------------------------------
		if (PROPERTY_NAME.equals(propertyName)) {
			name = "";
		} else if (PROPERTY_COMMENT.equals(propertyName)) {
			label = attributes.getValue(PROPERTY_ALIAS_NAME);
		} else if (PROPERTY_ALIAS.equals(propertyName)) {
			label = attributes.getValue(PROPERTY_ALIAS_NAME);
		} else if (PROPERTY_DOMAIN.equals(propertyName)) {
			manageDomain(attributes);
			// Même nom pour le domaine et les navigabilité
			manageNavigability(attributes);
		} else if (PROPERTY_ID.equals(propertyName)) {
			// On peut se retrouver en fin de fichier avec des xrefs qui reviennent.
			// On ne mets à jour que si on ne l'a pas fait.
			final String valeur = attributes.getValue(PROPERTY_ALIAS_NAME);
			isId = (valeur != null && valeur.contains(PROPERTY_ID_NAME));
		} else if (PROPERTY_MULTIPLICITY.equals(propertyName)) {
			final String lower = attributes.getValue(PROPERTY_MULTIPLICITY_LOWER_NAME);
			final String upper = attributes.getValue(PROPERTY_MULTIPLICITY_UPPER_NAME);
			multiplicity = lower + ".." + upper;
		} else if (PROPERTY_ROLE_MULTIPLICITY.equals(propertyName)) {
			manageMultiplicity(attributes);
		} else if (PROPERTY_CLASSE_A.equals(propertyName)) {
			final String valeur = attributes.getValue(PROPERTY_CLASSE_NAME);
			if (valeur != null) {
				classA = new TagId(valeur);
			}
		} else if (PROPERTY_CLASSE_B.equals(propertyName)) {
			final String valeur = attributes.getValue(PROPERTY_CLASSE_NAME);
			if (valeur != null) {
				classB = new TagId(valeur);
			}
		}
		//On ne tient pas compte des autres propriétés
	}

	private void manageNavigability(final Attributes attributes) {
		final String value = attributes.getValue(PROPERTY_ROLE_NAVIGABILITY_NAME);
		if (PROPERTY_NAVIGABILITY_NONE.equals(value)) {
			roleANavigability = false;
			roleBNavigability = false;
		} else if (PROPERTY_NAVIGABILITY_BI.equals(value)) {
			roleANavigability = true;
			roleBNavigability = true;
		} else if (PROPERTY_NAVIGABILITY_AB.equals(value)) {
			roleANavigability = false;
			roleBNavigability = true;
		} else if (PROPERTY_NAVIGABILITY_BA.equals(value)) {
			roleANavigability = true;
			roleBNavigability = false;
		}
	}

	private void manageMultiplicity(final Attributes attributes) {
		roleAMultiplicity = attributes.getValue(PROPERTY_ROLE_A_MULTIPLICITY);
		roleBMultiplicity = attributes.getValue(PROPERTY_ROLE_B_MULTIPLICITY);
		roleALabel = attributes.getValue(PROPERTY_ROLE_A_NAME);
		roleBLabel = attributes.getValue(PROPERTY_ROLE_B_NAME);
		if (roleALabel != null && roleALabel.startsWith("+")) {
			roleALabel = roleALabel.substring(1);
		}
		if (roleBLabel != null && roleBLabel.startsWith("+")) {
			roleBLabel = roleBLabel.substring(1);
		}
	}

	private void manageDomain(final Attributes attributes) {
		if (domain == null || domain.isEmpty()) {
			domain = attributes.getValue(PROPERTY_DOMAIN_NAME);
		}

	}

	//==========================================================================
	//==========================================================================
	//==========================================================================
	/** {@inheritDoc} */
	@Override
	public String toString() {
		final StringBuilder buffer = new StringBuilder(type + "::" + name);
		if (type == EAXmiType.Association) {
			buffer.append(" [roleA=" + getRoleALabel() + ", roleB=" + getRoleBLabel() + "]");
		} else {
			buffer.append(" [label=" + label + ", multiplicity=" + multiplicity + "]");
		}
		return buffer.toString();
	}

	/**
	 * @return Nom du package
	 */
	String getPackageName() {
		//Si on est un package, on renvoit le nom.
		if (getType() == EAXmiType.Package) {
			return getName();
		}
		// On remonte la hiérarchie pour trouver le pacakge.
		return getParent().getPackageName();

	}

}
