package io.vertigo.dynamo.plugins.export.csv;

import io.vertigo.commons.codec.CodecManager;
import io.vertigo.commons.codec.Encoder;
import io.vertigo.dynamo.domain.metamodel.DataType;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.export.Export;
import io.vertigo.dynamo.export.ExportDtParametersReadable;
import io.vertigo.dynamo.export.ExportField;
import io.vertigo.dynamo.impl.export.core.ExportHelper;
import io.vertigo.kernel.lang.Assertion;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * Export avec ETAT.
 * 
 * @author pchretien, npiedeloup
 * @version $Id: CSVExporter.java,v 1.8 2014/06/26 12:31:45 npiedeloup Exp $
 */
final class CSVExporter {
	/**
	 * S�parateur csv : par d�faut ";".
	 */
	private static final String SEPARATOR = ";";

	/**
	 * Caract�re de fin de ligne
	 */
	private static final String END_LINE = "" + (char) 13 + (char) 10;

	/**
	 * Encoder CSV
	 */
	private final Encoder<String, String> csvEncoder;

	private final Map<DtField, Map<Object, String>> referenceCache = new HashMap<>();
	private final Map<DtField, Map<Object, String>> denormCache = new HashMap<>();
	private final ExportHelper exportHelper;

	/**
	 * Constructeur.
	 * @param codecManager Manager des codecs
	 * @param exportHelper Helper d'export.
	 */
	CSVExporter(final CodecManager codecManager, final ExportHelper exportHelper) {
		Assertion.checkNotNull(codecManager);
		Assertion.checkNotNull(exportHelper);
		//---------------------------------------------------------------------
		csvEncoder = codecManager.getCsvEncoder();
		this.exportHelper = exportHelper;
	}

	/**
	 * M�thode principale qui g�re l'export d'un tableau vers un fichier CVS.
	 * On ajoute le BOM UTF8 si le fichier est g�n�r� en UTF-8 pour une bonne ouverture dans Excel.
	 * @param documentParameters Param�tres du document � exporter
	 * @param out Flux de sortie
	 * @throws IOException Exception d'ecriture
	 */
	void exportData(final Export documentParameters, final OutputStream out) throws IOException {
		final Charset charset = Charset.forName("UTF-8");
		try (final Writer writer = new OutputStreamWriter(out, charset.name())) {
			// on met le BOM UTF-8 afin d'avoir des ouvertures correctes avec excel
			writer.append('\uFEFF');
			final boolean isMultiData = documentParameters.getReportDataParameters().size() > 1;
			for (final ExportDtParametersReadable resourceParams : documentParameters.getReportDataParameters()) {
				exportHeader(resourceParams, writer);
				exportData(resourceParams, writer);
				if (isMultiData) {
					writer.write("\"\"");
					writer.write(END_LINE);
				}
			}
		}
	}

	/**
	 * R�alise l'export des donn�es d'en-t�te.
	 * @param parameters de cet export
	 * @param out Le flux d'�criture des donn�es export�es.
	 * @throws IOException Exception lors de l'�criture dans le flux.
	 */
	private void exportHeader(final ExportDtParametersReadable parameters, final Writer out) throws IOException {
		final String title = parameters.getTitle();
		if (title != null) {
			out.write(encodeString(title));
			out.write(END_LINE);
		}

		String sep = "";
		for (final ExportField exportColumn : parameters.getExportFields()) {
			out.write(sep);
			out.write(encodeString(exportColumn.getLabel().getDisplay()));
			sep = SEPARATOR;
		}
		out.write(END_LINE);
	}

	/**
	 * R�alise l'export des donn�es de contenu.
	 * @param parameters de cet export
	 * @param out Le flux d'�criture des donn�es export�es.
	 * @throws IOException Exception lors de l'�criture dans le flux.
	 */
	private void exportData(final ExportDtParametersReadable parameters, final Writer out) throws IOException {
		// Parcours des DTO de la DTC
		if (parameters.hasDtObject()) {
			exportLine(parameters.getDtObject(), parameters, out);
		} else {
			for (final DtObject dto : parameters.getDtList()) {
				exportLine(dto, parameters, out);
			}
		}
	}

	private void exportLine(final DtObject dto, final ExportDtParametersReadable parameters, final Writer out) throws IOException {
		String sep = "";
		String sValue;
		for (final ExportField exportColumn : parameters.getExportFields()) {
			final DtField dtField = exportColumn.getDtField();
			out.write(sep);
			sValue = exportHelper.getText(referenceCache, denormCache, dto, exportColumn);
			// si toutes les colonnes de cette ligne sont vides,
			// on n'obtient pas une ligne correctement format�e ...
			if ("".equals(sValue)) {
				sValue = " ";
			}
			if (dtField.getDomain().getDataType() == DataType.BigDecimal) {
				out.write(encodeNumber(sValue));
			} else {
				out.write(encodeString(sValue));
			}
			sep = SEPARATOR;
		}
		out.write(END_LINE);
	}

	/**
	 * Encode la cha�ne export�e en csv.
	 *
	 * @param str La cha�ne � encoder.
	 * @return La cha�ne encod�e.
	 */
	private String encodeString(final String str) {
		return '\"' + csvEncoder.encode(str) + '\"';

	}

	/**
	 * Encode la cha�ne export�e en csv.
	 *
	 * @param str La cha�ne � encoder.
	 * @return La cha�ne encod�e.
	 */
	private String encodeNumber(final String str) {
		return encodeString(str).replace('.', ',');
	}

}
