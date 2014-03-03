package io.vertigo.dynamo.database.statement;

/**
 * Résultat d'un select.
 * @author pchretien
 */
public final class QueryResult {
	private final Object value;
	private final int sqlRowCount;

	public QueryResult(final Object value, final int sqlRowCount) {
		this.value = value; //Peut être null
		this.sqlRowCount = sqlRowCount;
	}

	/**
	 * @return Objet ou liste résultat 
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * @return Nombre de ligne lues en base.
	 */
	public int getSQLRowCount() {
		return sqlRowCount;
	}
}
