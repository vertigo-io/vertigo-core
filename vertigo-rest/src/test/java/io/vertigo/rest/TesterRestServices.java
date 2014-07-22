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
package io.vertigo.rest;

import io.vertigo.dynamo.collections.CollectionsManager;
import io.vertigo.dynamo.collections.DtListFunction;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.dynamo.impl.collections.functions.filter.DtListChainFilter;
import io.vertigo.dynamo.impl.collections.functions.filter.DtListFilter;
import io.vertigo.dynamo.impl.collections.functions.filter.DtListRangeFilter;
import io.vertigo.dynamo.impl.collections.functions.filter.DtListValueFilter;
import io.vertigo.dynamo.impl.collections.functions.filter.FilterFunction;
import io.vertigo.kernel.exception.VUserException;
import io.vertigo.kernel.lang.MessageText;
import io.vertigo.kernel.lang.Option;
import io.vertigo.kernel.util.DateUtil;
import io.vertigo.persona.security.KSecurityManager;
import io.vertigo.rest.engine.UiContext;
import io.vertigo.rest.engine.UiListState;
import io.vertigo.rest.exception.VSecurityException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

//basé sur http://www.restapitutorial.com/lessons/httpmethods.html

public final class TesterRestServices implements RestfulService {

	@Inject
	private KSecurityManager securityManager;
	@Inject
	private CollectionsManager collectionsManager;

	/*private final enum Group {
		Friends("FRD", "Friends"), //
		Familly("FAM", "Familly"), //
		CoWorkers("CWO", "Colleagues"), Familiar("FAR", "Familiar"),
	}*/

	public static enum Honorific {
		Mr("MR_", "Mr", "Mister"), //
		Miss("MIS", "Miss", "Miss"), //
		Mrs("MRS", "Mrs", "Mrs"), //
		Ms("MS_", "Ms.", "Ms."), //
		Dr("DR_", "Dr.", "Doctor"), //
		Cpt("CAP", "Cpt", "Captain"), //
		Cch("CCH", "Cch", "Coach"), //

		Off("OFF", "Off", "Officer"), //
		Rev("REV", "Rev", "Reverend"), //
		Fth("FTH", "Fth", "Father"), //
		PhD("PHD", "PhD", "Professor"), //
		Mst("MST", "Mst", "Master"); //

		private final String code;

		//		private final String abbreviation;
		//		private final String label;

		Honorific(final String code, final String abbreviation, final String label) {
			this.code = code;
			//			this.abbreviation = abbreviation;
			//			this.label = label;
		}

		public String getCode() {
			return code;
		}
	}

	private final Map<Long, Contact> contacts = new HashMap<>();

	public TesterRestServices() throws ParseException {
		appendContact(Honorific.Mr, "Martin", "Jean", parseDate("19/05/1980"), //
				createAddress("1, rue de Rivoli", "", "Paris", "75001", "France"), //
				"jean.martin@gmail.com", "01 02 03 04 05");
		appendContact(Honorific.Miss, "Dubois", "Marie", parseDate("20/06/1981"), //
				createAddress("2, rue Beauregard", "", "Paris", "75002", "France"), //
				"marie.dubois@gmail.com", "01 13 14 15 16");
		appendContact(Honorific.Cpt, "Petit", "Philippe", parseDate("18/04/1979"), //
				createAddress("3, rue Meslay", "", "Paris", "75003", "France"), //
				"philippe.petit@gmail.com", "01 24 25 26 27");
		appendContact(Honorific.Off, "Durant", "Nathalie", parseDate("21/07/1982"), //
				createAddress("4, avenue Victoria", "", "Paris", "75004", "France"), //
				"nathalie.durant@gmail.com", "01 35 36 37 38");
		appendContact(Honorific.PhD, "Leroy", "Michel", parseDate("17/03/1978"), //
				createAddress("5, boulevard Saint-Marcel", "", "Paris", "75005", "France"), //
				"michel.leroy@gmail.com", "01 46 47 48 49");
		appendContact(Honorific.Ms, "Moreau", "Isabelle", parseDate("22/08/1983"), //
				createAddress("6, boulevard Raspail", "", "Paris", "75006", "France"), //
				"isabelle.moreau@gmail.com", "01 57 58 59 50");
		appendContact(Honorific.Rev, "Lefebvre", "Alain", parseDate("16/02/1977"), //
				createAddress("7, rue Cler", "", "Paris", "75007", "France"), //
				"alain.lefebvre@gmail.com", "01 68 69 60 61");
		appendContact(Honorific.Dr, "Garcia", "Sylvie", parseDate("23/09/1984"), //
				createAddress("8, rue de Ponthieu", "", "Paris", "75008", "France"), //
				"sylvie.garcia@gmail.com", "01 79 70 71 72");
		appendContact(Honorific.Mst, "Roux", "Patrick", parseDate("15/01/1976"), //
				createAddress("9, avenue Frochot", "", "Paris", "75009", "France"), //
				"patrick.roux@gmail.com", "01 80 81 82 83");
		appendContact(Honorific.Mrs, "Fournier", "Catherine", parseDate("24/10/1985"), //
				createAddress("10, avenue Claude Vellefaux", "", "Paris", "75010", "France"), //
				"catherine.fournier@gmail.com", "01 91 92 93 94");
	}

