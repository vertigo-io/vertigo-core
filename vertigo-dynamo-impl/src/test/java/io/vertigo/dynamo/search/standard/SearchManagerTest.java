package io.vertigo.dynamo.search.standard;

import io.vertigo.dynamo.search.AbstractSearchManagerTest;

/**
 * @author  npiedeloup
 */
public class SearchManagerTest extends AbstractSearchManagerTest {
	//Index
	private static final String IDX_CAR = "IDX_CAR";

	/**{@inheritDoc}*/
	@Override
	protected void doSetUp() {
		init(IDX_CAR);
	}

}
