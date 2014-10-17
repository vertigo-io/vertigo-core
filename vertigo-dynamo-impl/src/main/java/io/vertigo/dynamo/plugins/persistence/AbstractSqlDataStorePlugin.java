/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidiere - BP 159 - 92357 Le Plessis Robinson Cedex - France
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.vertigo.dynamo.plugins.persistence;

import io.vertigo.core.Home;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.spaces.definiton.Definition;
import io.vertigo.core.spaces.definiton.DefinitionUtil;
import io.vertigo.dynamo.domain.metamodel.DataType;
import io.vertigo.dynamo.domain.metamodel.Domain;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.metamodel.association.AssociationNNDefinition;
import io.vertigo.dynamo.domain.metamodel.association.AssociationNode;
import io.vertigo.dynamo.domain.metamodel.association.DtListURIForAssociation;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtListURI;
import io.vertigo.dynamo.domain.model.DtListURIAll;
import io.vertigo.dynamo.domain.model.DtListURIForCriteria;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.domain.util.AssociationUtil;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.dynamo.persistence.Criteria;
import io.vertigo.dynamo.persistence.DataStorePlugin;
import io.vertigo.dynamo.persistence.FilterCriteria;
import io.vertigo.dynamo.persistence.FilterCriteriaBuilder;
import io.vertigo.dynamo.task.TaskManager;
import io.vertigo.dynamo.task.metamodel.TaskDefinition;
import io.vertigo.dynamo.task.metamodel.TaskDefinitionBuilder;
import io.vertigo.dynamo.task.model.Task;
import io.vertigo.dynamo.task.model.TaskBuilder;
import io.vertigo.dynamo.task.model.TaskEngine;
import io.vertigo.dynamo.task.model.TaskResult;
import io.vertigo.dynamox.domain.formatter.FormatterNumber;
import io.vertigo.dynamox.task.AbstractTaskEngineSQL;
import io.vertigo.dynamox.task.TaskEngineProc;
import io.vertigo.dynamox.task.TaskEngineSelect;

import java.util.HashMap;
import java.util.Map;

/**
 * Implémentation d'un Store SQL Oracle.
 * Ce store vérifie que lors de la
 * - création d'un enregistrement il y a un et un seul enregistrement créé.
 * - modification d'un enregistrement il y a un et un seul enregistrement modifié.
 * - suppression d'un enregistrement il y a un et un seul enregistrement supprimé.
 *
 * @author  pchretien
 */
public abstract class AbstractSqlDataStorePlugin implements DataStorePlugin {
	private static final FilterCriteria<?> EMPTY_FILTER_CRITERIA = new FilterCriteriaBuilder<>().build();

	private static final String DOMAIN_PREFIX = DefinitionUtil.getPrefix(Domain.class);
	private static final char SEPARATOR = Definition.SEPARATOR;

	/**
	 * Domaine à usage interne.
	 * Ce domaine n'est pas enregistré.
	 */
	private final Domain integerDomain;

	private enum TASK {
		/** Prefix de la tache SELECT.*/
		TK_SELECT,
		/** Prefix de la tache INSERT.*/
		TK_INSERT,
		/** Prefix de la tache UPDATE.*/
		TK_UPDATE,
		/** Prefix de la tache DELETE.*/
		TK_DELETE,
		/** Prefix de la tache COUNT.*/
		TK_COUNT
	}

	private final TaskManager taskManager;

	/**
	 * Constructeur.
	 * @param workManager Manager des works
	 */
	protected AbstractSqlDataStorePlugin(final TaskManager taskManager) {
		Assertion.checkNotNull(taskManager);
		//---------------------------------------------------------------------
		this.taskManager = taskManager;
		integerDomain = new Domain("DO_INTEGER_SQL", DataType.Integer, new FormatterNumber("FMT_INTEGER_SQL"));
	}

	/**
	 * Crée une tache à partir d'une définition.
	 * @param taskDefinition Definition de la tache
	 * @return Nouvelle tache
	 */
	protected static final TaskBuilder createTaskBuilder(final TaskDefinition taskDefinition) {
		return new TaskBuilder(taskDefinition);
	}

