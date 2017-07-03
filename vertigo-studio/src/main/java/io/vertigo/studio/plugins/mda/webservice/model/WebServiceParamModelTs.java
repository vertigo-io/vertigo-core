package io.vertigo.studio.plugins.mda.webservice.model;

import java.util.Locale;

import io.vertigo.vega.webservice.metamodel.WebServiceParam;
import io.vertigo.vega.webservice.metamodel.WebServiceParam.WebServiceParamType;

/**
 * FreeMarker Model of WebServiceParamModel.
 * @author npiedeloup
 */
public final class WebServiceParamModelTs {

	private final WebServiceParam webServiceParam;
	private final TypeModelTs typeModelTs;

	/**
	 * Constructor.
	 * @param webServiceParam web service param
	 */
	public WebServiceParamModelTs(final WebServiceParam webServiceParam) {
		this.webServiceParam = webServiceParam;
		typeModelTs = new TypeModelTs(webServiceParam.getGenericType());
	}

	/**
	 * @return param type
	 */
	public TypeModelTs getTypeModel() {
		return typeModelTs;
	}

	/**
	 * @return param name
	 */
	public String getName() {
		String paramName = webServiceParam.getName();
		if (webServiceParam.getParamType() == WebServiceParamType.Body) {
			paramName = webServiceParam.getType().getSimpleName();
		}
		paramName = paramName.replaceAll("\\W", "");
		return paramName.substring(0, 1).toLowerCase(Locale.ENGLISH) + paramName.substring(1);
	}
}
