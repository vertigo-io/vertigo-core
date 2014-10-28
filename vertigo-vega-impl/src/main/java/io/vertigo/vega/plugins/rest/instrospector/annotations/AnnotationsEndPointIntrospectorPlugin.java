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
package io.vertigo.vega.plugins.rest.instrospector.annotations;

import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.lang.Option;
import io.vertigo.vega.rest.EndPointIntrospectorPlugin;
import io.vertigo.vega.rest.EndPointTypeUtil;
import io.vertigo.vega.rest.RestfulService;
import io.vertigo.vega.rest.metamodel.EndPointDefinition;
import io.vertigo.vega.rest.metamodel.EndPointDefinition.Verb;
import io.vertigo.vega.rest.metamodel.EndPointDefinitionBuilder;
import io.vertigo.vega.rest.metamodel.EndPointParam;
import io.vertigo.vega.rest.metamodel.EndPointParam.ImplicitParam;
import io.vertigo.vega.rest.metamodel.EndPointParam.RestParamType;
import io.vertigo.vega.rest.metamodel.EndPointParamBuilder;
import io.vertigo.vega.rest.model.DtListDelta;
import io.vertigo.vega.rest.model.UiListState;
import io.vertigo.vega.rest.stereotype.AccessTokenConsume;
import io.vertigo.vega.rest.stereotype.AccessTokenMandatory;
import io.vertigo.vega.rest.stereotype.AccessTokenPublish;
import io.vertigo.vega.rest.stereotype.AnonymousAccessAllowed;
import io.vertigo.vega.rest.stereotype.AutoSortAndPagination;
import io.vertigo.vega.rest.stereotype.DELETE;
import io.vertigo.vega.rest.stereotype.Doc;
import io.vertigo.vega.rest.stereotype.ExcludedFields;
import io.vertigo.vega.rest.stereotype.GET;
import io.vertigo.vega.rest.stereotype.HeaderParam;
import io.vertigo.vega.rest.stereotype.IncludedFields;
import io.vertigo.vega.rest.stereotype.InnerBodyParam;
import io.vertigo.vega.rest.stereotype.POST;
import io.vertigo.vega.rest.stereotype.PUT;
import io.vertigo.vega.rest.stereotype.PathParam;
import io.vertigo.vega.rest.stereotype.PathPrefix;
import io.vertigo.vega.rest.stereotype.QueryParam;
import io.vertigo.vega.rest.stereotype.ServerSideConsume;
import io.vertigo.vega.rest.stereotype.ServerSideRead;
import io.vertigo.vega.rest.stereotype.ServerSideSave;
import io.vertigo.vega.rest.stereotype.SessionInvalidate;
import io.vertigo.vega.rest.stereotype.SessionLess;
import io.vertigo.vega.rest.stereotype.Validate;
import io.vertigo.vega.rest.validation.DefaultDtObjectValidator;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

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
		final PathPrefix pathPrefix = method.getDeclaringClass().getAnnotation(PathPrefix.class);
		if (pathPrefix != null) {
			builder.withPathPrefix(pathPrefix.value());
		}
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
				builder.withAutoSortAndPagination(true);
			} else if (annotation instanceof Doc) {
				builder.withDoc(((Doc) annotation).value());
			}
		}
		if (builder.hasVerb()) {
			final Type[] paramType = method.getGenericParameterTypes();
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

	private static EndPointParam buildEndPointParam(final Annotation[] annotations, final Type paramType) {
		final EndPointParamBuilder builder = new EndPointParamBuilder(paramType);
		if (EndPointTypeUtil.isAssignableFrom(DtObject.class, paramType)) {
			builder.withValidatorClasses(DefaultDtObjectValidator.class);
		} else if (EndPointTypeUtil.isAssignableFrom(DtListDelta.class, paramType)) {
			builder.withValidatorClasses(DefaultDtObjectValidator.class);
		} else if (isImplicitParam(paramType)) {
			builder.with(RestParamType.Implicit, getImplicitParam(paramType).name());
		} else if (UiListState.class.equals(paramType)) {
			builder.with(RestParamType.Body, "listState"); //UiListState don't need to be named, it will be retrieve from body
		}
		for (final Annotation annotation : annotations) {
			if (annotation instanceof PathParam) {
				builder.with(RestParamType.Path, ((PathParam) annotation).value());
			} else if (annotation instanceof QueryParam) {
				builder.with(RestParamType.Query, ((QueryParam) annotation).value());
			} else if (annotation instanceof HeaderParam) {
				builder.with(RestParamType.Header, ((HeaderParam) annotation).value());
			} else if (annotation instanceof InnerBodyParam) {
				builder.with(RestParamType.InnerBody, ((InnerBodyParam) annotation).value());
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

	private static ImplicitParam getImplicitParam(final Type paramType) {
		for (final ImplicitParam implicitParam : ImplicitParam.values()) {
			if (implicitParam.getImplicitType().equals(paramType)) {
				return implicitParam;
			}
		}
		return null;
	}

	private static final boolean isImplicitParam(final Type paramType) {
		for (final ImplicitParam implicitParam : ImplicitParam.values()) {
			if (implicitParam.getImplicitType().equals(paramType)) {
				return true;
			}
		}
		return false;
	}
}
