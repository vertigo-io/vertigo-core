/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.dynamock.domain.famille;

import io.vertigo.dynamo.domain.metamodel.annotation.Association;
import io.vertigo.dynamo.domain.metamodel.annotation.AssociationNN;
import io.vertigo.dynamo.domain.metamodel.annotation.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.annotation.Field;
import io.vertigo.dynamo.domain.metamodel.association.DtListURIForAssociation;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.dynamo.persistence.PersistenceManager;
import io.vertigo.dynamock.domain.car.Car;

/**
 * Attention cette classe est générée automatiquement !
 * Objet de données Famille
 */
@javax.persistence.Entity
@javax.persistence.Table(name = "FAMILLE")
@DtDefinition
public final class Famille implements DtObject {
	/** SerialVersionUID. */
	private static final long serialVersionUID = 1L;

	private Long famId;
	private String libelle;
	private DtList<Car> voituresFamille;
	private DtList<Car> voituresLocation;

	/**
	 * Champ : PRIMARY_KEY.
	 * récupère la valeur de la propriété 'identifiant de la famille'. 
	 * @return Long famId <b>Obligatoire</b>
	 */
	@javax.persistence.Id
	@javax.persistence.SequenceGenerator(name = "sequence", sequenceName = "SEQ_FAMILLE")
	@javax.persistence.GeneratedValue(strategy = javax.persistence.GenerationType.SEQUENCE, generator = "sequence")
	@javax.persistence.Column(name = "FAM_ID")
	@Field(domain = "DO_IDENTIFIANT", type = "PRIMARY_KEY", notNull = true, label = "identifiant de la famille")
	public final Long getFamId() {
		return famId;
	}

	/**
	 * Champ : PRIMARY_KEY.
	 * Définit la valeur de la propriété 'identifiant de la famille'.
	 * @param famId Long <b>Obligatoire</b>
	 */
	public final void setFamId(final Long famId) {
		this.famId = famId;
	}

	/**
	 * Champ : DATA.
	 * récupère la valeur de la propriété 'Libelle'. 
	 * @return String libelle 
	 */
	@javax.persistence.Column(name = "LIBELLE")
	@Field(domain = "DO_STRING", label = "Libelle")
	public final String getLibelle() {
		return libelle;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'Libelle'.
	 * @param libelle String 
	 */
	public final void setLibelle(final String libelle) {
		this.libelle = libelle;
	}

	/**
	 * Champ : COMPUTED.
	 * récupère la valeur de la propriété calculée 'Libelle'. 
	 * @return String description 
	 */
	@javax.persistence.Column(name = "DESCRIPTION")
	@javax.persistence.Transient
	@Field(domain = "DO_LIBELLE_LONG", type = "COMPUTED", persistent = false, label = "Libelle")
	public final String getDescription() {
		final StringBuilder builder = new StringBuilder();
		builder.append(getLibelle());
		builder.append('[');
		builder.append(getFamId());
		builder.append(']');
		return builder.toString();
	}

	/**
	 * Association : Voitures de la famille.
	 */
	@javax.persistence.Transient
	@Association(name = "A_FAM_CAR_FAMILLE", fkFieldName = "FAM_ID", primaryDtDefinitionName = "DT_FAMILLE", primaryIsNavigable = false, primaryRole = "Famille", primaryLabel = "Famille", primaryMultiplicity = "1..1", foreignDtDefinitionName = "DT_CAR", foreignIsNavigable = true, foreignRole = "VoituresFamille", foreignLabel = "Voitures de la famille", foreignMultiplicity = "0..*")
	public final DtList<Car> getVoituresFamilleList() {
		//		return this.<.domain.car.Car> getList(getVoituresFamilleListURI());
		final DtListURIForAssociation fkDtListURI = getVoituresFamilleDtListURI();
		io.vertigo.kernel.lang.Assertion.checkNotNull(fkDtListURI);
		//---------------------------------------------------------------------
		//On est toujours dans un mode lazy.
		if (voituresFamille == null) {
			voituresFamille = io.vertigo.kernel.Home.getComponentSpace().resolve(PersistenceManager.class).getBroker().getList(fkDtListURI);
		}
		return voituresFamille;
	}

	/**
	 * Association URI: Voitures de la famille.
	 * @return URI de l'association
	 */
	@javax.persistence.Transient
	@Association(name = "A_FAM_CAR_FAMILLE", fkFieldName = "FAM_ID", primaryDtDefinitionName = "DT_FAMILLE", primaryIsNavigable = false, primaryRole = "Famille", primaryLabel = "Famille", primaryMultiplicity = "1..1", foreignDtDefinitionName = "DT_CAR", foreignIsNavigable = true, foreignRole = "VoituresFamille", foreignLabel = "Voitures de la famille", foreignMultiplicity = "0..*")
	public final DtListURIForAssociation getVoituresFamilleDtListURI() {
		return DtObjectUtil.createDtListURI(this, "A_FAM_CAR_FAMILLE", "VoituresFamille");
	}

	/**
	 * Association : Voitures de location.
	 */
	@javax.persistence.Transient
	@AssociationNN(name = "A_FAM_CAR_LOCATION", tableName = "FAM_CAR_LOCATION", dtDefinitionA = "DT_FAMILLE", dtDefinitionB = "DT_CAR", navigabilityA = false, navigabilityB = true, roleA = "Famille", roleB = "VoituresLocation", labelA = "Famille", labelB = "Voitures de location")
	public final DtList<Car> getVoituresLocationList() {
		//		return this.<.domain.car.Car> getList(getVoituresLocationListURI());
		final DtListURIForAssociation fkDtListURI = getVoituresLocationDtListURI();
		io.vertigo.kernel.lang.Assertion.checkNotNull(fkDtListURI);
		//---------------------------------------------------------------------
		//On est toujours dans un mode lazy.
		if (voituresLocation == null) {
			voituresLocation = io.vertigo.kernel.Home.getComponentSpace().resolve(PersistenceManager.class).getBroker().getList(fkDtListURI);
		}
		return voituresLocation;
	}

	/**
	 * Association URI: Voitures de location.
	 * @return URI de l'association
	 */
	@javax.persistence.Transient
	@AssociationNN(name = "A_FAM_CAR_LOCATION", tableName = "FAM_CAR_LOCATION", dtDefinitionA = "DT_FAMILLE", dtDefinitionB = "DT_CAR", navigabilityA = false, navigabilityB = true, roleA = "Famille", roleB = "VoituresLocation", labelA = "Famille", labelB = "Voitures de location")
	public final DtListURIForAssociation getVoituresLocationDtListURI() {
		return DtObjectUtil.createDtListURI(this, "A_FAM_CAR_LOCATION", "VoituresLocation");
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return DtObjectUtil.toString(this);
	}
}
