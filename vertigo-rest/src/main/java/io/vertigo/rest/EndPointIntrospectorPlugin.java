package io.vertigo.rest;

import io.vertigo.kernel.component.Plugin;
import io.vertigo.rest.metamodel.EndPointDefinition;

import java.util.List;

/**
 * RestfulServices endPoint introspector.
 * @author npiedeloup
 */
public interface EndPointIntrospectorPlugin extends Plugin {

	/**
	 * Introspect RestfulService class, looking for "Rest End Point Definitions. 
	 * @param resfultServiceClass Class to introspect
	 * @return List of EndPointDefinition found
	 */
	List<EndPointDefinition> instrospectEndPoint(Class<? extends RestfulService> resfultServiceClass);

}
