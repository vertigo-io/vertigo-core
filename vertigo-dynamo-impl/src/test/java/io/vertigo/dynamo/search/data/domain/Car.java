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
package io.vertigo.dynamo.search.data.domain;

import io.vertigo.dynamo.domain.model.KeyConcept;
import io.vertigo.dynamo.domain.stereotype.DtDefinition;
import io.vertigo.dynamo.domain.stereotype.Field;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.lang.VSystemException;

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

	@Field(domain = "DO_IDENTIFIANT", type = "ID", required = true, label = "identifiant de la voiture")
	public final Long getId() {
		return id;
	}

	public final void setId(final Long id) {
		this.id = id;
	}

	@Field(domain = "DO_KEYWORD", required = true, label = "Constructeur")
	public final String getMake() {
		return make;
	}

	public final void setMake(final String make) {
		this.make = make;
	}

	@Field(domain = "DO_FULL_TEXT", required = true, label = "Modéle")
	public final String getModel() {
		return model;
	}

	public final void setModel(final String model) {
		this.model = model;
	}

	@Field(domain = "DO_FULL_TEXT", required = true, label = "Descriptif")
	public final String getDescription() {
		return description;
	}

	public final void setDescription(final String description) {
		this.description = description;
	}

	@Field(domain = "DO_INTEGER", required = true, label = "Année")
	public final Integer getYear() {
		return year;
	}

	public final void setYear(final Integer year) {
		this.year = year;
	}

	@Field(domain = "DO_INTEGER", required = true, label = "Kilométrage")
	public final Integer getKilo() {
		return kilo;
	}

	public final void setKilo(final Integer kilo) {
		this.kilo = kilo;
	}

	@Field(domain = "DO_INTEGER", required = true, label = "Prix")
	public final Integer getPrice() {
		return price;
	}

	public final void setPrice(final Integer price) {
		this.price = price;
	}

	@Field(domain = "DO_CONSO", required = true, label = "Consomation")
	public java.math.BigDecimal getConsommation() {
		return consommation;
	}

	public void setConsommation(final java.math.BigDecimal consommation) {
		this.consommation = consommation;
	}

	@Field(domain = "DO_KEYWORD", required = true, label = "Type de moteur")
	public final String getMotorType() {
		return motorType;
	}

	public final void setMotorType(final String motorType) {
		this.motorType = motorType;
	}

	@Field(domain = "DO_IDENTIFIANT", type = "FOREIGN_KEY", required = true, label = "Famille")
	public final Long getFamId() {
		return famId;
	}

	public final void setFamId(final Long famId) {
		this.famId = famId;
	}

	@Field(domain = "DO_KEYWORD", type = "COMPUTED", persistent = false, label = "model sort")
	public String getModelSort() {
		throw new VSystemException("Can't use index copyTo field");
	}

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
