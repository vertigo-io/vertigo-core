package io.vertigo.dynamo.plugins.environment.loaders.poweramc.core;

import io.vertigo.kernel.lang.Assertion;

import java.util.ArrayList;
import java.util.List;

/**
 * Objets de la grammaire powerAMC.
 * Chaque objet de la grammaire possède une série d'attributs, ceux-ci sont présentés ci-dessous.
 * Il s'agit de faciliter la transposition du Modèle générique XML en un Modèle typé.
 * @author pchretien
 * @version $Id: ObjectOOM.java,v 1.4 2013/10/22 12:30:19 pchretien Exp $
 */
final class OOMObject {
	private static final String PROPERTY_CODE = "a:Code";
	private static final String PROPERTY_NAME = "a:Name";
	private static final String PROPERTY_COMMENT = "a:Comment";
	private static final String PROPERTY_PERSISTENT = "a:Persistent";
	private static final String PROPERTY_MULTIPLICITY = "a:Multiplicity";

	private static final String PROPERTY_ROLE_A_MULTIPLICITY = "a:RoleAMultiplicity";
	private static final String PROPERTY_ROLE_B_MULTIPLICITY = "a:RoleBMultiplicity";

	private static final String PROPERTY_ROLE_A_NAVIGABILITY = "a:RoleANavigability";
	private static final String PROPERTY_ROLE_B_NAVIGABILITY = "a:RoleBNavigability";

	//Dans l'OOM le nom du role correspond au libellé et non au code !!
	private static final String PROPERTY_ROLE_A_NAME = "a:RoleAName";
	private static final String PROPERTY_ROLE_B_NAME = "a:RoleBName";

	private final OOMId id;
	private final OOMObject parent;
	private final OOMType type;
	private final List<OOMObject> childList = new ArrayList<>();
	private final List<OOMId> refList = new ArrayList<>();

	//Données spécifiques
	private String code;
	private String name;

	//=========Gestion des attributes============================
	private String label;
	private String multiplicity;
	private String persistent;
	//===========================================================

	//=========Gestion des associations==========================
	private String roleALabel;
	private String roleBLabel;

	private String roleAMultiplicity;
	private String roleBMultiplicity;

	private Boolean roleANavigability;
	private Boolean roleBNavigability;

	//============================================================
	private final OOMObject root;

	/**
	 * Constructeur de la racine
	 */
	private OOMObject() {
		id = null;
		type = null;
		parent = null;
		root = this;
	}

	private OOMObject(final OOMObject parent, final OOMId id, final OOMType type) {
		Assertion.checkNotNull(parent);
		Assertion.checkNotNull(id);
		Assertion.checkNotNull(type);
		//------------------------------------------------------------------
		this.parent = parent;
		this.id = id;
		this.type = type;
		root = parent.root;
	}

	static OOMObject createdRoot() {
		return new OOMObject();
	}

	OOMObject createObjectOOM(final OOMId newId, final OOMType newType) {
		final OOMObject created = new OOMObject(this, newId, newType);
		childList.add(created);
		return created;
	}

	List<OOMId> getRefList() {
		return refList;
	}

	List<OOMObject> getChildList() {
		return childList;
	}

	OOMType getType() {
		return type;
	}

	OOMObject getParent() {
		return parent;
	}

	OOMId getId() {
		return id;
	}

	void addIdOOM(final OOMId idOOM) {
		refList.add(idOOM);
	}

	String getPersistent() {
		return persistent;
	}

	String getMultiplicity() {
		return multiplicity;
	}

	String getCode() {
		Assertion.checkNotNull(code);
		return code;
	}

	String getName() {
		Assertion.checkNotNull(name);
		return name;
	}

	String getLabel() {
		return label != null ? label : getName();
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

	void setProperty(final String propertyName, final String propertyValue) {
		Assertion.checkNotNull(propertyName);
		//----------------------------------------------------------------------
		if (PROPERTY_CODE.equals(propertyName)) {
			code = propertyValue;
		} else if (PROPERTY_NAME.equals(propertyName)) {
			name = propertyValue;
		} else if (PROPERTY_COMMENT.equals(propertyName)) {
			label = propertyValue;
		} else if (PROPERTY_PERSISTENT.equals(propertyName)) {
			persistent = propertyValue;
		} else if (PROPERTY_MULTIPLICITY.equals(propertyName)) {
			multiplicity = propertyValue;
		} else if (PROPERTY_ROLE_A_MULTIPLICITY.equals(propertyName)) {
			roleAMultiplicity = propertyValue;
		} else if (PROPERTY_ROLE_B_MULTIPLICITY.equals(propertyName)) {
			roleBMultiplicity = propertyValue;
		} else if (PROPERTY_ROLE_A_NAVIGABILITY.equals(propertyName)) {
			roleANavigability = "1".equals(propertyValue);
		} else if (PROPERTY_ROLE_B_NAVIGABILITY.equals(propertyName)) {
			roleBNavigability = "1".equals(propertyValue);
		} else if (PROPERTY_ROLE_A_NAME.equals(propertyName)) {
			roleALabel = propertyValue;
		} else if (PROPERTY_ROLE_B_NAME.equals(propertyName)) {
			roleBLabel = propertyValue;
		}
		//On ne tient pas compte des autres propriétés
	}

	//==========================================================================
	//==========================================================================
	//==========================================================================
	/** {@inheritDoc} */
	@Override
	public String toString() {
		String s = type + "::" + name;
		if (type == OOMType.Association) {
			s += " [roleA=" + getRoleALabel() + ", roleB=" + getRoleBLabel() + ']';
		} else {
			s += " [label=" + label + ", multiplicity=" + multiplicity + ']';
		}
		return s;
	}

	//	/**
	//	 * Sortie sur la console des objets trouvés.
	//	 */
	//	void display() {
	//		this.display(0);
	//	}
	//
	//	private void display(final int n) {
	//		for (final ObjectOOM child : childList) {
	//			for (int i = 0; i < n; i++) {
	//				System.out.print(".");
	//			}
	//			System.out.println(child);
	//			child.display(n + 1);
	//		}
	//
	//		for (final IdOOM ref : refList) {
	//			for (int i = 0; i < n; i++) {
	//				System.out.print(".");
	//			}
	//			System.out.println(ref);
	//		}
	//	}

	/**
	 * @return Nom du package
	 */
	String getPackageName() {
		//1. On vérifie que cet objet est bien un package
		Assertion.checkArgument(getType() == OOMType.Package, "père de l''objet {0} doit être un package", this);
		//----------------------------------------------------------------------
		//2. Si on arrive à la racine on s'arrète
		if (getParent().equals(root)) {
			return getName();
		}
		return getParent().getPackageName() + '.' + getName();
	}
}
