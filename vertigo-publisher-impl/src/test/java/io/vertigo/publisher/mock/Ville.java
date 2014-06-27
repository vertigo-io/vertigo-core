package io.vertigo.publisher.mock;

import io.vertigo.dynamo.domain.model.DtObject;

/**
 * Attention cette classe est g�n�r�e automatiquement !
 * Objet de donn�es AbstractVille
 */
@io.vertigo.dynamo.domain.metamodel.annotation.DtDefinition(persistent = false)
public final class Ville implements DtObject {
	/** SerialVersionUID. */
	private static final long serialVersionUID = 1L;

	@io.vertigo.dynamo.domain.metamodel.annotation.Field(domain = "DO_STRING", label = "Nom")
	private String nom;
	@io.vertigo.dynamo.domain.metamodel.annotation.Field(domain = "DO_STRING", label = "Code postal")
	private String codePostal;

	/**
	 * Champ : DATA.
	 * R�cup�re la valeur de la propri�t� 'Nom'. 
	 * @return String nom 
	 */
	public final String getNom() {
		return nom;
	}

	/**
	 * Champ : DATA.
	 * D�finit la valeur de la propri�t� 'Nom'.
	 * @param nom String 
	 */
	public final void setNom(final String nom) {
		this.nom = nom;
	}

	/**
	 * Champ : DATA.
	 * R�cup�re la valeur de la propri�t� 'Code postal'. 
	 * @return String codePostal 
	 */
	public final String getCodePostal() {
		return codePostal;
	}

	/**
	 * Champ : DATA.
	 * D�finit la valeur de la propri�t� 'Code postal'.
	 * @param codePostal String 
	 */
	public final void setCodePostal(final String codePostal) {
		this.codePostal = codePostal;
	}
}