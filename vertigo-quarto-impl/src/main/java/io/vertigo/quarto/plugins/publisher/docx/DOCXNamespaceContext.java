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
		switch (prefix){
			case"wpc":
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
			default :
				return XMLConstants.DEFAULT_NS_PREFIX;
		}
	}

	/** {@inheritDoc} */
	public String getPrefix(final String namespace) {
		if ("http://schemas.microsoft.com/office/word/2010/wordprocessingCanvas".equals(namespace)) {
			return "wpc";
		} else if ("http://schemas.openxmlformats.org/markup-compatibility/2006".equals(namespace)) {
			return "mc";
		} else if ("urn:schemas-microsoft-com:office:office".equals(namespace)) {
			return "o";
		} else if ("http://schemas.openxmlformats.org/officeDocument/2006/relationships".equals(namespace)) {
			return "r";
		} else if ("http://schemas.openxmlformats.org/officeDocument/2006/math".equals(namespace)) {
			return "m";
		} else if ("urn:schemas-microsoft-com:vml".equals(namespace)) {
			return "v";
		} else if ("http://schemas.microsoft.com/office/word/2010/wordprocessingDrawing".equals(namespace)) {
			return "wp14";
		} else if ("http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing".equals(namespace)) {
			return "wp";
		} else if ("urn:schemas-microsoft-com:office:word".equals(namespace)) {
			return "w10";
		} else if ("http://schemas.openxmlformats.org/wordprocessingml/2006/main".equals(namespace)) {
			return "w";
		} else if ("http://schemas.microsoft.com/office/word/2010/wordml".equals(namespace)) {
			return "w14";
		} else if ("http://schemas.microsoft.com/office/word/2010/wordprocessingGroup".equals(namespace)) {
			return "wpg";
		} else if ("http://schemas.microsoft.com/office/word/2010/wordprocessingInk".equals(namespace)) {
			return "wpi";
		} else if ("http://schemas.microsoft.com/office/word/2006/wordml".equals(namespace)) {
			return "wne";
		} else if ("http://schemas.microsoft.com/office/word/2010/wordprocessingShape".equals(namespace)) {
			return "wps";
		}
		return null;
	}

	/** {@inheritDoc} */
	public Iterator<String> getPrefixes(final String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}
