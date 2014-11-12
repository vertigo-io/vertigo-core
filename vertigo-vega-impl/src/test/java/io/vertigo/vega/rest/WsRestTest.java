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

import io.vertigo.AbstractTestCaseJU4;
import io.vertigo.commons.cache.CacheManager;
import io.vertigo.commons.codec.CodecManager;
import io.vertigo.commons.impl.cache.CacheManagerImpl;
import io.vertigo.commons.impl.codec.CodecManagerImpl;
import io.vertigo.commons.impl.resource.ResourceManagerImpl;
import io.vertigo.commons.locale.LocaleManager;
import io.vertigo.commons.plugins.cache.map.MapCachePlugin;
import io.vertigo.commons.plugins.resource.java.ClassPathResourceResolverPlugin;
import io.vertigo.commons.resource.ResourceManager;
import io.vertigo.core.config.AppConfigBuilder;
import io.vertigo.dynamo.collections.CollectionsManager;
import io.vertigo.dynamo.export.ExportManager;
import io.vertigo.dynamo.file.FileManager;
import io.vertigo.dynamo.impl.collections.CollectionsManagerImpl;
import io.vertigo.dynamo.impl.environment.EnvironmentManagerImpl;
import io.vertigo.dynamo.impl.export.ExportManagerImpl;
import io.vertigo.dynamo.impl.file.FileManagerImpl;
import io.vertigo.dynamo.impl.kvdatastore.KVDataStoreManagerImpl;
import io.vertigo.dynamo.impl.persistence.PersistenceManagerImpl;
import io.vertigo.dynamo.impl.task.TaskManagerImpl;
import io.vertigo.dynamo.kvdatastore.KVDataStoreManager;
import io.vertigo.dynamo.persistence.PersistenceManager;
import io.vertigo.dynamo.plugins.environment.loaders.java.AnnotationLoaderPlugin;
import io.vertigo.dynamo.plugins.environment.loaders.kpr.KprLoaderPlugin;
import io.vertigo.dynamo.plugins.environment.registries.domain.DomainDynamicRegistryPlugin;
import io.vertigo.dynamo.plugins.export.pdf.PDFExporterPlugin;
import io.vertigo.dynamo.plugins.kvdatastore.delayedmemory.DelayedMemoryKVDataStorePlugin;
import io.vertigo.dynamo.plugins.persistence.postgresql.PostgreSqlDataStorePlugin;
import io.vertigo.dynamo.task.TaskManager;
import io.vertigo.engines.command.TcpVCommandEngine;
import io.vertigo.persona.impl.security.KSecurityManagerImpl;
import io.vertigo.persona.plugins.security.loaders.SecurityResourceLoaderPlugin;
import io.vertigo.persona.security.KSecurityManager;
import io.vertigo.vega.impl.rest.RestManagerImpl;
import io.vertigo.vega.impl.rest.catalog.CatalogRestServices;
import io.vertigo.vega.impl.rest.catalog.SwaggerRestServices;
import io.vertigo.vega.impl.rest.filter.JettyMultipartConfig;
import io.vertigo.vega.impl.rest.handler.RateLimitingHandler;
import io.vertigo.vega.impl.security.UiSecurityTokenManagerImpl;
import io.vertigo.vega.plugins.rest.instrospector.annotations.AnnotationsEndPointIntrospectorPlugin;
import io.vertigo.vega.plugins.rest.routesregister.sparkjava.SparkJavaRoutesRegister;
import io.vertigo.vega.rest.WsRestHandler.DtDefinitions;
import io.vertigo.vega.security.UiSecurityTokenManager;
import io.vertigoimpl.commons.locale.LocaleManagerImpl;
import io.vertigoimpl.engines.rest.cmd.ComponentCmdRestServices;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.Test;

import spark.Spark;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.filter.session.SessionFilter;
import com.jayway.restassured.parsing.Parser;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

public final class WsRestTest extends AbstractTestCaseJU4 {
	private static final String HEADER_ACCESS_TOKEN = "x-access-token";

