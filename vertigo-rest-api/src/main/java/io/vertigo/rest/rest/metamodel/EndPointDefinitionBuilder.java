package io.vertigo.rest.rest.metamodel;

import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.Builder;
import io.vertigo.kernel.util.StringUtil;
import io.vertigo.rest.rest.metamodel.EndPointDefinition.Verb;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** 
 * EndPointDefinition Builder.
 *  
 * @author npiedeloup
 */
public final class EndPointDefinitionBuilder implements Builder<EndPointDefinition> {
	private final Method method;
	private Verb verb;
	private String path;
	private final String acceptType = "application/json"; //default
	private boolean needSession = true;
	private boolean sessionInvalidate;
	private boolean needAuthentication = true;
	private Set<String> includedFields;
	private Set<String> excludedFields;
	private boolean accessTokenPublish;
	private boolean accessTokenMandatory;
	private boolean accessTokenConsume;
	private boolean serverSideSave;
	private boolean autoSortAndPagination;
	private String doc = "";
	private final List<EndPointParam> endPointParams = new ArrayList<>();

	/**
	 * Constructeur.
	 */
	public EndPointDefinitionBuilder(final Method method) {
		Assertion.checkNotNull(method);
		//---------------------------------------------------------------------
		this.method = method;
	}

	public EndPointDefinition build() {
		return new EndPointDefinition(//
				//"EP_" + StringUtil.camelToConstCase(restFullServiceClass.getSimpleName()) + "_" + StringUtil.camelToConstCase(method.getName()), //
				"EP_" + verb + "_" + StringUtil.camelToConstCase(path.replaceAll("[//{}]", "_")), //
				verb, //
				path, //
				acceptType, //
				method, //
				needSession, //
				sessionInvalidate, //
				needAuthentication, //
				accessTokenPublish,//
				accessTokenMandatory,//
				accessTokenConsume,//
				serverSideSave,//
				autoSortAndPagination,//
				includedFields, //
				excludedFields, //
				endPointParams, //
				doc);
	}

	public void with(Verb verb, String path) {
		Assertion.checkState(verb == null, "A verb is already specified on {0}", method.getName());
		Assertion.checkArgNotEmpty(path, "Route path must be specified on {0}", method.getName());
		this.verb = verb;
		this.path = path;
	}

	public boolean hasVerb() {
		return verb != null;
	}

	public void withAccessTokenConsume(boolean accessTokenConsume) {
		this.accessTokenConsume = accessTokenConsume;
	}

	public void withNeedAuthentication(boolean needAuthentication) {
		this.needAuthentication = needAuthentication;
	}

	public void withNeedSession(boolean needSession) {
		this.needSession = needSession;
	}

	public void withSessionInvalidate(boolean sessionInvalidate) {
		this.sessionInvalidate = sessionInvalidate;
	}

	public void withExcludedFields(String[] excludedFields) {
		this.excludedFields = asSet(excludedFields);
	}

	public void withIncludedFields(String[] includedFields) {
		this.includedFields = asSet(includedFields);
	}

	public void withAccessTokenPublish(boolean accessTokenPublish) {
		this.accessTokenPublish = accessTokenPublish;
	}

	public void withAccessTokenMandatory(boolean accessTokenMandatory) {
		this.accessTokenMandatory = accessTokenMandatory;
	}

	public void withServerSideSave(boolean serverSideSave) {
		this.serverSideSave = serverSideSave;
	}

	public void withAutoSortAndPagination(boolean autoSortAndPagination) {
		this.autoSortAndPagination = autoSortAndPagination;
	}

	public void withDoc(String doc) {
		this.doc = doc;
	}

	public void withEndPointParam(EndPointParam endPointParam) {
		endPointParams.add(endPointParam);
	}

	private static Set<String> asSet(final String[] fields) {
		if (fields == null) {
			return Collections.emptySet();
		}
		return new LinkedHashSet<>(Arrays.asList(fields));
	}
}
