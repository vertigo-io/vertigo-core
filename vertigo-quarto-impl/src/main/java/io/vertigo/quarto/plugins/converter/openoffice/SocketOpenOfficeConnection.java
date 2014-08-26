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

/**
 * Implémentation de connexion à OpenOffice en mode Socket (mode le plus simple).
 * <p>
 * <b>Attention</b> Il faut configurer OpenOffice pour qu'il accepte cette connexion.
 * Soit en modifiant le fichier de conf :
 * <code>OOoBasePath\share\registry\data\org\openoffice\Setup.xcu</code>
 * Juste après cette ligne-ci : <code><node oor:name=\"Office\"></code>
 * Il faut ajouter les lignes suivantes :
 * <code><prop oor:name=\"ooSetupConnectionURL\" oor:type=\"xs:string\">
 * <value>socket,host=localhost,port=8100;urp;</value>
 * </prop></code>
 * Ensuite, il faut relancer OpenOffice
 * <p>
 * Soit par ligne de commande (<b>a tester</b> : http://linuxfr.org/forums/15/16106.html) :
 * <code>/usr/bin/xvfb-run -a /usr/bin/openoffice -invisible "-accept=socket,host=localhost,port=8100;urp;StarOffice.Service.Manager" &</code>
 * <p>
 * Repris de JodConverter 2.2.0 (http://www.artofsolving.com/opensource/jodconverter)
 * @author npiedeloup
 */
final class SocketOpenOfficeConnection extends AbstractOpenOfficeConnection {
	/**
	 * Constructeur utilisant des paramètres de connexion spécifiques.
	 * @param host spécifique
	 * @param port spécifique
	 */
	SocketOpenOfficeConnection(final String host, final int port) {
		super("socket,host=" + host + ",port=" + port + ",tcpNoDelay=1");
	}
}
