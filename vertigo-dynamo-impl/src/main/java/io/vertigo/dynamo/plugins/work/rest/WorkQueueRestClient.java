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
package io.vertigo.dynamo.plugins.work.rest;

import io.vertigo.commons.codec.CodecManager;
import io.vertigo.dynamo.work.WorkEngineProvider;
import io.vertigo.dynamo.work.WorkItem;
import io.vertigo.dynamo.work.WorkResultHandler;
import io.vertigo.kernel.exception.VRuntimeException;
import io.vertigo.kernel.lang.Assertion;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.inject.Named;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status.Family;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.WebResource;

/**
 * Plugin g�rant l'api de distributedWorkQueueManager en REST avec jersey.
 * Pour la partie appel webService voir http://ghads.wordpress.com/2008/09/24/calling-a-rest-webservice-from-java-without-libs/
 *
 * @author npiedeloup
 * @version $Id: WorkQueueRestClient.java,v 1.12 2014/02/27 10:31:19 pchretien Exp $
 */
final class WorkQueueRestClient {
	private static final Logger LOG = Logger.getLogger(WorkQueueRestClient.class);
	private final CodecManager codecManager;
	private final String nodeUID;
	private final String serverUrl;
	private final Client locatorClient;
	private final ConcurrentMap<String, Object> lockByWorkType = new ConcurrentHashMap<>();

	/**
	 * Constructeur.
	 */
	public WorkQueueRestClient(@Named("nodeUID") final String nodeUID, @Named("serverUrl") final String serverUrl, final CodecManager codecManager) {
		Assertion.checkArgNotEmpty(nodeUID);
		Assertion.checkArgNotEmpty(serverUrl);
		Assertion.checkNotNull(codecManager);
		//---------------------------------------------------------------------
		this.nodeUID = nodeUID;
		this.serverUrl = serverUrl;
		this.codecManager = codecManager;
		locatorClient = Client.create();
		locatorClient.addFilter(new com.sun.jersey.api.client.filter.GZIPContentEncodingFilter());
	}

	public WorkItem<?, Object> pollWorkItem(final String workType) {
		//call methode distante, passe le workItem � started
		try {
			final String jsonResult;
			lockByWorkType.putIfAbsent(workType, new Object());
			//Cette tache est synchronized sur le workType, pour �viter de surcharger le serveur en demandes multiple
			synchronized (lockByWorkType.get(workType)) {
				final WebResource remoteWebResource = locatorClient.resource(serverUrl + "/pollWork/" + workType + "?nodeUID=" + nodeUID);
				final ClientResponse response = remoteWebResource.get(ClientResponse.class);
				checkResponseStatus(response);
				jsonResult = response.getEntity(String.class);
			}
			if (!jsonResult.isEmpty()) { //le json est vide s'il n'y a pas de tache en attente
				final String[] result = new Gson().fromJson(jsonResult, String[].class);
				final String uuid = result[0];
				final byte[] serializedResult = codecManager.getBase64Codec().decode(result[1]);
				final Object work = codecManager.getCompressedSerializationCodec().decode(serializedResult);
				LOG.info("pollWork(" + workType + ") : 1 Work");
				return new WorkItem<>(work, new WorkEngineProvider(workType), new CallbackWorkResultHandler(uuid, this));
			}
			LOG.info("pollWork(" + workType + ") : no Work");
			//pas de travaux : inutil d'attendre le poll attend d�j� 1s cot� serveur				
		} catch (final ClientHandlerException c) {
			LOG.warn("[pollWork] Erreur de connexion au serveur " + serverUrl + "/pollWork/" + workType + " (" + c.getMessage() + ")");
			//En cas d'erreur on attend quelques secondes, pour attendre que le serveur revienne
			try {
				lockByWorkType.putIfAbsent(serverUrl, new Object());
				//En cas d'absence du serveur, 
				//ce synchronized permet d'�taler les appels au serveur de chaque worker : le premier attendra 2s, le second 2+2s, le troisi�me : 4+2s, etc..
				//d�s le retour du serveur, on r�cup�re un worker toute les 2s
				synchronized (lockByWorkType.get(serverUrl)) {
					Thread.sleep(2000); //on veut bien un sleep
				}
			} catch (final InterruptedException e) {
				//rien on retourne
			}
		} catch (final Exception c) {
			LOG.warn("[pollWork] Erreur de traitement de l'acc�s au serveur " + serverUrl + "/pollWork/" + workType + " (" + c.getMessage() + ")", c);
		}
		return null;
	}

	private static class CallbackWorkResultHandler implements WorkResultHandler {
		private final WorkQueueRestClient workQueueRestClient;
		private final String uuid;

		public CallbackWorkResultHandler(final String uuid, final WorkQueueRestClient workQueueRestClient) {
			this.uuid = uuid;
			this.workQueueRestClient = workQueueRestClient;
		}

		public void onStart() {
			workQueueRestClient.sendOnStart(uuid);
		}

		public void onSuccess(final Object result) {
			workQueueRestClient.sendOnSuccess(uuid, result);
		}

		public void onFailure(final Throwable error) {
			workQueueRestClient.sendOnFailure(uuid, error);
		}

	}

	private void sendOnStart(final String uuid) {
		//call methode distante
		final WebResource remoteWebResource = locatorClient.resource(serverUrl + "/event/start/" + uuid);
		try {
			final ClientResponse response = remoteWebResource.accept(MediaType.TEXT_PLAIN).post(ClientResponse.class);
			checkResponseStatus(response);
		} catch (final Exception c) {
			LOG.warn("[onStart] Erreur de connexion au serveur " + remoteWebResource.getURI() + " (" + c.getMessage() + ")");
		}
	}

	private void sendOnSuccess(final String uuid, final Object result) {
		//call methode distante
		final WebResource remoteWebResource = locatorClient.resource(serverUrl + "/event/success/" + uuid);
		try {
			final byte[] serializedResult = codecManager.getCompressedSerializationCodec().encode((Serializable) result);
			final String jsonResult = codecManager.getBase64Codec().encode(serializedResult);
			final ClientResponse response = remoteWebResource.accept(MediaType.TEXT_PLAIN).post(ClientResponse.class, jsonResult);
			checkResponseStatus(response);
		} catch (final Exception c) {
			LOG.warn("[onSuccess] Erreur de connexion au serveur " + remoteWebResource.getURI() + " (" + c.getMessage() + ")");
		}
	}

	private void sendOnFailure(final String uuid, final Throwable error) {
		//call methode distante
		final WebResource remoteWebResource = locatorClient.resource(serverUrl + "/event/failure/" + uuid);
		try {
			final byte[] serializedResult = codecManager.getCompressedSerializationCodec().encode(error);
			final String jsonResult = codecManager.getBase64Codec().encode(serializedResult);
			final ClientResponse response = remoteWebResource.accept(MediaType.TEXT_PLAIN).post(ClientResponse.class, jsonResult);
			checkResponseStatus(response);
		} catch (final Exception c) {
			LOG.warn("[onFailure] Erreur de connexion au serveur " + remoteWebResource.getURI() + " (" + c.getMessage() + ")");
		}
	}

	private void checkResponseStatus(final ClientResponse response) {
		final Status status = response.getClientResponseStatus();
		if (status.getFamily() == Family.SUCCESSFUL) {
			return;
		}
		throw new VRuntimeException("Une erreur est survenue : " + status.getStatusCode() + " " + status.getReasonPhrase());
	}

}
