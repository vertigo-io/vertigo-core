package ${packageName};

import io.vertigo.account.authorization.metamodel.OperationName;
import io.vertigo.account.authorization.metamodel.AuthorizationName;

/**
 * Warning. This class is generated automatically !
 *
 * Enum of the security authorizations and operations associated with secured entities known by the application.
 */
public final class ${classSimpleName} {

	private  ${classSimpleName}() {
		//private constructor
	}

<#list securedentities as securedEntity>
	/**
	 * Authorizations of ${securedEntity.entity.classSimpleName}.
	 */
	public enum ${securedEntity.entity.classSimpleName}Authorizations implements AuthorizationName {
		<#list securedEntity.operations as operation>
			 /** ${operation.comment.orElse(operation.name)}. */
			${operation.name}<#if operation_has_next>,<#else>;</#if>
		</#list>
		
		/**
		 * Get the associated authorization.
		 *
		 * @param code authorization code
		 * @return authorization
		 */
		public static Authorization of(final String code) {
			return Home.getApp().getDefinitionSpace().resolve(code, Authorization.class);
		}
	
		/**
		 * Get the associated authorization.
		 *
		 * @return role
		 */
		public Authorization getAuthorization() {
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
