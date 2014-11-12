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

import com.sun.star.frame.XComponentLoader;
import com.sun.star.ucb.XFileIdentifierConverter;
import com.sun.star.ucb.XSimpleFileAccess;

/**
 * A UNO remote protocol connection to a listening OpenOffice.org instance.
 *
 * API de connexion à OpenOffice repris de JodConverter 2.2.0 (http://www.artofsolving.com/opensource/jodconverter)
 *
 * @author npiedeloup
 */
interface OpenOfficeConnection extends AutoCloseable {

	/**
	 * Ouvre la connexion à OpenOffice.
	 * @throws ConnectException e
	 */
	void connect() throws ConnectException;

	/**
	 * Ferme la connexion à OpenOffice.
	 */
	@Override
	void close();

	/**
	 * @return the com.sun.star.frame.Desktop service
	 */
	XComponentLoader getDesktop();

	/**
	 * @return the com.sun.star.ucb.FileContentProvider service
	 */
	XFileIdentifierConverter getFileContentProvider();

	/**
	 * @return the XSimpleFileAccess service
	 */
	XSimpleFileAccess getSimpleFileAccess();
}
