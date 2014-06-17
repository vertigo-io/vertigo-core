package io.vertigo.studio.plugins.mda.domain;

import io.vertigo.dynamo.domain.metamodel.DataType;

import java.util.List;

import freemarker.template.SimpleScalar;
import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

/**
 * Méthode Freemarker 'sql'.
 * si config.setSharedVariable("sql", new TemplateMethodSqlOracle());
 * Exemple : ${sql(field.domain.dataType)}
 * TemplateMethodModel : les params sont considérés comme des String.
 * 
 * @author  dchallas
 * @version $Id: TemplateMethodSqlOracle.java,v 1.2 2014/01/20 17:47:58 pchretien Exp $
 */
public final class TemplateMethodSqlOracle implements TemplateMethodModel {

	/** {@inheritDoc}*/
	public TemplateModel exec(final List params) throws TemplateModelException {
		final String type = (String) params.get(0);
		return new SimpleScalar(getSqlType(type));
	}

	private String getSqlType(final String type) {
		final DataType dataType = DataType.valueOf(type);
		switch (dataType) {
			case BigDecimal:
				return "NUMBER(10,2)";
			case Boolean:
				return "NUMBER(1)";
			case Date:
				return "DATE";
			case Integer:
				return "NUMBER(10)";
			case Long:
				//18 parce que 
				//max = 2^63 -1 ; min = - 2^63 
				//et 2^63 =  9.22337204 × 10^18
				return "NUMBER(18)";
			case String:
				return "VARCHAR2(15)";
			case Double:
				throw new IllegalArgumentException("Type non géré : " + dataType);
			case DtList:
				throw new IllegalArgumentException("Type non géré : " + dataType);
			case DtObject:
				throw new IllegalArgumentException("Type non géré : " + dataType);
			default:
				throw new IllegalArgumentException("Type inconnu : " + dataType);
		}
	}

}
