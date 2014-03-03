package io.vertigo.dynamo.impl.database;

import io.vertigo.dynamo.database.connection.ConnectionProvider;
import io.vertigo.kernel.component.Plugin;

/**
* Plugin du provider de connexions.
*
* @author pchretien
* @version $Id: ConnectionProviderPlugin.java,v 1.3 2013/10/22 12:24:21 pchretien Exp $
*/
public interface ConnectionProviderPlugin extends ConnectionProvider, Plugin {
	//
}
