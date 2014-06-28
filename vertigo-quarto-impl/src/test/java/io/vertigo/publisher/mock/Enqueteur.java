package io.vertigo.publisher.mock;

import io.vertigo.dynamo.domain.model.DtObject;

/**
 * Attention cette classe est g�n�r�e automatiquement !
 * Objet de donn�es AbstractEnqueteur
 */
@io.vertigo.dynamo.domain.metamodel.annotation.DtDefinition(persistent = false)
public final class Enqueteur implements DtObject {
	/**
	 * SerialVersionUID.
	 */
	private static final long serialVersionUID = 1L;

	@io.vertigo.dynamo.domain.metamodel.annotation.Field(domain = "DO_STRING", label = "Nom")
	private String nom;
	@io.vertigo.dynamo.domain.metamodel.annotation.Field(domain = "DO_STRING", label = "Prenom")
	private String prenom;

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
	 * R�cup�re la valeur de la propri�t� 'Prenom'. 
	 * @return String prenom 
	 */
	public final String getPrenom() {
		return prenom;
	}

	/**
	 * Champ : DATA.
	 * D�finit la valeur de la propri�t� 'Prenom'.
	 * @param prenom String 
	 */
	public final void setPrenom(final String prenom) {
		this.prenom = prenom;
	}
}