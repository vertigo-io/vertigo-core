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
package io.vertigoimpl.engines.elastica.redis;

import io.vertigo.lang.Assertion;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import redis.clients.jedis.JedisPool;

/**
 * Proxy http pour communication http au lieu de rmi.
 *
 * @author pchretien
 */
final class RedisInvocationHandler<F> implements InvocationHandler {
	//	private final String serverURL;
	//	private final String address;
	private final Class<F> beanClass;
	//	private String cookie;

	private final ZClientWork clientWork;

	/**
	 * Constructeur.
	 */
	RedisInvocationHandler(final JedisPool jedisPool, final Class<F> beanClass) {
		Assertion.checkNotNull(jedisPool);
		Assertion.checkNotNull(beanClass, "Classe du Bean non renseignée");
		//---------------------------------------------------------------------
		this.clientWork = new ZClientWork(jedisPool);
		this.beanClass = beanClass;
	}

	//	/**
	//	 * Ouvre la connection http.
	//	 * @param serverURL java.lang.String
	//	 * @param methodName java.lang.String
	//	 * @return java.net.URLConnection
	//	 * @throws java.io.IOException   Exception de communication
	//	 */
	//	private static URLConnection openConnection(final String serverURL, final String address, final String methodName) throws IOException {
	//		// gzip=false pour indiquer à un éventuel filtre http de compression
	//		// que le flux est déjà compressé par HttpProxyClient et HttpProxyServer
	//		//System.out.println(">>>url= " + serverURL + '/' + address + '/' + methodName + "?gzip=false");
	//		final URL url = new URL(serverURL + '/' + address + '/' + methodName + "?gzip=false");
	//		final URLConnection connection = url.openConnection();
	//		connection.setDoOutput(true);
	//		connection.setUseCaches(false);
	//		connection.setRequestProperty("Content-Type", java.awt.datatransfer.DataFlavor.javaSerializedObjectMimeType);
	//		connection.setRequestProperty("Accept-Encoding", "gzip");
	//		connection.setConnectTimeout(60 * 1000);
	//		connection.setReadTimeout(10 * 60 * 1000);
	//
	//		return connection;
	//	}

	//==========================================================================
	//==========================================================================
	//==========================================================================

	/** {@inheritDoc} */
	@Override
	public Object invoke(final Object proxy, final Method method, final Object[] parameters) throws Throwable {
		//System.out.println(">>invoke");
		Assertion.checkNotNull(proxy);
		Assertion.checkNotNull(method);
		//Les méthodes sans paramètres envoient null
		//---------------------------------------------------------------------
		final ZMethod zmethod = new ZMethod(beanClass, method, parameters);
		final int timeoutSeconds = 10;
		return clientWork.process(zmethod, timeoutSeconds);
		//		final Object result = doInvoke(method, parameters == null ? new Object[0] : parameters);
		//		if (result instanceof Throwable) {
		//			throw (Throwable) result;
		//		}
		//		return result;
	}
	//	private Object doInvoke(final Method method, final Object[] parameters) throws IOException {
	//
	//		final URLConnection connection = openConnection(serverURL, address, method.getName());
	//		final HttpTunnellingRequest request = new HttpTunnellingRequest(beanClass.getName(), method.getName(), method.getParameterTypes(), parameters);
	//		HttpTunnellingReaderWriterUtil.write(request, connection.getOutputStream());
	//
	//		if (cookie != null) {
	//			// on met le cookie ici pour pouvoir assurer si besoin un suivi de session par Cookie dans le load-balancer
	//			connection.setRequestProperty("Cookie", cookie);
	//		}
	//
	//		connection.connect();
	//
	//		final String setCookie = connection.getHeaderField("Set-Cookie");
	//		if (setCookie != null) {
	//			cookie = setCookie;
	//		}
	//
	//		return read(connection);
	//	}

	/**
	 * Lit l'objet renvoyé dans le flux de réponse.
	 * Il y a une spécificité sur la connexion qui peut avoir un flux d'erreur qu'il faut fermer.
	 * @return java.lang.Object
	 * @param connection java.net.URLConnection
	 * @throws java.io.IOException   Exception de communication
	 */
	//	private static Object read(final URLConnection connection) throws IOException {
	//		try {
	//			return HttpTunnellingReaderWriterUtil.read(connection.getInputStream());
	//		} catch (final ClassNotFoundException e) {
	//			//Une classe transmise par le serveur n'a pas été trouvée
	//			throw new KRuntimeException("Une classe transmise par le serveur n'a pas été trouvée", e);
	//		} finally {
	//			// ce close doit être fait en finally
	//			// (http://java.sun.com/j2se/1.5.0/docs/guide/net/http-keepalive.html)
	//			if (connection instanceof HttpURLConnection) {
	//				final InputStream error = ((HttpURLConnection) connection).getErrorStream();
	//				if (error != null) {
	//					error.close();
	//				}
	//			}
	//		}
	//	}
}
