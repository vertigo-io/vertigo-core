/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2016, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.dynamo.collections.data.domain;

import io.vertigo.dynamo.domain.model.KeyConcept;
import io.vertigo.dynamo.domain.stereotype.DtDefinition;
import io.vertigo.dynamo.domain.stereotype.Field;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.lang.VSystemException;

/**
 * Attention cette classe est générée automatiquement ! Objet de données Car
 */
@javax.persistence.Entity
@javax.persistence.Table(name = "CAR")
@DtDefinition
public final class Car implements KeyConcept {
	/** SerialVersionUID. */
	private static final long serialVersionUID = 1L;

	private Long id;
	private String make;
	private String model;
	private String description;
	private Integer year;
	private Integer kilo;
	private Integer price;
	private java.math.BigDecimal consommation;
	private String motorType;
	private Long famId;

	/**
	 * Champ : ID.
	 * récupère la valeur de la propriété 'identifiant de la voiture'.
	 *
	 * @return Long id <b>Obligatoire</b>
	 */
	@javax.persistence.Id
	@javax.persistence.SequenceGenerator(name = "sequence", sequenceName = "SEQ_CAR")
	@javax.persistence.GeneratedValue(strategy = javax.persistence.GenerationType.SEQUENCE, generator = "sequence")
	@javax.persistence.Column(name = "ID")
	@Field(domain = "DO_IDENTIFIANT", type = "ID", required = true, label = "identifiant de la voiture")
	public final Long getId() {
		return id;
	}

	/**
	 * Champ : ID.
	 * Définit la valeur de la propriété 'identifiant de la voiture'.
	 *
	 * @param id
	 *            Long <b>Obligatoire</b>
	 */
	public final void setId(final Long id) {
		this.id = id;
	}

	/**
	 * Champ : DATA. récupère la valeur de la propriété 'Constructeur'.
	 *
	 * @return String make <b>Obligatoire</b>
	 */
	@javax.persistence.Column(name = "MAKE")
	@Field(domain = "DO_KEYWORD", required = true, label = "Constructeur")
	public final String getMake() {
		return make;
	}

	/**
	 * Champ : DATA. Définit la valeur de la propriété 'Constructeur'.
	 *
	 * @param make
	 *            String <b>Obligatoire</b>
	 */
	public final void setMake(final String make) {
		this.make = make;
	}

	/**
	 * Champ : DATA. récupère la valeur de la propriété 'Modéle'.
	 *
	 * @return String model <b>Obligatoire</b>
	 */
	@javax.persistence.Column(name = "MODEL")
	@Field(domain = "DO_FULL_TEXT", required = true, label = "Modéle")
	public final String getModel() {
		return model;
	}

	/**
	 * Champ : DATA. Définit la valeur de la propriété 'Modéle'.
	 *
	 * @param model
	 *            String <b>Obligatoire</b>
	 */
	public final void setModel(final String model) {
		this.model = model;
	}

	/**
	 * Champ : DATA. récupère la valeur de la propriété 'Descriptif'.
	 *
	 * @return String description <b>Obligatoire</b>
	 */
	@javax.persistence.Column(name = "DESCRIPTION")
	@Field(domain = "DO_FULL_TEXT", required = true, label = "Descriptif")
	public final String getDescription() {
		return description;
	}

	/**
	 * Champ : DATA. Définit la valeur de la propriété 'Descriptif'.
	 *
	 * @param description
	 *            String <b>Obligatoire</b>
	 */
	public final void setDescription(final String description) {
		this.description = description;
	}

	/**
	 * Champ : DATA. récupère la valeur de la propriété 'Année'.
	 *
	 * @return Integer year <b>Obligatoire</b>
	 */
	@javax.persistence.Column(name = "YEAR")
	@Field(domain = "DO_INTEGER", required = true, label = "Année")
	public final Integer getYear() {
		return year;
	}

	/**
	 * Champ : DATA. Définit la valeur de la propriété 'Année'.
	 *
	 * @param year
	 *            Integer <b>Obligatoire</b>
	 */
	public final void setYear(final Integer year) {
		this.year = year;
	}

