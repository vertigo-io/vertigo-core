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
package io.vertigo.vega.rest;

import io.vertigo.core.lang.MessageText;
import io.vertigo.core.lang.Option;
import io.vertigo.dynamo.collections.CollectionsManager;
import io.vertigo.dynamo.collections.DtListFunction;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.dynamo.export.Export;
import io.vertigo.dynamo.export.ExportBuilder;
import io.vertigo.dynamo.export.ExportDtParameters;
import io.vertigo.dynamo.export.ExportFormat;
import io.vertigo.dynamo.export.ExportManager;
import io.vertigo.dynamo.file.model.KFile;
import io.vertigo.dynamo.impl.collections.functions.filter.DtListChainFilter;
import io.vertigo.dynamo.impl.collections.functions.filter.DtListFilter;
import io.vertigo.dynamo.impl.collections.functions.filter.DtListRangeFilter;
import io.vertigo.dynamo.impl.collections.functions.filter.DtListValueFilter;
import io.vertigo.dynamo.impl.collections.functions.filter.FilterFunction;
import io.vertigo.kernel.exception.VUserException;
import io.vertigo.kernel.util.DateUtil;
import io.vertigo.persona.security.KSecurityManager;
import io.vertigo.vega.rest.engine.UiContext;
import io.vertigo.vega.rest.exception.VSecurityException;
import io.vertigo.vega.rest.metamodel.UiListState;
import io.vertigo.vega.rest.stereotype.AccessTokenConsume;
import io.vertigo.vega.rest.stereotype.AccessTokenMandatory;
import io.vertigo.vega.rest.stereotype.AccessTokenPublish;
import io.vertigo.vega.rest.stereotype.AnonymousAccessAllowed;
import io.vertigo.vega.rest.stereotype.AutoSortAndPagination;
import io.vertigo.vega.rest.stereotype.DELETE;
import io.vertigo.vega.rest.stereotype.Doc;
import io.vertigo.vega.rest.stereotype.ExcludedFields;
import io.vertigo.vega.rest.stereotype.GET;
import io.vertigo.vega.rest.stereotype.IncludedFields;
import io.vertigo.vega.rest.stereotype.InnerBodyParam;
import io.vertigo.vega.rest.stereotype.POST;
import io.vertigo.vega.rest.stereotype.PUT;
import io.vertigo.vega.rest.stereotype.PathParam;
import io.vertigo.vega.rest.stereotype.PathPrefix;
import io.vertigo.vega.rest.stereotype.QueryParam;
import io.vertigo.vega.rest.stereotype.ServerSideRead;
import io.vertigo.vega.rest.stereotype.ServerSideSave;
import io.vertigo.vega.rest.stereotype.SessionInvalidate;
import io.vertigo.vega.rest.stereotype.SessionLess;
import io.vertigo.vega.rest.stereotype.Validate;
import io.vertigo.vega.rest.validation.UiMessageStack;
import io.vertigo.vega.rest.validation.ValidationUserException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;

//bas� sur http://www.restapitutorial.com/lessons/httpmethods.html

@PathPrefix("/test")
public final class TesterRestServices implements RestfulService {

	@Inject
	private KSecurityManager securityManager;
	@Inject
	private CollectionsManager collectionsManager;
	@Inject
	private ExportManager exportManager;

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
	@GET("/login")
	public void login() {
		//code 200
		securityManager.getCurrentUserSession().get().authenticate();
	}

	@SessionInvalidate
	@GET("/logout")
	public void logout() {
		//code 200
	}

	@SessionLess
	@AnonymousAccessAllowed
	@GET("/anonymousTest")
	public List<Contact> anonymousTest() {
		//offset + range ?
		//code 200
		return new ArrayList<>(contacts.values());
	}

	@GET("/authentifiedTest")
	public List<Contact> authentifiedTest() {
		//offset + range ?
		//code 200
		return new ArrayList<>(contacts.values());
	}

	@Doc("send param type='Confirm' or type = 'Contact' \n Return 'OK' or 'Contact'")
	@GET("/twoResult")
	public UiContext testTwoResult(@QueryParam("type") final String type) {
		final UiContext result = new UiContext();
		if ("Confirm".equals(type)) {
			result.put("message", "Are you sure");
		} else {
			result.put("contact", contacts.get(1));
		}
		//offset + range ?
		//code 200
		return result;
	}

