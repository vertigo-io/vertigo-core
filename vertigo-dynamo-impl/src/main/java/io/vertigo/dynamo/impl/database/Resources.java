package io.vertigo.dynamo.impl.database;

import io.vertigo.kernel.lang.MessageKey;

/**
 * 
 * @author jmforhan
 */
public enum Resources implements MessageKey {
	/**
	 * Value to big for field in database.
	 */
	DYNAMO_SQL_CONSTRAINT_TOO_BIG_VALUE,

	/**
	 * Impossible to delete entry due to referential constraint.
	 */
	DYNAMO_SQL_CONSTRAINT_IMPOSSIBLE_TO_DELETE,

	/**
	 * Unicity constraint problem.
	 */
	DYNAMO_SQL_CONSTRAINT_ALREADY_REGISTRED,
}
