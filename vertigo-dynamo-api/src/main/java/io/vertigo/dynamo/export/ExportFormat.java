package io.vertigo.dynamo.export;


/**
 * Formats de sortie support�s par le manager d'�dition.
 * @author pchretien, npiedeloup
 * @version $Id: ExportFormat.java,v 1.1 2013/07/10 15:46:44 npiedeloup Exp $
 */
public enum ExportFormat {

	/**
	 * Document Excel.
	 */
	XLS("application/vnd.ms-excel"),

	/**
	 * Document RTF.
	 */
	RTF("text/rtf"),

	/**
	 * Comma Separated Value.
	 */
	CSV("application/csv.ms-excel"),

	/**
	 * Document PDF.
	 */
	PDF("application/pdf"),

	/**
	 * Document ODS.
	 */
	ODS("application/vnd.oasis.opendocument.spreadsheet");

	private final String typeMime;

	/**
	 * Constructeur.
	 * @param typeMime Type mime associ�
	 */
	private ExportFormat(final String typeMime) {
		this.typeMime = typeMime;
	}

	/**
	 * @return Type mime associ�
	 */
	public String getTypeMime() {
		return typeMime;
	}
}
