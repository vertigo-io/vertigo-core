package io.vertigo.dynamo.plugins.search.elasticsearch;

import io.vertigo.dynamo.domain.metamodel.Domain;
import io.vertigo.dynamo.domain.metamodel.DtProperty;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Option;

final class IndexType {
	private static final String INDEX_STORED = "stored";
	private static final String INDEX_NOT_STORED = "notStored";

	private final String indexAnalyzer;
	private final String indexDataType;
	private final boolean indexStored;

	// par convention l'indexType du domain => l'analyzer de l'index
	// L'indexType peut-être compléter pour préciser le type si différente de string avec le séparateur :

	static Option<IndexType> readIndexType(final Domain domain) {
		final String indexType = domain.getProperties().getValue(DtProperty.INDEX_TYPE);
		if (indexType == null) {
			return Option.none();
		}
		return Option.some(new IndexType(indexType, domain));
	}

	private IndexType(final String indexType, final Domain domain) {
		Assertion.checkNotNull(indexType);
		//-----
		checkIndexType(indexType, domain);
		// par convention l'indexType du domain => l'analyzer de l'index
		// L'indexType peut-être compléter pour préciser le type si différente de string avec le séparateur :
		final String[] indexTypeArray = indexType.split(":", 3);
		indexAnalyzer = indexTypeArray[0]; //le premier est toujours l'analyzer
		//le deuxième est optionnel et soit indexDataType, soit le indexStored
		final String secondParam = indexTypeArray.length >= 2 ? indexTypeArray[1] : "string";
		if (INDEX_STORED.equals(secondParam) || INDEX_NOT_STORED.equals(secondParam)) {
			indexDataType = "string";
			indexStored = INDEX_STORED.equals(secondParam);
		} else {
			indexDataType = secondParam;
			//le troisième est optionnel et est le indexStored
			final String thirdParam = indexTypeArray.length == 3 ? indexTypeArray[2] : INDEX_STORED;
			Assertion.checkArgument(thirdParam.equals(INDEX_STORED) || thirdParam.equals(INDEX_NOT_STORED),
					"indexType ({0}) should respect this usage : indexType : \"myAnalyzer{:myDataType}{:stored|notStored\"}", indexType);
			indexStored = INDEX_STORED.equals(thirdParam);
		}
	}

	private void checkIndexType(final String indexType, final Domain domain) {
		// On peut préciser pour chaque domaine le type d'indexation
		// Calcul automatique  par default.
		switch (domain.getDataType()) {
			case Boolean: // native
			case Date: // native
			case Double: // native
			case Integer: // native
			case Long: // native
				break;
			case String:
			case BigDecimal:
				if (indexType == null) {
					throw new IllegalArgumentException("## Précisez la valeur \"indexType\" dans le domain [" + domain + "].");
				}
				break;
			case DataStream: // IllegalArgumentException
			case DtObject: // IllegalArgumentException
			case DtList: // IllegalArgumentException
			default: // IllegalArgumentException
				throw new IllegalArgumentException("Type de donnée non pris en charge pour l'indexation [" + domain + "].");
		}
	}

	public String getIndexAnalyzer() {
		return indexAnalyzer;
	}

	public String getIndexDataType() {
		return indexDataType;
	}

	public boolean isIndexStored() {
		return indexStored;
	}
}
