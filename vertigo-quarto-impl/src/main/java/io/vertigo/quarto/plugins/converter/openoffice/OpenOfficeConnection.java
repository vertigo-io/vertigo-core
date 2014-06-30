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
 * API de connexion � OpenOffice repris de JodConverter 2.2.0 (http://www.artofsolving.com/opensource/jodconverter)
 *
 * @author npiedeloup
 * @version $Id: OpenOfficeConnection.java,v 1.1 2013/07/10 15:45:43 npiedeloup Exp $
 */
interface OpenOfficeConnection {

	/**
	 * Ouvre la connexion � OpenOffice.
	 * @throws ConnectException e
	 */
	void connect() throws ConnectException;

	/**
	 * Ferme la connexion � OpenOffice.
	 */
	void disconnect();

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