	/**
	 * Champ : DATA. récupère la valeur de la propriété 'Kilométrage'.
	 *
	 * @return Integer kilo <b>Obligatoire</b>
	 */
	@javax.persistence.Column(name = "KILO")
	@Field(domain = "DO_INTEGER", required = true, label = "Kilométrage")
	public final Integer getKilo() {
		return kilo;
	}

	/**
	 * Champ : DATA. Définit la valeur de la propriété 'Kilométrage'.
	 *
	 * @param kilo
	 *            Integer <b>Obligatoire</b>
	 */
	public final void setKilo(final Integer kilo) {
		this.kilo = kilo;
	}

	/**
	 * Champ : DATA. récupère la valeur de la propriété 'Prix'.
	 *
	 * @return Integer price <b>Obligatoire</b>
	 */
	@javax.persistence.Column(name = "PRICE")
	@Field(domain = "DO_INTEGER", required = true, label = "Prix")
	public final Integer getPrice() {
		return price;
	}

	/**
	 * Champ : DATA. Définit la valeur de la propriété 'Prix'.
	 *
	 * @param price
	 *            Integer <b>Obligatoire</b>
	 */
	public final void setPrice(final Integer price) {
		this.price = price;
	}

	/**
	 * Champ : DATA.
	 * Récupère la valeur de la propriété 'Consommation'.
	 * @return java.math.BigDecimal consommation <b>Obligatoire</b>
	 */
	@javax.persistence.Column(name = "CONSOMMATION")
	@Field(domain = "DO_CONSO", required = true, label = "Consomation")
	public java.math.BigDecimal getConsommation() {
		return consommation;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'Consommation'.
	 * @param consommation java.math.BigDecimal <b>Obligatoire</b>
	 */
	public void setConsommation(final java.math.BigDecimal consommation) {
		this.consommation = consommation;
	}

	/**
	 * Champ : DATA. récupère la valeur de la propriété 'Type de moteur'.
	 *
	 * @return String motorType <b>Obligatoire</b>
	 */
	@javax.persistence.Column(name = "MOTOR_TYPE")
	@Field(domain = "DO_KEYWORD", required = true, label = "Type de moteur")
	public final String getMotorType() {
		return motorType;
	}

	/**
	 * Champ : DATA. Définit la valeur de la propriété 'Type de moteur'.
	 *
	 * @param motorType
	 *            String <b>Obligatoire</b>
	 */
	public final void setMotorType(final String motorType) {
		this.motorType = motorType;
	}

	/**
	 * Champ : FOREIGN_KEY. récupère la valeur de la propriété 'Famille'.
	 *
	 * @return Long famId <b>Obligatoire</b>
	 */
	@javax.persistence.Column(name = "FAM_ID")
	@Field(domain = "DO_IDENTIFIANT", type = "FOREIGN_KEY", required = true, label = "Famille")
	public final Long getFamId() {
		return famId;
	}

	/**
	 * Champ : FOREIGN_KEY. Définit la valeur de la propriété 'Famille'.
	 *
	 * @param famId
	 *            Long <b>Obligatoire</b>
	 */
	public final void setFamId(final Long famId) {
		this.famId = famId;
	}

	// Association : Famille non navigable

	// Association : Famille non navigable

	/**
	 * Champ : COMPUTED.
	 * Récupère la valeur de la propriété calculée 'model sort'.
	 * @return String modelSort
	 */
	@javax.persistence.Column(name = "MODEL_SORT")
	@javax.persistence.Transient
	@Field(domain = "DO_KEYWORD", type = "COMPUTED", persistent = false, label = "model sort")
	public String getModelSort() {
		throw new VSystemException("Can't use index copyTo field");
	}

	/**
	 * Champ : COMPUTED.
	 * Récupère la valeur de la propriété calculée 'index all'.
	 * @return String allText
	 */
	@javax.persistence.Column(name = "ALL_TEXT")
	@javax.persistence.Transient
	@Field(domain = "DO_FULL_TEXT", type = "COMPUTED", persistent = false, label = "index all")
	public String getAllText() {
		throw new VSystemException("Can't use index copyTo field");
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return DtObjectUtil.toString(this);
	}
}
