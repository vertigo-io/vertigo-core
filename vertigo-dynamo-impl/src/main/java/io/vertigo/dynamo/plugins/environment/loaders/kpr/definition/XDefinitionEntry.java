package io.vertigo.dynamo.plugins.environment.loaders.kpr.definition;

import io.vertigo.dynamo.impl.environment.kernel.model.DynamicDefinition;
import io.vertigo.kernel.lang.Assertion;

import java.util.List;

/**
 * Une entrée de type définition est définie (XOR) 
 * - soit par un champ et une définition, 
 * - soit par un champ et une liste de clés de définition.
 * 
 * @author pchretien
 */
public final class XDefinitionEntry {
	/**
	 * Obligatoire
	 */
	private final String fieldName;
	private final DynamicDefinition definition;
	private final List<String> definitionKeys;

	/**
	 * Constructeur.
	 * 
	 * @param definitionKeyList Liste des clés de définition
	 * @param fieldName Nom du champ
	 */
	public XDefinitionEntry(final String fieldName, final List<String> definitionKeyList) {
		Assertion.checkNotNull(fieldName);
		Assertion.checkNotNull(definitionKeyList);
		// ----------------------------------------------------------------------
		this.fieldName = fieldName;
		definition = null;
		definitionKeys = definitionKeyList;
	}

	/**
	 * Constructeur.
	 * 
	 * @param fieldName Nom du champ
	 * @param definition Définition
	 */
	public XDefinitionEntry(final String fieldName, final DynamicDefinition definition) {
		Assertion.checkNotNull(fieldName);
		Assertion.checkNotNull(definition);
		// ----------------------------------------------------------------------
		this.fieldName = fieldName;
		this.definition = definition;
		definitionKeys = null;
	}

	/**
	 * @return Nom du champ
	 */
	public String getFieldName() {
		return fieldName;
	}

	/**
	 * Permet de savoir dans quel mode on se trouve (Definition XOR List<DefinitionKey>).
	 * 
	 * @return boolean
	 */
	public boolean containsDefinition() {
		return definition != null;
	}

	/**
	 * @return Définition
	 */
	public DynamicDefinition getDefinition() {
		Assertion.checkNotNull(definition);
		// ----------------------------------------------------------------------
		return definition;
	}

	/**
	 * @return List des clés de définition
	 */
	public List<String> getDefinitionKeys() {
		Assertion.checkNotNull(definitionKeys);
		// ----------------------------------------------------------------------
		return definitionKeys;
	}
}