	private Date parseDate(final String dateStr) throws ParseException {
		return new SimpleDateFormat("dd/MM/yyyy").parse(dateStr);
	}

	private void appendContact(final Honorific honorific, final String name, final String firstName, final Date birthday, final Address address, final String email, final String... tels) {
		final long conId = contacts.size() + 1;
		final Contact contact = new Contact();
		contact.setConId(conId);
		contact.setHonorificCode(honorific.getCode());
		contact.setName(name);
		contact.setFirstName(firstName);
		contact.setBirthday(birthday);
		contact.setAddress(address);
		contact.setEmail(email);
		contact.setTels(Arrays.asList(tels));
		contacts.put(conId, contact);
		Response.status(200).build();
	}

	private Address createAddress(final String street1, final String street2, final String city, final String postalCode, final String country) {
		final Address address = new Address();
		address.setStreet1(street1);
		address.setStreet2(street2);
		address.setCity(city);
		address.setPostalCode(postalCode);
		address.setCountry(country);
		return address;
	}

	@AnonymousAccessAllowed
	@GET("/test/login")
	public void login() {
		//code 200
		securityManager.getCurrentUserSession().get().authenticate();
	}

	@SessionLess
	@AnonymousAccessAllowed
	@GET("/test/anonymousTest")
	public List<Contact> anonymousTest() {
		//offset + range ?
		//code 200
		return new ArrayList<>(contacts.values());
	}

	@GET("/test/authentifiedTest")
	public List<Contact> authentifiedTest() {
		//offset + range ?
		//code 200
		return new ArrayList<>(contacts.values());
	}

	@Doc("Use passPhrase : RtFM")
	@GET("/test/docTest/{passPhrase}")
	public List<Contact> docTest(@PathParam("passPhrase") final String passPhrase) throws VSecurityException {
		if (!"RtFM".equals(passPhrase)) {
			throw new VSecurityException("Bad passPhrase, check the doc in /catalog");
		}
		return new ArrayList<>(contacts.values());
	}

	@GET("/test/{conId}")
	public Contact testRead(@PathParam("conId") final long conId) {
		final Contact contact = contacts.get(conId);
		if (contact == null) {
			//404 ?
			throw new VUserException(new MessageText("Contact #" + conId + " unknown", null));
		}
		//200
		return contact;
	}

	@GET("/test/grantAccess/")
	@AccessTokenPublish
	public void testAccessToken() {
		//access token publish
	}

	@GET("/test/limitedAccess/{conId}")
	@AccessTokenMandatory
	public Contact testAccessToken(@PathParam("conId") final long conId) {
		return testRead(conId);
	}

	@GET("/test/oneTimeAccess/{conId}")
	@AccessTokenConsume
	public Contact testAccessTokenConsume(@PathParam("conId") final long conId) {
		return testRead(conId);
	}

	@GET("/test/filtered/{conId}")
	@ExcludedFields({ "birthday", "email" })
	@ServerSideSave
	public Contact testFilteredRead(@PathParam("conId") final long conId) {
		final Contact contact = contacts.get(conId);
		if (contact == null) {
			//404 ?
			throw new VUserException(new MessageText("Contact #" + conId + " unknown", null));
		}
		//200
		return contact;
	}

