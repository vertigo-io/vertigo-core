package io.vertigo.dynamo.search;

import io.vertigo.dynamo.collections.ListFilter;
import io.vertigo.dynamo.collections.facet.model.FacetedQuery;
import io.vertigo.dynamo.collections.facet.model.FacetedQueryResult;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.search.metamodel.IndexDefinition;
import io.vertigo.dynamo.search.model.Index;
import io.vertigo.dynamo.search.model.SearchQuery;
import io.vertigo.kernel.component.Plugin;

import java.util.Collection;

/**
 * Plugin offrant des services de recherche.
 *  
 * @author pchretien
 * @version $Id: SearchServicesPlugin.java,v 1.5 2014/01/28 18:53:45 pchretien Exp $
 */
public interface SearchServicesPlugin extends Plugin {

	/**
	 * Enregistre un resolver de nom, entre ceux du DT et ceux du schéma Solr.
	 * @param indexDefinition Type de l'index
	 * @param indexFieldNameResolver Resolver de nom de champs DT/Solr
	 */
	void registerIndexFieldNameResolver(IndexDefinition indexDefinition, IndexFieldNameResolver indexFieldNameResolver);

	/**
	 * Ajout de plusieurs ressources à l'index.
	 * Si les éléments étaient déjà dans l'index ils sont remplacés.
	 * @param <I> Type de l'objet contenant les champs à indexer
	 * @param <R> Type de l'objet resultant de la recherche
	 * @param indexDefinition Type de l'index
	 * @param indexCollection Liste des objets à pousser dans l'index (I + R)
	 */
	<I extends DtObject, R extends DtObject> void putAll(IndexDefinition indexDefinition, Collection<Index<I, R>> indexCollection);

	/**
	 * Ajout d'une ressource à l'index.
	 * Si l'élément était déjà dans l'index il est remplacé.
	 * @param <I> Type de l'objet contenant les champs à indexer
	 * @param <R> Type de l'objet resultant de la recherche
	 * @param indexDefinition Type de l'index
	 * @param index Objet à pousser dans l'index (I + R)
	 */
	<I extends DtObject, R extends DtObject> void put(IndexDefinition indexDefinition, Index<I, R> index);

	/**
	 * Récupération du résultat issu d'une requête.
	 * @param searchQuery critères initiaux
	 * @param facetedQuery critères de filtrage  
	 * @param <R> Type de l'objet resultant de la recherche
	 * @return Résultat correspondant à la requête
	 */
	<R extends DtObject> FacetedQueryResult<R, SearchQuery> loadList(final SearchQuery searchQuery, final FacetedQuery facetedQuery);

	/**
	 * @param indexDefinition  Type de l'index
	 * @return Nombre de document indexés
	 */
	long count(IndexDefinition indexDefinition);

	/**
	 * Suppression d'une ressource de l'index.
	 * @param indexDefinition Type de l'index
	 * @param uri URI de la ressource à supprimer
	 */
	void remove(IndexDefinition indexDefinition, final URI uri);

	/** 
	 * Suppression des données correspondant à un filtre.
	 * @param indexDefinition Type de l'index
	 * @param listFilter Filtre des éléments à supprimer 
	 */
	void remove(IndexDefinition indexDefinition, final ListFilter listFilter);
}
