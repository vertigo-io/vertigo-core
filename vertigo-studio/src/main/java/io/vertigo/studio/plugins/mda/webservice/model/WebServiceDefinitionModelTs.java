package io.vertigo.studio.plugins.mda.webservice.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.vertigo.vega.webservice.metamodel.WebServiceDefinition;
import io.vertigo.vega.webservice.metamodel.WebServiceParam;

public class WebServiceDefinitionModelTs {

	private final WebServiceDefinition webServiceDefinition;
	private final List<WebServiceParamModelTs> webServiceParamModelTsList = new ArrayList<>();
	private final TypeModelTs returnType;

	public WebServiceDefinitionModelTs(final WebServiceDefinition webServiceDefinition) {
		this.webServiceDefinition = webServiceDefinition;

		for (final WebServiceParam wsParam : webServiceDefinition.getWebServiceParams()) {
			webServiceParamModelTsList.add(new WebServiceParamModelTs(wsParam));
		}

		returnType = new TypeModelTs(webServiceDefinition.getMethod().getGenericReturnType());
	}

	public String getMethodName() {
		return webServiceDefinition.getMethod().getName();
	}

	public List<WebServiceParamModelTs> getWebServiceParams() {
		return webServiceParamModelTsList;
	}

	public String getJsServerCallMethod() {
		String verb = webServiceDefinition.getVerb().toString().toLowerCase();
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

	public String getFunctionnalPackageName() {
		final String[] fullPackageNameSplited = webServiceDefinition.getMethod().getDeclaringClass().getPackage().toString().split("\\.");
		return fullPackageNameSplited[fullPackageNameSplited.length - 1];
	}

	public boolean isGet() {
		return "GET".equals(webServiceDefinition.getVerb().toString());
	}

	public boolean isDelete() {
		return "DELETE".equals(webServiceDefinition.getVerb().toString());
	}

	public String getPath() {
		final String path = webServiceDefinition.getPath();
		return path.replaceAll("\\{(.+?)\\}", "\\${$1}");
	}

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
