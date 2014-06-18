package io.vertigo.dynamo.persistence;

import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.dynamock.domain.car.Car;
import io.vertigo.kernel.component.ComponentInitializer;

/**
 * Initialisation des listes de références.
 * 
 * @author jmforhan
 */
public class PersistenceManagerInitializer implements ComponentInitializer<PersistenceManager> {

	/** {@inheritDoc} */
	public void init(final PersistenceManager persistenceManager) {
		final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(Car.class);
		persistenceManager.getBrokerConfiguration().registerCacheable(dtDefinition, 3600, true);
	}
}