	/**
	 * @param uri Uri de l'objet
	 * @return DtDefinition associée
	 */
	protected static final DtDefinition getDtDefinition(final URI<? extends DtObject> uri) {
		return uri.getDefinition();
	}

	/**
	 * Nom de la table en fonction de la définition du DT mappé.
	 *
	 * @param dtDefinition Définition du DT mappé
	 * @return Nom de la table
	 */
	protected static final String getTableName(final DtDefinition dtDefinition) {
		return dtDefinition.getLocalName();
	}

	/** {@inheritDoc} */
	public <D extends DtObject> D load(final URI<D> uri) {
		final DtDefinition dtDefinition = getDtDefinition(uri);

		final String tableName = getTableName(dtDefinition);
		final String taskName = TASK.TK_SELECT.toString() + '_' + tableName + "_BY_URI";

		final DtField pk = dtDefinition.getIdField().get();
		final String pkFieldName = pk.getName();
		final StringBuilder request = new StringBuilder();
		request.append(" select * from ");
		request.append(tableName);
		request.append(" where ").append(pkFieldName).append(" = #").append(pkFieldName).append('#');
		postAlterLoadRequest(request);
		final TaskDefinition taskDefinition = new TaskDefinitionBuilder(taskName).//
				withEngine(TaskEngineSelect.class)//
				.withRequest(request.toString())//
				.withAttribute(pkFieldName, pk.getDomain(), true, true)
				//IN, obligatoire
				.withAttribute("dto", Home.getDefinitionSpace().resolve(DOMAIN_PREFIX + SEPARATOR + uri.<DtDefinition> getDefinition().getName() + "_DTO", Domain.class), false, false) //OUT, non obligatoire
				.build();

		final Task task = createTaskBuilder(taskDefinition)//
				.withValue(pkFieldName, uri.getKey())//
				.build();
		final TaskResult taskResult = process(task);

		return taskResult.<D> getValue("dto");
	}

	/** {@inheritDoc} */
	public final <D extends DtObject> DtList<D> loadList(final DtListURI uri) {
		// Assertion.precondition(uri instanceof DtListURIForAssociation, "cas non traité {0}", uri.toURN());
		// uri.toURN >>  java.lang.IllegalArgumentException: uri urn[dynamoimpl.persistence.utilx.DtListURIForDtCriteria]::null non serializable
		//---------------------------------------------------------------------
		final DtList<D> dtc;
		if (uri instanceof DtListURIForAssociation) {
			final DtListURIForAssociation dtListURIForAssociation = (DtListURIForAssociation) uri;
			if (dtListURIForAssociation.getAssociationDefinition().isAssociationSimpleDefinition()) {
				dtc = loadListFromSimpleAssociation(dtListURIForAssociation);
			} else {
				dtc = loadListFromNNAssociation(dtListURIForAssociation);
			}
		} else if (uri instanceof DtListURIAll) {
			//on charge toute la liste
			dtc = loadList(uri.getDtDefinition(), null, null);
		} else if (uri instanceof DtListURIForCriteria<?>) {
			//@todo : A voir
			final DtListURIForCriteria<D> dtListURIForDtCriteria = (DtListURIForCriteria<D>) uri;
			dtc = loadList(uri.getDtDefinition(), dtListURIForDtCriteria.getCriteria(), dtListURIForDtCriteria.getMaxRows());
		} else {
			throw new IllegalArgumentException("cas non traité " + uri);
		}
		dtc.setURI(uri);
		return dtc;
	}

