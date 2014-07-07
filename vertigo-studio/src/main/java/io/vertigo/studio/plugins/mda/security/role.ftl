package ${packageName};


/**
 * Attention cette classe est générée automatiquement !
 */
public enum ${classSimpleName} {

<#list roles as role>
	/**
	 * ${role.name}.
	 */
	${role.name},
</#list>
}
