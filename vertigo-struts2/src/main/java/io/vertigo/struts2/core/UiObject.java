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
package io.vertigo.struts2.core;

import io.vertigo.core.spaces.definiton.DefinitionReference;
import io.vertigo.dynamo.domain.metamodel.DataType;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.metamodel.Formatter;
import io.vertigo.dynamo.domain.metamodel.FormatterException;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.VSystemException;
import io.vertigo.lang.VUserException;
import io.vertigo.util.StringUtil;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Objet d'IHM, fournit les valeurs formatés des champs de l'objet métier sous-jacent.
 * Implements Map<String, Object> car struts poste des String[] que l'on reconverti en String (on prend le premier).
 *
 * @author pchretien, npiedeloup
 * @param <D> Type de DtObject représenté par cet Input
 */
public final class UiObject<D extends DtObject> implements Map<String, Serializable>, Serializable {

	private static final long serialVersionUID = -4639050257543017072L;
	private static final Serializable FORMAT_ERROR_VALUE = "UI_FORMAT_ERROR_VALUE"; //String car new Serializable change a chaque redémarrage : n'est pas sérialisable
	private static final String DOMAIN_MULTIPLE_IDS = "DO_MULTIPLE_IDS";

	/**
	 * Index de transformation des propriétés CamelCase en champs du Dt en const
	 */
	private final Map<String, String> keysIndex = new HashMap<>();

	/** Référence vers la Définition. */
	private final DefinitionReference<DtDefinition> dtDefinitionRef;

	/**
	 * DtObject dont on gère le buffer d'input.
	 */
	private final D dto;
	private final Map<String, String> inputBuffer = new LinkedHashMap<>();
	private final Map<String, Serializable> modifiedTypedValues = new LinkedHashMap<>();

	private transient boolean isChecked; //init a false
	private transient UiObjectErrors uiObjectErrors;

	// =========================================================================
	// ========================CONSTRUCTEUR=====================================
	// ==========================================================================
	/**
	 * Constructeur.
	 * @param dto DtObject d'origine
	 */
	public UiObject(final D dto) {
		Assertion.checkNotNull(dto);
		//-----
		this.dto = dto;
		this.dtDefinitionRef = new DefinitionReference<>(DtObjectUtil.findDtDefinition(dto));
		for (final DtField dtField : getDtDefinition().getFields()) {
			keysIndex.put(StringUtil.constToLowerCamelCase(dtField.getName()), dtField.getName());
		}
	}

	/**
	 * @return Objet interne utilisé par le context pour indexer les clés par DtObject.
	 */
	D getInnerObject() {
		return dto;
	}

	/**
	 * @return DtDefinition de l'objet métier
	 */
	public DtDefinition getDtDefinition() {
		return dtDefinitionRef.get();
	}

	private UiObjectErrors getUiObjectErrors() {
		if (uiObjectErrors == null) {
			uiObjectErrors = new UiObjectErrors(dto);
		}
		return uiObjectErrors;
	}

	/** {@inheritDoc} */
	@Override
	public Serializable get(final Object key) {
		final String keyFieldName = String.class.cast(key);
		Assertion.checkArgNotEmpty(keyFieldName);
		Assertion.checkArgument(Character.isLowerCase(keyFieldName.charAt(0)) && !keyFieldName.contains("_"), "Le nom du champs doit-être en camelCase ({0}).", keyFieldName);
		final String constFieldName = keysIndex.get(keyFieldName);
		Assertion.checkArgNotEmpty(constFieldName);
		//-----
		final DtField dtField = getDtField(constFieldName);
		if (isMultiple(dtField)) {
			final String strValue = getValueAsString(dtField);
			return parseMultipleValue(strValue);
		} else if (isBoolean(dtField)) {
			final Boolean value = (Boolean) getTypedValue(dtField);
			final String strValue = value != null ? String.valueOf(value) : null;
			return strValue;
		} else {
			return getValueAsString(dtField);
		}
	}