	private <D extends DtObject> DtList<D> loadListFromNNAssociation(final DtListURIForAssociation dtcUri) {
		final DtDefinition dtDefinition = dtcUri.getDtDefinition();
		final String tableName = getTableName(dtDefinition);

		final String taskName = TASK.TK_SELECT.toString() + "_N_N_LIST_" + tableName + "_BY_URI";

		//PK de la DtList recherchée
		final String pkFieldName = dtDefinition.getIdField().get().getName();
		//FK dans la table nn correspondant à la collection recherchée. (clé de jointure ).
		final AssociationNNDefinition associationNNDefinition = dtcUri.getAssociationDefinition().castAsAssociationNNDefinition();
		final String joinTableName = associationNNDefinition.getTableName();
		final DtDefinition joinDtDefinition = AssociationUtil.getAssociationNode(associationNNDefinition, dtcUri.getRoleName()).getDtDefinition();
		final DtField joinDtField = joinDtDefinition.getIdField().get();

		//La condition s'applique sur l'autre noeud de la relation (par rapport à la collection attendue)
		final AssociationNode associationNode = AssociationUtil.getAssociationNodeTarget(associationNNDefinition, dtcUri.getRoleName());
		final DtField fkField = associationNode.getDtDefinition().getIdField().get();
		final String fkFieldName = fkField.getName();

		final StringBuilder request = new StringBuilder(" select t.* from ");
		request.append(tableName).append(" t");
		//On établit une jointure fermée entre la pk et la fk de la collection recherchée.
		request.append(" join ").append(joinTableName);
		request.append(" j on j.").append(joinDtField.getName()).append(" = t.").append(pkFieldName);
		//Condition de la recherche
		request.append(" where j.").append(fkFieldName).append(" = #").append(fkFieldName).append('#');
		postAlterLoadRequest(request);

		final TaskDefinition taskDefinition = new TaskDefinitionBuilder(taskName)//
		.withEngine(TaskEngineSelect.class)//
		.withRequest(request.toString())//
		.withAttribute(fkFieldName, fkField.getDomain(), true, true)//
		.withAttribute("dtc", Home.getDefinitionSpace().resolve(DOMAIN_PREFIX + SEPARATOR + dtDefinition.getName() + "_DTC", Domain.class), true, false)//OUT, obligatoire
		.build();

		final URI<? extends DtObject> uri = dtcUri.getSource();

		final Task task = createTaskBuilder(taskDefinition)//
				.withValue(fkFieldName, uri.getKey())//
				.build();
		final TaskResult taskResult = process(task);

		return getDtList(taskResult);
	}

	private static <D extends DtObject> DtList<D> getDtList(final TaskResult taskResult) {
		return taskResult.getValue("dtc");
	}

	private <D extends DtObject> DtList<D> loadListFromSimpleAssociation(final DtListURIForAssociation dtcUri) {
		final DtDefinition dtDefinition = dtcUri.getDtDefinition();
		final DtField fkField = dtcUri.getAssociationDefinition().castAsAssociationSimpleDefinition().getFKField();
		final Object value = dtcUri.getSource().getKey();

		final FilterCriteria<D> filterCriteria = new FilterCriteriaBuilder<D>() //
				.withFilter(fkField.getName(), value) //
				.build();
		return doLoadList(dtDefinition, filterCriteria, null);
	}

	/**
	 * Ajoute à la requete les éléments techniques nécessaire pour limiter le resultat à {maxRows}.
	 * @param separator Séparateur de la close where à utiliser
	 * @param request Buffer de la requete
	 * @param maxRows Nombre de lignes max
	 */
	protected abstract void appendMaxRows(final String separator, final StringBuilder request, final Integer maxRows);

	/** {@inheritDoc} **/
	public <D extends DtObject> DtList<D> loadList(final DtDefinition dtDefinition, final Criteria<D> criteria, final Integer maxRows) {
		Assertion.checkArgument(criteria == null || criteria instanceof FilterCriteria<?>, "Ce store ne gére que les FilterCriteria");
		//---------------------------------------------------------------------
		final FilterCriteria<D> filterCriteria = (FilterCriteria<D>) (criteria == null ? EMPTY_FILTER_CRITERIA : criteria);
		return this.doLoadList(dtDefinition, filterCriteria, maxRows);
	}

