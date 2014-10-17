package io.vertigo.dynamock.dao.car;

import javax.inject.Inject;

import io.vertigo.core.Home;
import io.vertigo.lang.Assertion;
import io.vertigo.dynamo.persistence.PersistenceManager;
import io.vertigo.dynamo.task.TaskManager;
import io.vertigo.dynamo.task.metamodel.TaskDefinition;
import io.vertigo.dynamo.task.model.Task;
import io.vertigo.dynamo.task.model.TaskBuilder;
import io.vertigo.dynamo.task.model.TaskResult;
import io.vertigo.dynamo.impl.persistence.util.DAOBroker;

/**
 * DAO : Accès à un object (DTO, DTC). 
 * CarDAO
 */
public final class CarDAO extends DAOBroker<io.vertigo.dynamock.domain.car.Car, java.lang.Long> {
	/** Liste des taches. */
	private static enum Tasks {
		/** Tache TK_LIST_CARS */
		TK_LIST_CARS,
	}

	/** Constante de paramètre de la tache DTO_CAR_IN. */
	private static final String ATTR_IN_TK_LIST_CARS_DTO_CAR_IN = "DTO_CAR_IN";

	/** Constante de paramètre de la tache DTO_CAR_OUT. */
	private static final String ATTR_OUT_TK_LIST_CARS_DTO_CAR_OUT = "DTO_CAR_OUT";

	private final TaskManager taskManager;

	/**
	 * Contructeur.
	 * @param persistenceManager Manager de persistance
	 * @param taskManager Manager de Task
	 */
	@Inject
	public CarDAO(final PersistenceManager persistenceManager, final TaskManager taskManager) {
		super(io.vertigo.dynamock.domain.car.Car.class, persistenceManager);
		Assertion.checkNotNull(taskManager);
		//---------------------------------------------------------------------
		this.taskManager = taskManager;
	}

	/**
	 * Création d'une tache.
	 * @param task Type de la tache
	 * @return Builder de la tache
	 */
	private TaskBuilder createTaskBuilder(final Tasks task) {
		final TaskDefinition taskDefinition = Home.getDefinitionSpace().resolve(task.toString(), TaskDefinition.class);
		return new TaskBuilder(taskDefinition);
	}

	/**
	 * Execute la tache TK_LIST_CARS.
	 * @param dtoCarIn io.vertigo.dynamock.domain.car.Car 
	 * @return io.vertigo.dynamock.domain.car.Car dtoCarOut
	*/
	public io.vertigo.dynamock.domain.car.Car listCars(final io.vertigo.dynamock.domain.car.Car dtoCarIn) {
		final Task task = createTaskBuilder(Tasks.TK_LIST_CARS)//
				.withValue(ATTR_IN_TK_LIST_CARS_DTO_CAR_IN, dtoCarIn)//
				.build();
		final TaskResult taskResult = taskManager.execute(task);
		return taskResult.<io.vertigo.dynamock.domain.car.Car> getValue(ATTR_OUT_TK_LIST_CARS_DTO_CAR_OUT);
	}

}
