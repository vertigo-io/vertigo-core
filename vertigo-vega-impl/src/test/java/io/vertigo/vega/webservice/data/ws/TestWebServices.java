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
package io.vertigo.vega.webservice.data.ws;

import io.vertigo.core.resource.ResourceManager;
import io.vertigo.dynamo.collections.CollectionsManager;
import io.vertigo.dynamo.collections.DtListFunction;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.dynamo.export.ExportManager;
import io.vertigo.dynamo.export.model.Export;
import io.vertigo.dynamo.export.model.ExportBuilder;
import io.vertigo.dynamo.export.model.ExportFormat;
import io.vertigo.dynamo.file.FileManager;
import io.vertigo.dynamo.file.model.VFile;
import io.vertigo.dynamo.impl.collections.functions.filter.DtListChainFilter;
import io.vertigo.dynamo.impl.collections.functions.filter.DtListFilter;
import io.vertigo.dynamo.impl.collections.functions.filter.DtListRangeFilter;
import io.vertigo.dynamo.impl.collections.functions.filter.DtListValueFilter;
import io.vertigo.dynamo.impl.collections.functions.filter.FilterFunction;
import io.vertigo.lang.MessageText;
import io.vertigo.lang.Option;
import io.vertigo.lang.VUserException;
import io.vertigo.persona.security.VSecurityManager;
import io.vertigo.util.DateUtil;
import io.vertigo.util.StringUtil;
import io.vertigo.vega.engines.webservice.json.UiContext;
import io.vertigo.vega.webservice.WebServices;
import io.vertigo.vega.webservice.data.domain.Contact;
import io.vertigo.vega.webservice.data.domain.ContactCriteria;
import io.vertigo.vega.webservice.data.domain.ContactDao;
import io.vertigo.vega.webservice.data.domain.ContactValidator;
import io.vertigo.vega.webservice.data.domain.EmptyPkValidator;
import io.vertigo.vega.webservice.data.domain.MandatoryPkValidator;
import io.vertigo.vega.webservice.exception.VSecurityException;
import io.vertigo.vega.webservice.model.DtListDelta;
import io.vertigo.vega.webservice.model.ExtendedObject;
import io.vertigo.vega.webservice.model.UiListState;
import io.vertigo.vega.webservice.stereotype.AccessTokenConsume;
import io.vertigo.vega.webservice.stereotype.AccessTokenMandatory;
import io.vertigo.vega.webservice.stereotype.AccessTokenPublish;
import io.vertigo.vega.webservice.stereotype.AnonymousAccessAllowed;
import io.vertigo.vega.webservice.stereotype.AutoSortAndPagination;
import io.vertigo.vega.webservice.stereotype.DELETE;
import io.vertigo.vega.webservice.stereotype.Doc;
import io.vertigo.vega.webservice.stereotype.ExcludedFields;
import io.vertigo.vega.webservice.stereotype.GET;
import io.vertigo.vega.webservice.stereotype.HeaderParam;
import io.vertigo.vega.webservice.stereotype.IncludedFields;
import io.vertigo.vega.webservice.stereotype.InnerBodyParam;
import io.vertigo.vega.webservice.stereotype.POST;
import io.vertigo.vega.webservice.stereotype.PUT;
import io.vertigo.vega.webservice.stereotype.PathParam;
import io.vertigo.vega.webservice.stereotype.PathPrefix;
import io.vertigo.vega.webservice.stereotype.QueryParam;
import io.vertigo.vega.webservice.stereotype.ServerSideRead;
import io.vertigo.vega.webservice.stereotype.ServerSideSave;
import io.vertigo.vega.webservice.stereotype.SessionInvalidate;
import io.vertigo.vega.webservice.stereotype.SessionLess;
import io.vertigo.vega.webservice.stereotype.Validate;
import io.vertigo.vega.webservice.validation.UiMessageStack;
import io.vertigo.vega.webservice.validation.ValidationUserException;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

//basé sur http://www.restapitutorial.com/lessons/httpmethods.html

@PathPrefix("/test")
public final class TestWebServices implements WebServices {