	@Doc("Use passPhrase : RtFM")
	@GET("/docTest/{passPhrase}")
	public List<Contact> docTest(@PathParam("passPhrase") final String passPhrase) throws VSecurityException {
		if (!"RtFM".equals(passPhrase)) {
			throw new VSecurityException("Bad passPhrase, check the doc in /catalog");
		}
		return new ArrayList<>(contacts.values());
	}

	@GET("/{conId}")
	public Contact testRead(@PathParam("conId") final long conId) {
		final Contact contact = contacts.get(conId);
		if (contact == null) {
			//404 ?
			throw new VUserException(new MessageText("Contact #" + conId + " unknown", null));
		}
		//200
		return contact;
	}

	@GET("/export/pdf/")
	public KFile testExportContacts() {
		final DtList<Contact> fullList = asDtList(contacts.values(), Contact.class);
		final ExportDtParameters dtParameter = exportManager.createExportListParameters(fullList)//
				.build();

		final Export export = new ExportBuilder(ExportFormat.PDF, "contacts")//
		.withExportDtParameters(dtParameter)//
		.withAuthor("vertigo-test")//
		.build();

		final KFile result = exportManager.createExportFile(export);
		//200
		return result;
	}

	@GET("/export/pdf/{conId}")
	public KFile testExportContact(@PathParam("conId") final long conId) {
		final Contact contact = contacts.get(conId);
		final ExportDtParameters dtParameter = exportManager.createExportObjectParameters(contact)//
				.build();

		final Export export = new ExportBuilder(ExportFormat.PDF, "contact" + conId + ".pdf")//
		.withExportDtParameters(dtParameter)//
		.withAuthor("vertigo-test")//
		.build();

		final KFile result = exportManager.createExportFile(export);
		//200
		return result;
	}

	@GET("/grantAccess/")
	@AccessTokenPublish
	public void testAccessToken() {
		//access token publish
	}

	@GET("/limitedAccess/{conId}")
	@AccessTokenMandatory
	public Contact testAccessToken(@PathParam("conId") final long conId) {
		return testRead(conId);
	}

	@GET("/oneTimeAccess/{conId}")
	@AccessTokenConsume
	public Contact testAccessTokenConsume(@PathParam("conId") final long conId) {
		return testRead(conId);
	}

	@GET("/filtered/{conId}")
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
	@PUT("/{conId}")
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
	@PUT("/filtered/{conId}")
	@ServerSideSave
	public Contact filteredUpdateByExclude(//
			final @Validate({ ContactValidator.class, MandatoryPkValidator.class })//
			@ServerSideRead//
			@ExcludedFields({ "conId", "name" }) Contact contact) {
		if (contact.getName() == null || contact.getName().isEmpty()) {
			//400
			throw new VUserException(new MessageText("Name is mandatory", null));
		}

		contacts.put(contact.getConId(), contact);
		//200
		return contact;
	}

	//PUT is indempotent : ID obligatoire
	@PUT("/filtered2/{conId}")
	@ServerSideSave
	public Contact filteredUpdateByInclude(//
			final @Validate({ ContactValidator.class, MandatoryPkValidator.class })//
			@ServerSideRead//
			@IncludedFields({ "firstName", "email" }) Contact contact) {
		if (contact.getName() == null || contact.getName().isEmpty()) {
			//400
			throw new VUserException(new MessageText("Name is mandatory", null));
		}

		contacts.put(contact.getConId(), contact);
		//200
		return contact;
	}

	@DELETE("/{conId}")
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
	@POST("/multipart")
	public List<Contact> testMultiPartBodyObject(@InnerBodyParam("contactFrom") final Contact contactFrom, @InnerBodyParam("contactTo") final Contact contactTo) {
		final List<Contact> result = new ArrayList<>(2);
		result.add(contactFrom);
		result.add(contactTo);
		//offset + range ?
		//code 200
		return result;
	}

	@Doc("Test ws-rest multipart body with primitives. Send a body with an object of to field : contactId1, contactId2. Each one should be an json of long.")
	@ServerSideSave
	@ExcludedFields({ "address", "tels" })
	@POST("/multipartLong")
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
	@POST("/uiContext")
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
	@POST("/multipartServerClient")
	public List<Contact> testMultiPartBodyClientId(//
			@InnerBodyParam("contactFrom") @ServerSideRead final Contact contactFrom, //
			@InnerBodyParam("contactTo") @ServerSideRead final Contact contactTo) {
		final List<Contact> result = new ArrayList<>(2);
		result.add(contactFrom);
		result.add(contactTo);
		//offset + range ?
		//code 200
		return result;
	}