	/** {@inheritDoc} */
	@Override
	public String put(final String fieldName, final Serializable value) {
		final String constFieldName = keysIndex.get(fieldName);
		Assertion.checkArgNotEmpty(constFieldName);
		Assertion.checkNotNull(value, "La valeur formatée ne doit pas être null mais vide ({0})", fieldName);
		Assertion.checkState(value instanceof String || value instanceof String[], "Les données saisies doivent être de type String ou String[] ({0} : {1})", fieldName, value.getClass());
		//-----
		final DtField dtField = getDtField(constFieldName);
		if (isMultiple(dtField)) {
			final String strValue = formatMultipleValue((String[]) value);
			return inputBuffer.put(constFieldName, formatValue(dtField, strValue));
		}
		final String strValue = requestParameterToString(value);
		return inputBuffer.put(constFieldName, formatValue(dtField, strValue));
	}

	/**
	 * @param dtField Champs
	 * @return Valeur typée du champs
	 * @throws IllegalAccessError Si le champs possède une erreur de formatage
	 */
	Object getTypedValue(final DtField dtField) {
		if (hasFormatError(dtField)) {
			throw new IllegalAccessError("Le champ " + dtField.getName() + " possède une erreur de formattage et doit être lu par son UiObject");
		}
		//-----
		return doGetTypedValue(dtField);
	}

	/**
	 * Permet de forcer la valeur de l'uiObject depuis l'action.
	 * Réinitialise les erreurs.
	 * @param fieldName Nom du champs
	 * @param value Valeur du champs
	 */
	public void setTypedValue(final String fieldName, final Serializable value) {
		final String constFieldName = keysIndex.get(fieldName);
		final DtField dtField = getDtField(constFieldName);
		isChecked = false;

		//on a trois choix :
		// 1) soit on ne fait pas de controle ici (sera fait par le check plus tard)
		// 2) soit on fait un check et on remplit la stack d'erreur
		// 3) soit on check et on lance une Runtime si erreur (comme dans DtObject)
		//100924 NPI : choix retenu 1
		doSetTypedValue(constFieldName, value);
		inputBuffer.put(constFieldName, getValueAsString(dtField));
	}

	// ==========================================================================

	/**
	 * Valide l'objet d'IHM pour mettre à jour l'objet métier.
	 * @param validator Validateur à utilisé, peut-être spécifique à l'objet.
	 * @param uiMessageStack Pile des messages qui sera mise à jour
	 * @return Objet métier mis à jour
	 * @throws VUserException Si des erreurs ont été levées
	 */
	public D validate(final UiObjectValidator<D> validator, final UiMessageStack uiMessageStack) throws VUserException {
		//pour compatibilité ascendante : on check, puis on flush
		if (!isChecked) {
			check(validator, uiMessageStack);
		}
		return flush();
	}

	/**
	 * Si il n'y a pas d'erreur dans l'objet d'IHM, l'objet métier est mis à jour et retourné.
	 * @return Objet métier mis à jour
	 * @throws VUserException Si des erreurs ont été levées
	 */
	public D flush() throws VUserException {
		if (!isModified()) {
			return dto;
		}

		Assertion.checkState(isChecked, "Le UiObject n'a pas encore été vérifié avec un validator");
		// Si le buffer possède des erreurs, on lance une KUser avec l'ensemble des erreurs du process.
		if (getUiObjectErrors().hasError()) {
			throw new ValidationUserException(Collections.<UiError> emptyList());
		}
		// On recopie le buffer dans le dto.
		// Le buffer ne possède pas d'erreur on peut donc le copier.
		for (final String fieldName : inputBuffer.keySet()) {
			final DtField dtField = getDtField(fieldName);
			dtField.getDataAccessor().setValue(dto, doGetTypedValue(dtField));
		}
		return dto;
	}

	/**
	 * Valide l'objet d'IHM et place les erreurs rencontrées dans l'action Struts.
	 * @param validator Validateur à utilisé, peut-être spécifique à l'objet.
	 * @param uiMessageStack Pile des messages qui sera mise à jour
	 */
	public void check(final UiObjectValidator<D> validator, final UiMessageStack uiMessageStack) {
		Assertion.checkNotNull(validator);
		//-----
		//on vide les erreurs (sauf de format), avant de valider l'objet.
		//sinon messages d'erreurs en doublons.
		getUiObjectErrors().clearErrors();

		//on recheck les erreurs de format
		addFormatErrors();
		//puis les contraintes
		validator.validate(this, inputBuffer.keySet(), getUiObjectErrors());
		isChecked = true;
		compactModifiedSet();
		getUiObjectErrors().flushIntoAction(uiMessageStack);
	}

