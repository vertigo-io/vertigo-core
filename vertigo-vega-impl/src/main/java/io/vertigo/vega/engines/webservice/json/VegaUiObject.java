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
package io.vertigo.vega.engines.webservice.json;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import io.vertigo.core.spaces.definiton.DefinitionReference;
import io.vertigo.dynamo.domain.metamodel.Domain;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.metamodel.Formatter;
import io.vertigo.dynamo.domain.metamodel.FormatterException;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.lang.Assertion;
import io.vertigo.util.StringUtil;
import io.vertigo.vega.webservice.validation.DtObjectErrors;
import io.vertigo.vega.webservice.validation.DtObjectValidator;
import io.vertigo.vega.webservice.validation.UiMessageStack;

/**
 * UiObject is used as an Input buffer from client.
 * It managed to :
 * - merge a serverSideObject and an inputBufferObject
 * - check validators
 * - return merged Object
 *
 * @author pchretien, npiedeloup
 * @param <D> DtObject type
 */
public class VegaUiObject<D extends DtObject> implements io.vertigo.vega.webservice.model.UiObject<D> {
	private static final long serialVersionUID = -4639050257543017072L;

	/**
	 * Index de transformation des propriétés CamelCase en champs du Dt en const
	 */
	protected final Map<String, String> camel2ConstIndex = new HashMap<>();
	private final Map<String, String> const2CamelIndex = new HashMap<>();

	/** Référence vers la définition. */
	private final DefinitionReference<DtDefinition> dtDefinitionRef;

	private String inputKey;
	private D inputDto;
	private final Map<String, String> inputBuffer = new LinkedHashMap<>();

	private transient boolean isChecked; //init a false

	/**
	 * DtObject dont on gère le buffer d'input.
	 */
	private D serverSideDto;
	private String serverSideToken;

	private transient DtObjectErrors dtObjectErrors;

	// =========================================================================
	// ========================CONSTRUCTEUR=====================================
	// ==========================================================================
	/**
	 * Constructor.
	 * @param inputDto partial object translated from input
	 * @param modifiedFields modified fieldNames
	 */
	public VegaUiObject(final D inputDto, final Set<String> modifiedFields) {
		Assertion.checkNotNull(inputDto, "inputObject can't be null");
		Assertion.checkNotNull(modifiedFields, "modifiedFields can't be null");
		//-----
		this.inputDto = inputDto;
		this.dtDefinitionRef = new DefinitionReference<>(DtObjectUtil.findDtDefinition(inputDto));
		for (final DtField dtField : dtDefinitionRef.get().getFields()) {
			camel2ConstIndex.put(StringUtil.constToLowerCamelCase(dtField.getName()), dtField.getName());
			const2CamelIndex.put(dtField.getName(), StringUtil.constToLowerCamelCase(dtField.getName()));
		}

		for (final String field : modifiedFields) {
			setTypedValue(field, (Serializable) getDtField(field).getDataAccessor().getValue(inputDto));
		}

	}

	// ==========================================================================

	/**
	 * @return Server Side Token , null if none
	 */
	@Override
	public String getServerSideToken() {
		return serverSideToken;
	}

	/**
	 * @param serverSideToken Server Side Token
	 */
	@Override
	public void setServerSideToken(final String serverSideToken) {
		this.serverSideToken = serverSideToken;
	}

	/**
	 * @return Server Side Object , null if none
	 */
	@Override
	public D getServerSideObject() {
		return serverSideDto;
	}

	/**
	 * @param serverSideDto Object keep server side
	 */
	@Override
	public void setServerSideObject(final D serverSideDto) {
		Assertion.checkNotNull(serverSideDto, "ServerSideObject can't be null");
		//-----
		this.serverSideDto = serverSideDto;
	}

	/**
	 * @param inputKey Object reference keep in this request context (for error handling)
	 */
	@Override
	public void setInputKey(final String inputKey) {
		this.inputKey = inputKey;
	}

	/**
	 * @return Object reference keep in this request context (for error handling)
	 */
	@Override
	public String getInputKey() {
		return inputKey;
	}

	/**
	 * @return DtDefinition de l'objet métier
	 */
	@Override
	public DtDefinition getDtDefinition() {
		return dtDefinitionRef.get();
	}

	private DtObjectErrors getDtObjectErrors() {
		if (dtObjectErrors == null) {
			dtObjectErrors = new DtObjectErrors();
		}
		return dtObjectErrors;
	}

	// ==========================================================================

	private void mergeInput() {
		Assertion.checkNotNull(serverSideDto, "serverSideDto is mandatory");
		Assertion.checkNotNull(inputDto, "inputDto is mandatory");
		//-----
		for (final DtField dtField : getDtDefinition().getFields()) {
			if (isModified(const2CamelIndex.get(dtField.getName()))) {
				dtField.getDataAccessor().setValue(serverSideDto, dtField.getDataAccessor().getValue(inputDto));
			}
		}
	}

