package io.vertigo.studio.plugins.mda.webservice.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import io.vertigo.vega.webservice.metamodel.WebServiceDefinition;
import io.vertigo.vega.webservice.metamodel.WebServiceParam;

/**
 * Model for TypeScript generation.
 * @author npiedeloup
 */
public class WebServiceDefinitionModelTs {

	private final WebServiceDefinition webServiceDefinition;
	private final List<WebServiceParamModelTs> webServiceParamModelTsList = new ArrayList<>();
	private final TypeModelTs returnType;

	/**
	 * Constructor.
	 * @param webServiceDefinition WebService to generate
	 */
	public WebServiceDefinitionModelTs(final WebServiceDefinition webServiceDefinition) {
		this.webServiceDefinition = webServiceDefinition;

		for (final WebServiceParam wsParam : webServiceDefinition.getWebServiceParams()) {
			webServiceParamModelTsList.add(new WebServiceParamModelTs(wsParam));
		}

		returnType = new TypeModelTs(webServiceDefinition.getMethod().getGenericReturnType());
	}

	/**
	 * @return Method name
	 */
	public String getMethodName() {
		return webServiceDefinition.getMethod().getName();
	}

	/**
	 * @return List of WebService params
	 */
	public List<WebServiceParamModelTs> getWebServiceParams() {
		return webServiceParamModelTsList;
	}

	/**
	 * @return Js Server Call Method
	 */
	public String getJsServerCallMethod() {
		String verb = webServiceDefinition.getVerb().toString().toLowerCase(Locale.ROOT);
		if (isDelete()) {
			verb = "del";
		}
		final StringBuilder method = new StringBuilder(verb);
		if (returnType.isList()) {
			method.append("List");
		}
		if (!returnType.isVoid()) {
			method.append('<').append(returnType.getJsGenericType()).append('>');
		}
		return method.toString();
	}

	/**
	 * @return Extract functional modul from package name
	 */
	public String getFunctionnalPackageName() {
		final String[] fullPackageNameSplited = webServiceDefinition.getMethod().getDeclaringClass().getPackage().toString().split("\\.");
		return fullPackageNameSplited[fullPackageNameSplited.length - 1];
	}

	/**
	 * @return if get
	 */
	public boolean isGet() {
		return "GET".equals(webServiceDefinition.getVerb().toString());
	}

	/**
	 * @return if delete
	 */
	public boolean isDelete() {
		return "DELETE".equals(webServiceDefinition.getVerb().toString());
	}

	/**
	 * @return WebService path
	 */
	public String getPath() {
		final String path = webServiceDefinition.getPath();
		return path.replaceAll("\\{(.+?)\\}", "\\${$1}");
	}

	/**
	 * @return Import list
	 */
	public Set<String> getImportList() {
		final Set<String> importList = new HashSet<>();
		for (final WebServiceParamModelTs param : webServiceParamModelTsList) {
			final String importDeclaration = param.getTypeModel().getImportDeclaration();
			if (importDeclaration != null) {
				importList.add(importDeclaration);
			} else if ("facetQueryInput".equals(param.getName())) {
				importList.add("type FacetQueryInput = any;");
			}
		}
		final String importReturnTypeDeclaration = returnType.getImportDeclaration();
		if (importReturnTypeDeclaration != null) {
			importList.add(importReturnTypeDeclaration);
		}

		return importList;
	}
}