	private <D extends DtObject> DtList<D> doLoadList(final DtDefinition dtDefinition, final FilterCriteria<D> filterCriteria, final Integer maxRows) {
		Assertion.checkNotNull(dtDefinition);
		Assertion.checkNotNull(filterCriteria);
		//---------------------------------------------------------------------
		//TODO domainMap inutil, non ?
		final Map<String, Domain> domainMap = new HashMap<>();
		for (final String fieldName : filterCriteria.getFilterMap().keySet()) {
			domainMap.put(fieldName, dtDefinition.getField(fieldName).getDomain());
		}
		for (final String fieldName : filterCriteria.getPrefixMap().keySet()) {
			domainMap.put(fieldName, dtDefinition.getField(fieldName).getDomain());
		}
		//Il faudrait vérifier que les filtres portent tous sur des champs du DT, mais la tache le fera.
		//----------------------------------------------------------------------

		final String tableName = getTableName(dtDefinition);
		final String taskName = getListTaskName(tableName, filterCriteria);
		final String request = createLoadAllLikeQuery(tableName, filterCriteria, maxRows);

		final TaskDefinitionBuilder taskDefinitionBuilder = new TaskDefinitionBuilder(taskName)//
		.withEngine(TaskEngineSelect.class)//
		.withRequest(request.toString());
		//IN, obligatoire
		for (final String fieldName : filterCriteria.getFilterMap().keySet()) {
			taskDefinitionBuilder.withAttribute(fieldName, domainMap.get(fieldName), true, true);
		}
		for (final String fieldName : filterCriteria.getPrefixMap().keySet()) {
			taskDefinitionBuilder.withAttribute(fieldName, domainMap.get(fieldName), true, true);
		}
		//OUT, obligatoire
		final TaskDefinition taskDefinition = taskDefinitionBuilder//
				.withAttribute("dtc", Home.getDefinitionSpace().resolve(DOMAIN_PREFIX + SEPARATOR + dtDefinition.getName() + "_DTC", Domain.class), true, false)//
				.build();

		final TaskBuilder taskBuilder = createTaskBuilder(taskDefinition);
		for (final Map.Entry<String, Object> filterEntry : filterCriteria.getFilterMap().entrySet()) {
			taskBuilder.withValue(filterEntry.getKey(), filterEntry.getValue());
		}
		for (final Map.Entry<String, String> prefixEntry : filterCriteria.getPrefixMap().entrySet()) {
			taskBuilder.withValue(prefixEntry.getKey(), prefixEntry.getValue());
		}
		final TaskResult taskResult = process(taskBuilder.build());

		return getDtList(taskResult);
	}

	/**
	 * Exécution d'une tache de façon synchrone.
	 * @param task Tache à executer.
	 * @return TaskResult de la tache
	 */
	protected final TaskResult process(final Task task) {
		return taskManager.execute(task);
	}

	private <D extends DtObject> String createLoadAllLikeQuery(final String tableName, final FilterCriteria<D> filterCriteria, final Integer maxRows) {
		final StringBuilder request = new StringBuilder("select * from ").append(tableName);
		String sep = " where ";
		for (final String fieldName : filterCriteria.getFilterMap().keySet()) {
			request.append(sep);
			request.append(fieldName);
			if (filterCriteria.getFilterMap().get(fieldName) != null) {
				request.append(" = #").append(fieldName).append('#');
			} else {
				request.append(" is null");
			}
			sep = " and ";
		}
		for (final String fieldName : filterCriteria.getPrefixMap().keySet()) {
			request.append(sep);
			request.append(fieldName).append(" like #").append(fieldName).append('#');
			request.append(" || '%%'");
			sep = " and ";
		}
		if (maxRows != null) {
			appendMaxRows(sep, request, maxRows);
		}
		postAlterLoadRequest(request);
		return request.toString();
	}

