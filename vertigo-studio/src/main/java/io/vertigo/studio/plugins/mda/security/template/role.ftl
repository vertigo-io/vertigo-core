package ${packageName};

import io.vertigo.app.Home;
import io.vertigo.persona.security.metamodel.Role;


/**
 * Warning. This class is generated automatically !
 *
 * Enum of the roles known by the vertigo application.
 */
public enum ${classSimpleName} {

<#list roles as role>
	/**
	 * ${role.description}.
	 */
	${role.name}<#if role_has_next>,<#else>;</#if>
</#list>


	/**
	 * Get the associated role for Vertigo.
	 *
	 * @param code
	 *            role code
	 * @return role
	 */
	public static Role getSecurityRole(final String code) {
		return Home.getApp().getDefinitionSpace().resolve(code, Role.class);
	}

	/**
	 * Get the associated role for Vertigo.
	 *
	 * @return role
	 */
	public Role getSecurityRole() {
		return getSecurityRole(name());
	}

}
