package io.vertigo.dynamo.impl.collections.facet.model;

import io.vertigo.dynamo.collections.CollectionsManager;
import io.vertigo.dynamo.collections.ListFilter;
import io.vertigo.dynamo.collections.facet.metamodel.FacetDefinition;
import io.vertigo.dynamo.collections.facet.metamodel.FacetedQueryDefinition;
import io.vertigo.dynamo.collections.facet.model.Facet;
import io.vertigo.dynamo.collections.facet.model.FacetValue;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.MessageText;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Factory de FacetedQueryDefinition.
 * Permet de créer les définitions avant de les enregistrer dans via la registry dans le namespace.
 * @author pchretien, npiedeloup
 * @version $Id: FacetFactory.java,v 1.4 2014/01/20 17:45:43 pchretien Exp $
 */
public final class FacetFactory {
	private final CollectionsManager collectionManager;

	public FacetFactory(final CollectionsManager collectionManager) {
		Assertion.checkNotNull(collectionManager);
		//---------------------------------------------------------------------
		this.collectionManager = collectionManager;
	}

	/**
	 * Création d'une liste de facettes à partir d'une liste.
	 * @param facetedQueryDefinition Requête
	 * @param dtList Liste 
	 * @return Liste des facettes.
	 */
	public List<Facet> createFacets(final FacetedQueryDefinition facetedQueryDefinition, final DtList<?> dtList) {
		Assertion.checkNotNull(facetedQueryDefinition);
		Assertion.checkNotNull(dtList);
		//---------------------------------------------------------------------
		final List<Facet> facets = new ArrayList<>();
		//Pour chaque type de facette
		for (final FacetDefinition facetDefinition : facetedQueryDefinition.getFacetDefinitions()) {
			facets.add(createFacet(facetDefinition, dtList));
		}
		return facets;
	}

	private <D extends DtObject> DtList<D> apply(final ListFilter listFilter, final DtList<D> fullDtList) {
		//on délégue à CollectionsManager les méthodes de requête de filtrage.
		return collectionManager.<D> createFilter(listFilter)//
				.apply(fullDtList);
	}

	private Facet createFacet(final FacetDefinition facetDefinition, final DtList<?> dtList) {
		if (facetDefinition.isRangeFacet()) {
			//Cas des facettes par 'range' 
			return createFacetRange(facetDefinition, dtList);
		}
		//Cas des facettes par 'term'
		return createTermFacet(facetDefinition, dtList);
	}

	private Facet createFacetRange(final FacetDefinition facetDefinition, final DtList<?> dtList) {
		//map résultat avec le count par FacetFilter
		final Map<FacetValue, Long> facetValues = new LinkedHashMap<>();

		for (final FacetValue facetRange : facetDefinition.getFacetRanges()) {
			//Pour chaque Valeur de facette on compte le nombre d'élements.
			final DtList<?> facetFilteredList = apply(facetRange.getListFilter(), dtList);
			facetValues.put(facetRange, Long.valueOf(facetFilteredList.size()));
		}
		return new Facet(facetDefinition, facetValues);
	}

	private static Facet createTermFacet(final FacetDefinition facetDefinition, final DtList<?> dtList) {
		//Cas des facettes par Term 
		final DtField dtField = facetDefinition.getDtField();
		//on garde un index pour incrémenter le facetFilter pour chaque Term
		final Map<Object, FacetValue> facetFilterIndex = new HashMap<>();
		//et la map résultat avec le count par FacetFilter
		final Map<FacetValue, Long> facetValues = new HashMap<>();

		FacetValue facetValue;
		for (final DtObject dto : dtList) {
			final Object value = dtField.getDataAccessor().getValue(dto);
			facetValue = facetFilterIndex.get(value);
			if (facetValue == null) {
				final String valueAsString = dtField.getDomain().getFormatter().valueToString(value, dtField.getDomain().getDataType());
				final MessageText label = new MessageText(valueAsString, null);
				//on garde la syntaxe Solr pour l'instant
				final ListFilter listFilter = new ListFilter(dtField.getName() + ":\"" + valueAsString + "\"");
				facetValue = new FacetValue(listFilter, label);
				facetFilterIndex.put(value, facetValue);
				facetValues.put(facetValue, 0L);
			}
			facetValues.put(facetValue, facetValues.get(facetValue) + 1L);
		}

		//tri des facettes
		final Comparator<FacetValue> facetComparator = new Comparator<FacetValue>() {
			public int compare(final FacetValue o1, final FacetValue o2) {
				final int compareNbDoc = (int) (facetValues.get(o2) - facetValues.get(o1));
				return compareNbDoc != 0 ? compareNbDoc : o1.getLabel().getDisplay().compareToIgnoreCase(o2.getLabel().getDisplay());
			}
		};
		final Map<FacetValue, Long> sortedFacetValues = new TreeMap<>(facetComparator);
		sortedFacetValues.putAll(facetValues);
		return new Facet(facetDefinition, sortedFacetValues);
	}
}
