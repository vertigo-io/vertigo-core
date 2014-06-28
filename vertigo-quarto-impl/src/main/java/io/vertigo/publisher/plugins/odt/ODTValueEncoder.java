package io.vertigo.publisher.plugins.odt;

import io.vertigo.commons.codec.Encoder;
import io.vertigo.kernel.util.StringUtil;

/**
 * Impl�mentation de l'encodage des donn�es dans un fichier ODT.
 * 
 * @author npiedeloup
 * @version $Id: ODTValueEncoder.java,v 1.4 2014/01/24 17:59:57 pchretien Exp $
 */
public final class ODTValueEncoder implements Encoder<String, String> {

	/** {@inheritDoc} */
	public String encode(final String toEncode) {
		if (toEncode == null) {
			return null;
		}
		final StringBuilder result = new StringBuilder(toEncode);
		StringUtil.replace(result, "&", "&amp;");
		StringUtil.replace(result, "<", "&lt;");
		StringUtil.replace(result, ">", "&gt;");
		StringUtil.replace(result, "\"", "&quot;");
		StringUtil.replace(result, "\'", "&apos;");
		StringUtil.replace(result, "\n", "<text:line-break/>");
		StringUtil.replace(result, "\t", "<text:tab/>");
		StringUtil.replace(result, String.valueOf((char) 128), String.valueOf((char) 8364));

		//on remet les &#xxxx; non encod�
		final String strResult = result.toString();
		return strResult.replaceAll("&amp;#([0-9]{2,4});", "&#$1;");
	}

}
