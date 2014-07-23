package io.vertigo.rest.plugins.instrospector.annotations;

import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.Option;
import io.vertigo.kernel.util.StringUtil;
import io.vertigo.rest.EndPointIntrospectorPlugin;
import io.vertigo.rest.RestfulService;
import io.vertigo.rest.RestfulService.AccessTokenConsume;
import io.vertigo.rest.RestfulService.AccessTokenMandatory;
import io.vertigo.rest.RestfulService.AccessTokenPublish;
import io.vertigo.rest.RestfulService.AnonymousAccessAllowed;
import io.vertigo.rest.RestfulService.AutoSortAndPagination;
import io.vertigo.rest.RestfulService.DELETE;
import io.vertigo.rest.RestfulService.Doc;
import io.vertigo.rest.RestfulService.ExcludedFields;
import io.vertigo.rest.RestfulService.GET;
import io.vertigo.rest.RestfulService.IncludedFields;
import io.vertigo.rest.RestfulService.InnerBodyParam;
import io.vertigo.rest.RestfulService.POST;
import io.vertigo.rest.RestfulService.PUT;
import io.vertigo.rest.RestfulService.PathParam;
import io.vertigo.rest.RestfulService.QueryParam;
import io.vertigo.rest.RestfulService.ServerSideConsume;
import io.vertigo.rest.RestfulService.ServerSideRead;
import io.vertigo.rest.RestfulService.ServerSideSave;
import io.vertigo.rest.RestfulService.SessionLess;
import io.vertigo.rest.RestfulService.Validate;
import io.vertigo.rest.metamodel.EndPointDefinition;
import io.vertigo.rest.metamodel.EndPointDefinition.Verb;
import io.vertigo.rest.metamodel.EndPointParam;
import io.vertigo.rest.metamodel.EndPointParam.ImplicitParam;
import io.vertigo.rest.metamodel.EndPointParam.RestParamType;
import io.vertigo.rest.validation.DefaultDtObjectValidator;
import io.vertigo.rest.validation.DtObjectValidator;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
* @author npiedeloup 
*/
public final class AnnotationsEndPointIntrospectorPlugin implements EndPointIntrospectorPlugin {
	public List<EndPointDefinition> instrospectEndPoint(final Class<? extends RestfulService> restfulServiceClass) {
		final List<EndPointDefinition> endPointDefinitions = new ArrayList<>();
		for (final Method method : restfulServiceClass.getMethods()) {
			final Option<EndPointDefinition> endPointDefinition = buildEndPoint(method, restfulServiceClass);
			if (endPointDefinition.isDefined()) {
				endPointDefinitions.add(endPointDefinition.get());
			}
		}
		return endPointDefinitions;
	}

	private static <C extends RestfulService> Option<EndPointDefinition> buildEndPoint(final Method method, final Class<C> restFullServiceClass) {
		Verb verb = null;
		String path = null;
		final String acceptType = "application/type"; //default
		boolean needSession = true;
		boolean needAuthentication = true;
		String[] includedFields = null;
		String[] excludedFields = null;
		boolean accessTokenPublish = false;
		boolean accessTokenMandatory = false;
		boolean accessTokenConsume = false;
		boolean serverSideSave = false;
		boolean autoSortAndPagination = false;
		String doc = "";
		for (final Annotation annotation : method.getAnnotations()) {
			if (annotation instanceof GET) {
				Assertion.checkState(verb == null, "A verb is already specified on {0}", method.getName());
				verb = Verb.GET;
				path = ((GET) annotation).value();
			} else if (annotation instanceof POST) {
				Assertion.checkState(verb == null, "A verb is already specified on {0}", method.getName());
				verb = Verb.POST;
				path = ((POST) annotation).value();
			} else if (annotation instanceof PUT) {
				Assertion.checkState(verb == null, "A verb is already specified on {0}", method.getName());
				verb = Verb.PUT;
				path = ((PUT) annotation).value();
			} else if (annotation instanceof DELETE) {
				Assertion.checkState(verb == null, "A verb is already specified on {0}", method.getName());
				verb = Verb.DELETE;
				path = ((DELETE) annotation).value();
			} else if (annotation instanceof AnonymousAccessAllowed) {
				needAuthentication = false;
			} else if (annotation instanceof SessionLess) {
				needSession = false;
			} else if (annotation instanceof ExcludedFields) {
				excludedFields = ((ExcludedFields) annotation).value();
			} else if (annotation instanceof IncludedFields) {
				includedFields = ((IncludedFields) annotation).value();
			} else if (annotation instanceof AccessTokenPublish) {
				accessTokenPublish = true;
			} else if (annotation instanceof AccessTokenMandatory) {
				accessTokenMandatory = true;
			} else if (annotation instanceof AccessTokenConsume) {
				accessTokenMandatory = true;
				accessTokenConsume = true;
			} else if (annotation instanceof ServerSideSave) {
				serverSideSave = true;
			} else if (annotation instanceof AutoSortAndPagination) {
				serverSideSave = true;
				autoSortAndPagination = true;
			} else if (annotation instanceof Doc) {
				doc = ((Doc) annotation).value();
			}
		}
		if (verb != null) {
			Assertion.checkArgNotEmpty(path, "Route path must be specified on {0}", method.getName());
			//-----------------------------------------------------------------
			final Class<?>[] paramType = method.getParameterTypes();
			final Annotation[][] parameterAnnotation = method.getParameterAnnotations();

			final List<EndPointParam> endPointParams = new ArrayList<>();
			for (int i = 0; i < paramType.length; i++) {
				final EndPointParam endPointParam = buildEndPointParam(parameterAnnotation[i], paramType[i]);
				//Assertion.checkArgNotEmpty(paramName, "Le paramName n'a pas été précisé sur {0}", method.getName());
				endPointParams.add(endPointParam);
			}
			//---
			final EndPointDefinition endPointDefinition = new EndPointDefinition(//
					//"EP_" + StringUtil.camelToConstCase(restFullServiceClass.getSimpleName()) + "_" + StringUtil.camelToConstCase(method.getName()), //
					"EP_" + verb + "_" + StringUtil.camelToConstCase(path.replaceAll("[//{}]", "_")), //
					verb, //
					path, //
					acceptType, //
					method, //
					needSession, //
					needAuthentication, //
					accessTokenPublish,//
					accessTokenMandatory,//
					accessTokenConsume,//
					serverSideSave,//
					autoSortAndPagination,//
					asSet(includedFields), //
					asSet(excludedFields), //
					endPointParams, //
					doc);
			return Option.some(endPointDefinition);
		}
		return Option.none();
	}

