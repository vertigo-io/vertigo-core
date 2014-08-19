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
//
// JODConverter - Java OpenDocument Converter
// Copyright (C) 2004-2007 - Mirko Nasato <mirko@artofsolving.com>
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
// http://www.gnu.org/copyleft/lesser.html
//
package io.vertigo.quarto.plugins.converter.openoffice;

import java.net.ConnectException;

import org.apache.log4j.Logger;

import com.sun.star.beans.XPropertySet;
import com.sun.star.bridge.XBridge;
import com.sun.star.bridge.XBridgeFactory;
import com.sun.star.comp.helper.Bootstrap;
import com.sun.star.connection.NoConnectException;
import com.sun.star.connection.XConnection;
import com.sun.star.connection.XConnector;
import com.sun.star.frame.XComponentLoader;
import com.sun.star.lang.EventObject;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XEventListener;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.ucb.XFileIdentifierConverter;
import com.sun.star.ucb.XSimpleFileAccess;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

/**
 * API de connexion à OpenOffice repris de JodConverter 2.2.0 (LGPL = utilisable pour logiciel propriétaire)
 * (http://www.artofsolving.com/opensource/jodconverter).
 * La différence est la suppression de la dépendance au logger d'origine, pour pointer sur log4j.
 * 
 * Cet utilitaire de connexion à OpenOffice n'est pas Multi-Thread !!
 * 
 * @author npiedeloup
 * @version $Id: AbstractOpenOfficeConnection.java,v 1.1 2013/07/10 15:45:43 npiedeloup Exp $
 */
abstract class AbstractOpenOfficeConnection implements OpenOfficeConnection, XEventListener {

	private final Logger logger = Logger.getLogger(getClass());

	private final String connectionString;
	private XComponent bridgeComponent;
	private XMultiComponentFactory serviceManager;
	private XComponentContext componentContext;
	private boolean connected; // initialisé à false
	private boolean expectingDisconnection; // initialisé à false

	/**
	 * Constructeur.
	 * @param connectionString String
	 */
	AbstractOpenOfficeConnection(final String connectionString) {
		this.connectionString = connectionString;
	}

	/** {@inheritDoc} */
	public final void connect() throws ConnectException {
		logger.debug("connecting");
		try {
			final XComponentContext localContext = Bootstrap.createInitialComponentContext(null);
			final XMultiComponentFactory localServiceManager = localContext.getServiceManager();
			final XConnector connector = UnoRuntime.queryInterface(XConnector.class, localServiceManager.createInstanceWithContext("com.sun.star.connection.Connector", localContext));
			final XConnection connection = connector.connect(connectionString);
			final XBridgeFactory bridgeFactory = UnoRuntime.queryInterface(XBridgeFactory.class, localServiceManager.createInstanceWithContext("com.sun.star.bridge.BridgeFactory", localContext));
			final XBridge bridge = bridgeFactory.createBridge("", "urp", connection, null);
			bridgeComponent = UnoRuntime.queryInterface(XComponent.class, bridge);
			bridgeComponent.addEventListener(this);
			//TODO : attention : Déjà observé le queryInterface attend indéfiniment sans timeout ce qui bloque le thread et tout autre converssion (car synchronized)
			serviceManager = UnoRuntime.queryInterface(XMultiComponentFactory.class, bridge.getInstance("StarOffice.ServiceManager"));
			final XPropertySet properties = UnoRuntime.queryInterface(XPropertySet.class, serviceManager);
			componentContext = UnoRuntime.queryInterface(XComponentContext.class, properties.getPropertyValue("DefaultContext"));
			connected = true;
			logger.info("connected");
		} catch (final NoConnectException connectException) {
			final ConnectException e = new ConnectException("connection failed: " + connectionString + ": " + connectException.getMessage());
			e.initCause(connectException);
			throw e;
		} catch (final Exception exception) {
			throw new OpenOfficeException("connection failed: " + connectionString, exception);
		}
	}

	/** {@inheritDoc} */
	public final void disconnect() {
		logger.debug("disconnecting");
		expectingDisconnection = true;
		bridgeComponent.dispose();
	}

	/** {@inheritDoc} */
	public final void disposing(final EventObject event) {
		connected = false;
		if (expectingDisconnection) {
			logger.info("disconnected");
		} else {
			logger.error("disconnected unexpectedly");
		}
		expectingDisconnection = false;
	}

	private Object getService(final String className) {
		try {
			if (!connected) {
				logger.info("trying to (re)connect");
				connect();
			}
			return serviceManager.createInstanceWithContext(className, componentContext);
		} catch (final Exception exception) {
			throw new OpenOfficeException("could not obtain service: " + className, exception);
		}
	}

	/** {@inheritDoc} */
	public final XComponentLoader getDesktop() {
		return UnoRuntime.queryInterface(XComponentLoader.class, getService("com.sun.star.frame.Desktop"));
	}

	/** {@inheritDoc} */
	public final XFileIdentifierConverter getFileContentProvider() {
		return UnoRuntime.queryInterface(XFileIdentifierConverter.class, getService("com.sun.star.ucb.FileContentProvider"));
	}

	/** {@inheritDoc} */
	public final XSimpleFileAccess getSimpleFileAccess() {
		return UnoRuntime.queryInterface(XSimpleFileAccess.class, getService("com.sun.star.ucb.SimpleFileAccess"));
	}
}
