package io.vertigo.studio.plugins.mda.webservice.model;

import java.util.Locale;

import io.vertigo.vega.webservice.metamodel.WebServiceParam;
import io.vertigo.vega.webservice.metamodel.WebServiceParam.WebServiceParamType;

public final class WebServiceParamModelTs {

	private final WebServiceParam webServiceParam;
	private final TypeModelTs typeModelTs;

	public WebServiceParamModelTs(final WebServiceParam webServiceParam) {
		this.webServiceParam = webServiceParam;
		typeModelTs = new TypeModelTs(webServiceParam.getGenericType());
	}

	public TypeModelTs getTypeModel() {
		return typeModelTs;
	}

	public String getName() {
		String paramName = webServiceParam.getName();
		if (webServiceParam.getParamType() == WebServiceParamType.Body) {
			paramName = webServiceParam.getType().getSimpleName();
		}
		paramName = paramName.replaceAll("\\W", "");
		return paramName.substring(0, 1).toLowerCase(Locale.ENGLISH) + paramName.substring(1);
	}
}