	/**
	 * Vérifie les UiObjects de la liste et remplis la pile d'erreur.
	 * @param uiMessageStack Pile des messages qui sera mise à jour
	 */
	@Override
	public void checkFormat(final UiMessageStack uiMessageStack) {
		if (getDtObjectErrors().hasError()) {
			getDtObjectErrors().flushIntoMessageStack(inputKey, uiMessageStack);
		}
		isChecked = true;
	}

	/**
	 * Merge et Valide l'objet d'IHM et place les erreurs rencontrées dans la stack.
	 * @param dtObjectValidators Validateurs à utiliser, peut-Ãªtre spécifique Ã  l'objet.
	 * @param uiMessageStack Pile des messages qui sera mise Ã  jour
	 * @return Objet métier mis Ã  jour
	 */
	@Override
	public D mergeAndCheckInput(final List<DtObjectValidator<D>> dtObjectValidators, final UiMessageStack uiMessageStack) {
		Assertion.checkNotNull(dtObjectValidators);
		//-----
		if (!isChecked) {
			checkFormat(uiMessageStack);
		}
		//we update inputBuffer with older datas
		if (serverSideDto != null) { //If serverSideObject was kept, we merge input with server object
			mergeInput();
			compactModifiedSet();
		}
		final D objectToValidate = serverSideDto != null ? serverSideDto : inputDto;
		//we remove older errors
		getDtObjectErrors().clearErrors();
		//we check validator
		for (final DtObjectValidator<D> dtObjectValidator : dtObjectValidators) {
			dtObjectValidator.validate(objectToValidate, inputBuffer.keySet(), getDtObjectErrors());
		}

		getDtObjectErrors().flushIntoMessageStack(inputKey, uiMessageStack);
		inputBuffer.clear();
		if (serverSideDto != null) {
			inputDto = (D) DtObjectUtil.createDtObject(getDtDefinition());
			return serverSideDto;
		}
		return inputDto;
	}

	/**
	 * Mise à jour des données typées.
	 * Verifie si la valeur correspond à une modification.
	 * Si oui, la valeur est gardée, sinon la saisie de l'utilisateur est vidée.
	 */
	private void compactModifiedSet() {
		Assertion.checkNotNull(serverSideDto, "serverSideDto is mandatory");
		Assertion.checkNotNull(inputDto, "inputDto is mandatory");
		//-----
		final List<String> modifiedFields = inputBuffer.keySet().stream().collect(Collectors.toList());
		for (final String camelField : modifiedFields) {
			//Si le champs n'a pas d'erreur
			//On regarde pour vider le buffer
			if (!getDtObjectErrors().hasError(camelField)) {
				// ======================================================================
				// ======================Mise Ã  jour différentielle du BUFFER============
				// ======================================================================
				final DtField dtField = getDtField(camelField);
				// Egalité entre la valeur d'origine et la valeur saisie.
				if (Objects.equals(dtField.getDataAccessor().getValue(serverSideDto), dtField.getDataAccessor().getValue(inputDto))) {
					// Si la valeur saisie est identique à la valeur d'origine
					// alors on purge le buffer de saisie.
					inputBuffer.remove(camelField);
				}
			}
		}
	}

	protected DtField getDtField(final String camelField) {
		return getDtDefinition().getField(camel2ConstIndex.get(camelField));
	}

	/**
	 * @param fieldName Champs
	 * @return Si le champs à été modifié dans le UiObject
	 */
	@Override
	public boolean isModified(final String fieldName) {
		Assertion.checkArgNotEmpty(fieldName);
		//-----
		return inputBuffer.containsKey(fieldName);
	}

	/**
	 * @return All modified fieldNames (camel)
	 */
	@Override
	public Set<String> getModifiedFields() {
		return inputBuffer.keySet();
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return new StringBuilder("uiObject(modified:")
				.append(inputBuffer.keySet())
				.append(" over dto:")
				.append(serverSideDto.toString())
				.append(")")
				.toString();
	}

	/** {@inheritDoc} */
	@Override
	public String getInputValue(final String fieldName) {
		Assertion.checkArgNotEmpty(fieldName);
		Assertion.checkArgument(Character.isLowerCase(fieldName.charAt(0)) && !fieldName.contains("_"), "Le nom du champs doit-être en camelCase ({0}).", fieldName);
		//-----
		if (hasFormatError(fieldName)) {
			return inputBuffer.get(fieldName);
		}
		final Object value = doGetTypedValue(fieldName);
		final Domain domain = getDtField(fieldName).getDomain();
		if (domain.getDataType().isPrimitive()) {
			final Formatter formatter = domain.getFormatter();
			return formatter.valueToString(value, domain.getDataType());
		}
		return null; // Les liste et les objets ne sont pas gérés

	}

	/** {@inheritDoc} */
	@Override
	public void setInputValue(final String fieldName, final String stringValue) {
		Assertion.checkArgNotEmpty(fieldName);
		Assertion.checkNotNull(stringValue, "La valeur formatée ne doit pas être null mais vide ({0})", fieldName);
		//-----
		final DtField dtField = getDtField(fieldName);
		inputBuffer.put(fieldName, formatValue(dtField, stringValue));

	}

