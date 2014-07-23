package io.vertigo.rest.plugins.rest.routesregister.sparkjava;

import io.vertigo.kernel.Home;
import io.vertigo.rest.impl.rest.handler.WsRestRoute;
import io.vertigo.rest.rest.RestManager;
import io.vertigo.rest.rest.filter.CorsAllower;
import io.vertigo.rest.rest.metamodel.EndPointDefinition;

import java.util.Collection;

import spark.Spark;
import spark.servlet.SparkApplication;

/**
 * Application class, use to register Spark-java route.
 * Could be embedded in Tomcat Server (see http://www.sparkjava.com/readme.html#title19)
 * 
 * @author npiedeloup
 */
public final class SparkJavaRoutesRegister implements SparkApplication {

	/**
	 * Spark-java application class.
	 * Translate EndPointDefinitions to Spark routes.
	 */
	public void init() {
		final RestManager restManager = Home.getComponentSpace().resolve(RestManager.class);
		restManager.scanAndRegisterRestfulServices();

		//Translate EndPoint to route
		final Collection<EndPointDefinition> endPointDefinitions = Home.getDefinitionSpace().getAll(EndPointDefinition.class);

		//Spark.before(new IE8CompatibilityFix("8"));
		Spark.before(new CorsAllower());

		for (final EndPointDefinition endPointDefinition : endPointDefinitions) {
			switch (endPointDefinition.getVerb()) {
				case GET:
					Spark.get(new WsRestRoute(endPointDefinition));
					break;
				case POST:
					Spark.post(new WsRestRoute(endPointDefinition));
					break;
				case PUT:
					Spark.put(new WsRestRoute(endPointDefinition));
					break;
				case DELETE:
					Spark.delete(new WsRestRoute(endPointDefinition));
					break;
				default:
					throw new UnsupportedOperationException();
			}
		}
	}
}
