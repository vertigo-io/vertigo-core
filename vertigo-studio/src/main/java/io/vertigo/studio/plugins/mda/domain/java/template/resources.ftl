package ${packageName};

import io.vertigo.lang.MessageKey;

/**
 * Attention cette classe est générée automatiquement !
 * Resources du module ${packageName}
 */
public enum ${simpleClassName} implements MessageKey {
<#list dtDefinitions as dtDefinition>

	/***********************************************************
	/** ${dtDefinition.classSimpleName}.
	/***********************************************************
	<#list dtDefinition.fields as dtField>
	/**
	 * ${dtField.label}.
	 */
	${dtField.resourceKey},
	</#list>
</#list>
}
 