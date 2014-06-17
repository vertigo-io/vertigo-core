package io.vertigo.dynamo.impl.database.vendor.core;

import java.sql.SQLException;

/**
 * Exception si un Blob possède une taille trop importante.
 * @author pchretien
 */
final class OffLimitsException extends SQLException {
	private static final long serialVersionUID = 1L;

	OffLimitsException(final long maxLength) {
		super("BLOB trop gros (limite:" + maxLength + "o)");
	}
}
