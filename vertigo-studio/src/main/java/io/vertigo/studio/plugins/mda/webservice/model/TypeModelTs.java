package io.vertigo.studio.plugins.mda.webservice.model;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import io.vertigo.studio.plugins.mda.webservice.JsFileNameUtil;

public class TypeModelTs {

	private boolean isObject = false;
	private boolean isList = false;
	private String jsGenericType;
	private String jsImportPath = null;

	public TypeModelTs(final Type type) {
		String typeValue;
		if (type instanceof ParameterizedType) {
			final ParameterizedType genericListType = (ParameterizedType) type;
			final Class<?> genericClass = (Class<?>) genericListType.getActualTypeArguments()[0];
			typeValue = genericClass.toString();
			isList = List.class.getTypeName().equals(genericListType.getTypeName().split("<")[0]);
		} else {
			typeValue = type.toString();
		}
		if (typeValue.contains(".domain.")) {
			final String[] packageSplitted = typeValue.split("\\.");
			jsImportPath = "../../../" + packageSplitted[packageSplitted.length - 2] + "/config/entity-definition-gen/";
		}
		typeValue = typeValue.replaceAll("class |interface ", "").replaceAll("(?<=^|<)[a-z]+\\.[a-z\\.]+", "");
		switch (typeValue) {
			case "String":
			case "Date":
				jsGenericType = "string";
				break;
			case "Long":
			case "long":
			case "Integer":
			case "int":
				jsGenericType = "number";
				break;
			case "boolean":
			case "Boolean":
				jsGenericType = "boolean";
				break;
			case "VFile":
			case "vfile":
				jsGenericType = "any";
				break;
			default:
				isObject = true;
				jsGenericType = typeValue;
				break;
		}
	}

	public String getImportDeclaration() {
		if (getImportPath() != null) {
			return "import { " + getJsGenericType() + " } from \"" + getImportPath() + getJsClassName() + "\"";
		}
		return null;
	}

	public String getJsGenericType() {
		return jsGenericType;
	}

	public String getJsClassName() {
		return JsFileNameUtil.convertCamelCaseToJsCase(getJsGenericType());
	}

	public String getImportPath() {
		return jsImportPath;
	}

	public String getJsType() {
		return jsGenericType + (isList ? "[]" : "");
	}

	public boolean isObject() {
		return isObject;
	}

	public boolean isList() {
		return isList;
	}

	public boolean isVoid() {
		return "void".equals(jsGenericType);
	}
}
