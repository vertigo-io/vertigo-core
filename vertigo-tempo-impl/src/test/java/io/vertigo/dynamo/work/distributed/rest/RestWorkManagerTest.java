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
package io.vertigo.dynamo.work.distributed.rest;

import io.vertigo.dynamo.work.AbstractWorkManagerTest;
import io.vertigo.dynamo.work.MyWorkResultHanlder;
import io.vertigo.dynamo.work.WorkEngineProvider;
import io.vertigo.dynamo.work.WorkManager;
import io.vertigo.dynamo.work.mock.SlowWork;
import io.vertigo.dynamo.work.mock.SlowWorkEngine;

import java.io.IOException;
import java.net.URI;

import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.grizzly.http.server.HttpServer;
import org.junit.Assert;
import org.junit.Test;

import com.sun.jersey.api.container.grizzly2.GrizzlyServerFactory;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;

/**
 * @author npiedeloup
 */
public final class RestWorkManagerTest extends AbstractWorkManagerTest {
	@Inject
	private WorkManager workManager;
	private HttpServer httpServer;
	private ClientNode clientNode1;

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

	protected static ClientNode startClientNode(final int numClient) throws IOException {
		System.out.println("Starting ClientNode " + numClient + "...");
		final ClientNode clientNode = new ClientNode(numClient, 30);//duree de vie 30s max
		clientNode.start();
		return clientNode;
	}

	/**
	 * Initialisation du test pour implé spécifique.
	 * @throws Exception Erreur
	 */
	@Override
	protected void doSetUp() throws Exception {
		//pour éviter le mécanisme d'attente du client lorsque le serveur est absend, on démarre le serveur puis le client
		httpServer = startServer();
		Thread.sleep(500);
		clientNode1 = startClientNode(1);
		System.out.println(String.format("Jersey app started with WADL available at " + "%sapplication.wadl", BASE_URI));
	}

	/**
	 * Finalisation du test pour implé spécifique.
	 * @throws Exception Erreur
	 */
	@Override
	protected void doTearDown() throws Exception {
		if (httpServer != null) {
			System.out.println("Stopping grizzly...");
			httpServer.stop(); //TODO this stop don't interrupt handler threads. check with an 2.3.x grizzly version
			httpServer = null;
			/*for (final Thread thread : Thread.getAllStackTraces().keySet()) {
				if (thread.getName().contains("Grizzly")) {
					thread.interrupt();
				}
			}*/
		}
		if (clientNode1 != null) {
			System.out.println("Stopping ClientNode...");
			clientNode1.stop();
			clientNode1 = null;
		}
		System.out.println("All was stopped, quit now");
	}

	/**
	 * Teste l'exécution asynchrone d'une tache avec une durée de timeOut trop courte.
	 */
	@Test
	public void testDeadNode() throws InterruptedException, IOException {
		final MyWorkResultHanlder<Boolean> workResultHanlder = new MyWorkResultHanlder<>();
		final SlowWork slowWork = new SlowWork(1000);
		for (int i = 0; i < 20; i++) {
			workManager.schedule(slowWork, new WorkEngineProvider<>(SlowWorkEngine.class), workResultHanlder);
		}
		Thread.sleep(2000);
		clientNode1.stop(); //On stop le client1 avec des jobs en cours. Ils doivent être dedispatchés après détection des noeuds morts
		Thread.sleep(1000);
		final ClientNode clientNode2 = startClientNode(2);
		try {
			final boolean finished = workResultHanlder.waitFinish(20, 35 * 1000); //Le timeout des nodes est configuré à 20s
			System.out.println(workResultHanlder);
			Assert.assertEquals(null, workResultHanlder.getLastThrowable());
			Assert.assertTrue(finished);
		} finally {
			clientNode2.stop();
		}
	}

}
