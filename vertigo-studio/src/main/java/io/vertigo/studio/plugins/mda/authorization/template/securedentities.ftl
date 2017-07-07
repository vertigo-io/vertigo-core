package ${packageName};

import io.vertigo.account.authorization.metamodel.OperationName;
import io.vertigo.account.authorization.metamodel.PermissionName;

/**
 * Warning. This class is generated automatically !
 *
 * Enum of the security permissions and operations associated with secured entities known by the application.
 */
public final class ${classSimpleName} {

	private  ${classSimpleName}() {
		//private constructor
	}

<#list securedentities as securedEntity>
	/**
	 * Permissions of ${securedEntity.entity.classSimpleName}.
	 */
	public enum ${securedEntity.entity.classSimpleName}Permissions implements PermissionName {
		<#list securedEntity.operations as operation>
			 /** ${operation.comment.orElse(operation.name)}. */
			${operation.name}<#if operation_has_next>,<#else>;</#if>
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

	/**
	 * Operations of ${securedEntity.entity.classSimpleName}.
	 */
	public enum ${securedEntity.entity.classSimpleName}Operations implements OperationName<${securedEntity.entity.classSimpleName}> {
		<#list securedEntity.operations as operation>
			/** ${operation.comment.orElse(operation.operation.get())}. */
			${operation.operation.get()}<#if operation_has_next>,<#else>;</#if>
		</#list>
	}
</#list>
}
