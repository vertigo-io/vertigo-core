package io.vertigo.rest.plugins.rest.instrospector.annotations;

import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.Option;
import io.vertigo.kernel.util.StringUtil;
import io.vertigo.rest.rest.EndPointIntrospectorPlugin;
import io.vertigo.rest.rest.RestfulService;
import io.vertigo.rest.rest.RestfulService.AccessTokenConsume;
import io.vertigo.rest.rest.RestfulService.AccessTokenMandatory;
import io.vertigo.rest.rest.RestfulService.AccessTokenPublish;
import io.vertigo.rest.rest.RestfulService.AnonymousAccessAllowed;
import io.vertigo.rest.rest.RestfulService.AutoSortAndPagination;
import io.vertigo.rest.rest.RestfulService.DELETE;
import io.vertigo.rest.rest.RestfulService.Doc;
import io.vertigo.rest.rest.RestfulService.ExcludedFields;
import io.vertigo.rest.rest.RestfulService.GET;
import io.vertigo.rest.rest.RestfulService.IncludedFields;
import io.vertigo.rest.rest.RestfulService.InnerBodyParam;
import io.vertigo.rest.rest.RestfulService.POST;
import io.vertigo.rest.rest.RestfulService.PUT;
import io.vertigo.rest.rest.RestfulService.PathParam;
import io.vertigo.rest.rest.RestfulService.QueryParam;
import io.vertigo.rest.rest.RestfulService.ServerSideConsume;
import io.vertigo.rest.rest.RestfulService.ServerSideRead;
import io.vertigo.rest.rest.RestfulService.ServerSideSave;
import io.vertigo.rest.rest.RestfulService.SessionInvalidate;
import io.vertigo.rest.rest.RestfulService.SessionLess;
import io.vertigo.rest.rest.RestfulService.Validate;
import io.vertigo.rest.rest.metamodel.EndPointDefinition;
import io.vertigo.rest.rest.metamodel.EndPointDefinition.Verb;
import io.vertigo.rest.rest.metamodel.EndPointDefinitionBuilder;
import io.vertigo.rest.rest.metamodel.EndPointParam;
import io.vertigo.rest.rest.metamodel.EndPointParam.ImplicitParam;
import io.vertigo.rest.rest.metamodel.EndPointParam.RestParamType;
import io.vertigo.rest.rest.metamodel.EndPointParamBuilder;
import io.vertigo.rest.rest.validation.DefaultDtObjectValidator;
import io.vertigo.rest.rest.validation.DtObjectValidator;

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

	/** {@inheritDoc} */
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
		final EndPointDefinitionBuilder builder = new EndPointDefinitionBuilder(method);
		
		for (final Annotation annotation : method.getAnnotations()) {
			if (annotation instanceof GET) {
				builder.with(Verb.GET, ((GET) annotation).value());				
			} else if (annotation instanceof POST) {
				builder.with(Verb.POST, ((POST) annotation).value());
			} else if (annotation instanceof PUT) {
				builder.with(Verb.PUT, ((PUT) annotation).value());
			} else if (annotation instanceof DELETE) {
				builder.with(Verb.DELETE, ((DELETE) annotation).value());
			} else if (annotation instanceof AnonymousAccessAllowed) {
				builder.withNeedAuthentication(false);
			} else if (annotation instanceof SessionLess) {
				builder.withNeedSession(false);
			} else if (annotation instanceof SessionInvalidate) {
				builder.withSessionInvalidate(true);
			} else if (annotation instanceof ExcludedFields) {
				builder.withExcludedFields(((ExcludedFields) annotation).value());
			} else if (annotation instanceof IncludedFields) {
				builder.withIncludedFields(((IncludedFields) annotation).value());
			} else if (annotation instanceof AccessTokenPublish) {
				builder.withAccessTokenPublish(true);
			} else if (annotation instanceof AccessTokenMandatory) {
				builder.withAccessTokenMandatory(true);
			} else if (annotation instanceof AccessTokenConsume) {
				builder.withAccessTokenMandatory(true);
				builder.withAccessTokenConsume(true);
			} else if (annotation instanceof ServerSideSave) {
				builder.withServerSideSave(true);
			} else if (annotation instanceof AutoSortAndPagination) {
				builder.withServerSideSave(true);
				builder.withAutoSortAndPagination(true);
			} else if (annotation instanceof Doc) {
				builder.withDoc(((Doc) annotation).value());
			}
		}
		if (builder.hasVerb()) {
			final Class<?>[] paramType = method.getParameterTypes();
			final Annotation[][] parameterAnnotation = method.getParameterAnnotations();

			for (int i = 0; i < paramType.length; i++) {
				final EndPointParam endPointParam = buildEndPointParam(parameterAnnotation[i], paramType[i]);
				builder.withEndPointParam(endPointParam);
			}
			//---
			return Option.some(builder.build());
		}
		return Option.none();
	}

	private static EndPointParam buildEndPointParam(final Annotation[] annotations, final Class<?> paramType) {
		EndPointParamBuilder builder = new EndPointParamBuilder(paramType);
		if (DtObject.class.isAssignableFrom(paramType)) {
			builder.withValidatorClasses(DefaultDtObjectValidator.class);
		} else if (ImplicitParam.UiMessageStack.getImplicitType().equals(paramType)) {
			builder.with(RestParamType.Implicit, ImplicitParam.UiMessageStack.name());
		} else if (ImplicitParam.UiListState.getImplicitType().equals(paramType)) {
			builder.with(RestParamType.Implicit, ImplicitParam.UiListState.name());
		}
		
		for (final Annotation annotation : annotations) {
			if (annotation instanceof PathParam) {
				builder.with(RestParamType.Path, ((PathParam) annotation).value());
			} else if (annotation instanceof QueryParam) {
				builder.with(RestParamType.Query, ((QueryParam) annotation).value());
			} else if (annotation instanceof InnerBodyParam) {
				builder.with(RestParamType.MultiPartBody, ((InnerBodyParam) annotation).value());				
			} else if (annotation instanceof Validate) {
				builder.withValidatorClasses(((Validate) annotation).value());					
			} else if (annotation instanceof ExcludedFields) {
				builder.withExcludedFields(((ExcludedFields) annotation).value());	
			} else if (annotation instanceof IncludedFields) {
				builder.withIncludedFields(((IncludedFields) annotation).value());	
			} else if (annotation instanceof ServerSideRead) {
				builder.withNeedServerSideToken(true);	
			} else if (annotation instanceof ServerSideConsume) {
				builder.withNeedServerSideToken(true);	
				builder.withConsumeServerSideToken(true);
			}
		}		
		return builder.build();		
	}
}
