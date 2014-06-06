package io.vertigo.dynamo.plugins.environment.loaders.eaxmi.core;

import io.vertigo.kernel.lang.Assertion;

/**
 * Classe de gestion des associations pour le loader XMI.
 * Extension Enterprise Architect seulement.
 * @author pforhan
 *
 */
public class EAXmiAssociation {
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
	EAXmiAssociation(final String code, final String packageName, final String multiplicityA, final String multiplicityB, final String roleLabelA, final String roleLabelB, final String codeA, final String codeB, final boolean navigabilityA, final boolean navigabilityB) {
		Assertion.checkArgNotEmpty(code);
		Assertion.checkArgNotEmpty(multiplicityA);
		Assertion.checkArgNotEmpty(multiplicityB);
		Assertion.checkArgNotEmpty(roleLabelA);
		Assertion.checkArgNotEmpty(roleLabelB);
		Assertion.checkArgNotEmpty(codeA);
		Assertion.checkArgNotEmpty(codeB);
		//---------------------------------------------------------------------
		this.code = code;
		this.packageName = packageName;

		// On gèe le cas du 1 tout seul.
		if ("1".equals(multiplicityA)) {
			this.multiplicityA = "1..1";
		} else {
			this.multiplicityA = multiplicityA;
		}
		if ("1".equals(multiplicityB)) {
			this.multiplicityB = "1..1";
		} else {
			this.multiplicityB = multiplicityB;
		}
		
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
	 * Le code suivant est déduit du code.  
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
	 * @return Multiplicité de B.
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
	 * @return Code de l'entité A participant à l'association
	 */
	public String getCodeA() {
		return codeA;
	}

	/**
	 * @return Code de l'entité B participant à  l'association.
	 */
	public String getCodeB() {
		return codeB;
	}

}
