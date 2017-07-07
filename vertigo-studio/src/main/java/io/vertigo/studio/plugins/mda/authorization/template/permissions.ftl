package ${packageName};

import io.vertigo.app.Home;
import io.vertigo.account.authorization.metamodel.Permission;
import io.vertigo.account.authorization.metamodel.PermissionName;


/**
 * Warning. This class is generated automatically !
 *
 * Enum of the permissions known by the application.
 */
public enum ${classSimpleName} implements PermissionName {

<#list permissions as permission>
	/**
	 * ${permission.comment.orElse(permission.name)}.
	 */
	${permission.name}<#if permission_has_next>,<#else>;</#if>
</#list>


	/**
	 * Get the associated permission.
	 *
	 * @param code permission code
	 * @return permission
	 */
	public static Permission of(final String code) {
		return Home.getApp().getDefinitionSpace().resolve(code, Permission.class);
	}

	/**
	 * Get the associated permission.
	 *
	 * @return role
	 */
	public Permission getPermission() {
		return of(name());
	}

}