	private static <D extends DtObject> String getListTaskName(final String tableName, final FilterCriteria<D> filter) {
		final StringBuilder sb = new StringBuilder(TASK.TK_SELECT.toString());
		sb.append("_LIST_");
		sb.append(tableName);
		//si il y a plus d'un champs : on nomme _BY_CRITERIA, sinon le nom sera trop long
		if (filter.getFilterMap().size() + filter.getPrefixMap().size() <= 1) {
			String sep = "_BY_";
			for (final String filterName : filter.getFilterMap().keySet()) {
				sb.append(sep);
				sb.append(filterName);
				sep = "_AND_";
			}
			sep = "_PREFIXED_ON_";
			for (final String filterName : filter.getPrefixMap().keySet()) {
				sb.append(sep);
				sb.append(filterName);
				sep = "_AND_";
			}
		} else {
			sb.append("_BY_CRITERIA");
		}
		String result = sb.toString();
		if (result.length() > 40) {
			final int indexOfList = result.indexOf("_LIST_");
			result = result.substring(0, indexOfList + "_LIST_".length()) + result.substring(indexOfList + "_LIST_".length() + result.length() - 40);
		}

		return result;
	}

	/**
	 * Post traitement de modification d'une request de chargement (SELECT).
	 * Ceci permet de rajouter des begin,end ou des SET de properties particulieres.
	 * TODO voir s'il ne faudrait pas des paramètres de la tache.
	 * @param request Request à mettre à jour
	 */
	protected void postAlterLoadRequest(final StringBuilder request) {
		//rien par defaut;
	}

	//==========================================================================
	//=============================== Ecriture =================================
	//==========================================================================
	/** {@inheritDoc} */
	public void put(final DtObject dto) {
		//Mode insertion ssi uri est null
		// sinon mode update.
		final boolean insert = DtObjectUtil.getId(dto) == null;
		final boolean saved = put(dto, insert);
		if (!saved) {
			throw new RuntimeException(insert ? "Aucune ligne insérée" : "Aucune ligne modifiée");
		}
	}

	/** {@inheritDoc} */
	public void merge(final DtObject dto) {
		//On fait un update
		boolean saved = put(dto, false);
		if (!saved) {
			//Si l'update ne marche pas on fait un insert
			saved = put(dto, true);
		}
		if (!saved) {
			throw new RuntimeException("Aucune ligne répliquée");
		}
	}

	/**
	 * Création de la requête SQL d'insert.
	 * @param dtDefinition Définition de DT
	 * @return Requête SQL
	 */
	protected abstract String createInsertQuery(final DtDefinition dtDefinition);

	/**
	 * Création de la requête SQL d'update.
	 * @param dtDefinition Définition de DT
	 * @return Requête SQL
	 */
	protected static final String createUpdateQuery(final DtDefinition dtDefinition) {
		final String tableName = getTableName(dtDefinition);
		final DtField pk = dtDefinition.getIdField().get();
		final StringBuilder request = new StringBuilder();
		request.append("update ").append(tableName).append(" set ");
		String separator = "";
		for (final DtField dtField : dtDefinition.getFields()) {
			//On ne met à jour que les champs persistants hormis la PK
			if (dtField.isPersistent() && dtField.getType() != DtField.FieldType.PRIMARY_KEY) {
				request.append(separator);
				request.append(dtField.getName()).append(" = #DTO.").append(dtField.getName()).append('#');
				separator = ", ";
			}
		}
		request.append(" where ").append(pk.getName()).append(" = #DTO.").append(pk.getName()).append('#');
		return request.toString();
	}

	/**
	 * @param insert Si opération de type insert
	 * @return Classe du moteur de tache à utiliser
	 */
	protected abstract Class<? extends TaskEngine> getTaskEngineClass(final boolean insert);

