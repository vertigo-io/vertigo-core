package io.vertigo.dynamo.impl.export.core;

import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.dynamo.export.ExportDtParameters;
import io.vertigo.dynamo.export.ExportDtParametersReadable;
import io.vertigo.dynamo.export.ExportField;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.MessageText;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Implementation standard de ExportDtParameters.
 *
 * @author pchretien, npiedeloup
 * @version $Id: ExportDtParametersImpl.java,v 1.7 2014/02/27 10:23:30 pchretien Exp $
 */
public final class ExportDtParametersImpl implements ExportDtParameters, ExportDtParametersReadable {
	/**
	 * List des champs � exporter
	 */
	private final List<ExportField> exportFieldList = new ArrayList<>();

	/**
	 * Objet � exporter. 
	 * dto XOR dtc est renseign�. 
	 */
	private final DtObject dto;
	private final DtList<?> dtc;
	private final DtDefinition dtDefinition;

	private String title;

	/**
	 * Constructeur.
	 * @param dto DTO � exporter
	 */
	public ExportDtParametersImpl(final DtObject dto) {
		Assertion.checkNotNull(dto);
		//---------------------------------------------------------------------
		this.dto = dto;
		dtc = null;
		dtDefinition = DtObjectUtil.findDtDefinition(dto);
	}

	/**
	 * Constructeur.
	 * @param dtc DTC � exporter
	 */
	public ExportDtParametersImpl(final DtList<?> dtc) {
		Assertion.checkNotNull(dtc);
		//---------------------------------------------------------------------
		this.dtc = dtc;
		dto = null;
		dtDefinition = dtc.getDefinition();
	}

	// NULL
	/** {@inheritDoc} */
	public String getTitle() {
		return title;
	}

	/** {@inheritDoc} */
	public boolean hasDtObject() {
		return dto != null;
	}

	/** {@inheritDoc} */
	public DtObject getDtObject() {
		Assertion.checkNotNull(dto);
		//---------------------------------------------------------------------
		return dto;
	}

	/** {@inheritDoc} */
	public DtList<?> getDtList() {
		Assertion.checkNotNull(dtc);
		//---------------------------------------------------------------------
		return dtc;
	}

	/** {@inheritDoc} */
	public List<ExportField> getExportFields() {
		// si la liste des colonnes est vide alors par convention on les prend toutes.
		if (exportFieldList.isEmpty()) {
			final Collection<DtField> fieldCollection = dtDefinition.getFields();
			final List<ExportField> defaultExportFieldList = new ArrayList<>(fieldCollection.size());
			for (final DtField dtField : fieldCollection) {
				defaultExportFieldList.add(new ExportField(dtField));
			}
			exportFieldList.addAll(defaultExportFieldList);
		}
		return java.util.Collections.unmodifiableList(exportFieldList);
	}

	/** {@inheritDoc} */
	public void addExportField(final DtField exportfield) {
		addExportField(exportfield, null);
	}

	/** {@inheritDoc} */
	public void addExportDenormField(final DtField exportfield, final DtList<?> list, final DtField displayfield) {
		addExportDenormField(exportfield, list, displayfield, null);
	}

	/** {@inheritDoc} */
	public void addExportField(final DtField exportfield, final MessageText overridedLabel) {
		Assertion.checkNotNull(exportfield);
		// On v�rifie que la colonne est bien dans la d�finition de la DTC
		Assertion.checkArgument(dtDefinition.getFields().contains(exportfield), "Le champ " + exportfield.getName() + " n'est pas dans la liste � exporter");
		// On ne v�rifie pas que les champs ne sont plac�s qu'une fois
		// car pour des raisons diverses ils peuvent l'�tre plusieurs fois.
		// ----------------------------------------------------------------------
		final ExportField exportField = new ExportField(exportfield);
		if (overridedLabel != null) { // si on surcharge le label
			exportField.setLabel(overridedLabel);
		}
		exportFieldList.add(exportField);
	}

	/** {@inheritDoc} */
	public void addExportDenormField(final DtField exportfield, final DtList<?> list, final DtField displayfield, final MessageText overridedLabel) {
		Assertion.checkNotNull(exportfield);
		// On v�rifie que la colonne est bien dans la d�finition de la DTC
		Assertion.checkArgument(dtDefinition.getFields().contains(exportfield), "Le champ " + exportfield.getName() + " n'est pas dans la liste � exporter");
		// On ne v�rifie pas que les champs ne sont plac�s qu'une fois
		// car pour des raisons diverses ils peuvent l'�tre plusieurs fois.
		// ----------------------------------------------------------------------
		final ExportDenormField exportField = new ExportDenormField(exportfield, list, displayfield);
		if (overridedLabel != null) { // si on surcharge le label
			exportField.setLabel(overridedLabel);
		}
		exportFieldList.add(exportField);
	}

	/**
	 * @param title Titre de cet objet/liste
	 */
	public void setTitle(final String title) {
		Assertion.checkState(title == null, "Titre deja renseign�");
		Assertion.checkArgNotEmpty(title, "Titre doit �tre non vide");
		// ---------------------------------------------------------------------
		this.title = title;
	}
}
