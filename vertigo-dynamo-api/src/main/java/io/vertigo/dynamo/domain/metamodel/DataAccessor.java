package io.vertigo.dynamo.domain.metamodel;

import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.util.BeanUtil;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.util.StringUtil;

/**
 * Permet d'accéder aux données d'un objet par son champ.
 * - Soit l'accès est dynamique
 * 	alors l'objet doit implémenter l'interface Dynamic
 * - Soit l'accès est statique 
 *  alors l'objet doit posséder les méthodes (setter et getter) en concordance avec le nom du champ. 
 * 
 * @author  pchretien
 * @version $Id: DataAccessor.java,v 1.3 2013/10/22 12:25:18 pchretien Exp $
 */
public final class DataAccessor {
	private final DtField dtField;
	private final String ccFieldName;

	DataAccessor(final DtField dtField) {
		Assertion.checkNotNull(dtField);
		//---------------------------------------------------------------------
		this.dtField = dtField;
		ccFieldName = StringUtil.constToCamelCase(dtField.getName(), false);
	}

	/**
	 * Setter Générique.
	 * Garantit que la valeur passée est conforme
	 *  - au type enregistré pour le champ
	 *  - les contraintes ne sont pas vérifiées.
	 *
	 * @param value Object
	 */
	public void setValue(final DtObject dto, final Object value) {
		//On vérifie le type java de l'objet. 
		dtField.getDomain().getDataType().checkValue(value);
		// -------------------------------------------------------------------------
		if (dtField.isDynamic()) {
			((Dynamic) dto).setValue(dtField, value);
		} else {
			//Dans le cas d'un champ statique
			BeanUtil.setValue(dto, ccFieldName, value);
		}
	}

	/**
	 * Getter générique.
	 * Garantit que la valeur retournée est conforme
	 *  - au type enregistré pour le champ
	 *
	 *  Attention : en mode BEAN cette méthode lance une erreur
	 * si il existe une seule erreur sur le champ concerné !!
	 *
	 * @return valeur non typée
	 */
	public Object getValue(final DtObject dto) {
		if (dtField.isDynamic()) {
			return ((Dynamic) dto).getValue(dtField);
		}
		//Dans le cas d'un champ statique
		return BeanUtil.getValue(dto, ccFieldName);
	}
}
