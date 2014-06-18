package io.vertigo.dynamo.plugins.persistence;

import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtListURIAll;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.persistence.Criteria;
import io.vertigo.dynamo.persistence.StorePlugin;

/**
 * Class abstraite des Stores de données static et immutable.
 * @author npiedeloup
 */
public abstract class AbstractStaticStorePlugin implements StorePlugin {

	/** {@inheritDoc} */
	@Override
	public int count(final DtDefinition dtDefinition) {
		return loadList(new DtListURIAll(dtDefinition)).size();
	}

	/** {@inheritDoc} */
	@Override
	@Deprecated
	public <D extends DtObject> DtList<D> loadList(final DtDefinition dtDefinition, final Criteria<D> criteria, final Integer maxRows) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc} */
	@Override
	public void put(final DtObject dto) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc} */
	@Override
	public void remove(final URI<? extends DtObject> uri) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc} */
	@Override
	public void merge(final DtObject dto) {
		throw new UnsupportedOperationException();
	}

}
