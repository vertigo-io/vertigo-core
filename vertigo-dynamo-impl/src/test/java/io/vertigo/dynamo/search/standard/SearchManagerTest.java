package io.vertigo.dynamo.search.standard;

import io.vertigo.dynamo.search.AbstractSearchManagerTest;

/**
 * @author  npiedeloup
 * @version $Id: SearchManagerTest.java,v 1.1 2013/07/11 14:31:25 npiedeloup Exp $
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