	//@POST is non-indempotent
	@POST("/test")
	public Contact testPost(//
			final @Validate({ ContactValidator.class, MandatoryPkValidator.class }) Contact contact) {
		if (contact.getConId() != null) {
			throw new VUserException(new MessageText("Contact #" + contact.getConId() + " already exist", null));
		}
		if (contact.getName() == null || contact.getName().isEmpty()) {
			throw new VUserException(new MessageText("Name is mandatory", null));
		}
		final long nextId = getNextId();
		contact.setConId(nextId);
		contacts.put(nextId, contact);
		//code 201 + location header : GET route
		return contact;
	}

	//PUT is indempotent : ID obligatoire
	@PUT("/test/{conId}")
	public Contact testUpdate(//
			final @Validate({ ContactValidator.class, MandatoryPkValidator.class }) Contact contact) {
		if (contact.getName() == null || contact.getName().isEmpty()) {
			//400
			throw new VUserException(new MessageText("Name is mandatory", null));
		}

		contacts.put(contact.getConId(), contact);
		//200
		return contact;
	}

	//PUT is indempotent : ID obligatoire
	@PUT("/test/filtered/{conId}")
	@ExcludedFields({ "conId", "name" })
	@ServerSideSave
	public Contact filteredUpdate(//
			final @Validate({ ContactValidator.class, MandatoryPkValidator.class })//
			@ServerSideRead//
			Contact contact) {
		if (contact.getName() == null || contact.getName().isEmpty()) {
			//400
			throw new VUserException(new MessageText("Name is mandatory", null));
		}

		contacts.put(contact.getConId(), contact);
		//200
		return contact;
	}

	@DELETE("/test/{conId}")
	public void delete(@PathParam("conId") final long conId) {
		if (!contacts.containsKey(conId)) {
			//404
			throw new VUserException(new MessageText("Contact #" + conId + " unknown", null));
		}
		if (conId < 5) {
			//401
			throw new VUserException(new MessageText("You don't have enought rights", null));
		}
		//200
		contacts.remove(conId);
	}

	@Doc("Test ws-rest multipart body with objects. Send a body with an object of to field : contactFrom, contactTo. Each one should be an json of Contact.")
	@POST("/test/multipart")
	public List<Contact> testMultiPartBodyObject(@InnerBodyParam("contactFrom") final Contact contactFrom, @InnerBodyParam("contactTo") final Contact contactTo) {
		final List<Contact> contacts = new ArrayList<Contact>(2);
		contacts.add(contactFrom);
		contacts.add(contactTo);
		//offset + range ?
		//code 200
		return contacts;
	}

	@Doc("Test ws-rest multipart body with primitives. Send a body with an object of to field : contactId1, contactId2. Each one should be an json of long.")
	@ServerSideSave
	@ExcludedFields({ "address", "tels" })
	@POST("/test/multipartLong")
	public DtList<Contact> testMultiPartBodyLong(@InnerBodyParam("contactId1") final long contactIdFrom, @InnerBodyParam("contactId2") final long contactIdTo) {
		final DtList<Contact> result = new DtList<>(Contact.class);
		result.add(contacts.get(contactIdFrom));
		result.add(contacts.get(contactIdTo));
		//offset + range ?
		//code 200
		return result;
	}

	@Doc("Test ws-rest returning UiContext. Send a body with an object of to field : contactId1, contactId2. Each one should be an json of long. You get partial Contacts with clientId in each one")
	@ServerSideSave
	@ExcludedFields({ "conId", "email", "birthday", "address", "tels" })
	@POST("/test/uiContext")
	public UiContext testMultiPartBody(@InnerBodyParam("contactId1") final long contactIdFrom, @InnerBodyParam("contactId2") final long contactIdTo) {
		final UiContext uiContext = new UiContext();
		uiContext.put("contactFrom", contacts.get(contactIdFrom));
		uiContext.put("contactTo", contacts.get(contactIdTo));
		uiContext.put("testLong", 12);
		uiContext.put("testString", "the String test");
		uiContext.put("testDate", DateUtil.newDate());
		uiContext.put("testEscapedString", "the EscapedString \",} test");
		//offset + range ?
		//code 200
		return uiContext;
	}

	@Doc("Test ws-rest multipart body with serverSide objects. Send a body with an object of to field : contactFrom, contactTo. Each one should be an partial json of Contact with clientId.")
	@POST("/test/multipartServerClient")
	public List<Contact> testMultiPartBodyClientId(//
			@InnerBodyParam("contactFrom") @ServerSideRead final Contact contactFrom, //
			@InnerBodyParam("contactTo") @ServerSideRead final Contact contactTo) {
		final List<Contact> contacts = new ArrayList<Contact>(2);
		contacts.add(contactFrom);
		contacts.add(contactTo);
		//offset + range ?
		//code 200
		return contacts;
	}