	private static final int WS_PORT = 8088;
	private static boolean sparkInitialized = false;
	private final SessionFilter sessionFilter = new SessionFilter();

	/**
	 * Configuration des tests.
	 * @param appConfigBuilder builder
	 */
	@Override
	protected void configMe(final AppConfigBuilder appConfigBuilder) {
		// Création de l'état de l'application
		// Initialisation de l'état de l'application
		// @formatter:off
		appConfigBuilder
			.withSilence(false)
			.withCommandEngine(new TcpVCommandEngine(4406))
			.withResource("classes", DtDefinitions.class.getName())
			.withResource("kpr", "ksp/execution.kpr")
			.beginModule("commons")
				.beginComponent(LocaleManager.class, LocaleManagerImpl.class)
					.withParam("locales", "fr")
				.endComponent()
				.beginComponent(ResourceManager.class, ResourceManagerImpl.class)
					.beginPlugin( ClassPathResourceResolverPlugin.class).endPlugin()
				.endComponent()
				.beginComponent(KSecurityManager.class, KSecurityManagerImpl.class)
					.withParam("userSessionClassName", TestUserSession.class.getName())
					.beginPlugin(SecurityResourceLoaderPlugin.class).endPlugin()
				.endComponent() //
			.endModule()
			.beginModule("dynamo").withNoAPI()
				.beginComponent(CodecManager.class, CodecManagerImpl.class).endComponent()
				.beginComponent(CollectionsManager.class, CollectionsManagerImpl.class).endComponent()
				.beginComponent(FileManager.class, FileManagerImpl.class).endComponent()
				.beginComponent(KVDataStoreManager.class, KVDataStoreManagerImpl.class)
					.beginPlugin(DelayedMemoryKVDataStorePlugin.class)
						.withParam("dataStoreName", "UiSecurityStore")
						.withParam("timeToLiveSeconds", "120")
					.endPlugin()
				.endComponent()
				.beginComponent(PersistenceManager.class, PersistenceManagerImpl.class)
					.beginPlugin(PostgreSqlDataStorePlugin.class)
						.withParam("sequencePrefix","SEQ_")
					.endPlugin()
				.endComponent()
				.beginComponent(CacheManager.class, CacheManagerImpl.class)
					.beginPlugin( MapCachePlugin.class).endPlugin()
				.endComponent()
				.beginComponent(TaskManager.class, TaskManagerImpl.class).endComponent()
				.beginComponent(ExportManager.class, ExportManagerImpl.class)
					.beginPlugin(PDFExporterPlugin.class).endPlugin()
				.endComponent()
				.beginComponent(EnvironmentManagerImpl.class)
					.beginPlugin(AnnotationLoaderPlugin.class).endPlugin()
					.beginPlugin(KprLoaderPlugin.class).endPlugin()
					.beginPlugin(DomainDynamicRegistryPlugin.class).endPlugin()
				.endComponent()
			.endModule()
			.beginModule("restServices").withNoAPI().withInheritance(RestfulService.class)
				.beginComponent(ComponentCmdRestServices.class).endComponent()
				.beginComponent(ContactsRestServices.class).endComponent()
				.beginComponent(TesterRestServices.class).endComponent()
				.beginComponent(SwaggerRestServices.class).endComponent()
				.beginComponent(CatalogRestServices.class).endComponent()
			.endModule()
			.beginModule("restCore").withNoAPI().withInheritance(Object.class)
				.beginComponent(RestManager.class, RestManagerImpl.class)
					.beginPlugin(AnnotationsEndPointIntrospectorPlugin.class).endPlugin()
				.endComponent()
				.beginComponent(RateLimitingHandler.class).endComponent()
				.beginComponent(UiSecurityTokenManager.class, UiSecurityTokenManagerImpl.class)
					.withParam("storeName", "UiSecurityStore")
				.endComponent()
			.endModule();
		// @formatter:on
	}

