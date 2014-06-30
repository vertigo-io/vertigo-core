package io.vertigo.dynamo.export;

import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.MessageText;

/**
 * D�finition d'une colonne � exporter.
 *
 * @author pchretien, npiedeloup
 * @version $Id: ExportField.java,v 1.4 2014/01/20 17:49:10 pchretien Exp $
 */
public class ExportField {
	private final DtField dtField;
	private MessageText label;

	/**
	 * Constructeur.
	 * @param dtField DtField
	 */
	public ExportField(final DtField dtField) {
		this.dtField = dtField;
	}

	/**
	 * @return DtField
	 */
	public final DtField getDtField() {
		return dtField;
	}

	/**
	 * @return Label du dtField
	 */
	public final MessageText getLabel() {
		//Selon que le label est surcharg� ou non 
		return label != null ? label : dtField.getLabel();
	}

	//--------------------------------------------------------------------------

	/**
	 * Gestion d'un libell� surchargeant celui du DT.
	 * @param label de la colonne
	 */
	public final void setLabel(final MessageText label) {
		Assertion.checkNotNull(label);
		//---------------------------------------------------------------------
		this.label = label;
	}

}
