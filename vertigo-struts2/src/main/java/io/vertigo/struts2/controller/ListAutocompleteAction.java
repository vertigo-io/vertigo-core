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
package io.vertigo.struts2.controller;

import io.vertigo.dynamo.collections.CollectionsManager;
import io.vertigo.dynamo.collections.DtListProcessor;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.transaction.KTransactionManager;
import io.vertigo.dynamo.transaction.KTransactionWritable;
import io.vertigo.lang.MessageText;
import io.vertigo.lang.VUserException;
import io.vertigo.struts2.core.AbstractActionSupport;
import io.vertigo.struts2.core.ContextRef;
import io.vertigo.struts2.core.UiList;
import io.vertigo.util.StringUtil;

import java.util.Collection;
import java.util.Collections;

import javax.inject.Inject;

/**
 * Service web de l'autocomplete des listes.
 * @author npiedeloup
 */
public final class ListAutocompleteAction extends AbstractActionSupport {

	private static final long serialVersionUID = -488467479129486060L;

	private final ContextRef<String> termRef = new ContextRef<>("term", String.class, this);
	private final ContextRef<String> listRef = new ContextRef<>("list", String.class, this);
	private final ContextRef<String> listKeyRef = new ContextRef<>("listKey", String.class, this);
	private final ContextRef<String> listValueRef = new ContextRef<>("listValue", String.class, this);
	@Inject
	private CollectionsManager collectionsManager;
	@Inject
	private KTransactionManager transactionManager; //used for search in linked masterdatalist

	/** {@inheritDoc} */
	@Override
	protected void initContext() {
		// rien
	}

	/**
	 * Ajoute dans la response le json de la recherche.
	 * @return Outcome de la requete Ajax.
	 */
	public <D extends DtObject> String searchFullText() {
		final String searchString = termRef.get();
		final Object contextList = getModel().get(listRef.get());
		final DtList<D> list;
		if (contextList instanceof UiList) {
			list = ((UiList<D>) contextList).flush();
		} else {
			throw new VUserException(new MessageText("La liste n'est pas du bon type {0}", null, listRef.get()));
		}
		//final UiList<D> uiList = getModel().getUiList(listRef.get());
		//final DtList<D> list = uiList.validate(new UiObjectValidator<D>(), getUiMessageStack());
		//final DtList<D> list = uiList.flush(); //flush sans validator car elle ne doit pas avoir été modifiée
		final DtDefinition dtDefinition = list.getDefinition();
		final DtField keyField;
		final DtField labelField;
		if (listKeyRef.exists()) {
			keyField = dtDefinition.getField(StringUtil.camelToConstCase(listKeyRef.get()));
		} else {
			keyField = dtDefinition.getIdField().get();
		}
		if (listValueRef.exists()) {
			labelField = dtDefinition.getField(StringUtil.camelToConstCase(listValueRef.get()));
		} else {
			labelField = dtDefinition.getDisplayField().get();
		}

		final Collection<DtField> searchedFields = Collections.singletonList(labelField);
		final DtList<D> results;
		try (final KTransactionWritable transaction = transactionManager.createCurrentTransaction()) { //Open a transaction because all fields are indexed. If there is a MDL it was load too.
			final DtListProcessor fullTextFilter = collectionsManager.createDtListProcessor()//
					.filter(searchString != null ? searchString : "", 20, searchedFields);
			results = fullTextFilter.apply(list);
		}
		return createAjaxResponseBuilder() //
				.withJson(toJson(results, keyField, labelField)) //
				.send();
	}

	//	private String getSearchString() {
	//		String paramSearchString = term.get();
	//		Assertion.checkNotNull(paramSearchString, "Champs : {0} non présent dans la request", "term");
	//
	//		// On doit décoder la chaine UTF-8 qui a été récupérée en ISO-8859-1 par getParameter (tomcat récupère
	//		// par défaut la requête en ISO-8859-1. pour forcer l'UTF-8 il faut modifier le server.xml (trop risqué en cours de projet)
	//		try {
	//			paramSearchString = new String(paramSearchString.getBytes("ISO-8859-1"), "UTF-8");
	//		} catch (final UnsupportedEncodingException e) {
	//			throw new VRuntimeException("Erreur ce n'est plus un encodage UTF-8 qui est passé par l'appel Ajax", e);
	//		}
	//		return paramSearchString;
	//	}

	private static String toJson(final DtList<?> dtList, final DtField keyField, final DtField labelField) {
		//final DtField keyField = dtList.getDefinition().getField(StringUtil.camelToConstCase(listKey));
		//final DtField labelField = dtList.getDefinition().getField(StringUtil.camelToConstCase(listLabel));
		final StringBuilder sb = new StringBuilder();
		String sep = "";
		sb.append("[");
		for (final DtObject dto : dtList) {
			sb.append(sep);
			sb.append("{\"key\":");
			sb.append("\"");
			final Object keyValue = keyField.getDataAccessor().getValue(dto);
			sb.append(keyValue);
			sb.append("\",\"value\":");
			sb.append("\"");
			final String labelValue = (String) labelField.getDataAccessor().getValue(dto);
			final String labelEncoded = jsonEncode(labelValue);
			sb.append(labelEncoded);
			sb.append("\"}");
			sep = ", ";
		}
		sb.append("]");
		return sb.toString();
	}

	private static String jsonEncode(final String json) {
		String jsonEncoded = json.replaceAll("([\"\\\\])", "\\$1");// " => \" et \ => \\ (ils sont echappés avec \ devant)
		jsonEncoded = jsonEncoded.replaceAll("\n", "|");// \n => | (interdit en json)
		return jsonEncoded;
	}
}
