package io.vertigo.quarto.converter.distributed;

import io.vertigo.quarto.converter.AbstractConverterManagerTest;

import java.io.IOException;
import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import org.glassfish.grizzly.http.server.HttpServer;

import com.sun.jersey.api.container.grizzly2.GrizzlyServerFactory;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;

/**
 * Test de l'impl�mentation avec le plugin OpenOfficeRemoteConverterPlugin.
 * 
 * @author npiedeloup
 * @version $Id: ConverterManagerDistributedTest.java,v 1.2 2014/06/26 12:30:48 npiedeloup Exp $
 */
public final class ConverterManagerDistributedTest extends AbstractConverterManagerTest {
	/** {@inheritDoc} */
	@Override
	protected String[] getManagersXmlFileName() {
		return new String[] { "./managers-test-distributed.xml" };
	}

	private HttpServer httpServer;
	private ClientNode clientNode;

	private static URI getBaseURI() {
		return UriBuilder.fromUri("http://0.0.0.0/").port(10001).build(); //0.0.0.0 permet d'indiquer que l'url est accessible depuis l'interface r�seau exterieur : localhost n'est acc�ssible qu'en loopback.
	}

	public static final URI BASE_URI = getBaseURI();

	protected static HttpServer startServer() throws IOException {
		System.out.println("Starting grizzly...");
		final ResourceConfig rc = new PackagesResourceConfig("kasperx.work.plugins.rest");
		rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS, com.sun.jersey.api.container.filter.GZIPContentEncodingFilter.class.getName());
		rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_RESPONSE_FILTERS, com.sun.jersey.api.container.filter.GZIPContentEncodingFilter.class.getName());
		return GrizzlyServerFactory.createHttpServer(BASE_URI, rc);
	}

	protected static ClientNode startClientNode() throws IOException {
		System.out.println("Starting ClientNode...");
		final ClientNode clientNode = new ClientNode(2 * 60);//dur�e de vie 2 min max
		clientNode.start();
		return clientNode;
	}

	/**
	 * Initialisation du test pour impl� sp�cifique.
	 * @throws Exception Erreur
	 */
	@Override
	protected void doSetUp() throws Exception {
		super.doSetUp();
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
		super.doTearDown();
		Thread.sleep(500);
		if (clientNode != null) {
			clientNode.stop();
		}
		if (httpServer != null) {
			httpServer.stop();
		}
	}
}
