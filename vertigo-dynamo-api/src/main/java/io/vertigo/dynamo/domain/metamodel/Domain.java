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
package io.vertigo.dynamo.domain.metamodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.vertigo.app.Home;
import io.vertigo.core.spaces.definiton.Definition;
import io.vertigo.core.spaces.definiton.DefinitionPrefix;
import io.vertigo.core.spaces.definiton.DefinitionReference;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.VSystemException;

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
	private final String name;
	private final DataType dataType;

	/** Formatter. */
	private final DefinitionReference<FormatterDefinition> formatterDefinitionRef;

	/** Contraintes du domaine. */
	private final List<DefinitionReference<ConstraintDefinition>> constraintDefinitionRefs;

	/** Conteneur des couples (propriétés, valeur) */
	private final Properties properties;

	/**
	 * Nom de la Définition dans le cas de DtObject ou DtList
	 */
	private final String dtDefinitionName;

	/**
	 * Constructeur.
	 * @param dataType Type Dynamo
	 */
	public Domain(final String name, final DataType dataType) {
		this(name, dataType, null, Collections.<ConstraintDefinition> emptyList(), new PropertiesBuilder().build());
	}

	/**
	 * Constructor.
	 * @param name the name of the domain
	 * @param dataType the type of the domain
	 * @param formatterDefinition the formatter 
	 * @param constraintDefinitions the list of constraints
	 * @param properties Map des (DtProperty, value)
	 */
	public Domain(final String name, final DataType dataType, final FormatterDefinition formatterDefinition, final List<ConstraintDefinition> constraintDefinitions, final Properties properties) {
		//--Vérification des contrats
		Assertion.checkArgNotEmpty(name);
		//formatterDefinition can be null
		Assertion.checkNotNull(constraintDefinitions);
		Assertion.checkNotNull(properties);
		//-----
		this.name = name;
		this.dataType = dataType;
		formatterDefinitionRef = formatterDefinition == null ? null : new DefinitionReference<>(formatterDefinition);
		//On rend la liste des contraintes non modifiable
		final List<DefinitionReference<ConstraintDefinition>> myConstraintDefinitionRefs = new ArrayList<>();
		for (final ConstraintDefinition constraintDefinition : constraintDefinitions) {
			myConstraintDefinitionRefs.add(new DefinitionReference<>(constraintDefinition));
		}
		constraintDefinitionRefs = Collections.unmodifiableList(myConstraintDefinitionRefs);
		//========================MISE A JOUR DE LA MAP DES PROPRIETES==========
		this.properties = buildProperties(constraintDefinitions, properties);

		//Mise à jour de la FK.
		if (this.properties.getValue(DtProperty.TYPE) != null) {
			Assertion.checkArgument(!getDataType().isPrimitive(), "Le type ne peut être renseigné que pour des types non primitifs");
			//-----
			//On ne s'intéresse qu'au type de DTO et DTC dont le type de DT est déclaré
			dtDefinitionName = this.properties.getValue(DtProperty.TYPE);
		} else {
			dtDefinitionName = null;
		}
	}

	private static Properties buildProperties(final List<ConstraintDefinition> constraintDefinitions, final Properties inputProperties) {
		final PropertiesBuilder propertiesBuilder = new PropertiesBuilder();
		for (final Property property : inputProperties.getProperties()) {
			propertiesBuilder.addValue(property, inputProperties.getValue(property));
		}

		//On récupère les propriétés d'après les contraintes
		for (final ConstraintDefinition constraintDefinition : constraintDefinitions) {
			propertiesBuilder.addValue(constraintDefinition.getProperty(), constraintDefinition.getPropertyValue());
		}
		return propertiesBuilder.build();
	}

	/**
	 * Returns the type of the domain.
	 *
	 * @return the type.
	 */
	public DataType getDataType() {
		return dataType;
	}

	/**
	 * Returns the formatter of the domain.
	 *
	 * @return the formatter.
	 */
	public FormatterDefinition getFormatter() {
		Assertion.checkNotNull(formatterDefinitionRef, "no formatter defined on {0}", this);
		//-----
		return formatterDefinitionRef.get();
	}

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
		for (final DefinitionReference<ConstraintDefinition> constraintDefinitionRef : constraintDefinitionRefs) {
			//Il suffit d'une contrainte non respectée pour qu'il y ait non validation
			if (!constraintDefinitionRef.get().checkConstraint(value)) {
				throw new ConstraintException(constraintDefinitionRef.get().getErrorMessage());
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
				throw new VSystemException("Le domain {0} n'est ni un DTO ni une DTC", getName());
			}
			throw new VSystemException("Le domain {0} est un DTO/DTC mais typé de façon dynamique donc sans DtDefinition.", getName());
		}
		return Home.getApp().getDefinitionSpace().resolve(dtDefinitionName, DtDefinition.class);
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
