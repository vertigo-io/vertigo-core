package io.vertigo.publisher.mock;

import io.vertigo.dynamo.domain.model.DtObject;

/**
 * Attention cette classe est g�n�r�e automatiquement !
 * Objet de donn�es AbstractEnquete
 */
@io.vertigo.dynamo.domain.metamodel.annotation.DtDefinition(persistent = false)
public final class Enquete implements DtObject {
	/** SerialVersionUID. */
	private static final long serialVersionUID = 1L;

	@io.vertigo.dynamo.domain.metamodel.annotation.Field(domain = "DO_BOOLEAN", label = "Termin�e?")
	private Boolean enqueteTerminee;
	@io.vertigo.dynamo.domain.metamodel.annotation.Field(domain = "DO_STRING", label = "Code")
	private String codeEnquete;
	@io.vertigo.dynamo.domain.metamodel.annotation.Field(domain = "DO_STRING", label = "Sexe")
	private String fait;
	@io.vertigo.dynamo.domain.metamodel.annotation.Field(domain = "DO_BOOLEAN", label = "Sexe")
	private Boolean siGrave;

	/**
	 * Champ : DATA.
	 * R�cup�re la valeur de la propri�t� 'Termin�e?'. 
	 * @return Boolean enqueteTerminee 
	 */
	public final Boolean getEnqueteTerminee() {
		return enqueteTerminee;
	}

	/**
	 * Champ : DATA.
	 * D�finit la valeur de la propri�t� 'Termin�e?'.
	 * @param enqueteTerminee Boolean 
	 */
	public final void setEnqueteTerminee(final Boolean enqueteTerminee) {
		this.enqueteTerminee = enqueteTerminee;
	}

	/**
	 * Champ : DATA.
	 * R�cup�re la valeur de la propri�t� 'Code'. 
	 * @return String codeEnquete 
	 */
	public final String getCodeEnquete() {
		return codeEnquete;
	}

	/**
	 * Champ : DATA.
	 * D�finit la valeur de la propri�t� 'Code'.
	 * @param codeEnquete String 
	 */
	public final void setCodeEnquete(final String codeEnquete) {
		this.codeEnquete = codeEnquete;
	}

	/**
	 * Champ : DATA.
	 * R�cup�re la valeur de la propri�t� 'Sexe'. 
	 * @return String fait 
	 */
	public final String getFait() {
		return fait;
	}

	/**
	 * Champ : DATA.
	 * D�finit la valeur de la propri�t� 'Sexe'.
	 * @param fait String 
	 */
	public final void setFait(final String fait) {
		this.fait = fait;
	}

	/**
	 * Champ : DATA.
	 * R�cup�re la valeur de la propri�t� 'Sexe'. 
	 * @return Boolean siGrave 
	 */
	public final Boolean getSiGrave() {
		return siGrave;
	}

	/**
	 * Champ : DATA.
	 * D�finit la valeur de la propri�t� 'Sexe'.
	 * @param siGrave Boolean 
	 */
	public final void setSiGrave(final Boolean siGrave) {
		this.siGrave = siGrave;
	}
}