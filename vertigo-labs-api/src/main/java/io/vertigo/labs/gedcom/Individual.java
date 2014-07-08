package io.vertigo.labs.gedcom;

import io.vertigo.dynamo.domain.metamodel.annotation.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.annotation.Field;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.labs.geocoder.GeoLocation;

@DtDefinition
public final class Individual implements DtObject {
	/** SerialVersionUID. */
	private static final long serialVersionUID = 1L;

	@Field(domain = "DO_CODE", type = "PRIMARY_KEY", notNull = true, label = "identifiant")
	private String id;
	@Field(domain = "DO_LIBELLE", notNull = true, label = "Name")
	private String name;
	@Field(domain = "DO_LIBELLE", notNull = true, label = "Pr�noms")
	private String givenName;
	@Field(domain = "DO_LIBELLE", notNull = true, label = "Nom de famille")
	private String surName;
	@Field(domain = "DO_LIBELLE", notNull = true, label = "Date de naissance")
	private String birthDate;
	@Field(domain = "DO_LIBELLE", notNull = true, label = "Lieu de Naissance")
	private String birthPlace;

	@Field(domain = "DO_LIBELLE", notNull = true, label = "Date de d�c�s")
	private String deathDate;
	@Field(domain = "DO_LIBELLE", notNull = true, label = "Lieu de d�c�s")
	private String deathPlace;

	@Field(domain = "DO_CODE", notNull = true, label = "Sexe")
	private String sex;

	//	Option<Individual> getFather(Individual individual);
	//
	//	Option<Individual> getMother(Individual individual);
	//-----------------
	private GeoLocation location;

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setGivenName(String givenName) {
		this.givenName = givenName;
	}

	public String getGivenName() {
		return givenName;
	}

	public void setSurName(String surName) {
		this.surName = surName;
	}

	public String getSurName() {
		return surName;
	}

	public void setSex(String sex) {
		this.sex = sex;
	}

	public String getSex() {
		return sex;
	}

	public void setBirthDate(String birthDate) {
		this.birthDate = birthDate;
	}

	public String getBirthDate() {
		return birthDate;
	}

	@Field(domain = "DO_INTEGER", type = "COMPUTED", notNull = true, label = "Annee")
	public Integer getBirth() {
		return GedcomUtil.findYear(getBirthDate());
	}

	public void setBirthPlace(String birthPlace) {
		this.birthPlace = birthPlace;
	}

	public String getBirthPlace() {
		return birthPlace;
	}

	public void setLocation(GeoLocation location) {
		this.location = location;
	}

	public GeoLocation getLocation() {
		return location;
	}

	@Field(domain = "DO_CODE", type = "COMPUTED", notNull = true, label = "D�partement")
	public String getDepartement() {
		return location == null ? null : location.getLevel2();
	}

	//	@Field(domain = "DO_CODE", type = "COMPUTED", notNull = true, label = "D�partement")
	//	public String getDepartement() {
	//		return location == null ? null : location.getLevel1() + '/' + location.getLevel2();
	//	}

	public void setDeathDate(String deathDate) {
		this.deathDate = deathDate;
	}

	public void setDeathPlace(String deathPlace) {
		this.deathPlace = deathPlace;
	}
}
