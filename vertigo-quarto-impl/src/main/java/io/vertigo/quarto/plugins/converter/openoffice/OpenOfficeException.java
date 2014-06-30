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
 * API de d'exception OpenOffice repris de JodConverter 2.2.0 (http://www.artofsolving.com/opensource/jodconverter).
 *
 * @author npiedeloup
 * @version $Id: OpenOfficeException.java,v 1.1 2013/07/10 15:45:43 npiedeloup Exp $
 */
final class OpenOfficeException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	/**
	 * @param message d'erreur
	 * @param cause de l'erreur
	 */
	OpenOfficeException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
