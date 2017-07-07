package io.vertigo.vega.impl.webservice.healthcheck;

import io.vertigo.vega.webservice.WebServices;
import io.vertigo.vega.webservice.stereotype.AnonymousAccessAllowed;
import io.vertigo.vega.webservice.stereotype.GET;
import io.vertigo.vega.webservice.stereotype.SessionLess;

/**
 * Healthcheck WebService.
 * @author xdurand (30 mars 2017 18:00:02)
 */
public final class HealthcheckWebServices implements WebServices {

	/**
	 * Healthcheck WebService.
	 * @return constant string "OK" that can be used to monitor the technical health.
	 */
	@SuppressWarnings("static-method")
	@SessionLess
	@AnonymousAccessAllowed
	@GET("/healthcheck")
	public String healthcheck() {
		return "OK";
	}

}
