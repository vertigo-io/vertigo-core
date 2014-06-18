package io.vertigo.dynamo.persistence;

import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.kernel.lang.Assertion;

import java.util.ArrayList;
import java.util.List;

/**
 * Groupement de Critères de type union, intersection et exclusion de groupe.
 * TODO : vérifier la profondeur des groupes et mettre une limite (genre 3)
 * @author npiedeloup
 * @param <D> Type de l'objet
 */
public final class GroupCriteria<D extends DtObject> implements Criteria<D> {
	private static final long serialVersionUID = -5842091616664522090L;

	/**
	 * Type de jointure.
	 */
	public enum JoinType {
		/**
		 * Union.
		 */
		UNION,
		/**
		 * Intersection.
		 */
		INTERSEC,
		/**
		 * Exclusion : on aura le premier groupe moins les autres.
		 */
		EXCLUDE
	}

	private final GroupCriteria<D> firstGroupCriteria;//Pour l'exclude (on prend forcément un group, pour simplifier l'API)

	private final List<FilterCriteria<D>> filterCriterias = new ArrayList<>();
	private final List<GroupCriteria<D>> groupCriterias = new ArrayList<>();
	private final JoinType joinType;

	/**
	 * Constructeur de groupe.
	 * @param joinType type de jointure des éléments du groupe
	 */
	public GroupCriteria(final JoinType joinType) {
		Assertion.checkNotNull(joinType);
		Assertion.checkArgument(JoinType.EXCLUDE != joinType, "Pour l'exclusion, vous devez précisier le premier group, ou filter dans le constructeur");
		//----------------------------------------------------------------------
		this.joinType = joinType;
		firstGroupCriteria = null;
	}

	/**
	 * Constructeur de groupe !! <b>uniquement pour l'EXCLUDE</b> !!
	 * @param joinType type de jointure des éléments du groupe (EXCLUDE uniquement)
	 * @param firstGroup Premier group
	 */
	public GroupCriteria(final JoinType joinType, final GroupCriteria<D> firstGroup) {
		Assertion.checkNotNull(joinType);
		Assertion.checkNotNull(firstGroup);
		Assertion.checkArgument(JoinType.EXCLUDE == joinType, "Préciser le group dans le constructeur, n'est permis que pour l'EXCLUDE");
		//----------------------------------------------------------------------
		this.joinType = joinType;
		firstGroupCriteria = firstGroup;
	}

	/**
	 * @param group GroupCriteria à ajouter
	 */
	public void add(final GroupCriteria<D> group) {
		Assertion.checkNotNull(group);
		//----------------------------------------------------------------------
		groupCriterias.add(group);
	}

	/**
	 * Ajout un BooleanCriteria en intersection.
	 * @param filterCriteria FilterCriteria filtre à ajouter
	 */
	public void add(final FilterCriteria<D> filterCriteria) {
		Assertion.checkNotNull(filterCriteria);
		//----------------------------------------------------------------------
		filterCriterias.add(filterCriteria);
	}

	/**
	 * @return Type de jointure (UNION, INTERSEC, EXCLUDE)
	 */
	public JoinType getJoinType() {
		return joinType;
	}

	/**
	* Critère de recherche de type filtre.
	* @return Liste des filtres.
	*/
	public List<FilterCriteria<D>> getFilterCriterias() {
		return filterCriterias;
	}

	/**
	* Critère de prefix par champs.
	* @return Map des prefixes existant.
	*/
	public List<GroupCriteria<D>> getGroupCriterias() {
		return groupCriterias;
	}

	/**
	 * <b>Uniquement pour l'EXCLUDE</b>
	 * Retourne le groupe définissant l'ensemble de départ pour les exclusions.
	 * @return Premier groupe avec lequel sera fait les EXCLUDE
	 */
	public GroupCriteria<D> getFirstGroup() {
		Assertion.checkArgument(JoinType.EXCLUDE == joinType, "La notion de premier groupe n'est définit que pour l'EXCLUDE");
		//----------------------------------------------------------------------
		return firstGroupCriteria;
	}

	/**
	 * @return Si le group est vide
	 */
	public boolean isEmpty() {
		return groupCriterias.isEmpty() && filterCriterias.isEmpty() && firstGroupCriteria == null;
	}
}