	@POST("/test/search")
	public List<Contact> testSearch(final ContactCriteria contact) {
		final DtListFunction<Contact> filterFunction = createDtListFunction(contact, Contact.class);
		final DtList<Contact> result = filterFunction.apply((DtList<Contact>) contacts.values());
		//offset + range ?
		//code 200
		return result;
	}

	@POST("/test/searchPagined")
	public List<Contact> testSearchServicePagined(final ContactCriteria contact, final UiListState uiListState) {
		final DtListFunction<Contact> filterFunction = createDtListFunction(contact, Contact.class);
		final DtList<Contact> result = filterFunction.apply((DtList<Contact>) contacts.values());

		//offset + range ?
		//code 200
		return applySortAndPagination(result, uiListState);
	}

	private <D extends DtObject> DtList<D> applySortAndPagination(final DtList<D> unFilteredList, final UiListState uiListState) {
		final DtListFunction<D> sortFunction = collectionsManager.createSort(uiListState.getSortFieldName(), uiListState.isSortDesc(), true, true);
		final DtListFunction<D> filterFunction = collectionsManager.createFilterSubList(uiListState.getSkip(), uiListState.getSkip() + uiListState.getTop());
		final DtList<D> sortedDtc = sortFunction.apply(filterFunction.apply(unFilteredList));
		return sortedDtc;
	}

	@AutoSortAndPagination
	@POST("/test/searchAutoPagined")
	public List<Contact> testSearchServiceAutoPagined(final ContactCriteria contact) {
		final DtListFunction<Contact> filterFunction = createDtListFunction(contact, Contact.class);
		final DtList<Contact> result = filterFunction.apply((DtList<Contact>) contacts.values());
		//offset + range ?
		//code 200
		return result;
	}

	/*@GET("/test/searchFacet")
	public FacetedQueryResult<DtObject, ContactCriteria> testSearchServiceFaceted(final ContactCriteria contact) {
		final DtListFunction<Contact> filterFunction = createDtListFunction(contact, Contact.class);
		final DtList<Contact> result = filterFunction.apply((DtList<Contact>) contacts.values());
		
		//offset + range ?
		//code 200
		return result;
	}*/

	private static <C extends DtObject, O extends DtObject> DtListFunction<O> createDtListFunction(final C criteria, final Class<O> resultClass) {
		final List<DtListFilter<O>> filters = new ArrayList<>();
		final DtDefinition criteriaDefinition = DtObjectUtil.findDtDefinition(criteria);
		final DtDefinition resultDefinition = DtObjectUtil.findDtDefinition(resultClass);
		final Set<String> alreadyAddedField = new HashSet<>();
		for (final DtField field : criteriaDefinition.getFields()) {
			final String fieldName = field.getName();
			if (!alreadyAddedField.contains(fieldName)) { //when we consume two fields at once (min;max)
				final Object value = field.getDataAccessor().getValue(criteria);
				if (value != null) {
					if (fieldName.endsWith("_MIN") || fieldName.endsWith("_MAX")) {
						final String filteredField = fieldName.substring(0, fieldName.length() - "_MIN".length());
						final DtField resultDtField = resultDefinition.getField(filteredField);
						final DtField minField = fieldName.endsWith("_MIN") ? field : criteriaDefinition.getField(filteredField + "_MIN");
						final DtField maxField = fieldName.endsWith("_MAX") ? field : criteriaDefinition.getField(filteredField + "_MAX");
						final Comparable minValue = (Comparable) minField.getDataAccessor().getValue(criteria);
						final Comparable maxValue = (Comparable) maxField.getDataAccessor().getValue(criteria);
						filters.add(new DtListRangeFilter<O, Comparable>(resultDtField.getName(), Option.<Comparable> option(minValue), Option.<Comparable> option(maxValue), true, false));
					} else {
						filters.add(new DtListValueFilter<O>(field.getName(), (String) value));
					}
				}
			}
			//si null, alors on ne filtre pas
		}
		return new FilterFunction<>(new DtListChainFilter(filters.toArray(new DtListFilter[filters.size()])));
	}

	private long getNextId() {
		final long nextId = UUID.randomUUID().getMostSignificantBits();
		if (contacts.containsKey(nextId)) {
			return getNextId();
		}
		return nextId;
	}
}
