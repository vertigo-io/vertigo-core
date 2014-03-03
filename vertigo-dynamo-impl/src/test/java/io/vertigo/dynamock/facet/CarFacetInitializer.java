package io.vertigo.dynamock.facet;

import io.vertigo.dynamo.collections.facet.metamodel.FacetDefinition;
import io.vertigo.dynamo.collections.facet.metamodel.FacetDefinitionByRangeBuilder;
import io.vertigo.dynamo.collections.facet.metamodel.FacetedQueryDefinition;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.dynamock.domain.car.Car;
import io.vertigo.kernel.Home;
import io.vertigo.kernel.lang.MessageText;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Configuration des facettes de l'objet de test Car.
 * @author npiedeloup
 * @version $Id: CarFacetInitializer.java,v 1.3 2014/01/20 17:50:27 pchretien Exp $
 */
public final class CarFacetInitializer {
	public static final String FCT_DESCRIPTION_CAR = "FCT_DESCRIPTION_CAR";

	public static final String FCT_MAKE_CAR = "FCT_MAKE_CAR";

	public static final String FCT_YEAR_CAR = "FCT_YEAR_CAR";

	public static final String FCT_CAR_SUFFIX = "_CAR";

	//Query sans facette
	public static final String QRY_CAR = "QRY_CAR";

	//Query avec facette sur le constructeur
	public static final String QRY_CAR_FACET = "QRY_CAR_FACET";

	/**
	 * Initialise les facettes de l'objet de test Car.
	 * Les Definitions : FacetedQueryDefinition et FacetDefinition doivent avoir été enregistrées.
	 */
	public static void initCarFacet() {
		final DtDefinition carDefinition = DtObjectUtil.findDtDefinition(Car.class);

		//On ajoute les types de requêtes par index
		FacetedQueryDefinition carQueryDefinition;
		//Le premier type de requête est simple, sans facette
		carQueryDefinition = createCarQueryDefinition();
		Home.getDefinitionSpace().put(carQueryDefinition, FacetedQueryDefinition.class);

		//Le secondtype de requête est contient des facettes
		carQueryDefinition = createCarQueryDefinitionWithFacets(carDefinition);
		Home.getDefinitionSpace().put(carQueryDefinition, FacetedQueryDefinition.class);
	}

	/*
	 * Création d'un type de requête sans facette
	 */
	private static FacetedQueryDefinition createCarQueryDefinition() {
		final List<FacetDefinition> facetDefinitionList = Collections.<FacetDefinition> emptyList();
		return new FacetedQueryDefinition(QRY_CAR, facetDefinitionList);
	}

	/*
	 * Création d'un type derequête avec les facettes suivantes
	 * - Par marque (keyword)
	 * - Par année  (range)
	 * - Par kilométrage (range)
	 * - Par term sur description
	 */
	private static FacetedQueryDefinition createCarQueryDefinitionWithFacets(final DtDefinition carDefinition) {
		final List<FacetDefinition> facetDefinitions = new ArrayList<>();

		//Facette par description
		final DtField descriptionDtField = carDefinition.getField("DESCRIPTION");
		FacetDefinition facetDefinition;
		facetDefinition = FacetDefinition.createFacetDefinitionByTerm(FCT_DESCRIPTION_CAR, descriptionDtField, new MessageText("description", null));
		Home.getDefinitionSpace().put(facetDefinition, FacetDefinition.class);
		facetDefinitions.add(facetDefinition);

		//Facette par constructeur
		final DtField makeDtField = carDefinition.getField("MAKE");
		facetDefinition = FacetDefinition.createFacetDefinitionByTerm(FCT_MAKE_CAR, makeDtField, new MessageText("Par constructeur", null));
		Home.getDefinitionSpace().put(facetDefinition, FacetDefinition.class);
		facetDefinitions.add(facetDefinition);

		//Facette par range de date
		final DtField yearDtField = carDefinition.getField("YEAR");
		facetDefinition = new FacetDefinitionByRangeBuilder(FCT_YEAR_CAR, yearDtField, new MessageText("Par date", null))//
				.withFacetValue("YEAR:[* TO 2000]", "avant 2000")//
				.withFacetValue("YEAR:[2000 TO 2005]", "2000-2005")//
				.withFacetValue("YEAR:[2005 TO *]", "après 2005")//
				.build();

		Home.getDefinitionSpace().put(facetDefinition, FacetDefinition.class);
		facetDefinitions.add(facetDefinition);

		return new FacetedQueryDefinition(QRY_CAR_FACET, facetDefinitions);
	}
}
