package io.vertigo.dynamo.persistence;

import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.dynamock.domain.car.Car;
import io.vertigo.kernel.component.ComponentInitializer;

/**
 * Initialisation des listes de références.
 * 
 * @author jmforhan
 * @version $Id: PersistenceManagerInitializer.java,v 1.3 2014/01/20 17:52:40 pchretien Exp $
 */
public class PersistenceManagerInitializer implements ComponentInitializer<PersistenceManager> {

	/** {@inheritDoc} */
	public void init(final PersistenceManager persistenceManager) {
		final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(Car.class);
		persistenceManager.getBrokerConfiguration().registerCacheable(dtDefinition, 3600, true);
	}
}