	/**
	 * @param dtField Champs
	 * @return Valeur typée du champs
	 * @throws IllegalAccessError Si le champs possède une erreur de formatage
	 */
	protected <T> T getTypedValue(final String fieldName, final Class<T> type) {
		Assertion.checkArgNotEmpty(fieldName);
		Assertion.checkNotNull(type);
		//-----
		if (hasFormatError(fieldName)) {
			throw new IllegalAccessError("Le champ " + fieldName + " possède une erreur de formattage et doit être lu par son UiObject");
		}
		return type.cast(doGetTypedValue(fieldName));
	}

	/* (non-Javadoc)
	 * @see io.vertigo.vega.webservice.model.UiObject#setTypedValue(java.lang.String, java.io.Serializable)
	 */
	@Override
	public void setTypedValue(final String fieldName, final Serializable value) {
		final DtField dtField = getDtField(fieldName);
		isChecked = false;

		//on a trois choix :
		// 1) soit on ne fait pas de controle ici (sera fait par le check plus tard)
		// 2) soit on fait un check et on remplit la stack d'erreur
		// 3) soit on check et on lance une Runtime si erreur (comme dans DtObject)
		//100924 NPI : choix retenu 1
		doSetTypedValue(dtField, value);
		inputBuffer.put(fieldName, getInputValue(fieldName));
	}

	/**
	 * Récupération des données formatées.
	 *
	 * @param dtField Champ
	 * @return Valeur formatée (typée)
	 */
	private Object doGetTypedValue(final String fieldName) {
		Assertion.checkArgNotEmpty(fieldName);
		//
		final DtField dtField = getDtField(fieldName);
		if (isModified(fieldName)) {
			//Si le tableaux des valeurs formatées n'a pas été créé la valeur est null.
			return dtField.getDataAccessor().getValue(inputDto);
		}
		if (serverSideDto != null) {
			return dtField.getDataAccessor().getValue(serverSideDto);
		}
		return null;
	}

	/**
	 * @return Si des champs ont été modifiés dans le UiObject
	 */
	@Override
	public boolean isModified() {
		return !inputBuffer.isEmpty();
	}

	/**
	 * @param dtField Champs
	 * @return Si le champs a une erreur de formatage
	 */
	boolean hasFormatError(final String fieldName) {
		Assertion.checkArgNotEmpty(fieldName);
		//-----
		return isModified(fieldName) && getDtObjectErrors().hasError(fieldName);
	}

	private String formatValue(final DtField dtField, final String value) {
		isChecked = false;
		getDtObjectErrors().clearErrors(dtField.getName());
		final Formatter formatter = dtField.getDomain().getFormatter();
		try {
			final Serializable typedValue = (Serializable) formatter.stringToValue(value, dtField.getDomain().getDataType());
			doSetTypedValue(dtField, typedValue);
			return formatter.valueToString(typedValue, dtField.getDomain().getDataType());
		} catch (final FormatterException e) { //We don't log nor rethrow this exception
			/** Erreur de typage.	 */
			getDtObjectErrors().addError(StringUtil.constToLowerCamelCase(dtField.getName()), e.getMessageText());
			return value;
		}
	}

	private void doSetTypedValue(final DtField dtField, final Serializable typedValue) {
		dtField.getDataAccessor().setValue(inputDto, typedValue);
	}

	/* (non-Javadoc)
	 * @see io.vertigo.vega.webservice.model.UiObject#getInteger(java.lang.String)
	 */
	@Override
	public Integer getInteger(final String fieldName) {
		return getTypedValue(fieldName, Integer.class);
	}

	/* (non-Javadoc)
	 * @see io.vertigo.vega.webservice.model.UiObject#getLong(java.lang.String)
	 */
	@Override
	public Long getLong(final String fieldName) {
		return getTypedValue(fieldName, Long.class);
	}

	/* (non-Javadoc)
	 * @see io.vertigo.vega.webservice.model.UiObject#getString(java.lang.String)
	 */
	@Override
	public String getString(final String fieldName) {
		return getTypedValue(fieldName, String.class);
	}

	/* (non-Javadoc)
	 * @see io.vertigo.vega.webservice.model.UiObject#getBoolean(java.lang.String)
	 */
	@Override
	public Boolean getBoolean(final String fieldName) {
		return getTypedValue(fieldName, Boolean.class);
	}

	/* (non-Javadoc)
	 * @see io.vertigo.vega.webservice.model.UiObject#getDate(java.lang.String)
	 */
	@Override
	public Date getDate(final String fieldName) {
		return getTypedValue(fieldName, Date.class);
	}

	/* (non-Javadoc)
	 * @see io.vertigo.vega.webservice.model.UiObject#getBigDecimal(java.lang.String)
	 */
	@Override
	public BigDecimal getBigDecimal(final String fieldName) {
		return getTypedValue(fieldName, BigDecimal.class);
	}

}
