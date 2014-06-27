package io.vertigo.rest.handler;

import io.vertigo.kernel.Home;
import io.vertigo.kernel.di.injector.Injector;
import io.vertigo.rest.EndPointDefinition;

import javax.inject.Inject;

import spark.Request;
import spark.Response;
import spark.Route;

/**
 * Webservice Route for Spark.
 * @author npiedeloup
 */
public final class WsRestRoute extends Route {

	@Inject
	private ExceptionHandler exceptionHandler;
	@Inject
	private SessionHandler sessionHandler;
	@Inject
	private SecurityHandler securityHandler;

	private final HandlerChain handlerChain = new HandlerChain();

	public WsRestRoute(final String path, final EndPointDefinition endPointDefinition) {
		super(convertJaxRsPathToSpark(path));

		new Injector().injectMembers(this, Home.getComponentSpace());

		handlerChain.addHandler(exceptionHandler);
		if (endPointDefinition.isNeedSession()) {
			handlerChain.addHandler(sessionHandler);
		}
		if (endPointDefinition.isNeedAuthentification()) {
			handlerChain.addHandler(securityHandler);
		}
		handlerChain.addHandler(new RestfulServiceHandler(endPointDefinition));
	}

	private static String convertJaxRsPathToSpark(final String path) {
		final String newPath = path.replaceAll("(.*)\\{(.+)\\}(.*)", "$1:$2$3");
		return newPath;
	}

	@Override
	public Object handle(final Request request, final Response response) {
		try {
			return handlerChain.handle(request, response);
		} catch (final Throwable th) {
			System.err.println("Error " + th.getMessage());
			th.printStackTrace(System.err);
			return th.getMessage();
		}
	}
}
