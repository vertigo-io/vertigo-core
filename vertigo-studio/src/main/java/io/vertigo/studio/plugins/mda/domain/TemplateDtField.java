package io.vertigo.studio.plugins.mda.domain;

import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.util.StringUtil;

/**
 * Objet utilisé par FreeMarker.
 * 
 * @author pchretien
 */
public final class TemplateDtField {
	private final DtDefinition dtDefinition;
	private final DtField dtField;

	/***
	 * Constructeur.
	 * @param dtField Champ à générer
	 */
	TemplateDtField(final DtDefinition dtDefinition, final DtField dtField) {
		Assertion.checkNotNull(dtDefinition);
		Assertion.checkNotNull(dtField);
		//-----------------------------------------------------------------
		this.dtDefinition = dtDefinition;
		this.dtField = dtField;
	}

	/**
	 * Nom du champ en majuscules séparés par des _.  
	 * @return UN_NOM
	 */
	public String getName() {
		return dtField.getName();
	}

	/**
	 * @return DtDefinition
	 */
	public DtDefinition getDtDefinition() {
		return dtDefinition;
	}

	/**
	 * @return DtField 
	 */
	public DtField getDtField() {
		return dtField;
	}

	/**
	 * Nom du champ en CamelCase.  
	 * La premiére lettre est en majuscule
	 * si besoin la première lettre en miniscule avec FreeMarker : ${dtField.nameLowerCase?uncap_first}
	 * @return UnNom
	 */
	public String getNameLowerCase() {
		return StringUtil.constToCamelCase(dtField.getName(), true);
	}

	/**
	 * @return Type du champ (
	 */
	public String getType() {
		return dtField.getType().name();
	}

	/**
	 * @return Type java du champ
	 */
	public String getJavaType() {
		return DomainUtil.buildJavaType(dtField.getDomain());
	}

	/**
	 * @return Label du champ
	 */
	public String getDisplay() {
		return dtField.getLabel().getDisplay();
	}

	/**
	 * @return Si la propriété est non null
	 */
	public boolean isNotNull() {
		return dtField.isNotNull();
	}

	/**
	 * @return Code java correspondant à l'expression de ce champ calculé
	 */
	public String getJavaCode() {
		return dtField.getComputedExpression().getJavaCode();
	}
}
