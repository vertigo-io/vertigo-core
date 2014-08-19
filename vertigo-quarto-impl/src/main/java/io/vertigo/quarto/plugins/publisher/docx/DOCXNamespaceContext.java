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
package io.vertigo.quarto.plugins.publisher.docx;

import java.util.Iterator;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

/**
 * Resolver de namespaces pour les docx.
 * 
 * @author adufranne
 * @version $Id: DOCXNamespaceContext.java,v 1.1 2013/07/11 13:24:48 npiedeloup Exp $
 */
public class DOCXNamespaceContext implements NamespaceContext {

	/** {@inheritDoc} */
	public String getNamespaceURI(final String prefix) {
		switch (prefix) {
			case "wpc":
				return "http://schemas.microsoft.com/office/word/2010/wordprocessingCanvas";
			case "mc":
				return "http://schemas.openxmlformats.org/markup-compatibility/2006";
			case "o":
				return "urn:schemas-microsoft-com:office:office";
			case "r":
				return "http://schemas.openxmlformats.org/officeDocument/2006/relationships";
			case "m":
				return "http://schemas.openxmlformats.org/officeDocument/2006/math";
			case "v":
				return "urn:schemas-microsoft-com:vml";
			case "wp14":
				return "http://schemas.microsoft.com/office/word/2010/wordprocessingDrawing";
			case "wp":
				return "http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing";
			case "w10":
				return "urn:schemas-microsoft-com:office:word";
			case "w":
				return "http://schemas.openxmlformats.org/wordprocessingml/2006/main";
			case "w14":
				return "http://schemas.microsoft.com/office/word/2010/wordml";
			case "wpg":
				return "http://schemas.microsoft.com/office/word/2010/wordprocessingGroup";
			case "wpi":
				return "http://schemas.microsoft.com/office/word/2010/wordprocessingInk";
			case "wne":
				return "http://schemas.microsoft.com/office/word/2006/wordml";
			case "wps":
				return "http://schemas.microsoft.com/office/word/2010/wordprocessingShape";
			default:
				return XMLConstants.DEFAULT_NS_PREFIX;
		}
	}

	/** {@inheritDoc} */
	public String getPrefix(final String namespace) {
		switch (namespace) {
			case "http://schemas.microsoft.com/office/word/2010/wordprocessingCanvas":
				return "wpc";
			case "http://schemas.openxmlformats.org/markup-compatibility/2006":
				return "mc";
			case "urn:schemas-microsoft-com:office:office":
				return "o";
			case "http://schemas.openxmlformats.org/officeDocument/2006/relationships":
				return "r";
			case "http://schemas.openxmlformats.org/officeDocument/2006/math":
				return "m";
			case "urn:schemas-microsoft-com:vml":
				return "v";
			case "http://schemas.microsoft.com/office/word/2010/wordprocessingDrawing":
				return "wp14";
			case "http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing":
				return "wp";
			case "urn:schemas-microsoft-com:office:word":
				return "w10";
			case "http://schemas.openxmlformats.org/wordprocessingml/2006/main":
				return "w";
			case "http://schemas.microsoft.com/office/word/2010/wordml":
				return "w14";
			case "http://schemas.microsoft.com/office/word/2010/wordprocessingGroup":
				return "wpg";
			case "http://schemas.microsoft.com/office/word/2010/wordprocessingInk":
				return "wpi";
			case "http://schemas.microsoft.com/office/word/2006/wordml":
				return "wne";
			case "http://schemas.microsoft.com/office/word/2010/wordprocessingShape":
				return "wps";
			default:
				return null;
		}
	}

	/** {@inheritDoc} */
	public Iterator<String> getPrefixes(final String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}
