/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2017, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.studio.plugins.mda.webservice.model;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;

import io.vertigo.dynamo.domain.model.DtListState;
import io.vertigo.lang.Assertion;
import io.vertigo.vega.webservice.metamodel.WebServiceDefinition;
import io.vertigo.vega.webservice.metamodel.WebServiceParam;
import io.vertigo.vega.webservice.metamodel.WebServiceParam.WebServiceParamType;
import io.vertigo.vega.webservice.stereotype.PathPrefix;

/**
 * Model used to define a WebServiceDefinition.
 *
 * @author npiedeloup
 */
public final class WebServiceDefinitionModel {
	private final WebServiceDefinition webServiceDefinition;
	private final List<WebServiceParamModel> webServiceParams = new ArrayList<>();
	private final EnumMap<WebServiceParamType, List<WebServiceParamModel>> webServiceParamsPerType = new EnumMap<>(WebServiceParamType.class);

	/**
	 * Constructeur.
	 *
	 * @param webServiceDefinition WebServiceDefinition de l'objet à générer
	 */
	public WebServiceDefinitionModel(final WebServiceDefinition webServiceDefinition) {
		Assertion.checkNotNull(webServiceDefinition);
		//-----
		this.webServiceDefinition = webServiceDefinition;

		for (final WebServiceParamType webServiceParamType : WebServiceParamType.values()) {
			webServiceParamsPerType.put(webServiceParamType, new ArrayList<WebServiceParamModel>());
		}

		for (final WebServiceParam webServiceParam : webServiceDefinition.getWebServiceParams()) {
			if (DtListState.class.isAssignableFrom(webServiceParam.getType()) && webServiceParam.getParamType() == WebServiceParamType.Query) {
				final List<WebServiceParamModel> webServiceParamsQuery = webServiceParamsPerType.get(webServiceParam.getParamType());
				final WebServiceParamModel[] templateWebServiceParams = {
						new WebServiceParamModel("listState.top", "integer", false),
						new WebServiceParamModel("listState.skip", "integer", true),
						new WebServiceParamModel("listState.sortFieldName", "String", true),
						new WebServiceParamModel("listState.sortDesc", "boolean", true),
				};
				webServiceParams.addAll(Arrays.asList(templateWebServiceParams));
				webServiceParamsQuery.addAll(Arrays.asList(templateWebServiceParams));
			} else if (webServiceParam.getParamType() == WebServiceParamType.Implicit) {
				//rien
			} else {
				final WebServiceParamModel templateWebServiceParam = createTemplateWebServiceParam(webServiceParam);
				webServiceParams.add(templateWebServiceParam);
				final List<WebServiceParamModel> paramTypeParams = webServiceParamsPerType.get(webServiceParam.getParamType());
				paramTypeParams.add(templateWebServiceParam);
			}
		}
	}

	private WebServiceParamModel createTemplateWebServiceParam(final WebServiceParam webServiceParam) {
		String paramName = webServiceParam.getName();
		if (webServiceParam.getParamType() == WebServiceParamType.Body) {
			paramName = webServiceParam.getType().getSimpleName();
		}
		paramName = paramName.replaceAll("\\W", "");
		paramName = paramName.substring(0, 1).toLowerCase(Locale.ENGLISH) + paramName.substring(1);

		final String paramType = genericToString(webServiceParam.getGenericType());

		return new WebServiceParamModel(paramName, paramType, webServiceParam.isOptional());
	}

	private static String genericToString(final Type genericType) {
		return genericType.toString()
				.replaceAll("class |interface ", "")
				.replaceAll("(?<=^|<)[a-z]+\\.[a-z\\.]+", "");
	}

	public String getJavaFacadeName() {
		return webServiceDefinition.getMethod().getDeclaringClass().getName();
	}

	public String getPathPrefix() {
		final PathPrefix prefix = webServiceDefinition.getMethod().getDeclaringClass().getAnnotation(PathPrefix.class);
		return prefix != null ? prefix.value() : "/";
	}

	public String getPath() {
		final String path = webServiceDefinition.getPath();
		return path.replaceAll("\\{(.+?)\\}", "\\${$1}");
	}

	public String getVerb() {
		return webServiceDefinition.getVerb().toString();
	}

	public String getJsUrlMethodName() {
		final String methodName = webServiceDefinition.getMethod().getName();
		return "url" + methodName.substring(0, 1).toUpperCase(Locale.ENGLISH) + methodName.substring(1);
	}

	public String getJsMethodName() {
		return webServiceDefinition.getMethod().getName();
	}

	public String getReturnType() {
		return genericToString(webServiceDefinition.getMethod().getGenericReturnType());
	}

	public List<WebServiceParamModel> getWebServiceParams() {
		return webServiceParams;
	}

	public List<WebServiceParamModel> getQueryWebServiceParams() {
		return webServiceParamsPerType.get(WebServiceParamType.Query);
	}

	public List<WebServiceParamModel> getHeaderWebServiceParams() {
		return webServiceParamsPerType.get(WebServiceParamType.Header);
	}

	public List<WebServiceParamModel> getInnerBodyWebServiceParams() {
		return webServiceParamsPerType.get(WebServiceParamType.InnerBody);
	}

	public List<WebServiceParamModel> getBodyWebServiceParams() {
		return webServiceParamsPerType.get(WebServiceParamType.Body);
	}

	public List<WebServiceParamModel> getPathWebServiceParams() {
		return webServiceParamsPerType.get(WebServiceParamType.Path);
	}

}
