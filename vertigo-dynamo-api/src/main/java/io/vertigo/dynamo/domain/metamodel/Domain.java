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
package io.vertigo.dynamo.domain.metamodel;

import io.vertigo.core.Home;
import io.vertigo.core.spaces.definiton.Definition;
import io.vertigo.core.spaces.definiton.DefinitionPrefix;
import io.vertigo.core.spaces.definiton.DefinitionReference;
import io.vertigo.lang.Assertion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
* Un domaine permet d'enrichir les types primitifs
 * en les dotant en les  d'un fort sens métier.
 * Un domaine enrichit la notion de type primitif Dynamo.
 * Un domaine intègre une méthode de validation par contraintes.
 * Il intègre aussi un formatter.
 *
 * Un Domaine est un objet partagé par nature il est non modifiable.
 *
 * @author pchretien
 */
@DefinitionPrefix("DO")
public final class Domain implements Definition {
	/** Nom du domaine.*/
	private final String name;
	/** Type primitif. */
	private final DataType dataType;

	/** Formatter. */
	private final DefinitionReference<Formatter> formatterRef;

	/** Contraintes du domaine. */
	private final List<DefinitionReference<Constraint<?, Object>>> constraintRefs;

	/** Conteneur des couples (propriétés, valeur) */
	private final Properties properties;

	/**
	 * Nom de la Définition dans le cas de DtObject ou DtList
	 */
	private final String dtDefinitionName;

	/**
	 * Constructeur.
	 * @param dataType Type Dynamo
	 * @param formatter Formatter du domaine
	 */
	public Domain(final String name, final DataType dataType, final Formatter formatter) {
		this(name, dataType, formatter, Collections.<Constraint<?, Object>> emptyList(), new PropertiesBuilder().build());
	}

	/**
	 * Constructeur.
	 * @param dataType Type Dynamo
	 * @param formatter Formatter du domaine
	 * @param constraints Liste des contraintes du domaine
	 * @param properties Map des (DtProperty, value)
	 */
	public Domain(final String name, final DataType dataType, final Formatter formatter, final List<Constraint<?, Object>> constraints, final Properties properties) {
		//--Vérification des contrats-------------------------------------------
		Assertion.checkArgNotEmpty(name);
		Assertion.checkNotNull(formatter);
		Assertion.checkNotNull(constraints);
		Assertion.checkNotNull(properties);
		//----------------------------------------------------------------------
		this.name = name;
		this.dataType = dataType;
		formatterRef = new DefinitionReference<>(formatter);
		//On rend la liste des contraintes non modifiable
		final List<DefinitionReference<Constraint<?, Object>>> _constraintRefs = new ArrayList<>();
		for (final Constraint<?, Object> constraint : constraints) {
			_constraintRefs.add(new DefinitionReference<Constraint<?, Object>>(constraint));
		}
		constraintRefs = Collections.unmodifiableList(_constraintRefs);
		//========================MISE A JOUR DE LA MAP DES PROPRIETES==========
		this.properties = buildProperties(constraints, properties);

		//Mise à jour de la FK.
		if (this.properties.getValue(DtProperty.TYPE) != null) {
			Assertion.checkArgument(!getDataType().isPrimitive(), "Le type ne peut être renseigné que pour des types non primitifs");
			//-----------------------------------------------------------------
			//On ne s'intéresse qu'au type de DTO et DTC dont le type de DT est déclaré
			dtDefinitionName = this.properties.getValue(DtProperty.TYPE);
		} else {
			dtDefinitionName = null;
		}
	}

	private static Properties buildProperties(final List<Constraint<?, Object>> constraints, final Properties inputProperties) {
		final PropertiesBuilder propertiesBuilder = new PropertiesBuilder();
		for (final Property property : inputProperties.getProperties()) {
			propertiesBuilder.withValue(property, inputProperties.getValue(property));
		}

		//On récupère les propriétés d'après les contraintes
		for (final Constraint<?, ?> constraint : constraints) {
			propertiesBuilder.withValue(constraint.getProperty(), constraint.getPropertyValue());
		}
		return propertiesBuilder.build();
	}

	/**
	 * Retourne le type du domaine.
	 *
	 * @return Type du champ.
	 */
	public DataType getDataType() {
		return dataType;
	}

	/**
	 * Retourne le formatter du domaine.
	 *
	 * @return Formatter.
	 */
	public Formatter getFormatter() {
		return formatterRef.get();
	}

	//	/**
	//	 * @return Liste des constraintes
	//	 */
	//	public List<Constraint<?, Object>> getConstraints() {
	//		return constraints;
	//	}

	/**
	 * @return propriétés
	 */
	public Properties getProperties() {
		return properties;
	}

	/**
	 * Teste si la valeur passée en paramètre est valide pour le champ.
	 * Lance une exception transapente(RunTime) avec message adequat si pb.
	 *
	 * @param value Valeur à valider
	 * @throws ConstraintException Erreur de vérification des contraintes
	 */
	public void checkValue(final Object value) throws ConstraintException {
		//1. On vérifie la conformité de la valeur par rapport au type du champ.
		getDataType().checkValue(value);

		//2. Dans le cas de l'implémentation standard on vérifie les contraintes
		for (final DefinitionReference<Constraint<?, Object>> constraintRef : constraintRefs) {
			//Il suffit d'une contrainte non respectée pour qu'il y ait non validation
			if (!constraintRef.get().checkConstraint(value)) {
				throw new ConstraintException(constraintRef.get().getErrorMessage());
			}
		}
	}

	//==========================================================================
	//Pour les domaines complexes (DTO & DTC) permet d'accéder à la définition des DTO et DTC
	//==========================================================================
	/**
	 * @return si il existe un DT identifié pour ce domain.
	 */
	public boolean hasDtDefinition() {
		return dtDefinitionName != null;
	}

	/**
	 * Permet pour les types composites (Beans et collections de beans)
	 * de connaitre leur définition.
	 * Ne peut pas être appelé pour des types primitifs. (ex : BigDecimal, String....)
	 * Fonctionne uniquement avec les domaines de type DtList et DtObject.
	 * @return dtDefinition des domaines de type DtList et DtObject.
	 */
	public DtDefinition getDtDefinition() {
		if (dtDefinitionName == null) {
			//On fournit un message d'erreur explicite
			if (getDataType().isPrimitive()) {
				throw new RuntimeException("Le domain " + getName() + " n'est ni un DTO ni une DTC");
			}
			throw new RuntimeException("Le domain " + getName() + " est un DTO/DTC mais typé de façon dynamique donc sans DtDefinition.");
		}
		return Home.getDefinitionSpace().resolve(dtDefinitionName, DtDefinition.class);
	}

	/** {@inheritDoc} */
	@Override
	public String getName() {
		return name;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return name;
	}
}
