package io.vertigo.quarto.publisher;

/**
 * Formats de sortie support�s.
 * 
 * @author pchretien, npiedeloup
 * @version $Id: PublisherFormat.java,v 1.1 2013/07/11 13:24:48 npiedeloup Exp $
 */
public enum PublisherFormat {
	/** OpenOffice Text. */
	ODT,
	/** DOCX Text. */
	DOCX;

	/**
	 * Mimetypes des diff�rents formats g�r�s.
	 * 
	 * @return Type Mime
	 */
	public String getTypeMime() {
		switch (this) {
			case ODT:
				return "application/vnd.oasis.opendocument.text";
			case DOCX:
				return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
			default:
				throw new IllegalArgumentException("Format " + this + "non reconnu");
		}
	}
}