	@Inject
	private VSecurityManager securityManager;
	@Inject
	private CollectionsManager collectionsManager;
	@Inject
	private ExportManager exportManager;
	@Inject
	private ResourceManager resourcetManager;
	@Inject
	private FileManager fileManager;
	@Inject
	private ContactDao contactDao;

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
		return contactDao.getList();
	}

	@GET("/authentifiedTest")
	public List<Contact> authentifiedTest() {
		//offset + range ?
		//code 200
		return contactDao.getList();
	}

	@Doc("send param type='Confirm' or type = 'Contact' \n Return 'OK' or 'Contact'")
	@GET("/twoResult")
	public UiContext testTwoResult(@QueryParam("type") final String type) {
		final UiContext result = new UiContext();
		if ("Confirm".equals(type)) {
			result.put("message", "Are you sure");
		} else {
			result.put("contact", contactDao.get(1L));
		}
		//offset + range ?
		//code 200
		return result;
	}

	@Doc("Use passPhrase : RtFM")
	@GET("/docTest/{passPhrase}")
	public List<Contact> docTest(@PathParam("passPhrase") final String passPhrase) throws VSecurityException {
		if (!"RtFM".equals(passPhrase)) {
			throw new VSecurityException(new MessageText("Bad passPhrase, check the doc in /catalog", null));
		}
		return contactDao.getList();
	}

	@Doc("Use passPhrase : RtFM")
	@GET("/docTest/")
	public List<Contact> docTestEmpty() throws VSecurityException {
		return docTest(null);
	}

	@Doc("Not the same than /docTest/")
	@GET("/docTest")
	public void docTest() throws VSecurityException {
		//rien
	}

	@GET("/{conId}")
	public Contact testRead(@PathParam("conId") final long conId) {
		final Contact contact = contactDao.get(conId);
		if (contact == null) {
			//404 ?
			throw new VUserException(new MessageText("Contact #" + conId + " unknown", null));
		}
		//200
		return contact;
	}

	@GET("/export/pdf/")
	public VFile testExportContacts() {
		final DtList<Contact> fullList = asDtList(contactDao.getList(), Contact.class);
		final Export export = new ExportBuilder(ExportFormat.PDF, "contacts")
				.beginSheet(fullList, "Contacts").endSheet()
				.withAuthor("vertigo-test")
				.build();

		final VFile result = exportManager.createExportFile(export);
		//200
		return result;
	}

	@GET("/export/pdf/{conId}")
	public VFile testExportContact(@PathParam("conId") final long conId) {
		final Contact contact = contactDao.get(conId);
		final Export export = new ExportBuilder(ExportFormat.PDF, "contact" + conId)
				.beginSheet(contact, "Contact").endSheet()
				.withAuthor("vertigo-test").build();

		final VFile result = exportManager.createExportFile(export);
		//200
		return result;
	}

	@GET("/grantAccess")
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
		final Contact contact = contactDao.get(conId);
		if (contact == null) {
			//404 ?
			throw new VUserException(new MessageText("Contact #" + conId + " unknown", null));
		}
		//200
		return contact;
	}

	//PUT is indempotent : ID obligatoire
	@PUT("/contactSyntax")
	public Contact testJsonSyntax(final Contact contact) {
		//200
		return contact;
	}

	//@POST is non-indempotent
	@POST("/contact")
	public Contact createContact( //create POST method -> 201 instead of 200 by convention
			final @Validate({ ContactValidator.class, EmptyPkValidator.class }) Contact contact) {
		if (contact.getName() == null || contact.getName().isEmpty()) {
			throw new VUserException(new MessageText("Name is mandatory", null));
		}
		contactDao.post(contact);
		//code 201 + location header : GET route
		return contact;
	}

	//PUT is indempotent : ID mandatory
	@PUT("/contact")
	public Contact testUpdate(
			final @Validate({ ContactValidator.class, MandatoryPkValidator.class }) Contact contact) {
		if (contact.getName() == null || contact.getName().isEmpty()) {
			//400
			throw new VUserException(new MessageText("Name is mandatory", null));
		}

		contactDao.put(contact);
		//200
		return contact;
	}

	//PUT is indempotent : ID mandatory
	@PUT("/contact/{conId}")
	public Contact testUpdateByPath(@PathParam("conId") final long conId,
			final @Validate({ ContactValidator.class, EmptyPkValidator.class }) Contact contact) {
		if (contact.getName() == null || contact.getName().isEmpty()) {
			//400
			throw new VUserException(new MessageText("Name is mandatory", null));
		}
		contact.setConId(conId);
		contactDao.put(contact);
		//200
		return contact;
	}

	//PUT is indempotent : ID obligatoire
	@Doc("Exclude conId and name.")
	@PUT("/filtered/*")
	@ServerSideSave
	public Contact filteredUpdateByExclude(
			final @Validate({ ContactValidator.class, MandatoryPkValidator.class }) @ServerSideRead @ExcludedFields({ "conId", "name" }) Contact contact) {
		if (contact.getName() == null || contact.getName().isEmpty()) {
			//400
			throw new VUserException(new MessageText("Name is mandatory", null));
		}

		contactDao.put(contact);
		//200
		return contact;
	}

	//PUT is indempotent : ID obligatoire
	@Doc("Only accept firstName and email. Will blocked if other fields are send.")
	@PUT("/filteredInclude/*")
	@ServerSideSave
	public Contact filteredUpdateByInclude(//
			final @Validate({ ContactValidator.class, MandatoryPkValidator.class })//
			@ServerSideRead//
			@IncludedFields({ "firstName", "email" }) Contact contact) {
		if (contact.getName() == null || contact.getName().isEmpty()) {
			//400
			throw new VUserException(new MessageText("Name is mandatory", null));
		}

		contactDao.put(contact);
		//200
		return contact;
	}

	@DELETE("/contact/{conId}")
	public void delete(@PathParam("conId") final long conId) throws VSecurityException {
		if (!contactDao.containsKey(conId)) {
			//404
			throw new VUserException(new MessageText("Contact #" + conId + " unknown", null));
		}
		if (conId < 5) {
			//401
			throw new VSecurityException(new MessageText("You don't have enought rights", null));
		}
		//200
		contactDao.remove(conId);
	}

	@Doc("Test ws multipart body with objects. Send a body with an object of to field : contactFrom, contactTo. Each one should be an json of Contact.")
	@POST("/innerbody")
	public List<Contact> testInnerBodyObject(@InnerBodyParam("contactFrom") final Contact contactFrom, @InnerBodyParam("contactTo") final Contact contactTo) {
		final List<Contact> result = new ArrayList<>(2);
		result.add(contactFrom);
		result.add(contactTo);
		//offset + range ?
		//code 200
		return result;
	}

	@Doc("Test ws multipart body with primitives. Send a body with an object of to field : contactId1, contactId2. Each one should be an json of long.")
	@ExcludedFields({ "address", "tels" })
	@POST("/innerLong")
	public List<Contact> testInnerBodyLong(@InnerBodyParam("contactId1") final long contactIdFrom, @InnerBodyParam("contactId2") final long contactIdTo) {
		final List<Contact> result = new ArrayList<>();
		result.add(contactDao.get(contactIdFrom));
		result.add(contactDao.get(contactIdTo));
		//offset + range ?
		//code 200
		return result;
	}

	@Doc("Test ws multipart body with primitives. Send a body with an object of to field : contactId1, contactId2. Each one should be an json of long.")
	@ServerSideSave
	@ExcludedFields({ "address", "tels" })
	@POST("/innerLongToDtList")
	public DtList<Contact> testInnerBodyLongToDtList(@InnerBodyParam("contactId1") final long contactIdFrom, @InnerBodyParam("contactId2") final long contactIdTo) {
		final DtList<Contact> result = new DtList<>(Contact.class);
		result.add(contactDao.get(contactIdFrom));
		result.add(contactDao.get(contactIdTo));
		//offset + range ?
		//code 200
		return result;
	}

	@Doc("Test ws returning UiContext. Send a body with an object of to field : contactId1, contactId2. Each one should be an json of long. You get partial Contacts with clientId in each one")
	@ServerSideSave
	@ExcludedFields({ "conId", "email", "birthday", "address", "tels" })
	@POST("/uiContext")
	public UiContext testInnerBody(@InnerBodyParam("contactId1") final long contactIdFrom, @InnerBodyParam("contactId2") final long contactIdTo) {
		final UiContext uiContext = new UiContext();
		uiContext.put("contactFrom", contactDao.get(contactIdFrom));
		uiContext.put("contactTo", contactDao.get(contactIdTo));
		uiContext.put("testLong", 12);
		uiContext.put("testString", "the String test");
		uiContext.put("testDate", DateUtil.newDate());
		uiContext.put("testEscapedString", "the EscapedString \",} test");
		//offset + range ?
		//code 200
		return uiContext;
	}

	@Doc("Test ws with multiple path params.")
	@ExcludedFields({ "address", "tels" })
	@POST("/multiPath/from/{conIdFrom}/to/{conIdTo}")
	public DtList<Contact> testMultiPathParam(//
			@PathParam("conIdFrom") final long contactIdFrom, //
			@PathParam("conIdTo") final long contactIdTo) {
		final DtList<Contact> result = new DtList<>(Contact.class);
		result.add(contactDao.get(contactIdFrom));
		result.add(contactDao.get(contactIdTo));
		//offset + range ?
		//code 200
		return result;
	}

	@Doc("Test ws multipart body with serverSide objects. Send a body with an object of to field : contactFrom, contactTo. Each one should be an partial json of Contact with clientId.")
	@POST("/innerBodyServerClient")
	public List<Contact> testInnerBodyClientId(//
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
		final DtList<Contact> fullList = asDtList(contactDao.getList(), Contact.class);
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
		final DtList<Contact> fullList = asDtList(contactDao.getList(), Contact.class);
		final DtList<Contact> result = filterFunction.apply(fullList);

		//offset + range ?
		//code 200
		return applySortAndPagination(result, uiListState);
	}

	@POST("/searchQueryPagined")
	@ExcludedFields({ "conId", "email", "birthday", "address", "tels" })
	public List<Contact> testSearchServiceQueryPagined(final ContactCriteria contact,
			@QueryParam("") final UiListState uiListState) {
		final DtListFunction<Contact> filterFunction = createDtListFunction(contact, Contact.class);
		final DtList<Contact> fullList = asDtList(contactDao.getList(), Contact.class);
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
		final DtList<Contact> fullList = asDtList(contactDao.getList(), Contact.class);
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

	@POST("/innerBodyValidationErrors")
	public List<Contact> testInnerBodyValidationErrors(//
			@InnerBodyParam("contactFrom") final Contact contactFrom, //
			@InnerBodyParam("contactTo") final Contact contactTo) {
		//offset + range ?
		//code 200
		if (contactFrom != null) {
			throw new ValidationUserException(new MessageText("Process validation error", null), contactFrom, "firstname");
		}
		return Collections.emptyList();
	}

	@POST("/uploadFile")
	public VFile testUploadFile(final @QueryParam("upfile") VFile inputFile, //
			final @QueryParam("id") Integer id, //
			final @QueryParam("note") String note) {

		return inputFile;
	}

	@AnonymousAccessAllowed
	@POST("/uploadFileFocus")
	public Integer testUploadFile(final @QueryParam("upfile") VFile inputFile) {
		return 1337;
	}

	@GET("/downloadFile")
	public VFile testDownloadFile(final @QueryParam("id") Integer id) {
		final URL imageUrl = resourcetManager.resolve("npi2loup.png");
		final File imageFile = asFile(imageUrl);
		final VFile imageVFile = fileManager.createFile("image" + id + ".png", "image/png", imageFile);
		return imageVFile;
	}

	@GET("/downloadNotModifiedFile")
	public VFile testDownloadNotModifiedFile(final @QueryParam("id") Integer id, final @HeaderParam("If-Modified-Since") Option<Date> ifModifiedSince, final HttpServletResponse response) {
		final VFile imageFile = testDownloadFile(id);
		if (ifModifiedSince.isDefined() && DateUtil.compareDateTime(imageFile.getLastModified(), ifModifiedSince.get()) <= 0) {
			response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
			return null;
			//this service must declared VFile as return type because it should return VFile when file was modified
		}
		return imageFile;
	}

	private static File asFile(final URL url) {
		File f;
		try {
			f = new File(url.toURI());
		} catch (final URISyntaxException e) {
			f = new File(url.getPath());
		}
		return f;
	}

	@POST("/saveListDelta")
	public String saveListDelta(final @Validate({ ContactValidator.class }) DtListDelta<Contact> myList) {
		return "OK : add " + myList.getCreated().size() + " contacts, update " + myList.getUpdated().size() + " contacts, removed " + myList.getDeleted().size();
	}

	@POST("/saveDtListContact")
	public String saveDtListContact(final @Validate({ ContactValidator.class }) DtList<Contact> myList) {
		for (final Contact contact : myList) {
			if (contact.getName() == null || contact.getName().isEmpty()) {
				//400
				throw new VUserException(new MessageText("Name is mandatory", null));
			}
		}
		return "OK : received " + myList.size() + " contacts";
	}

	@POST("/saveListContact")
	public String saveListContact(final List<Contact> myList) {
		return "OK : received " + myList.size() + " contacts";
	}

	@GET("/dtListMeta")
	public DtList<Contact> loadListMeta() {
		final DtList<Contact> result = new DtList<>(Contact.class);
		for (final Contact contact : contactDao.getList()) {
			result.add(contact);
		}
		result.setMetaData("testLong", 12);
		result.setMetaData("testString", "the String test");
		result.setMetaData("testDate", DateUtil.newDate());
		result.setMetaData("testEscapedString", "the EscapedString \",} test");
		return result;
	}

	@GET("/dtListMetaAsList")
	public List<Contact> loadListMetaAsList() {
		return loadListMeta();
	}

	@GET("/listComplexMeta")
	public DtList<Contact> loadListComplexMeta() {
		final DtList<Contact> result = loadListMeta();
		result.setMetaData("testLong", 12);
		result.setMetaData("testString", "the String test");
		result.setMetaData("testDate", DateUtil.newDate());
		result.setMetaData("testEscapedString", "the EscapedString \",} test");
		result.setMetaData("contact1", result.get(1));
		result.setMetaData("contact2", result.get(2));
		return result;
	}

	@GET("/headerParams")
	@Doc("Just send x-test-param:\"i'ts fine\"")
	public String testHeaderParams(final @HeaderParam("x-test-param") String testParam) {
		if (!"i'ts fine".equals(testParam)) {
			throw new VUserException(new MessageText("Bad param value. Read doc.", null));
		}
		return "OK";
	}

	//PUT is indempotent : ID obligatoire
	@PUT("/contactAliasName/{conId}")
	public Contact testUpdateByPath(@PathParam("conId") final long conId,
			final @Validate({ ContactValidator.class, EmptyPkValidator.class }) Contact contact,
			@InnerBodyParam("itsatoolongaliasforfieldcontactname") final String aliasName) {
		if (contact.getName() == null || contact.getName().isEmpty()) {
			//400
			throw new VUserException(new MessageText("Name is mandatory", null));
		}
		contact.setConId(conId);
		contact.setName(aliasName);
		contactDao.put(contact);
		//200
		return contact;
	}

	@GET("/contactExtended/{conId}")
	public ExtendedObject<Contact> testGetExtended(@PathParam("conId") final long conId) {
		final Contact contact = contactDao.get(conId);
		final ExtendedObject<Contact> result = new ExtendedObject<>(contact);
		result.put("vanillaUnsupportedMultipleIds", new int[] { 1, 2, 3 });
		//200
		return result;
	}

	@PUT("/contactExtended/{conId}")
	public ExtendedObject<Contact> testGetExtended(@PathParam("conId") final long conId,
			final @Validate({ ContactValidator.class, EmptyPkValidator.class }) Contact contact,
			@InnerBodyParam("vanillaUnsupportedMultipleIds") final int[] multipleIds) {
		contact.setConId(conId);
		contactDao.put(contact);
		final ExtendedObject<Contact> result = new ExtendedObject<>(contact);
		result.put("vanillaUnsupportedMultipleIds", multipleIds);
		//200
		return result;
	}

	@POST("/charset")
	public Contact testCharset(
			final Contact text) {
		//200
		return text;
	}

	/*@GET("/searchFacet")
	public FacetedQueryResult<DtObject, ContactCriteria> testSearchServiceFaceted(final ContactCriteria contact) {
		final DtListFunction<Contact> filterFunction = createDtListFunction(contact, Contact.class);
		final DtList<Contact> result = filterFunction.apply((DtList<Contact>) contacts.values());

		//offset + range ?
		//code 200
		return result;
	}*/

	private static <D extends DtObject> DtList<D> asDtList(final Collection<D> values, final Class<D> dtObjectClass) {
		final DtList<D> result = new DtList<>(dtObjectClass);
		for (final D element : values) {
			result.add(element);
		}
		return result;
	}

	private <D extends DtObject> DtList<D> applySortAndPagination(final DtList<D> unFilteredList, final UiListState uiListState) {
		final DtList<D> sortedList;
		if (uiListState.getSortFieldName() != null) {
			sortedList = collectionsManager.createDtListProcessor()
					.sort(StringUtil.camelToConstCase(uiListState.getSortFieldName()), uiListState.isSortDesc(), true, true)
					.apply(unFilteredList);
		} else {
			sortedList = unFilteredList;
		}
		final DtList<D> filteredList;
		if (uiListState.getTop() > 0) {
			final int listSize = sortedList.size();
			final int usedSkip = Math.min(uiListState.getSkip(), listSize);
			final int usedTop = Math.min(usedSkip + uiListState.getTop(), listSize);
			filteredList = collectionsManager.createDtListProcessor()
					.filterSubList(usedSkip, usedTop)
					.apply(sortedList);
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

}
