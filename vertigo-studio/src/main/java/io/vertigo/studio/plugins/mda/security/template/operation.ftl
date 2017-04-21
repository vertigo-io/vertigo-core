package ${packageName};


/**
 * Warning. This class is generated automatically !
 *
 * Enum of the security operation associated with permissions known by the vertigo application.
 */
public enum ${classSimpleName} {

<#list operations as operation>
	/**
	 * Security operation ${operation}.
	 */
	${operation},
</#list>
}
