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
		if ("wpc".equals(prefix)) {
			return "http://schemas.microsoft.com/office/word/2010/wordprocessingCanvas";
		} else if ("mc".equals(prefix)) {
			return "http://schemas.openxmlformats.org/markup-compatibility/2006";
		} else if ("o".equals(prefix)) {
			return "urn:schemas-microsoft-com:office:office";
		} else if ("r".equals(prefix)) {
			return "http://schemas.openxmlformats.org/officeDocument/2006/relationships";
		} else if ("m".equals(prefix)) {
			return "http://schemas.openxmlformats.org/officeDocument/2006/math";
		} else if ("v".equals(prefix)) {
			return "urn:schemas-microsoft-com:vml";
		} else if ("wp14".equals(prefix)) {
			return "http://schemas.microsoft.com/office/word/2010/wordprocessingDrawing";
		} else if ("wp".equals(prefix)) {
			return "http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing";
		} else if ("w10".equals(prefix)) {
			return "urn:schemas-microsoft-com:office:word";
		} else if ("w".equals(prefix)) {
			return "http://schemas.openxmlformats.org/wordprocessingml/2006/main";
		} else if ("w14".equals(prefix)) {
			return "http://schemas.microsoft.com/office/word/2010/wordml";
		} else if ("wpg".equals(prefix)) {
			return "http://schemas.microsoft.com/office/word/2010/wordprocessingGroup";
		} else if ("wpi".equals(prefix)) {
			return "http://schemas.microsoft.com/office/word/2010/wordprocessingInk";
		} else if ("wne".equals(prefix)) {
			return "http://schemas.microsoft.com/office/word/2006/wordml";
		} else if ("wps".equals(prefix)) {
			return "http://schemas.microsoft.com/office/word/2010/wordprocessingShape";
		}
		return XMLConstants.DEFAULT_NS_PREFIX;
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
