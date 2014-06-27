package io.vertigo.publisher.mock;

import io.vertigo.dynamo.domain.model.DtObject;

/**
 * Attention cette classe est g�n�r�e automatiquement !
 * Objet de donn�es AbstractMisEnCause
 */
@io.vertigo.dynamo.domain.metamodel.annotation.DtDefinition(persistent = false)
public final class MisEnCause implements DtObject {
	/** SerialVersionUID. */
	private static final long serialVersionUID = 1L;
	@io.vertigo.dynamo.domain.metamodel.annotation.Field(domain = "DO_BOOLEAN", label = "Sexe")
	private Boolean siHomme;
	@io.vertigo.dynamo.domain.metamodel.annotation.Field(domain = "DO_STRING", label = "Nom")
	private String nom;
	@io.vertigo.dynamo.domain.metamodel.annotation.Field(domain = "DO_STRING", label = "Prenom")
	private String prenom;
	@io.vertigo.dynamo.domain.metamodel.annotation.Field(domain = "DO_DT_ADDRESS_DTC", label = "Addresses connues")
	private io.vertigo.dynamo.domain.model.DtList<io.vertigo.publisher.mock.Address> adressesConnues;

	/**
	 * Champ : DATA.
	 * R�cup�re la valeur de la propri�t� 'Sexe'. 
	 * @return Boolean siHomme 
	 */
	public final Boolean getSiHomme() {
		return siHomme;
	}

	/**
	 * Champ : DATA.
	 * D�finit la valeur de la propri�t� 'Sexe'.
	 * @param siHomme Boolean 
	 */
	public final void setSiHomme(final Boolean siHomme) {
		this.siHomme = siHomme;
	}

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

	/**
	 * Champ : DATA.
	 * R�cup�re la valeur de la propri�t� 'Addresses connues'. 
	 * @return DtList<io.vertigo.publisher.mock.Address> adressesConnues 
	 */
	public final io.vertigo.dynamo.domain.model.DtList<io.vertigo.publisher.mock.Address> getAdressesConnues() {
		return adressesConnues;
	}

	/**
	 * Champ : DATA.
	 * D�finit la valeur de la propri�t� 'Addresses connues'.
	 * @param adressesConnues DtList<io.vertigo.publisher.mock.Address> 
	 */
	public final void setAdressesConnues(final io.vertigo.dynamo.domain.model.DtList<io.vertigo.publisher.mock.Address> adressesConnues) {
		this.adressesConnues = adressesConnues;
	}
}