	/**
	 * @param constFieldName Nom du champs
	 * @return Valeur typée
	 * @throws IllegalAccessError Si le champs possède une erreur de formatage
	 */
	public Integer getInteger(final String constFieldName) {
		return (Integer) getTypedValue(getDtField(constFieldName));
	}

	/**
	 * @param constFieldName Nom du champs
	 * @return Valeur typée
	 * @throws IllegalAccessError Si le champs possède une erreur de formatage
	 */
	public Long getLong(final String constFieldName) {
		return (Long) getTypedValue(getDtField(constFieldName));
	}

	/**
	 * @param constFieldName Nom du champs
	 * @return Valeur typée
	* @throws IllegalAccessError Si le champs possède une erreur de formatage
	  */
	public String getString(final String constFieldName) {
		return (String) getTypedValue(getDtField(constFieldName));
	}

	/**
	 * @param constFieldName Nom du champs
	 * @return Valeur typée
	 * @throws IllegalAccessError Si le champs possède une erreur de formatage
	 */
	public Boolean getBoolean(final String constFieldName) {
		return (Boolean) getTypedValue(getDtField(constFieldName));
	}

	/**
	 * @param constFieldName Nom du champs
	 * @return Valeur typée
	 * @throws IllegalAccessError Si le champs possède une erreur de formatage
	 */
	public Date getDate(final String constFieldName) {
		return (Date) getTypedValue(getDtField(constFieldName));
	}

	/**
	 * @param constFieldName Nom du champs
	 * @return Valeur typée
	 * @throws IllegalAccessError Si le champs possède une erreur de formatage
	 */
	public BigDecimal getBigDecimal(final String constFieldName) {
		return (BigDecimal) getTypedValue(getDtField(constFieldName));
	}

	private DtField getDtField(final String constFieldName) {
		return getDtDefinition().getField(constFieldName);
	}

	private static String requestParameterToString(final Serializable value) {
		return value instanceof String[] ? ((String[]) value)[0] : (String) value;
	}

	private static String formatMultipleValue(final String[] values) {
		final StringBuilder sb = new StringBuilder();
		String sep = "";
		for (final String value : values) {
			sb.append(sep);
			sb.append(value);
			sep = ";";
		}
		return sb.toString();
	}

	private static String[] parseMultipleValue(final String strValue) {
		return strValue.split(";");
	}

	private String getValueAsString(final DtField dtField) {
		if (hasFormatError(dtField)) {
			return inputBuffer.get(dtField.getName());
		}
		final Object value = doGetTypedValue(dtField);
		final Formatter formatter = dtField.getDomain().getFormatter();
		return formatter.valueToString(value, dtField.getDomain().getDataType());
	}

	private static boolean isMultiple(final DtField dtField) {
		return dtField.getDomain().getName().equals(DOMAIN_MULTIPLE_IDS);
	}

	private static boolean isBoolean(final DtField dtField) {
		return dtField.getDomain().getDataType() == DataType.Boolean;
	}

	/**
	 * Mise à jour des données typées.
	 * Verifie si la valeur correspond à une modification.
	 * Si oui, la valeur est gardée, sinon la saisie de l'utilisateur est vidée.
	 */
	private void compactModifiedSet() {
		final Set<String> unmodifiedFieldNameSet = new HashSet<>();
		for (final String constFieldName : inputBuffer.keySet()) {
			//Si le champs n'a pas d'erreur
			//On regarde pour vider le buffer
			final DtField dtField = getDtField(constFieldName);
			// Mise à jour différentielle du BUFFER : check égalité entre la valeur d'origine et la valeur saisie.
			if (!uiObjectErrors.hasError(dtField)
					&& Objects.equals(dtField.getDataAccessor().getValue(dto), doGetTypedValue(dtField))) {
				// Si la valeur saisie est identique à la valeur d'origine
				// alors on purge le buffer de saisie.
				modifiedTypedValues.remove(constFieldName);
				unmodifiedFieldNameSet.add(constFieldName);
			}
		}
		for (final String fieldName : unmodifiedFieldNameSet) {
			inputBuffer.remove(fieldName);
		}
	}