	@POST("/search")
	@ExcludedFields({ "conId", "email", "birthday", "address", "tels" })
	public List<Contact> testSearch(//
			@ExcludedFields({ "conId", "email", "birthday", "address", "tels" }) final ContactCriteria contact) {
		final DtListFunction<Contact> filterFunction = createDtListFunction(contact, Contact.class);
		final DtList<Contact> fullList = asDtList(contacts.values(), Contact.class);
		final DtList<Contact> result = filterFunction.apply(fullList);
		//offset + range ?
		//code 200
		return result;
	}

	@POST("/searchPagined")
	@ExcludedFields({ "conId", "email", "birthday", "address", "tels" })
	public List<Contact> testSearchServicePagined(//
			@InnerBodyParam("criteria") final ContactCriteria contact, //
			@InnerBodyParam("listState") final UiListState uiListState) {
		final DtListFunction<Contact> filterFunction = createDtListFunction(contact, Contact.class);
		final DtList<Contact> fullList = asDtList(contacts.values(), Contact.class);
		final DtList<Contact> result = filterFunction.apply(fullList);

		//offset + range ?
		//code 200
		return applySortAndPagination(result, uiListState);
	}

	@POST("/searchQueryPagined")
	@ExcludedFields({ "conId", "email", "birthday", "address", "tels" })
	public List<Contact> testSearchServiceQueryPagined(final ContactCriteria contact, final UiListState uiListState) {
		final DtListFunction<Contact> filterFunction = createDtListFunction(contact, Contact.class);
		final DtList<Contact> fullList = asDtList(contacts.values(), Contact.class);
		final DtList<Contact> result = filterFunction.apply(fullList);

		//offset + range ?
		//code 200
		return applySortAndPagination(result, uiListState);
	}

	@AutoSortAndPagination
	@POST("/searchAutoPagined")
	@ExcludedFields({ "conId", "email", "birthday", "address", "tels" })
	public List<Contact> testSearchServiceAutoPagined(final ContactCriteria contact) {
		final DtListFunction<Contact> filterFunction = createDtListFunction(contact, Contact.class);
		final DtList<Contact> fullList = asDtList(contacts.values(), Contact.class);
		final DtList<Contact> result = filterFunction.apply(fullList);
		//offset + range ?
		//code 200
		return result;
	}

	@POST("/uiMessage")
	public UiMessageStack testUiMessage(final Contact contact, final UiMessageStack uiMessageStack) {
		uiMessageStack.success("Your message have been received");
		uiMessageStack.info("We can complete messageStack : globaly or field by field");
		uiMessageStack.warning("This field must be read twice !!", contact, "birthday");
		if (uiMessageStack.hasErrors()) {
			throw new ValidationUserException();
		}
		return uiMessageStack;
	}

	@POST("/uploadFile")
	public KFile testUploadFile(final @QueryParam("upfile") KFile inputFile, //
			final @QueryParam("id") Integer id, //
			final @QueryParam("note") String note) {

		return inputFile;
	}

	/*@GET("/searchFacet")
	public FacetedQueryResult<DtObject, ContactCriteria> testSearchServiceFaceted(final ContactCriteria contact) {
		final DtListFunction<Contact> filterFunction = createDtListFunction(contact, Contact.class);
		final DtList<Contact> result = filterFunction.apply((DtList<Contact>) contacts.values());

		//offset + range ?
		//code 200
		return result;
	}*/

	private <D extends DtObject> DtList<D> asDtList(final Collection<D> values, final Class<D> dtObjectClass) {
		final DtList<D> result = new DtList<>(dtObjectClass);
		for (final D element : values) {
			result.add(element);
		}
		return result;
	}

	private <D extends DtObject> DtList<D> applySortAndPagination(final DtList<D> unFilteredList, final UiListState uiListState) {
		final DtList<D> sortedList;
		if (uiListState.getSortFieldName() != null) {
			final DtListFunction<D> sortFunction = collectionsManager.createSort(uiListState.getSortFieldName(), uiListState.isSortDesc(), true, true);
			sortedList = sortFunction.apply(unFilteredList);
		} else {
			sortedList = unFilteredList;
		}
		final DtList<D> filteredList;
		if (uiListState.getTop() > 0) {
			final int listSize = sortedList.size();
			final int usedSkip = Math.min(uiListState.getSkip(), listSize);
			final int usedTop = Math.min(usedSkip + uiListState.getTop(), listSize);
			final DtListFunction<D> filterFunction = collectionsManager.createFilterSubList(usedSkip, usedTop);
			filteredList = filterFunction.apply(sortedList);
		} else {
			filteredList = sortedList;
		}
		return filteredList;
	}

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
