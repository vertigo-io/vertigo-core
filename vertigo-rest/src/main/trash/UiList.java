package io.vertigo.rest.engine;

import io.vertigo.dynamo.domain.metamodel.DataType;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.metamodel.DefinitionReference;
import io.vertigo.kernel.util.StringUtil;
import io.vertigo.rest.validation.DtObjectErrors;
import io.vertigo.rest.validation.DtObjectValidator;
import io.vertigo.rest.validation.UiMessageStack;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class UiList<D extends DtObject> extends AbstractList<UiObject<D>> {
	/**
	* Index de transformation des propriétés CamelCase en champs du Dt en const
	*/
	private final Map<String, String> camel2ConstIndex = new HashMap<>();
	private final Map<String, String> const2CamelIndex = new HashMap<>();

	/** Référence vers la Définition. */
	private final DefinitionReference<DtDefinition> dtDefinitionRef;

	//server side
	private final Map<Integer, UiObject<D>> uiObjectByIndex = new HashMap<>();
	private List<D> serverSideDtList;
	private String serverSideToken;

	//search
	private OData filterMetaData;

	//input
	private final List<UiObject<D>> collCreates = new ArrayList<>();
	private final List<UiObject<D>> collUpdates = new ArrayList<>();
	private final List<UiObject<D>> collDeletes = new ArrayList<>();
	private List<D> inputDtList;

	private String inputKey;

	// =========================================================================
	// ========================CONSTRUCTEUR=====================================
	// ==========================================================================
	/**
	 * Constructor.
	 * @param dtoClass Object class buffered
	 */
	public UiList(final Class<D> dtListClass) {
		Assertion.checkNotNull(dtListClass);
		// -------------------------------------------------------------------------
		this.dtDefinitionRef = new DefinitionReference<>(DtObjectUtil.findDtDefinition(dtListClass));
		for (final DtField dtField : getDtDefinition().getFields()) {
			camel2ConstIndex.put(StringUtil.constToCamelCase(dtField.getName(), false), dtField.getName());
			const2CamelIndex.put(dtField.getName(), StringUtil.constToCamelCase(dtField.getName(), false));
		}
	}

	// ==========================================================================

	/**
	 * @return Server Side Token , null if none
	 */
	public final String getServerSideToken() {
		return serverSideToken;
	}

	/**
	 * @param serverSideToken Server Side Token
	 */
	public final void setServerSideToken(final String serverSideToken) {
		this.serverSideToken = serverSideToken;
	}

	/**
	 * @param serverSideDtList List keep server side
	 */
	public final void setServerSideList(final DtList<D> serverSideDtList) {
		Assertion.checkNotNull(serverSideDtList, "serverSideDtList can't be null");
		//-------------------------------------------------------------------------------
		this.serverSideDtList = serverSideDtList;
	}

	/**
	 * @param inputDto partial object translated from input
	 * @param modifiedFields modified fieldNames
	 */
	public final void addInputCreate(final UiObject<D> inputCreate, final Set<String> modifiedFields) {
		Assertion.checkNotNull(inputCreate, "inputObject can't be null");
		Assertion.checkNotNull(modifiedFields, "modifiedFields can't be null");
		Assertion.checkArgument(!modifiedFields.isEmpty(), "modifiedFields can't be empty");
		//-------------------------------------------------------------------------------
		this.collCreates.add(inputCreate);
	}

	/**
	 * @param inputDto partial object translated from input
	 * @param modifiedFields modified fieldNames
	 */
	public final void addInputUpdate(final UiObject<D> inputUpdate, final Set<String> modifiedFields) {
		Assertion.checkNotNull(inputUpdate, "inputObject can't be null");
		Assertion.checkNotNull(modifiedFields, "modifiedFields can't be null");
		Assertion.checkArgument(!modifiedFields.isEmpty(), "modifiedFields can't be empty");
		//-------------------------------------------------------------------------------
		this.collUpdates.add(inputUpdate);
	}

	/**
	 * @param inputDto partial object translated from input
	 * @param modifiedFields modified fieldNames
	 */
	public final void addInputDelete(final UiObject<D> inputDelete, final Set<String> modifiedFields) {
		Assertion.checkNotNull(inputDelete, "inputObject can't be null");
		Assertion.checkNotNull(modifiedFields, "modifiedFields can't be null");
		Assertion.checkArgument(!modifiedFields.isEmpty(), "modifiedFields can't be empty");
		//-------------------------------------------------------------------------------
		this.collDeletes.add(inputDelete);
	}

	/**
	 * @param inputKey Object reference keep in this request context (for error handling)
	 */
	public final void setInputKey(final String inputKey) {
		this.inputKey = inputKey;
	}

	/**
	 * @return Object reference keep in this request context (for error handling)
	 */
	public final String getInputKey() {
		return inputKey;
	}

	/**
	 * @return DtDefinition de l'objet métier
	 */
	public final DtDefinition getDtDefinition() {
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
		Assertion.checkNotNull(modifiedFields, "modifiedFields is mandatory");
		//---------------------------------------------------------------------
		for (final DtField dtField : getDtDefinition().getFields()) {
			if (!isModified(dtField)) {
				dtField.getDataAccessor().setValue(inputDto, dtField.getDataAccessor().getValue(serverSideDto));
			}
		}
	}

	/**
	 * Merge et Valide l'objet d'IHM et place les erreurs rencontrées dans la stack.
	 * @param dtObjectValidators Validateurs à utiliser, peut-être spécifique à l'objet.
	 * @param uiMessageStack Pile des messages qui sera mise à jour
	 * @return Objet métier mis à jour
	 */
	public D mergeAndCheckInput(final List<DtObjectValidator<D>> dtObjectValidators, final UiMessageStack uiMessageStack) {
		Assertion.checkNotNull(dtObjectValidators);
		//---------------------------------------------------------------------
		//we update inputBuffer with older datas
		if (serverSideDto != null) { //If serverSideObject was kept, we merge input with server object
			mergeInput();
		}
		//we remove older errors
		getDtObjectErrors().clearErrors();
		//we check validator
		for (final DtObjectValidator<D> dtObjectValidator : dtObjectValidators) {
			dtObjectValidator.validate(inputDto, modifiedFields, getDtObjectErrors());
		}

		if (serverSideDto != null) { //If serverSideObject was kept, we compact modified fieldSet
			compactModifiedSet();
		}
		getDtObjectErrors().flushIntoMessageStack(inputKey, uiMessageStack);

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
		Assertion.checkNotNull(modifiedFields, "modifiedFields is mandatory");
		//---------------------------------------------------------------------
		final Set<String> updatedModifiedFields = new HashSet<>(modifiedFields);
		for (final String camelField : modifiedFields) {
			//Si le champs n'a pas d'erreur
			//On regarde pour vider le buffer
			if (!dtObjectErrors.hasError(camelField)) {
				// ======================================================================
				// ======================Mise à jour différentielle du BUFFER============
				// ======================================================================
				final DtField dtField = getDtField(camelField);
				final DataType dataType = dtField.getDomain().getDataType();
				// égalité entre la valeur d'origine et la valeur saisie.
				if (dataType.equals(dtField.getDataAccessor().getValue(serverSideDto), dtField.getDataAccessor().getValue(inputDto))) {
					// Si la valeur saisie est identique à la valeur d'origine
					// alors on purge le buffer de saisie.
					updatedModifiedFields.remove(camelField);
				}
			}
		}
		modifiedFields = Collections.unmodifiableSet(new LinkedHashSet<>(updatedModifiedFields));
	}

	private DtField getDtField(final String camelField) {
		return getDtDefinition().getField(camel2ConstIndex.get(camelField));
	}

	/**
	 * @param fieldName Champs
	 * @return Si le champs à été modifié dans le UiObject
	 */
	public boolean isModified(final String fieldName) {
		Assertion.checkArgNotEmpty(fieldName);
		//---------------------------------------------------------------------
		return modifiedFields.contains(fieldName);
	}

	private boolean isModified(final DtField dtField) {
		Assertion.checkNotNull(dtField);
		//---------------------------------------------------------------------
		return modifiedFields.contains(const2CamelIndex.get(dtField.getName()));
	}

	/**
	 * Constructeur.
	 * @param dtList Liste à encapsuler
	 */
	public UiList(final DtList<D> dtList) {
		super(dtList.getDefinition());
		// -------------------------------------------------------------------------
		this.dtList = dtList;
		if (dtList.size() < 1000) {
			initUiObjectByIdIndex();
		}
	}

	// ==========================================================================

	/** {@inheritDoc} */
	protected DtList<D> obtainDtList() {
		return dtList;
	}

	/** {@inheritDoc} */
	@Override
	public final UiObject<D> get(final int index) {
		UiObject<D> element = uiObjectByIndex.get(index);
		if (element == null) {
			element = new UiObject<>(obtainDtList().get(index));
			uiObjectByIndex.put(index, element);
			Assertion.checkState(uiObjectByIndex.size() < 1000, "Trop d'élément dans le buffer uiObjectByIndex de la liste de {0}", getDtDefinition().getName());
		}
		return element;
	}

	/** {@inheritDoc} */
	@Override
	public final int size() {
		return obtainDtList().size();
	}

	/**
	 * Vérifie les UiObjects de la liste, met à jour les objets métiers et retourne la liste.
	 * @param validator Validateur à utilisé, peut-être spécifique à l'objet.
	 * @param uiMessageStack Pile des messages qui sera mise à jour
	 * @return Liste métier validée.
	 */
	public DtList<D> validate(final UiObjectValidator<D> validator, final UiMessageStack uiMessageStack) {
		check(validator, uiMessageStack);
		return flush();
	}

	/**
	 * @param validator
	 * @param action
	 * @param contextKey
	 */
	/**
	 * Vérifie les UiObjects de la liste et remplis la pile d'erreur.
	 * @param validator Validateur à utilisé
	 * @param uiMessageStack Pile des messages qui sera mise à jour
	 */
	public void check(final UiObjectValidator<D> validator, final UiMessageStack uiMessageStack) {
		//1. check Error => KUserException
		//on valide les éléments internes
		for (final UiObject<D> uiObject : getUiObjectBuffer()) {
			uiObject.check(validator, uiMessageStack);
		}
	}

	/**
	 * @return met à jour les objets métiers et retourne la liste.
	 */
	public DtList<D> flush() {
		//1. check Error => KUserException
		//on valide les éléments internes
		for (final UiObject<D> dtoInput : getUiObjectBuffer()) {
			dtoInput.flush();
		}
		clearUiObjectBuffer(); //on purge le buffer
		return dtList;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("uiList(" + dtList.size() + " element(s)");
		for (int i = 0; i < Math.min(dtList.size(), 50); i++) {
			sb.append("; ");
			sb.append(get(i));
		}
		sb.append(")");
		return sb.toString();
	}
}
