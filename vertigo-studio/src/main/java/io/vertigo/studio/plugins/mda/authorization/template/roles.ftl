package ${packageName};

import io.vertigo.app.Home;
import io.vertigo.account.authorization.metamodel.Role;


/**
 * Warning. This class is generated automatically !
 *
 * Enum of the roles known by the application.
 */
public enum ${classSimpleName} {

<#list roles as role>
	/**
	 * ${role.description}.
	 */
	${role.name}<#if role_has_next>,<#else>;</#if>
</#list>


	/**
	 * Get the associated role.
	 *
	 * @param code role code
	 * @return role
	 */
	public static Role of(final String code) {
		return Home.getApp().getDefinitionSpace().resolve(code, Role.class);
	}

	/**
	 * Get the associated role.
	 *
	 * @return role
	 */
	public Role getRole() {
		return of(name());
	}

}
