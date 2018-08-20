package io.vertigo.studio.plugins.mda.task.test;

import java.util.List;

import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;

/**
 *
 * Dummy values generator
 * @author mlaroche
 *
 */
public interface TaskTestDummyGenerator {

	/**
	 * Creates a dummy value for the specified type
	 * @param type class of the wanted object
	 * @param <T> class of the wanted object
	 * @return dummy value
	 */
	public <T> T dum(final Class<T> type);

	/**
	 * Creates a list of dummy values for the specified type
	 * @param clazz class of the wanted object
	 * @param <T> class of the wanted object
	 * @return dummy values as List
	 */
	public <T> List<T> dumList(final Class<T> clazz);

	/**
	 * Creates a dtList of dummy values for the specified type
	 * @param dtoClass class of the wanted object
	 * @param <D> class of the wanted object
	 * @return dummy values as DtList
	 */
	public <D extends DtObject> DtList<D> dumDtList(final Class<D> dtoClass);

	/**
	 * Creates a dummy dtObject for the specified type as new (no pk)
	 * @param dtoClass class of the wanted object
	 * @param <D> class of the wanted object
	 * @return dummy value
	 */
	public <D extends DtObject> D dumNew(final Class<D> dtoClass);

}