	/** {@inheritDoc} */
	@Override
	protected boolean cleanHomeForTest() {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	protected void doSetUp() throws Exception {
		// Will serve all static file are under "/public" in classpath if the route isn't consumed by others routes.
		// When using Maven, the "/public" folder is assumed to be in "/main/resources"
		//Spark.externalStaticFileLocation("d:/Projets/Projet_Kasper/SPA-Fmk/SPA-skeleton/public/");
		//Spark.externalStaticFileLocation("D:/@GitHub/vertigo/vertigo-vega-impl/src/test/resources/");
		//Spark.before(new IE8CompatibilityFix("8"));
		//Spark.before(new CorsAllower());
		//Translate EndPoint to route
		if (!sparkInitialized) {
			sparkInitialized = true;

			Spark.setPort(WS_PORT);
			final String tempDir = System.getProperty("java.io.tmpdir");
			Spark.before(new JettyMultipartConfig(tempDir));
			//Spark.before(new VegaMultipartConfig(tempDir));
			new SparkJavaRoutesRegister().init();

			//RestAsssured init
			RestAssured.baseURI = "http://localhost";
			RestAssured.port = WS_PORT;
			RestAssured.registerParser("application/json+list", Parser.JSON);
			RestAssured.registerParser("application/json+entity:Contact", Parser.JSON);
		}

		RestAssured.given()
				.filter(sessionFilter)
				.get("/test/login");
	}

	@Test
	public void testCatalog() {
		RestAssured.given()
				.expect()
				.statusCode(HttpStatus.SC_OK)
				.when()
				.get("/catalog");
	}

	@Test
	public void testSwaggerApi() {
		RestAssured.given()
				.expect()
				.statusCode(HttpStatus.SC_OK)
				.when()
				.get("/swaggerApi");
	}

	@Test
	public void testSwaggerUi() {
		RestAssured.given()
				.expect()
				.statusCode(HttpStatus.SC_OK)
				.when()
				.get("/swaggerUi");

		RestAssured.given()
				.expect()
				.statusCode(HttpStatus.SC_OK)
				.when()
				.get("/swaggerUi/swagger-ui.min.js");

		RestAssured.given()
				.expect()
				.statusCode(HttpStatus.SC_OK)
				.when()
				.get("/swaggerUi/lib/swagger-client.js");
	}

	@Test
	public void testLogout() {
		loggedAndExpect()
				.statusCode(HttpStatus.SC_NO_CONTENT)
				.when()
				.get("/test/logout");
	}

	@Test
	public void testLogin() {
		RestAssured.expect()
				.statusCode(HttpStatus.SC_NO_CONTENT)
				.when()
				.get("/test/login");
	}

	@Test
	public void testAnonymousTest() {
		RestAssured.expect()
				.statusCode(HttpStatus.SC_OK)
				.when()
				.get("/test/anonymousTest");
	}

	@Test
	public void testAuthentifiedTest() {
		loggedAndExpect()
				.statusCode(HttpStatus.SC_OK)
				.body("size()", Matchers.greaterThanOrEqualTo(10))
				.when()
				.get("/test/authentifiedTest");
	}

	@Test
	public void testUnauthentifiedTest() {
		expect()
				.statusCode(HttpStatus.SC_UNAUTHORIZED)
				.when()
				.get("/test/authentifiedTest");
	}

	@Test
	public void testTwoResultConfirm() {
		loggedAndExpect(given().param("type", "Confirm"))
				.statusCode(HttpStatus.SC_OK)
				.body("message", Matchers.equalTo("Are you sure"))
				.when()
				.get("/test/twoResult");
	}

	@Test
	public void testTwoResultContact() {
		loggedAndExpect(given().param("type", "Contact"))
				.statusCode(HttpStatus.SC_OK)
				.body("contact.conId", Matchers.equalTo(1))
				.when()
				.get("/test/twoResult");
	}

	@Test
	public void docTest1() {
		loggedAndExpect()
				.statusCode(HttpStatus.SC_OK)
				.body("size()", Matchers.greaterThanOrEqualTo(10))
				.when()
				.get("/test/docTest/RtFM");
	}

	@Test
	public void docTest2() {
		loggedAndExpect()
				.statusCode(HttpStatus.SC_FORBIDDEN)
				.when()
				.get("/test/docTest/myPass");
	}

	@Test
	public void docTest3() {
		loggedAndExpect()
				.statusCode(HttpStatus.SC_FORBIDDEN)
				.when()
				.get("/test/docTest/");
	}

	@Test
	public void testRead1() {
		loggedAndExpect()
				.body("conId", Matchers.equalTo(2))
				.statusCode(HttpStatus.SC_OK)
				.when()
				.get("/test/2");
	}

	@Test
	public void testRead2() {
		loggedAndExpect()
				.body("globalErrors", Matchers.contains("Contact #30 unknown"))
				.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
				.when()
				.get("/test/30");
	}

	@Test
	public void testExportContacts() {
		loggedAndExpect()
				.header("Content-Disposition", Matchers.equalToIgnoringCase("attachment;filename=contacts.pdf;filename*=UTF-8''contacts.pdf"))
				.header("Content-Length", Matchers.greaterThanOrEqualTo("2572"))
				.statusCode(HttpStatus.SC_OK)
				.when()
				.get("/test/export/pdf/");
	}

	@Test
	public void testExportOneContact() {
		loggedAndExpect()
				.header("Content-Disposition", Matchers.equalToIgnoringCase("attachment;filename=contact2.pdf;filename*=UTF-8''contact2.pdf"))
				.header("Content-Length", Matchers.equalTo("1703"))
				.statusCode(HttpStatus.SC_OK)
				.when()
				.get("/test/export/pdf/2");
	}

	@Test
	public void testGrantAccessToken() {
		loggedAndExpect()
				.statusCode(HttpStatus.SC_NO_CONTENT)
				.header(HEADER_ACCESS_TOKEN, Matchers.notNullValue())
				.when()
				.get("/test/grantAccess");
	}

	@Test
	public void testNoAccessToken() {
		loggedAndExpect()
				.statusCode(HttpStatus.SC_FORBIDDEN)
				.when()
				.get("/test/limitedAccess/3");
	}

	@Test
	public void testBadAccessToken() {
		loggedAndExpect(given().header(HEADER_ACCESS_TOKEN, "badToken"))
				.statusCode(HttpStatus.SC_FORBIDDEN)
				.when()
				.get("/test/limitedAccess/3");
	}

	@Test
	public void testLimitedAccessToken() {
		final String headerAccessToken = given()
				.filter(sessionFilter)
				.get("/test/grantAccess")
				.header(HEADER_ACCESS_TOKEN);

		loggedAndExpect(given().header(HEADER_ACCESS_TOKEN, headerAccessToken))
				.body("conId", Matchers.equalTo(3))
				.statusCode(HttpStatus.SC_OK)
				.when()
				.get("/test/limitedAccess/3");
	}

	@Test
	public void testLimitedAccessToken2() {
		final String headerAccessToken = given()
				.filter(sessionFilter)
				.get("/test/grantAccess")
				.header(HEADER_ACCESS_TOKEN);

		loggedAndExpect(given().header(HEADER_ACCESS_TOKEN, headerAccessToken))
				.body("conId", Matchers.equalTo(3))
				.statusCode(HttpStatus.SC_OK)
				.when()
				.get("/test/limitedAccess/3");

		loggedAndExpect(given().header(HEADER_ACCESS_TOKEN, headerAccessToken))
				.body("conId", Matchers.equalTo(3))
				.statusCode(HttpStatus.SC_OK)
				.when()
				.get("/test/limitedAccess/3");
	}

	@Test
	public void testAccessTokenConsume() {
		final String headerAccessToken = given()
				.filter(sessionFilter)
				.get("/test/grantAccess")
				.header(HEADER_ACCESS_TOKEN);

		loggedAndExpect(given().header(HEADER_ACCESS_TOKEN, headerAccessToken))
				.body("conId", Matchers.equalTo(1))
				.statusCode(HttpStatus.SC_OK)
				.when()
				.get("/test/oneTimeAccess/1");
	}

	@Test
	public void testAccessTokenConsume2() {
		final String headerAccessToken = given()
				.filter(sessionFilter)
				.get("/test/grantAccess")
				.header(HEADER_ACCESS_TOKEN);

		loggedAndExpect(given().header(HEADER_ACCESS_TOKEN, headerAccessToken))
				.body("conId", Matchers.equalTo(1))
				.statusCode(HttpStatus.SC_OK)
				.when()
				.get("/test/oneTimeAccess/1");

		loggedAndExpect(given().header(HEADER_ACCESS_TOKEN, headerAccessToken))
				.statusCode(HttpStatus.SC_FORBIDDEN)
				.when()
				.get("/test/oneTimeAccess/1");
	}

	@Test
	public void testFilteredRead() {
		loggedAndExpect()
				.body("conId", Matchers.equalTo(1))
				.body("honorificCode", Matchers.notNullValue())
				.body("name", Matchers.notNullValue())
				.body("firstName", Matchers.notNullValue())
				.body("birthday", Matchers.nullValue()) //excluded field
				.body("email", Matchers.nullValue()) //excluded field
				.statusCode(HttpStatus.SC_OK)
				.when()
				.get("/test/filtered/1");
	}

	@Test
	public void testPostContact() throws ParseException {
		doPostContact();
	}

	private Map<String, Object> doPostContact() throws ParseException {
		final Map<String, Object> newContact = createDefaultContact(null);

		final Long conId = loggedAndExpect(given().body(newContact))
				.body("conId", Matchers.notNullValue())
				.statusCode(HttpStatus.SC_OK)
				.when()
				.post("/test/contact")
				.body().path("conId");
		newContact.put("conId", conId);
		return newContact;
	}

	@Test
	public void testPostContactValidatorError() throws ParseException {
		final Map<String, Object> newContact = createDefaultContact(100L);

		loggedAndExpect(given().body(newContact))
				.body("fieldErrors.conId", Matchers.contains("Id must not be set"))
				.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
				.when()
				.post("/test/contact");

		final Map<String, Object> new2Contact = createDefaultContact(null);
		new2Contact.put("birthday", convertDate("24/10/2012"));

		loggedAndExpect(given().body(new2Contact))
				.body("fieldErrors.birthday", Matchers.contains("You can't add contact younger than 16"))
				.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
				.when()
				.post("/test/contact");
	}

	@Test
	public void testPostContactUserException() throws ParseException {
		final Map<String, Object> newContact = createDefaultContact(null);
		newContact.remove("name");

		loggedAndExpect(given().body(newContact))
				.body("globalErrors", Matchers.contains("Name is mandatory"))
				.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
				.when()
				.post("/test/contact");
	}

	@Test
	public void testPutContact() throws ParseException {
		final Map<String, Object> newContact = createDefaultContact(100L);

		loggedAndExpect(given().body(newContact))
				.body("conId", Matchers.equalTo(100))
				.body("honorificCode", Matchers.notNullValue())
				.body("name", Matchers.notNullValue())
				.body("firstName", Matchers.notNullValue())
				.body("birthday", Matchers.notNullValue())
				.body("email", Matchers.notNullValue())
				.statusCode(HttpStatus.SC_OK)
				.when()
				.put("/test/contact");
	}

	@Test
	public void testPutContactByPath() throws ParseException {
		final Map<String, Object> newContact = createDefaultContact(null);

		loggedAndExpect(given().body(newContact))
				.body("conId", Matchers.equalTo(101))
				.body("honorificCode", Matchers.notNullValue())
				.body("name", Matchers.notNullValue())
				.body("firstName", Matchers.notNullValue())
				.body("birthday", Matchers.notNullValue())
				.body("email", Matchers.notNullValue())
				.statusCode(HttpStatus.SC_OK)
				.when()
				.put("/test/contact/101");
	}

	@Test
	public void testPutContactValidatorError() throws ParseException {
		final Map<String, Object> newContact = createDefaultContact(null);

		loggedAndExpect(given().body(newContact))
				.body("fieldErrors.conId", Matchers.contains("Id is mandatory"))
				.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
				.when()
				.put("/test/contact");

		final Map<String, Object> new2Contact = createDefaultContact(100L);
		new2Contact.put("birthday", convertDate("24/10/2012"));

		loggedAndExpect(given().body(new2Contact))
				.body("fieldErrors.birthday", Matchers.contains("You can't add contact younger than 16"))
				.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
				.when()
				.put("/test/contact");
	}

	@Test
	public void testPutContactByPathValidatorError() throws ParseException {
		final Map<String, Object> new2Contact = createDefaultContact(null);
		new2Contact.put("birthday", convertDate("24/10/2012"));

		loggedAndExpect(given().body(new2Contact))
				.body("fieldErrors.birthday", Matchers.contains("You can't add contact younger than 16"))
				.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
				.when()
				.put("/test/contact/101");
	}

	@Test
	public void testPutContactUserException() throws ParseException {
		final Map<String, Object> newContact = createDefaultContact(100L);
		newContact.remove("name");

		loggedAndExpect(given().body(newContact))
				.body("globalErrors", Matchers.contains("Name is mandatory"))
				.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
				.when()
				.put("/test/contact");
	}

	@Test
	public void testPutContactByPathUserException() throws ParseException {
		final Map<String, Object> newContact = createDefaultContact(null);
		newContact.remove("name");

		loggedAndExpect(given().body(newContact))
				.body("globalErrors", Matchers.contains("Name is mandatory"))
				.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
				.when()
				.put("/test/contact/101");
	}

	@Test
	public void testFilteredUpdateByExclude() throws ParseException {
		final Map<String, Object> contact = doGetServerSideObject();

		final Long oldConId = (Long) contact.get("conId");
		final String oldName = (String) contact.get("name");
		final String oldFirstName = (String) contact.get("firstName");
		final String oldEmail = (String) contact.get("email");
		final String newFirstName = oldFirstName + "FNTest";
		final String newEmail = "ETest." + oldEmail;

		contact.remove("conId"); //can't modify conId
		contact.remove("name"); //can't modify name
		//contact.put("conId", 1000L);
		//contact.put("name", newName);
		contact.put("firstName", newFirstName);
		contact.put("email", newEmail);

		loggedAndExpect(given().body(contact))
				.body("conId", Matchers.equalTo(oldConId))//not changed
				.body("honorificCode", Matchers.equalTo(contact.get("honorificCode")))
				.body("name", Matchers.equalTo(oldName)) //not changed
				.body("firstName", Matchers.equalTo(newFirstName))
				.body("birthday", Matchers.equalTo(contact.get("birthday")))
				.body("email", Matchers.equalTo(newEmail))
				.statusCode(HttpStatus.SC_OK)
				.when()
				.put("/test/filtered/" + oldConId);
	}

	@Test
	public void testFilteredUpdateByExcludeErrors() throws ParseException {
		final Map<String, Object> contact = doGetServerSideObject();
		final Long oldConId = (Long) contact.get("conId");

		contact.put("conId", 1000L);//can't modify conId
		contact.put("name", "test"); //can't modify name
		loggedAndExpect(given().body(contact))
				.statusCode(HttpStatus.SC_FORBIDDEN)
				.when()
				.put("/test/filtered/" + oldConId);

		contact.put("conId", 1000L);//can't modify conId
		contact.remove("name"); //can't modify name
		loggedAndExpect(given().body(contact))
				.statusCode(HttpStatus.SC_FORBIDDEN)
				.when()
				.put("/test/filtered/" + oldConId);

		contact.remove("conId");//can't modify conId
		contact.put("name", "test"); //can't modify name
		loggedAndExpect(given().body(contact))
				.statusCode(HttpStatus.SC_FORBIDDEN)
				.when()
				.put("/test/filtered/" + oldConId);
	}

	@Test
	public void testFilteredUpdateServerTokenErrors() throws ParseException {
		final Map<String, Object> contact = doGetServerSideObject();
		final Long oldConId = (Long) contact.get("conId");

		contact.remove("conId");//can't modify conId
		contact.remove("name"); //can't modify name

		contact.put("serverToken", "badToken");
		loggedAndExpect(given().body(contact))
				.statusCode(HttpStatus.SC_FORBIDDEN)
				.when()
				.put("/test/filtered/" + oldConId);

		contact.remove("serverToken");
		loggedAndExpect(given().body(contact))
				.statusCode(HttpStatus.SC_FORBIDDEN)
				.when()
				.put("/test/filtered/" + oldConId);
	}

	@Test
	public void testPutContactTooLongField() throws ParseException {
		final Map<String, Object> newContact = createDefaultContact(null);
		final String newNameValue = "Here i am !!";
		newContact.put("itsatoolongaliasforfieldcontactname", newNameValue);

		loggedAndExpect(given().body(newContact))
				.body("conId", Matchers.equalTo(101))
				.body("name", Matchers.equalTo(newNameValue))
				.statusCode(HttpStatus.SC_OK)
				.when()
				.put("/test/contactAliasName/101");
	}

	//=========================================================================

	private ResponseSpecification expect() {
		return RestAssured.expect();
	}

	private RequestSpecification given() {
		return RestAssured.given();
	}

	private ResponseSpecification loggedAndExpect() {
		return RestAssured.given()
				.filter(sessionFilter)
				.expect();
	}

	private ResponseSpecification loggedAndExpect(final RequestSpecification given) {
		return given
				.filter(sessionFilter)
				.expect();
	}

	private Map<String, Object> doGetServerSideObject() throws ParseException {
		final Map<String, Object> contact = doPostContact();
		final Long oldConId = (Long) contact.get("conId");

		final Response getResponse = loggedAndExpect()
				.statusCode(HttpStatus.SC_OK)
				.when()
				.get("/test/filtered/" + oldConId);
		final String serverToken = getResponse.body().path("serverToken");
		contact.put("serverToken", serverToken);
		return contact;
	}

	private String convertDate(final String dateStr) throws ParseException {
		final Date date = new SimpleDateFormat("dd/MM/yyyy").parse(dateStr);
		return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(date);
	}

	private Map<String, Object> createDefaultContact(final Long conId) throws ParseException {
		final Map<String, Object> newContact = createContact2(conId, "Mrs", "Fournier", "Catherine", convertDate("24/10/1985"), //
				createAddress("10, avenue Claude Vellefaux", "", "Paris", "75010", "France"), //
				"catherine.fournier@gmail.com", "01 91 92 93 94");
		return newContact;
	}

	private Map<String, Object> createContact2(final Long conId, final String honorific, final String name, final String firstName, final String birthday, final Map<String, Object> address, final String email, final String... tels) {
		final Map<String, Object> contact = new HashMap<>();
		if (conId != null) {
			contact.put("conId", conId);
		}
		contact.put("honorificCode", honorific);
		contact.put("name", name);
		contact.put("firstName", firstName);
		contact.put("birthday", birthday);
		contact.put("address", address);
		contact.put("email", email);
		contact.put("tels", Arrays.asList(tels));
		return contact;
	}

	private Map<String, Object> createAddress(final String street1, final String street2, final String city, final String postalCode, final String country) {
		final Map<String, Object> address = new HashMap<>();
		address.put("street1", street1);
		address.put("street2", street2);
		address.put("city", city);
		address.put("postalCode", postalCode);
		address.put("country", country);
		return address;
	}
}
