package io.vertigo.publisher.plugins.docx;

import io.vertigo.commons.codec.Encoder;
import io.vertigo.kernel.util.StringUtil;

/**
 * Impl�mentation de l'encodage des donn�es dans un fichier ODT.
 * 
 * @author npiedeloup
 * @version $Id: DOCXValueEncoder.java,v 1.4 2014/01/24 17:59:57 pchretien Exp $
 */
public final class DOCXValueEncoder implements Encoder<String, String> {

	/** {@inheritDoc} */
	public String encode(final String toEncode) {
		if (toEncode == null) {
			return null;
		}
		final StringBuilder result = new StringBuilder(toEncode);
		StringUtil.replace(result, "&", "&amp;");
		StringUtil.replace(result, "<", "&lt;");
		StringUtil.replace(result, ">", "&gt;");
		StringUtil.replace(result, "\t", "<w:tab/>");
		StringUtil.replace(result, String.valueOf((char) 128), String.valueOf((char) 8364));

		//on remet les &#xxxx; non encod�
		final String strResult = result.toString();
		return strResult.replaceAll("&amp;#([0-9]{2,4});", "&#$1;");
	}

}