	private static EndPointParam buildEndPointParam(final Annotation[] annotations, final Class<?> paramType) {
		RestParamType restParamType = RestParamType.Body; //default
		String restParamName = null;
		final List<Class<? extends DtObjectValidator>> validatorClasses = new ArrayList<>();
		String[] includedFields = null;
		String[] excludedFields = null;
		boolean needServerSideToken = false;
		boolean consumeServerSideToken = false;
		for (final Annotation annotation : annotations) {
			if (annotation instanceof PathParam) {
				restParamType = RestParamType.Path;
				restParamName = ((PathParam) annotation).value();
			} else if (annotation instanceof QueryParam) {
				restParamType = RestParamType.Query;
				restParamName = ((QueryParam) annotation).value();
			} else if (annotation instanceof InnerBodyParam) {
				restParamType = RestParamType.MultiPartBody;
				restParamName = ((InnerBodyParam) annotation).value();
			} else if (annotation instanceof Validate) {
				validatorClasses.addAll(Arrays.asList(((Validate) annotation).value()));
			} else if (annotation instanceof ExcludedFields) {
				excludedFields = ((ExcludedFields) annotation).value();
			} else if (annotation instanceof IncludedFields) {
				includedFields = ((IncludedFields) annotation).value();
			} else if (annotation instanceof ServerSideRead) {
				needServerSideToken = true;
			} else if (annotation instanceof ServerSideConsume) {
				needServerSideToken = true;
				consumeServerSideToken = true;
			}
		}

		if (DtObject.class.isAssignableFrom(paramType)) {
			validatorClasses.add(0, DefaultDtObjectValidator.class);
		} else if (ImplicitParam.UiMessageStack.getImplicitType().equals(paramType)) {
			restParamType = RestParamType.Implicit;
			restParamName = ImplicitParam.UiMessageStack.name();
		} else if (ImplicitParam.UiListState.getImplicitType().equals(paramType)) {
			restParamType = RestParamType.Implicit;
			restParamName = ImplicitParam.UiListState.name();
		}

		//if no annotation : take request body

		if (restParamType == RestParamType.Body) {
			return new EndPointParam(restParamType, paramType, //
					asSet(includedFields), //
					asSet(excludedFields), //
					needServerSideToken, //
					consumeServerSideToken, //
					validatorClasses);
		}
		return new EndPointParam(restParamType, restParamName, paramType, //
				asSet(includedFields), //
				asSet(excludedFields), //
				needServerSideToken, //
				consumeServerSideToken, //
				validatorClasses);
	}

	private static Set<String> asSet(final String[] fields) {
		if (fields == null) {
			return Collections.emptySet();
		}
		return new LinkedHashSet<>(Arrays.asList(fields));
	}
	//
	//	private static List<String> computeExcludedFields(final String[] includedFields, final String[] excludedFields, final Class<?> paramType) {
	//		Assertion.checkArgument(includedFields == null || excludedFields == null, "Can't resolve excludedFields of {0}, if both includedFields and excludedFields are set", paramType.getSimpleName());
	//		if (includedFields == null && excludedFields == null) {
	//			return Collections.emptyList();
	//		} else if (includedFields == null) {
	//			return Arrays.asList(excludedFields);
	//		}
	//
	//		Assertion.checkState(DtObject.class.isAssignableFrom(paramType) || DtList.class.isAssignableFrom(paramType), "IncludeFields must be on DtObject or DtList ({0})", paramType.getSimpleName());
	//		final Class<? extends DtObject> dtObjectType = (Class<? extends DtObject>) paramType;
	//		final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(dtObjectType);
	//		for (final DtField dtField : dtDefinition.getFields()) {
	//			final String fieldName = StringUtil.constToCamelCase(dtField.getName(), false);
	//			//TODO extract 
	//		}
	//		return Collections.emptyList();
	//	}

}
