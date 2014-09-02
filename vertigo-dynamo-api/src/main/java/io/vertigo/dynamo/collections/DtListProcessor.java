package io.vertigo.dynamo.collections;

import io.vertigo.core.lang.Option;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;

import java.io.Serializable;
import java.util.Collection;

public interface DtListProcessor {
	/**
	 * Création d'un tri de colonne.
	 * @param fieldName Nom du champ concerné par le tri
	 * @param desc Si tri descendant
	 * @param nullLast Si les objets Null sont en derniers
	 * @param ignoreCase Si on ignore la casse
	 * @return Etat du tri
	 * @param <D> Type du DtObject
	 */
	DtListProcessor sort(final String fieldName, final boolean desc, final boolean nullLast, final boolean ignoreCase);

	//=======================FILTER============================================
	/**
	 * Filtre une DTC par recherche plein text, ne modifie pas la collection d'origine.
	 * @param <D> Type du DtObject
	 * @param keywords Liste de Mot-clé recherchés séparés par espace(préfix d'un mot)
	 * @param maxRows Nombre max de lignes retournées
	 * @param searchedFields Liste des champs sur lesquel porte la recherche  (nullable : tous)
	 * @return Collection filtrée
	 */
	DtListProcessor filter(final String keywords, final int maxRows, final Collection<DtField> searchedFields);

	/**
	 * Constructeur d'un filtre champ = valeur.
	 * @param fieldName Nom du champ
	 * @param value Valeur
	 * @return Filtre
	 * @param <D> Type du DtObject
	 */
	DtListProcessor filterByValue(final String fieldName, final Serializable value);

	/**
	 * Constructeur d'un filtre de range.
	 * @param fieldName Nom du champ
	 * @param min Valeur minimale
	 * @param max Valeur maximale
	 * @return Filtre
	 * @param <D> Type du DtObject
	 * @param <C> Type des bornes
	 */
	<C extends Comparable<?>> DtListProcessor filterByRange(final String fieldName, final Option<C> min, final Option<C> max);

	/**
	 * Constructeur de la function de filtrage à partir d'un filtre de liste.
	 * 
	 * @param listFilter Filtre de liste
	 * @return Function de filtrage
	 * @param <D> Type du DtObject
	 */
	DtListProcessor filter(final ListFilter listFilter);

	//=======================SUB LIST==========================================
	/**
	 * Sous Liste d'une DTC, ne modifie pas la collection d'origine.
	 * @param start Indexe de début (Inclus)
	 * @param end Indexe de fin (Exclus)
	 * @return Collection filtrée
	 * @param <D> Type du DtObject
	 */
	DtListProcessor filterSubList(final int start, final int end);

	<D extends DtObject> DtList<D> apply(final DtList<D> input);
}
