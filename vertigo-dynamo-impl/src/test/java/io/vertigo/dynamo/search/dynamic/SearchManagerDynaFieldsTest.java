package io.vertigo.dynamo.search.dynamic;

import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.metamodel.DtProperty;
import io.vertigo.dynamo.search.AbstractSearchManagerTest;
import io.vertigo.dynamo.search.IndexFieldNameResolver;

import java.util.HashMap;
import java.util.Map;

/**
 * @author  npiedeloup
 * @version $Id: SearchManagerDynaFieldsTest.java,v 1.2 2014/01/20 17:48:43 pchretien Exp $
 */
public final class SearchManagerDynaFieldsTest extends AbstractSearchManagerTest {
	//Index
	private static final String IDX_DYNA_CAR = "IDX_DYNA_CAR";

	/**{@inheritDoc}*/
	@Override
	protected void doSetUp() {
		init(IDX_DYNA_CAR);
		final Map<String, String> indexFieldsMap = new HashMap<>();
		for (final DtField dtField : carIndexDefinition.getIndexDtDefinition().getFields()) {
			String indexType = dtField.getDomain().getProperties().getValue(DtProperty.INDEX_TYPE);
			if (indexType == null) {
				indexType = dtField.getDomain().getDataType().name().toLowerCase();
			}
			indexFieldsMap.put(dtField.getName(), dtField.getName() + "_DYN" + indexType);
		}
		searchManager.getSearchServices().registerIndexFieldNameResolver(carIndexDefinition, new IndexFieldNameResolver(indexFieldsMap));
	}
}