	/**
	 * Récupération des données formatées.
	 *
	 * @param dtField Champ
	 * @return Valeur formatée (typée)
	 */
	private Object doGetTypedValue(final DtField dtField) {
		if (isModified(dtField)) {
			//Si le tableaux des valeurs formatées n'a pas été créé la valeur est null.
			return modifiedTypedValues.get(dtField.getName());
		}
		return dtField.getDataAccessor().getValue(dto);
	}

	/**
	 * @param dtField Champs
	 * @return Si le champs à été modifié dans le UiObject
	 */
	boolean isModified(final DtField dtField) {
		Assertion.checkNotNull(dtField);
		//-----
		return inputBuffer.containsKey(dtField.getName());
	}

	/**
	 * @return Si des champs ont été modifiés dans le UiObject
	 */
	boolean isModified() {
		return !inputBuffer.isEmpty();
	}

	/**
	 * @param dtField Champs
	 * @return Si le champs a une erreur de formatage
	 */
	boolean hasFormatError(final DtField dtField) {
		Assertion.checkNotNull(dtField);
		//-----
		return isModified(dtField) && FORMAT_ERROR_VALUE.equals(doGetTypedValue(dtField));
	}

	private void addFormatErrors() {
		for (final Map.Entry<String, Serializable> entry : modifiedTypedValues.entrySet()) {
			if (FORMAT_ERROR_VALUE.equals(entry.getValue())) {
				final String constFieldName = entry.getKey();
				final DtField dtField = getDtField(constFieldName);
				final Formatter formatter = dtField.getDomain().getFormatter();
				try {
					final Serializable typedValue = (Serializable) formatter.stringToValue(inputBuffer.get(constFieldName), dtField.getDomain().getDataType());
					throw new VSystemException("Erreur de formatage non reproduite ('" + inputBuffer.get(constFieldName) + "'=>" + typedValue + "), l'UiObject doit être désynchronisé. Recharger votre page. " + this.toString());
				} catch (final FormatterException e) {
					getUiObjectErrors().addError(dtField, e.getMessageText());
				}
			}
		}
	}

	private String formatValue(final DtField dtField, final String value) {
		isChecked = false;
		getUiObjectErrors().clearErrors(dtField);
		final Formatter formatter = dtField.getDomain().getFormatter();
		try {
			final Serializable typedValue = (Serializable) formatter.stringToValue(value, dtField.getDomain().getDataType());
			doSetTypedValue(dtField.getName(), typedValue);
			return formatter.valueToString(typedValue, dtField.getDomain().getDataType());
		} catch (final FormatterException e) { //We don't log nor rethrow this exception
			/** Erreur de typage.	 */
			doSetTypedValue(dtField.getName(), FORMAT_ERROR_VALUE);
			return value;
		}
	}

	private void doSetTypedValue(final String constFieldName, final Serializable typedValue) {
		modifiedTypedValues.put(constFieldName, typedValue);
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "uiObject(buffer:" + inputBuffer.toString() + " over dto:" + dto.toString() + ")";
	}

	/** {@inheritDoc} */
	@Override
	public boolean containsKey(final Object arg0) {
		return keysIndex.containsKey(arg0);
	}

	/** Non implémenté. */
	@Override
	public void clear() {
		throw new UnsupportedOperationException("Non implémenté");
	}

	/** Non implémenté. */
	@Override
	public boolean containsValue(final Object arg0) {
		throw new UnsupportedOperationException("Non implémenté");
	}

	/** Non implémenté. */
	@Override
	public Set<java.util.Map.Entry<String, Serializable>> entrySet() {
		throw new UnsupportedOperationException("Non implémenté");
	}

	/** {@inheritDoc} */
	@Override
	public boolean isEmpty() {
		return keysIndex.isEmpty();
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> keySet() {
		return keysIndex.keySet();
	}

	/** Non implémenté. */
	@Override
	public void putAll(final Map<? extends String, ? extends Serializable> arg0) {
		throw new UnsupportedOperationException("Non implémenté");
	}

	/** Non implémenté. */
	@Override
	public String remove(final Object arg0) {
		throw new UnsupportedOperationException("Non implémenté");
	}

	/** {@inheritDoc} */
	@Override
	public int size() {
		return keysIndex.size();
	}

	/** Non implémenté. */
	@Override
	public Collection<Serializable> values() {
		throw new UnsupportedOperationException("Non implémenté");
	}
}
