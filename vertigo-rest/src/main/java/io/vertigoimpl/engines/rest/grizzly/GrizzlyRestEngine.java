/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidiere - BP 159 - 92357 Le Plessis Robinson Cedex - France
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.vertigoimpl.engines.rest.grizzly;

import io.vertigo.kernel.engines.RestEngine;
import io.vertigo.kernel.exception.VRuntimeException;
import io.vertigo.kernel.lang.Activeable;
import io.vertigoimpl.engines.rest.cmd.ComponentCmd;

import java.io.IOException;
import java.net.URI;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.StaticHttpHandler;


import com.sun.jersey.api.container.grizzly2.GrizzlyServerFactory;
import com.sun.jersey.api.core.ClassNamesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;

/**
 * Implémentation du tableau de bord.
 * 
 * @author pchretien
 */
public final class GrizzlyRestEngine implements RestEngine, Activeable {
	private HttpServer httpServer;
	private final int port;

	@Inject
	public GrizzlyRestEngine(@Named("port") final int port) {
		this.port = port;
	}

	public void start() {
		try {
			httpServer = createServer(port);
		} catch (final IOException e) {
			throw new VRuntimeException(e);
		}
	}

	public void stop() {
		if (httpServer != null) {
			httpServer.stop();
		}
	}

	private static HttpServer createServer(final int port) throws IOException {
		//		final Set<Class<?>> spaces = resourceManager.getClassSelector().getTypesAnnotatedWith(Space.class);
		//final ResourceConfig rc = new ClassNamesResourceConfig(spaces.toArray(new Class[spaces.size()]));
		//		final ResourceConfig rc = new ClassNamesResourceConfig(kasperimpl.kraft.services.DefinitionServices.class, DataServices.class, ManagerServices.class, HomeServices.class);
		final ResourceConfig resourceConfig = new ClassNamesResourceConfig(ComponentCmd.class);
		final URI baseURI = UriBuilder.fromUri("http://localhost/").port(port).build();
		System.out.println(String.format("Jersey app started with WADL available at " + "%sapplication.wadl\nenter to stop it...", baseURI, baseURI));

		final HttpServer httpServer = GrizzlyServerFactory.createHttpServer(baseURI, resourceConfig);

		final StaticHttpHandler staticHttpHandler = new StaticHttpHandler(GrizzlyRestEngine.class.getResource("/web2").getFile());
		httpServer.getServerConfiguration().addHttpHandler(staticHttpHandler, "/vertigo");

		//		 new CLStaticHttpHandler(new URLClassLoader(new URL[] {
		//		            new File("target/jersey1-grizzly2-spring-1.0-SNAPSHOT.jar").toURI().toURL()}), "webapp/static2/"), "/jarstatic");
		//		
		//
		//		final StaticHttpHandler js = new StaticHttpHandler();
		//		js.addDocRoot(new File(KraftManager.class.getResource("webapp/js").getFile()));
		//		httpServer.getServerConfiguration().addHttpHandler(js, "/js");
		//
		//		final StaticHttpHandler img = new StaticHttpHandler();
		//		img.addDocRoot(new File(KraftManager.class.getResource("webapp/img").getFile()));
		//		httpServer.getServerConfiguration().addHttpHandler(img, "/img");
		//
		return httpServer;
	}
}
