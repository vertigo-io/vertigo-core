package io.vertigo.dynamo.work.distributed.rest;

import io.vertigo.dynamo.work.AbstractWorkManagerTest;

import java.io.IOException;
import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import org.glassfish.grizzly.http.server.HttpServer;

import com.sun.jersey.api.container.grizzly2.GrizzlyServerFactory;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;

/**
 * @author npiedeloup
 * $Id: DistributedWorkManagerTest.java,v 1.4 2014/01/20 18:57:06 pchretien Exp $
 */
public final class DistributedWorkManagerTest extends AbstractWorkManagerTest {

	private HttpServer httpServer;
	private ClientNode clientNode;

	private static URI getBaseURI() {
		return UriBuilder.fromUri("http://0.0.0.0/").port(10998).build();
	}

	public static final URI BASE_URI = getBaseURI();

	protected static HttpServer startServer() throws IOException {
		System.out.println("Starting grizzly...");
		final ResourceConfig rc = new PackagesResourceConfig("io.vertigo.dynamo.plugins.work.rest");
		rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS, com.sun.jersey.api.container.filter.GZIPContentEncodingFilter.class.getName());
		rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_RESPONSE_FILTERS, com.sun.jersey.api.container.filter.GZIPContentEncodingFilter.class.getName());
		return GrizzlyServerFactory.createHttpServer(BASE_URI, rc);
	}

	protected static ClientNode startClientNode() throws IOException {
		System.out.println("Starting ClientNode...");
		final ClientNode clientNode = new ClientNode(30);//duree de vie 30s max
		clientNode.start();
		return clientNode;
	}

	/**
	 * Initialisation du test pour impl� sp�cifique.
	 * @throws Exception Erreur
	 */
	@Override
	protected void doSetUp() throws Exception {
		//pour �viter le m�canisme d'attente du client lorsque le serveur est absend, on d�marre le serveur puis le client
		httpServer = startServer();
		Thread.sleep(500);
		clientNode = startClientNode();

		System.out.println(String.format("Jersey app started with WADL available at " + "%sapplication.wadl", BASE_URI));
	}

	/**
	 * Finalisation du test pour impl� sp�cifique.
	 * @throws Exception Erreur
	 */
	@Override
	protected void doTearDown() throws Exception {
		Thread.sleep(1000);
		if (clientNode != null) {
			System.out.println("Stoping ClientNode...");
			clientNode.stop();
		}
		Thread.sleep(250);
		if (httpServer != null) {
			System.out.println("Stoping grizzly...");
			httpServer.stop();
		}

	}
}
