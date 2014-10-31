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
package io.vertigo.dynamo.plugins.export.ods;

/**
 * Handler d'export ODS avec ODFDOM v0.7.
 * La version 0.8.7 d'ODFDOM genere un ods qui est lu par Excel mais avec une alerte (voir ODSExporter.java v 1.6 pour compatibilité avec odfdom-java 0.8.7).
 *
 * @author oboitel, npiedeloup
 */
//final class ODSExporter {
//	private static final String CRITERE_LABEL_CELL_STYLE = "CRITERE_LABEL_CELL_STYLE";
//	private static final String CRITERE_VALUE_CELL_STYLE = "CRITERE_VALUE_CELL_STYLE";
//	private static final String CRITERE_VALUE_DATE_CELL_STYLE = "CRITERE_VALUE_DATE_CELL_STYLE";
//	private static final String CRITERE_VALUE_NUMBER_CELL_STYLE = "CRITERE_VALUE_NUMBER_CELL_STYLE";
//	private static final String LIST_HEADER_CELL_STYLE = "LIST_HEADER_CELL_STYLE";
//	private static final String LIST_ROW_CELL_STYLE = "LIST_ROW_CELL_STYLE";
//	private static final String LIST_ROW_NUMBER_CELL_STYLE = "LIST_ROW_NUMBER_CELL_STYLE";
//	private static final String LIST_ROW_DATE_CELL_STYLE = "LIST_ROW_DATE_CELL_STYLE";
//
//	private static final String FONT_ARIAL = "Arial";
//	private static final String BORDER_05_PT = "0.018cm solid #000000";
//	private static final String COLOR_GREY = "#808080";
//	private static final String BOLD_PROPERTY = "bold";
//	private static final int MAX_COLUMN_WIDTH = 50;
//
//	private static enum ExportDataType {
//		/** Type Date. */
//		DATE,
//		/** Type numérique. */
//		NUMBER,
//		/** Type chaine. */
//		STRING
//	}
//
//	private final Map<String, OdfStyle> styleMap = new HashMap<>();
//	private final Map<DtField, Map<Object, String>> referenceCache = new HashMap<>();
//	private final Map<DtField, Map<Object, String>> denormCache = new HashMap<>();
//
//	private final ExportHelper exportHelper;
//
//	ODSExporter(final ExportHelper exportHelper) {
//		Assertion.checkNotNull(exportHelper);
//		//---------------------------------------------------------------------
//		this.exportHelper = exportHelper;
//	}
//
//	/**
//	 * Méthode principale qui gère l'export d'un tableau vers un fichier ODS.
//	 *
//	 * @param documentParameters Paramètres du document à exporter
//	 * @param out Flux de sortie
//	 * @throws Exception Erreur d'export
//	 */
//	void exportData(final Export documentParameters, final OutputStream out) throws Exception {
//		final OdfSpreadsheetDocument document = OdfSpreadsheetDocument.newSpreadsheetDocument();
//
//		// On initialise le contenu du document
//		final OdfOfficeSpreadsheet officeSpreadsheet = document.getContentRoot();
//		cleanOutDocument(officeSpreadsheet);
//
//		// On initialise les style qui seront appliqué dans le document
//		final OdfFileDom contentDom = document.getContentDom();
//		initDefaultStyle(contentDom);
//
//		// Pour chaque élément à exporter
//		final List<ExportDtParametersReadable> parameterss = documentParameters.getReportDataParameters();
//		for (int i = 0; i < parametersList.size(); i++) {
//			final ExportDtParametersImpl parameters = (ExportDtParametersImpl) parametersList.get(i);
//
//			// On crée une feuille
//			//final OdfTable table = new OdfTable(contentDom);
//			final OdfTable table = (OdfTable) OdfElementFactory.newOdfElement(contentDom, TableTableElement.ELEMENT_NAME);
//
//			// On initialise le titre de la feuille (feuille récapitulant la valeur des critères ou feuille du tableau de données)
//			final String title;
//			if (parameters.hasDtObject()) {
//				//continue;
//				title = "Critères de recherche";
//			} else {
//				title = documentParameters.getTitle();
//			}
//
//			table.setTableNameAttribute(title);
//
//			// On exporte les données
//			exportData(parameters, table, contentDom);
//
//			// On ajoute la feuille créé au document
//			officeSpreadsheet.appendChild(table);
//		}
//
//		// On ecrit le fichier dans le flux de sortie
//		document.save(out);
//	}
//
//	/**
//	 * Permet de supprimer toutes les feuilles préexistantes dans un document ODS vierge.
//	 * @param sheet Document ODS
//	 */
//	private static void cleanOutDocument(final OdfOfficeSpreadsheet sheet) {
//		Node childNode;
//
//		childNode = sheet.getFirstChild();
//		while (childNode != null) {
//			sheet.removeChild(childNode);
//			childNode = sheet.getFirstChild();
//		}
//	}
//
//	/**
//	 * Retourne le style associé à une clé donnée.
//	 *
//	 * @param styleType Clé du style recherché
//	 * @return Style associé à la clé donnée en entrée
//	 */
//	private String getStyle(final String styleType) {
//		Assertion.checkArgument(styleMap.containsKey(styleType), "");
//		//---------------------------------------------------------------------
//		return styleMap.get(styleType).getStyleNameAttribute();
//	}
//
//	/**
//	 * Création des styles applicables aux cellules du document.
//	 *
//	 * @param contentDom Contenu du fichier ODS
//	 */
//	private void initDefaultStyle(final OdfFileDom contentDom) {
//		styleMap.put(CRITERE_LABEL_CELL_STYLE, createCritereLabelCellStyle(contentDom));
//		styleMap.put(CRITERE_VALUE_CELL_STYLE, createCritereValueCellStyle(contentDom, ExportDataType.STRING));
//		styleMap.put(CRITERE_VALUE_DATE_CELL_STYLE, createCritereValueCellStyle(contentDom, ExportDataType.DATE));
//		styleMap.put(CRITERE_VALUE_NUMBER_CELL_STYLE, createCritereValueCellStyle(contentDom, ExportDataType.NUMBER));
//
//		styleMap.put(LIST_HEADER_CELL_STYLE, createListHeaderCellStyle(contentDom));
//		styleMap.put(LIST_ROW_CELL_STYLE, createListRowCellStyle(contentDom, ExportDataType.STRING));
//		styleMap.put(LIST_ROW_DATE_CELL_STYLE, createListRowCellStyle(contentDom, ExportDataType.DATE));
//		styleMap.put(LIST_ROW_NUMBER_CELL_STYLE, createListRowCellStyle(contentDom, ExportDataType.NUMBER));
//	}
//
//	/**
//	 * Création du style des noms des critères de recherche.
//	 *
//	 * @param contentDom Contenu du fichier ODS
//	 * @return Style des noms des critères de recherche
//	 */
//	private static OdfStyle createCritereLabelCellStyle(final OdfFileDom contentDom) {
//		final OdfStyle style = contentDom.getAutomaticStyles().newStyle(OdfStyleFamily.TableCell);
//
//		// BOLD
//		style.setProperty(StyleTextPropertiesElement.FontWeight, BOLD_PROPERTY);
//		style.setProperty(StyleTextPropertiesElement.FontWeightAsian, BOLD_PROPERTY);
//		style.setProperty(StyleTextPropertiesElement.FontWeightComplex, BOLD_PROPERTY);
//
//		// ARIAL
//		style.setProperty(StyleTextPropertiesElement.FontName, FONT_ARIAL);
//
//		// BORDER
//		style.setProperty(StyleTableCellPropertiesElement.Border, BORDER_05_PT);
//
//		// BACKGROUND COLOR
//		style.setProperty(StyleTableCellPropertiesElement.BackgroundColor, COLOR_GREY);
//
//		return style;
//	}
//
//	/**
//	 * Création du style des valeurs des critères de recherche.
//	 *
//	 * @param contentDom Contenu du fichier ODS
//	 * @return Style des valeurs des critères de recherche
//	 */
//	private static OdfStyle createCritereValueCellStyle(final OdfFileDom contentDom, final ExportDataType type) {
//		final OdfStyle style = contentDom.getAutomaticStyles().newStyle(OdfStyleFamily.TableCell);
//
//		// ARIAL
//		style.setProperty(StyleTextPropertiesElement.FontName, FONT_ARIAL);
//
//		// BORDER
//		style.setProperty(StyleTableCellPropertiesElement.Border, BORDER_05_PT);
//
//		switch (type) {
//			case DATE:
//				// TODO : vérifier que cela marche ! Si oui le généraliser pour corriger les erreurs de Fat Code
//				// final OdfNumberDateStyle dateStyle = new OdfNumberDateStyle(contentDom, "dd/MM/yyyy", "numberDateStyle", null);
//				final OdfNumberDateStyle dateStyle = (OdfNumberDateStyle) OdfElementFactory.newOdfElement(contentDom, NumberDateStyleElement.ELEMENT_NAME);
//				dateStyle.buildFromFormat("dd/MM/yyyy");
//				dateStyle.setStyleNameAttribute("numberDateStyle");
//				contentDom.getAutomaticStyles().appendChild(dateStyle);
//				style.setStyleDataStyleNameAttribute("numberDateStyle");
//				break;
//			case NUMBER:
//				final OdfNumberStyle numberStyle = new OdfNumberStyle(contentDom, "#0.00", "numberStyle");
//				contentDom.getAutomaticStyles().appendChild(numberStyle);
//				style.setStyleDataStyleNameAttribute("numberStyle");
//				break;
//
//			default:
//				break;
//		}
//
//		return style;
//	}
//
//	/**
//	 * Création du style des entêtes de colonne.
//	 *
//	 * @param contentDom Contenu du fichier ODS
//	 * @return Style des entêtes de colonne
//	 */
//	private static OdfStyle createListHeaderCellStyle(final OdfFileDom contentDom) {
//		final OdfStyle style = contentDom.getAutomaticStyles().newStyle(OdfStyleFamily.TableCell);
//
//		// BOLD
//		style.setProperty(StyleTextPropertiesElement.FontWeight, BOLD_PROPERTY);
//		style.setProperty(StyleTextPropertiesElement.FontWeightAsian, BOLD_PROPERTY);
//		style.setProperty(StyleTextPropertiesElement.FontWeightComplex, BOLD_PROPERTY);
//
//		// ARIAL
//		style.setProperty(StyleTextPropertiesElement.FontNameComplex, FONT_ARIAL);
//
//		// ALIGN
//		style.setProperty(StyleParagraphPropertiesElement.TextAlign, "center");
//
//		// BORDER
//		style.setProperty(StyleTableCellPropertiesElement.Border, BORDER_05_PT);
//
//		// BACKGROUND COLOR
//		style.setProperty(StyleTableCellPropertiesElement.BackgroundColor, COLOR_GREY);
//
//		return style;
//	}
//
//	/**
//	 * Création du style des cellules de données.
//	 *
//	 * @param contentDom Contenu du fichier ODS
//	 * @param type {@link DataType}
//	 * @return Style des cellules de données
//	 */
//	private static OdfStyle createListRowCellStyle(final OdfFileDom contentDom, final ExportDataType type) {
//		return createCritereValueCellStyle(contentDom, type);
//	}
//
//	/**
//	 * Exporte un élément.
//	 *
//	 * @param parameters Paramètres de l'élément à exporter
//	 * @param table Feuille dans laquelle l'export se fait
//	 * @param contentDom Contenu du fichier ODS.
//	 */
//	private void exportData(final ExportDtParametersImpl parameters, final OdfTable table, final OdfFileDom contentDom) {
//		// Column width
//		final Map<Short, Double> maxWidthPerColumn = new HashMap<>();
//
//		if (parameters.hasDtObject()) {
//			// c'est un critère à afficher
//			exportObject(parameters, table, contentDom, maxWidthPerColumn);
//		} else {
//			// c'est un tableau à afficher
//			exportList(parameters, table, contentDom, maxWidthPerColumn);
//		}
//
//		setColumnWiths(table, maxWidthPerColumn);
//	}
//
//	/**
//	 * Permet de fixer la taille des colonnes en fonction de leur contenu.
//	 *
//	 * @param table Feuille en cours
//	 * @param maxWidthPerColumn Map donnant pour chaque colonne la longueur à appliquer à la colonne
//	 */
//	private static void setColumnWiths(final OdfTable table, final Map<Short, Double> maxWidthPerColumn) {
//		OdfTableColumn column;
//		final short nbColumn = (short) maxWidthPerColumn.size();
//
//		for (short col = 0; col < nbColumn; col++) {
//			final Double maxLength = maxWidthPerColumn.get(col);
//			final double usesMaxLength = Math.min(maxLength.doubleValue(), MAX_COLUMN_WIDTH);
//
//			column = table.addTableColumn();
//			column.setProperty(StyleTableColumnPropertiesElement.ColumnWidth, getcolumnlength(usesMaxLength));
//		}
//	}
//
//	/**
//	 * Convertit la taille des colonnes en cm pour open office.
//	 *
//	 * @param length Taille en pixels à convertir en centimètres
//	 * @return Chaine de caracteres indiquant la taille de la colonne
//	 */
//	private static String getcolumnlength(final double length) {
//		if (length <= 10) {
//			return "2.5cm";
//		} else if (length <= 20) {
//			return "4.5cm";
//		} else if (length <= 30) {
//			return "6.5cm";
//		} else if (length <= 40) {
//			return "8.5cm";
//		} else {
//			return "10.5cm";
//		}
//	}
//
//	/**
//	 * Exporte un élément de type liste (tableau de donnée).
//	 *
//	 * @param parameters Paramètres de l'élément à exporter
//	 * @param table Feuille dans laquelle est réalisé l'export
//	 * @param contentDom Contenu du document ODS.
//	 * @param maxWidthPerColumn Map des longueurs de colonnes
//	 */
//	private void exportList(final ExportDtParametersImpl parameters, final OdfTable table, final OdfFileDom contentDom, final Map<Short, Double> maxWidthPerColumn) {
//		// On créé la ligne d'entête
//		//final OdfTableRow headerRow = new OdfTableRow(contentDom);
//		final OdfTableRow headerRow = (OdfTableRow) OdfElementFactory.newOdfElement(contentDom, TableTableRowElement.ELEMENT_NAME);
//		short cellIndex = 0;
//		for (final ExportField exportColumn : parameters.getExportFields()) {
//			//final OdfTableCell cell = new OdfTableCell(contentDom);
//			final OdfTableCell cell = (OdfTableCell) OdfElementFactory.newOdfElement(contentDom, TableTableCellElement.ELEMENT_NAME);
//			final String label = exportColumn.getLabel().getDisplay();
//			cell.setOfficeStringValueAttribute(label);
//			cell.setOfficeValueTypeAttribute(OfficeValueTypeAttribute.Value.STRING.toString());
//			cell.setTableStyleNameAttribute(getStyle(LIST_HEADER_CELL_STYLE));
//			headerRow.appendCell(cell);
//
//			updateMaxWidthPerColumn(label, 1.2, cellIndex, maxWidthPerColumn); // +20% pour le gras
//			cellIndex++;
//		}
//		table.appendRow(headerRow);
//
//		// On insert toutes les données du tableau
//		for (final DtObject dto : parameters.getDtList()) {
//			//final OdfTableRow row = new OdfTableRow(contentDom);
//			final OdfTableRow row = (OdfTableRow) OdfElementFactory.newOdfElement(contentDom, TableTableRowElement.ELEMENT_NAME);
//			cellIndex = 0;
//			Object value;
//			for (final ExportField exportColumn : parameters.getExportFields()) {
//				final DtField field = exportColumn.getDtField();
//				//final OdfTableCell cell = new OdfTableCell(contentDom);
//				final OdfTableCell cell = (OdfTableCell) OdfElementFactory.newOdfElement(contentDom, TableTableCellElement.ELEMENT_NAME);
//
//				value = exportHelper.getValue(referenceCache, denormCache, dto, exportColumn);
//				putValueInCell(value, cell, field.getDomain().getDataType(), cellIndex, maxWidthPerColumn, LIST_ROW_CELL_STYLE, LIST_ROW_DATE_CELL_STYLE, LIST_ROW_NUMBER_CELL_STYLE);
//
//				row.appendCell(cell);
//				cellIndex++;
//			}
//			table.appendRow(row);
//		}
//	}
//
//	/**
//	 * Exporte un élément de type objet (critères de recherche).
//	 *
//	 * @param parameters Paramètres de l'élément à exporter
//	 * @param table Feuille dans laquelle est réalisé l'export
//	 * @param contentDom Contenu du document ODS.
//	 * @param maxWidthPerColumn Map des longueurs de colonnes
//	 */
//	private void exportObject(final ExportDtParametersImpl parameters, final OdfTable table, final OdfFileDom contentDom, final Map<Short, Double> maxWidthPerColumn) {
//		final DtObject dto = parameters.getDtObject();
//		Object value;
//
//		updateMaxWidthPerColumn(null, 1, (short) 1, maxWidthPerColumn);
//
//		for (final ExportField exportColumn : parameters.getExportFields()) {
//			//final OdfTableRow row = new OdfTableRow(contentDom);
//			final OdfTableRow row = (OdfTableRow) OdfElementFactory.newOdfElement(contentDom, TableTableRowElement.ELEMENT_NAME);
//
//			// On insère le libellé du critère
//			//final OdfTableCell cell = new OdfTableCell(contentDom);
//			final OdfTableCell cell = (OdfTableCell) OdfElementFactory.newOdfElement(contentDom, TableTableCellElement.ELEMENT_NAME);
//			final String label = exportColumn.getLabel().getDisplay();
//			cell.setOfficeStringValueAttribute(label);
//			cell.setOfficeValueTypeAttribute(OfficeValueTypeAttribute.Value.STRING.toString());
//			cell.setTableStyleNameAttribute(getStyle(CRITERE_LABEL_CELL_STYLE));
//			row.appendCell(cell);
//			updateMaxWidthPerColumn(label, 1.2, (short) 0, maxWidthPerColumn);
//
//			// On insère la valeur du critère
//			// TODO Revoir l'affichage de la valeur d'un critère liste de référence qui ne fonctionne pas (cgodard)
//			final DtField field = exportColumn.getDtField();
//			//final OdfTableCell valueCell = new OdfTableCell(contentDom);
//			final OdfTableCell valueCell = (OdfTableCell) OdfElementFactory.newOdfElement(contentDom, TableTableCellElement.ELEMENT_NAME);
//			value = exportHelper.getValue(referenceCache, denormCache, dto, exportColumn);
//			putValueInCell(value, valueCell, field.getDomain().getDataType(), (short) 0, maxWidthPerColumn, CRITERE_VALUE_CELL_STYLE, CRITERE_VALUE_DATE_CELL_STYLE, CRITERE_VALUE_NUMBER_CELL_STYLE);
//			row.appendCell(valueCell);
//
//			table.appendRow(row);
//		}
//	}
//
//	/**
//	 * Insère une valeur dans une cellule.
//	 *
//	 * @param value Valeur à insérer
//	 * @param cell Cellule dans laquelle insérer la valeur
//	 * @param type Type de la valeur à insérer
//	 * @param cellIndex Index de la cellule
//	 * @param maxWidthPerColumn Map des longueurs des colonnes
//	 * @param style Style associé à la cellule
//	 * @param dateStyle Style pour une date
//	 * @param numberStyle Style pour un nombre
//	 */
//	private void putValueInCell(final Object value, final OdfTableCell cell, final DataType type, final short cellIndex, final Map<Short, Double> maxWidthPerColumn, final String style, final String dateStyle, final String numberStyle) {
//		String stringValueForColumnWidth;
//		cell.setTableStyleNameAttribute(getStyle(style));
//		if (value != null) {
//			stringValueForColumnWidth = String.valueOf(value);
//			if (value instanceof String) {
//				final String stringValue = (String) value;
//				cell.setOfficeValueTypeAttribute(OfficeValueTypeAttribute.Value.STRING.toString());
//				cell.setOfficeStringValueAttribute(stringValue);
//			} else if (value instanceof Integer) {
//				final Integer integerValue = (Integer) value;
//				cell.setOfficeValueTypeAttribute(OfficeValueTypeAttribute.Value.FLOAT.toString());
//				cell.setOfficeValueAttribute(integerValue.doubleValue());
//			} else if (value instanceof Double) {
//				final Double dValue = (Double) value;
//				cell.setOfficeValueTypeAttribute(OfficeValueTypeAttribute.Value.FLOAT.toString());
//				cell.setOfficeValueAttribute(dValue);
//				stringValueForColumnWidth = String.valueOf(Math.round(dValue.doubleValue() * 100) / 100d);
//			} else if (value instanceof Long) {
//				final Long lValue = (Long) value;
//				cell.setOfficeValueTypeAttribute(OfficeValueTypeAttribute.Value.FLOAT.toString());
//				cell.setOfficeValueAttribute(lValue.doubleValue());
//			} else if (value instanceof BigDecimal) {
//				final BigDecimal bigDecimalValue = (BigDecimal) value;
//				cell.setOfficeValueTypeAttribute(OfficeValueTypeAttribute.Value.FLOAT.toString());
//				cell.setOfficeValueAttribute(bigDecimalValue.doubleValue());
//				cell.setTableStyleNameAttribute(getStyle(numberStyle));
//				stringValueForColumnWidth = String.valueOf(Math.round(bigDecimalValue.doubleValue() * 100) / 100d);
//			} else if (value instanceof Boolean) {
//				final Boolean bValue = (Boolean) value;
//				final String cellvalue = Boolean.FALSE.equals(bValue) ? "Non" : "Oui";
//				cell.setOfficeValueTypeAttribute(OfficeValueTypeAttribute.Value.STRING.toString());
//				cell.setOfficeStringValueAttribute(cellvalue);
//			} else if (value instanceof Date) {
//				final Date dateValue = (Date) value;
//
//				cell.setOfficeValueTypeAttribute(OfficeValueTypeAttribute.Value.DATE.toString());
//				cell.setOfficeDateValueAttribute(dateToString(dateValue, "yyyy-MM-dd"));
//				cell.setTableStyleNameAttribute(getStyle(dateStyle));
//
//				stringValueForColumnWidth = "DD/MM/YYYY"; // ceci ne sert que pour déterminer la taille de la cellule, on a pas besoin de la vrai valeur
//
//			} else {
//				throw new UnsupportedOperationException("Type " + type + " non géré en export Excel");
//			}
//
//			if (maxWidthPerColumn != null) {
//				updateMaxWidthPerColumn(stringValueForColumnWidth, 1, cellIndex, maxWidthPerColumn);
//			}
//		}
//	}
//
//	private static String dateToString(final Date dateValue, final String format) {
//		if (dateValue == null) {
//			return "";
//		}
//		final SimpleDateFormat formatter = new SimpleDateFormat(format);
//		return formatter.format(dateValue);
//	}
//
//	/**
//	 * Met à jour la longueur d'une colonne en fonction de son contenu.
//	 *
//	 * @param value Valeur insérée dans la colonne
//	 * @param textSizeCoeff Coefficient à appliquer sur la longueur de la valeur: longueur cellule = longueur valeur * textSizeCoeff + 2
//	 * @param cellIndex Index de la colonne
//	 * @param maxWidthPerColumn Map des longueurs des colonnes
//	 */
//	private static void updateMaxWidthPerColumn(final String value, final double textSizeCoeff, final short cellIndex, final Map<Short, Double> maxWidthPerColumn) {
//		final double newLenght = value != null ? value.length() * textSizeCoeff + 2 : 0; // +textSizeCoeff% pour les majuscules,et +2 pour les marges
//		final Double oldLenght = maxWidthPerColumn.get(Short.valueOf(cellIndex));
//		if (oldLenght == null || oldLenght.doubleValue() < newLenght) {
//			maxWidthPerColumn.put(Short.valueOf(cellIndex), Double.valueOf(newLenght));
//		}
//	}
//}