	/**
	 * @param dto Objet à persiter
	 * @param insert Si opération de type insert (update sinon)
	 * @return Si "1 ligne sauvée", sinon "Aucune ligne sauvée"
	 */
	protected final boolean put(final DtObject dto, final boolean insert) {
		if (insert) {
			//Pour les SGBDs ne possédant pas de système de séquence il est nécessaire de calculer la clé en amont.
			preparePrimaryKey(dto);
		}
		final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(dto);

		final String tableName = getTableName(dtDefinition);
		final String taskName = (insert ? TASK.TK_INSERT : TASK.TK_UPDATE).toString() + '_' + tableName;

		final String request = insert ? createInsertQuery(dtDefinition) : createUpdateQuery(dtDefinition);

		final TaskDefinition taskDefinition = new TaskDefinitionBuilder(taskName)//
		.withEngine(getTaskEngineClass(insert))//IN, obligatoire
		.withRequest(request)//
		.withAttribute("DTO", Home.getDefinitionSpace().resolve(DOMAIN_PREFIX + SEPARATOR + dtDefinition.getName() + "_DTO", Domain.class), true, true)//
		.withAttribute(AbstractTaskEngineSQL.SQL_ROWCOUNT, integerDomain, true, false) //OUT, obligatoire
		.build();

		/*
		 * Création de la tache.
		 */
		final Task task = createTaskBuilder(taskDefinition)//
				.withValue("DTO", dto)//
				.build();

		final TaskResult taskResult = process(task);

		final int sqlRowCount = getSqlRowCount(taskResult);
		if (sqlRowCount > 1) {
			throw new RuntimeException(insert ? "Plus de 1 ligne a été insérée" : "Plus de 1 ligne a été modifiée");
		}
		return sqlRowCount != 0; // true si "1 ligne sauvée", false si "Aucune ligne sauvée"
	}

	/**
	 * Prépare la PK si il n'y a pas de système de sequence.
	 * @param dto Objet à sauvegarder (création ou modification)
	 */
	protected void preparePrimaryKey(final DtObject dto) {
		// rien par default
	}

	/** {@inheritDoc} */
	public void remove(final URI<? extends DtObject> uri) {
		final DtDefinition dtDefinition = getDtDefinition(uri);
		final DtField pk = dtDefinition.getIdField().get();
		final String tableName = getTableName(dtDefinition);
		final String taskName = TASK.TK_DELETE.toString() + '_' + tableName;

		final String pkFieldName = pk.getName();
		final StringBuilder request = new StringBuilder();
		request.append("delete from ").append(tableName);
		request.append(" where ").append(pkFieldName).append(" = #").append(pkFieldName).append('#');

		final TaskDefinition taskDefinition = new TaskDefinitionBuilder(taskName)//
		.withEngine(TaskEngineProc.class)//
		.withRequest(request.toString())//
		.withAttribute(pkFieldName, pk.getDomain(), true, true)//IN, obligatoire
		.withAttribute(AbstractTaskEngineSQL.SQL_ROWCOUNT, integerDomain, true, false) //OUT, obligatoire
		.build();

		final Task task = createTaskBuilder(taskDefinition)//
				.withValue(pkFieldName, uri.getKey())//
				.build();

		final TaskResult taskResult = process(task);

		final int sqlRowCount = getSqlRowCount(taskResult);
		if (sqlRowCount > 1) {
			throw new RuntimeException("Plus de 1 ligne a été supprimée");
		} else if (sqlRowCount == 0) {
			throw new RuntimeException("Aucune ligne supprimée");
		}
	}

	private static int getSqlRowCount(final TaskResult taskResult) {
		return taskResult.<Integer> getValue(AbstractTaskEngineSQL.SQL_ROWCOUNT).intValue();
	}

	/** {@inheritDoc} */
	public int count(final DtDefinition dtDefinition) {
		Assertion.checkNotNull(dtDefinition);
		Assertion.checkArgument(dtDefinition.isPersistent(), "DtDefinition n'est pas persistante");
		//---------------------------------------------------------------------
		final String tableName = getTableName(dtDefinition);
		final String request = "select count(*) as count from " + tableName;

		final String taskName = TASK.TK_COUNT.toString() + '_' + tableName;

		final Domain countDomain = new Domain("DO_COUNT", DataType.DtObject, new FormatterNumber("FMT_COUNT"));

		final TaskDefinition taskDefinition = new TaskDefinitionBuilder(taskName)//
		.withEngine(TaskEngineSelect.class)//
		.withRequest(request.toString())//
		.withAttribute("dto", countDomain, true, false)//OUT, obligatoire
		.build();

		final Task task = createTaskBuilder(taskDefinition)//
				.build();
		final TaskResult taskResult = process(task);
		final DtObject dto = taskResult.getValue("dto");
		return (Integer) DtObjectUtil.findDtDefinition(dto).getField("COUNT").getDataAccessor().getValue(dto);
	}
}
