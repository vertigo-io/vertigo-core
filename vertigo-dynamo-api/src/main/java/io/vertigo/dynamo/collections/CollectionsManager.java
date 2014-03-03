package io.vertigo.dynamo.collections;

import io.vertigo.dynamo.Function;
import io.vertigo.dynamo.collections.facet.model.FacetedQuery;
import io.vertigo.dynamo.collections.facet.model.FacetedQueryResult;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.kernel.component.Manager;
import io.vertigo.kernel.lang.Option;

import java.io.Serializable;
import java.util.Collection;

/**
 * Gestionnaire de la manipulation des collections.
 * Tri, filtre, facettage sur les DTC. 
 * @author  pchretien
 * @version $Id: CollectionsManager.java,v 1.4 2014/01/20 17:45:23 pchretien Exp $
 */
public interface CollectionsManager extends Manager {
	/**
	 * Création d'un tri de colonne.
	 * @param fieldName Nom du champ concerné par le tri
	 * @param desc Si tri descendant
	 * @param nullLast Si les objets Null sont en derniers
	 * @param ignoreCase Si on ignore la casse
	 * @return Etat du tri 
	 * @param <D> Type du DtObject
	 */
	<D extends DtObject> DtListFunction<D> createSort(final String fieldName, final boolean desc, final boolean nullLast, final boolean ignoreCase);

	//=======================FILTER============================================
	/**
	 * Filtre une DTC par recherche plein text, ne modifie pas la collection d'origine.
	 * @param <D> Type du DtObject
	 * @param keywords Liste de Mot-clé recherchés séparés par espace(préfix d'un mot)
	 * @param maxRows Nombre max de lignes retournées
	 * @param searchedFields Liste des champs sur lesquel porte la recherche  (nullable : tous)
	 * @return Collection filtrée
	 */
	<D extends DtObject> DtListFunction<D> createFilter(final String keywords, final int maxRows, final Collection<DtField> searchedFields);

	/**
	 * Constructeur d'un filtre champ = valeur.
	 * @param fieldName Nom du champ
	 * @param value Valeur
	 * @return Filtre
	 * @param <D> Type du DtObject
	 */
	<D extends DtObject> DtListFunction<D> createFilterByValue(final String fieldName, final Serializable value);

	/**
	 * Constructeur d'un filtre champ1 = valeur1 ET champ2 = valeur2.
	 * @param fieldName1 Nom du premier champ
	 * @param value1 Valeur du premier champ
	 * @param fieldName2 Nom du deuxième champ
	 * @param value2 Valeur du deuxième champ
	 * @return Filtre
	 * @param <D> Type du DtObject
	 */
	<D extends DtObject> DtListFunction<D> createFilterByTwoValues(final String fieldName1, final Serializable value1, final String fieldName2, final Serializable value2);

	/**
	 * Constructeur d'un filtre de range.
	 * @param fieldName Nom du champ
	 * @param min Valeur minimale 
	 * @param max Valeur maximale 
	 * @return Filtre
	 * @param <D> Type du DtObject
	 * @param <C> Type des bornes
	 */
	<D extends DtObject, C extends Comparable<?>> DtListFunction<D> createFilterByRange(final String fieldName, final Option<C> min, final Option<C> max);

	/**
	 * Constructeur de la function de filtrage à partir d'un filtre de liste.
	 * 
	 * @param listFilter Filtre de liste
	 * @return Function de filtrage
	 * @param <D> Type du DtObject
	 */
	<D extends DtObject> DtListFunction<D> createFilter(final ListFilter listFilter);

	//=======================SUB LIST==========================================
	/**
	 * Sous Liste d'une DTC, ne modifie pas la collection d'origine.
	 * @param start Indexe de début (Inclus)
	 * @param end Indexe de fin (Exclus)
	 * @return Collection filtrée
	 * @param <D> Type du DtObject
	 */
	<D extends DtObject> DtListFunction<D> createFilterSubList(final int start, final int end);

	//=======================Gestion des facettes==============================
	/**
	 * Facettage d'une liste selon une requete.
	 * Le facettage s'effectue en deux temps :
	 *  - Filtrage de la liste
	 *  - Facettage proprement dit
	 * @param dtList Liste à facetter 
	 * @param facetedQuery Requete à appliquer (filtrage)
	 * @param <R> Type de l'objet de la liste
	 * @return Résultat correspondant à la requête
	 */
	<R extends DtObject> FacetedQueryResult<R, DtList<R>> facetList(final DtList<R> dtList, final FacetedQuery facetedQuery);

	//========================misc=============================================
	/**
	 * @param <E> Type d'objet
	 * @return Fonction identité : input = out
	 */
	<E> Function<E, E> createIdentity();

}
