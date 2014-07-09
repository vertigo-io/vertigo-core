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
