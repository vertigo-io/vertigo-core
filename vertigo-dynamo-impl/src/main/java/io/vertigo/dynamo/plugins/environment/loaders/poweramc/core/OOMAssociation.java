package io.vertigo.dynamo.plugins.environment.loaders.poweramc.core;

import io.vertigo.kernel.lang.Assertion;

/**
 * Association.
 * Une association est définie par deux noeuds A et B.
 * Chaque noeud possède des propriétés :
 *  - Navigabilité
 *  - Cardinalité (appelée multiplicité dans poweramc)
 *  - Libellé
 *  - Code de l'entité à laquelle est attachée le noeud.
 *  
 *  D'autre part l'association possède son propre code.  
 * @author pchretien
 * @version $Id: AssociationOOM.java,v 1.4 2013/10/22 12:30:19 pchretien Exp $
 */
public final class OOMAssociation {
	private final String code;
	private final String packageName;

	private final String multiplicityA;
	private final String multiplicityB;

	private final boolean navigabilityA;
	private final boolean navigabilityB;

	private final String roleLabelA;
	private final String roleLabelB;

	private final String codeA;
	private final String codeB;

	/**
	 * Constructeur.
	 */
	OOMAssociation(final String code, final String packageName, final String multiplicityA, final String multiplicityB, final String roleLabelA, final String roleLabelB, final String codeA, final String codeB, final boolean navigabilityA, final boolean navigabilityB) {
		Assertion.checkArgNotEmpty(code);
		//Assertion.notEmpty(packageName);
		Assertion.checkArgNotEmpty(multiplicityA);
		Assertion.checkArgNotEmpty(multiplicityB);
		Assertion.checkArgNotEmpty(roleLabelA);
		Assertion.checkArgNotEmpty(roleLabelB);
		Assertion.checkArgNotEmpty(codeA);
		Assertion.checkArgNotEmpty(codeB);
		//---------------------------------------------------------------------
		this.code = code;
		this.packageName = packageName;

		this.multiplicityA = multiplicityA;
		this.multiplicityB = multiplicityB;

		this.roleLabelA = roleLabelA;
		this.roleLabelB = roleLabelB;

		this.codeA = codeA;
		this.codeB = codeB;

		this.navigabilityA = navigabilityA;
		this.navigabilityB = navigabilityB;
	}

	/**
	 * Formatage du code : AAA_YYY_{CODE NAME}
	 * AAA = Trois première lettre du code de A
	 * BBB = Trois première lettre du code de B
	 * Le nom défini par l'utilisateur est facultatif.
	 * 
	 * @return Code de l'association
	 */
	public String getCode() {
		return code;
	}

	/**
	 * Le code suivant est déduit dans l'OOM du code.  
	 * @return Nom de l'association défini par l'utilisateur. (Peut être null)
	 */
	public String getCodeName() {
		if (code.length() > 8 && code.charAt(3) == '_' && code.charAt(7) == '_') {
			return code.substring(8);
		}
		return null;
	}

	/**
	 * @return Nom du package.
	 */
	public String getPackageName() {
		return packageName;
	}

	/**
	 * @return Multiplicité de A.
	 */
	public String getMultiplicityA() {
		return multiplicityA;
	}

	/**
	 * @return Multiplicité de A.
	 */
	public String getMultiplicityB() {
		return multiplicityB;
	}

	/**
	 * @return Navigabilité de A.
	 */
	public boolean isNavigableA() {
		return navigabilityA;
	}

	/**
	 * @return Navigabilité de B.
	 */
	public boolean isNavigableB() {
		return navigabilityB;
	}

	/**
	 * @return Libellé du noeud A.
	 */
	public String getRoleLabelA() {
		return roleLabelA;
	}

	/**
	 * @return Libellé du noeud B.
	 */
	public String getRoleLabelB() {
		return roleLabelB;
	}

	/**
	 * @return Libellé du noeud B.
	 */
	public String getCodeA() {
		return codeA;
	}

	/**
	 * @return Code de l'entité B participant à l'association.
	 */
	public String getCodeB() {
		return codeB;
	}
}
