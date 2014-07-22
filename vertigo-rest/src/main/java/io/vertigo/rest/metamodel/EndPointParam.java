package io.vertigo.rest.metamodel;

import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.rest.engine.UiListState;
import io.vertigo.rest.validation.DtObjectValidator;
import io.vertigo.rest.validation.UiMessageStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * EndPoint param infos : 
 * - source type (query, path, body, innerBody or implicit)
 * - name
 * - type (class)
 * - includedField (for DtObjet or DtList)
 * - excludedField (for DtObjet or DtList)
 * - if object kept serverSide
 * - if one time token
 * - specific validators
 *
 * @author npiedeloup
 */
public final class EndPointParam {

	/**
	 * Parameter's source types.
	 */
	public static enum RestParamType {
		Query, Path, Body, MultiPartBody, Implicit
	}

	public static enum ImplicitParam {
		UiMessageStack(UiMessageStack.class), //
		UiListState(UiListState.class);

		private Class<?> implicitType;

		ImplicitParam(final Class<?> implicitType) {
			this.implicitType = implicitType;
		}

		public Class<?> getImplicitType() {
			return implicitType;
		}

	}

	private final RestParamType paramType;
	private final String name;
	private final Class<?> type;
	private final String fullName;
	private final List<String> includedFields;
	private final List<String> excludedFields;
	private final boolean needServerSideToken;
	private final boolean consumeServerSideToken;
	private final List<Class<? extends DtObjectValidator>> dtObjectValidatorClasses;

	/**
	 * @param paramType Parameter's source type
	 * @param type Parameter class
	 * @param includedFields List of included fieldNames
	 * @param excludedFields List of excluded fieldNames
	 * @param needServerSideToken if access token mandatory
	 * @param consumeServerSideToken if access token is consume (one time token)
	 * @param dtObjectValidatorClasses list of validator classes (order is keep)
	 */
	public EndPointParam(final RestParamType paramType, final Class<?> type, final List<String> includedFields, final List<String> excludedFields, final boolean needServerSideToken, final boolean consumeServerSideToken, final List<Class<? extends DtObjectValidator>> dtObjectValidatorClasses) {
		this(":" + paramType.name() + ":", paramType, null, type, includedFields, excludedFields, needServerSideToken, consumeServerSideToken, dtObjectValidatorClasses);
		Assertion.checkArgument(paramType == RestParamType.Body, "Name is mandatory, for this parameter type : {0}", paramType.name());
	}

	/**
	 * @param paramType Parameter type
	 * @param name Parameter name
	 * @param type Parameter class
	 * @param excludedFields List of excluded fieldNames
	 * @param needServerSideToken if access token mandatory
	 * @param consumeServerSideToken if access token is consume (one time token)
	 * @param dtObjectValidatorClasses List of validator classes (order is keep)
	 */
	public EndPointParam(final RestParamType paramType, final String name, final Class<?> type, final List<String> includedFields, final List<String> excludedFields, final boolean needServerSideToken, final boolean consumeServerSideToken, final List<Class<? extends DtObjectValidator>> dtObjectValidatorClasses) {
		this(":" + paramType.name() + ":" + name, paramType, name, type, includedFields, excludedFields, needServerSideToken, consumeServerSideToken, dtObjectValidatorClasses);
		Assertion.checkArgument(paramType != RestParamType.Body, "Body parameter have no name");
		Assertion.checkArgument(paramType != RestParamType.Implicit || isImplicitParam(name), "When ImplicitParam, name ({1}) must be one of {0}", ImplicitParam.values(), name);
		Assertion.checkArgNotEmpty(name);
	}

	private static boolean isImplicitParam(final String testedName) {
		for (final ImplicitParam existingParam : ImplicitParam.values()) {
			if (existingParam.name().equals(testedName)) {
				return true;
			}
		}
		return false;
	}

	private EndPointParam(final String fullName, final RestParamType paramType, final String name, final Class<?> type, final List<String> includedFields, final List<String> excludedFields, final boolean needServerSideToken, final boolean consumeServerSideToken, final List<Class<? extends DtObjectValidator>> dtObjectValidatorClasses) {
		Assertion.checkNotNull(paramType);
		Assertion.checkNotNull(type);
		Assertion.checkNotNull(includedFields);
		Assertion.checkNotNull(excludedFields);
		Assertion.checkNotNull(dtObjectValidatorClasses);
		Assertion.checkArgument(dtObjectValidatorClasses.isEmpty() || DtObject.class.isAssignableFrom(type), "Validators aren't supported for {0}", type.getSimpleName());
		//-----------------------------------------------------------------
		this.paramType = paramType;
		this.type = type;
		this.name = name;
		this.fullName = fullName;
		this.includedFields = Collections.unmodifiableList(new ArrayList<>(includedFields));
		this.excludedFields = Collections.unmodifiableList(new ArrayList<>(excludedFields));
		this.needServerSideToken = needServerSideToken;
		this.consumeServerSideToken = consumeServerSideToken;
		this.dtObjectValidatorClasses = Collections.unmodifiableList(new ArrayList<>(dtObjectValidatorClasses));
	}

	/**
	 * @return Parameter's source type
	 */
	public RestParamType getParamType() {
		return paramType;
	}

	/**
	 * @return Full name of this param.
	 */
	public String getFullName() {
		return fullName;
	}

	/**
	 * @return Parameter name in source
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return Parameter class
	 */
	public Class<?> getType() {
		return type;
	}

	/**
	 * @return List of included fieldNames
	 */
	public List<String> getIncludedFields() {
		return includedFields;
	}

	/**
	 * @return List of excluded fieldNames
	 */
	public List<String> getExcludedFields() {
		return excludedFields;
	}

	/**
	 * @return if access token mandatory
	 */
	public boolean isNeedServerSideToken() {
		return needServerSideToken;
	}

	/**
	 * @return if access token is consume (one time token)
	 */
	public boolean isConsumeServerSideToken() {
		return consumeServerSideToken;
	}

	/**
	 * @return List of validator classes (order is keep)
	 */
	public List<Class<? extends DtObjectValidator>> getDtObjectValidatorClasses() {
		return dtObjectValidatorClasses;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(type.getSimpleName());
		sb.append(" ");
		sb.append(fullName);
		return sb.toString();
	}
}
