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

import io.vertigo.core.Home;
import io.vertigo.vega.impl.rest.filter.JettyMultipartConfig;
import io.vertigo.vega.plugins.rest.routesregister.sparkjava.SparkJavaRoutesRegister;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import spark.Spark;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.filter.session.SessionFilter;
import com.jayway.restassured.parsing.Parser;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

public final class WsRestTest {
	private static final String HEADER_ACCESS_TOKEN = "x-access-token";
	private static final String UTF8_TEST_STRING = "? TM™ éè'`àöêõù Euro€ R®@©∆∏∑∞⅓۲²³œβ";

	private static final int WS_PORT = 8088;
	private final SessionFilter sessionFilter = new SessionFilter();

	@BeforeClass
	public static void setUp() {
		Home.start(MyApp.config());
		doSetUp();
	}

	@Before
	public void preTestLogin() {
		RestAssured.given()
				.filter(sessionFilter)
				.get("/test/login");
	}

	@AfterClass
	public static void tearDown() {
		Home.stop();
	}

	private static void doSetUp() {
		// Will serve all static file are under "/public" in classpath if the route isn't consumed by others routes.
		// When using Maven, the "/public" folder is assumed to be in "/main/resources"
		//Spark.externalStaticFileLocation("d:/Projets/Projet_Kasper/SPA-Fmk/SPA-skeleton/public/");
		//Spark.externalStaticFileLocation("D:/@GitHub/vertigo/vertigo-vega-impl/src/test/resources/");
		//Spark.before(new IE8CompatibilityFix("8"));
		//Spark.before(new CorsAllower());
		//Translate EndPoint to route

		Spark.setPort(WS_PORT);
		final String tempDir = System.getProperty("java.io.tmpdir");
		Spark.before(new JettyMultipartConfig(tempDir));

		//RestAsssured init
		RestAssured.baseURI = "http://localhost";
		RestAssured.port = WS_PORT;
		RestAssured.registerParser("application/json+list", Parser.JSON);
		RestAssured.registerParser("application/json+entity:Contact", Parser.JSON);

		//init must be done foreach tests as Home was restarted each times
		new SparkJavaRoutesRegister().init();
	}

	@Test
	public void testCatalog() {
		RestAssured.given()
				.expect()
				.body("size()", Matchers.greaterThanOrEqualTo(50)) //actually 62
				.statusCode(HttpStatus.SC_OK)
				.when()
				.get("/catalog");
	}

	@Test
	public void testSwaggerApi() {
		RestAssured.given()
				.expect()
				.body("swagger", Matchers.equalTo(2.0f))
				.body("info", Matchers.notNullValue())
				.body("info.size()", Matchers.equalTo(3))
				.body("basePath", Matchers.anything()) //can be null
				.body("paths", Matchers.notNullValue())
				.body("paths.size()", Matchers.greaterThanOrEqualTo(50)) //actually 57
				.body("definitions", Matchers.notNullValue())
				.body("definitions.size()", Matchers.greaterThanOrEqualTo(30)) //actually 37
				.statusCode(HttpStatus.SC_OK)
				.when().log().ifValidationFails()
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
				.get("/swaggerUi/css/screen.css");

		RestAssured.given()
				.expect()
				.statusCode(HttpStatus.SC_OK)
				.when()
				.get("/swaggerUi/images/logo_small.png");

		RestAssured.given()
				.expect()
				.statusCode(HttpStatus.SC_OK)
				.when()
				.get("/swaggerUi/images/throbber.gif");

		RestAssured.given()
				.expect()
				.statusCode(HttpStatus.SC_NOT_FOUND)
				.when()
				.get("/swaggerUi/test404.mp4");
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
	public void testJsonSyntaxError() {
		final String[] testOkJson = { "{ \"firstName\" : \"test\" }", "{ firstName : \"test\" }", "{ 'firstName' : \"test\" }", "{\n\t\"firstName\" : \"test\"\n}" };
		final String[] testBadInterpretedJson = { "{ \"firstName\" : test\" }" };
		final String[] testBadSyntaxJson = { " \"firstName\" : \"test\" }", "{ \"firstName : \"test\" }", "{ firstName\" : \"test\" }",
				"{ \"firstName\"  \"test\" }", "{ \"firstName\" : \"test }", "{ \"firstName\" : \"test\" " };

		for (final String testJson : testOkJson) {
			loggedAndExpect(given().body(testJson))
					.body("firstName", Matchers.equalTo("test"))
					.statusCode(HttpStatus.SC_OK)
					.when()
					.put("/test/contactSyntax");
		}

		for (final String testJson : testBadInterpretedJson) {
			loggedAndExpect(given().body(testJson))
					.body("firstName", Matchers.equalTo("test\""))
					.statusCode(HttpStatus.SC_OK)
					.when()
					.put("/test/contactSyntax");
		}

		for (final String testJson : testBadSyntaxJson) {
			loggedAndExpect(given().body(testJson))
					.body("globalErrors", Matchers.contains("Error parsing param :Body:[1] on service PUT /test/contactSyntax"))
					.statusCode(HttpStatus.SC_BAD_REQUEST)
					.when()
					.put("/test/contactSyntax");
		}
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
	public void testDeleteContact() throws ParseException {
		final Map<String, Object> newContact = createDefaultContact(null);
		loggedAndExpect(given().body(newContact))
				.statusCode(HttpStatus.SC_OK)
				.when()
				.put("/test/contact/105");

		loggedAndExpect(given().body(newContact))
				.statusCode(HttpStatus.SC_NO_CONTENT)
				.when()
				.delete("/test/contact/105");
	}

	@Test
	public void testDeleteContactErrors() throws ParseException {
		final Map<String, Object> newContact = createDefaultContact(null);
		loggedAndExpect(given().body(newContact))
				.statusCode(HttpStatus.SC_OK)
				.when()
				.put("/test/contact/106");

		loggedAndExpect(given().body(newContact))
				.statusCode(HttpStatus.SC_NO_CONTENT)
				.when()
				.delete("/test/contact/106");

		loggedAndExpect(given().body(newContact))
				.body("globalErrors", Matchers.contains("Contact #106 unknown"))
				.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
				.when()
				.delete("/test/contact/106");

		loggedAndExpect(given().body(newContact))
				.body("globalErrors", Matchers.contains("You don't have enought rights"))
				.statusCode(HttpStatus.SC_FORBIDDEN)
				.when()
				.delete("/test/contact/2");
	}

	@Test
	public void testPostInnerBodyObject() {
		final Map<String, String> contactFrom = given().filter(sessionFilter)
				.when().get("/test/5")
				.body().as(Map.class);

		final Map<String, String> contactTo = given().filter(sessionFilter)
				.when().get("/test/6")
				.body().as(Map.class);

		final Map<String, Object> fullBody = new HashMap<>();
		fullBody.put("contactFrom", contactFrom);
		fullBody.put("contactTo", contactTo);

		loggedAndExpect(given().body(fullBody))
				.body("size()", Matchers.equalTo(2))
				.body("get(0).conId", Matchers.equalTo(5))
				.body("get(0).firstName", Matchers.notNullValue())
				.body("get(1).conId", Matchers.equalTo(6))
				.body("get(1).firstName", Matchers.notNullValue())
				.statusCode(HttpStatus.SC_OK)
				.when()
				.post("/test/innerbody");
	}

	@Test
	public void testPostInnerBodyLong() {
		final Map<String, Object> fullBody = new HashMap<>();
		fullBody.put("contactId1", 6);
		fullBody.put("contactId2", 7);

		loggedAndExpect(given().body(fullBody))
				.body("size()", Matchers.equalTo(2))
				.body("get(0).conId", Matchers.equalTo(6))
				.body("get(0).firstName", Matchers.notNullValue())
				.body("get(1).conId", Matchers.equalTo(7))
				.body("get(1).firstName", Matchers.notNullValue())
				.statusCode(HttpStatus.SC_OK)
				.when()
				.post("/test/innerLong");
	}

	@Test
	public void testInnerBodyLongToDtList() {
		final Map<String, Object> fullBody = new HashMap<>();
		fullBody.put("contactId1", 6);
		fullBody.put("contactId2", 7);

		loggedAndExpect(given().body(fullBody))
				.body("serverToken", Matchers.notNullValue())
				.body("value.size()", Matchers.equalTo(2))
				.body("value.get(0).conId", Matchers.equalTo(6))
				.body("value.get(0).firstName", Matchers.notNullValue())
				.body("value.get(1).conId", Matchers.equalTo(7))
				.body("value.get(1).firstName", Matchers.notNullValue())
				.statusCode(HttpStatus.SC_OK)
				.when()
				.post("/test/innerLongToDtList");
	}

	@Test
	public void testUiContext() {
		final Map<String, Object> fullBody = new HashMap<>();
		fullBody.put("contactId1", 6);
		fullBody.put("contactId2", 7);

		loggedAndExpect(given().body(fullBody))
				.body("contactFrom.name", Matchers.equalTo("Moreau"))
				.body("contactFrom.serverToken", Matchers.notNullValue())
				.body("contactTo.name", Matchers.equalTo("Lefebvre"))
				.body("contactTo.serverToken", Matchers.notNullValue())
				.body("testLong", Matchers.equalTo(12))
				.body("testString", Matchers.equalTo("the String test"))
				//.body("testDate", Matchers.any(Date.class)) //get a string no directly a Date can't ensured
				.body("testEscapedString", Matchers.equalTo("the EscapedString \",} test"))
				.statusCode(HttpStatus.SC_OK)
				.when()
				.post("/test/uiContext");
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

		contact.remove("conId");//can't modify conId
		contact.remove("name"); //can't modify name
		loggedAndExpect(given().body(contact))
				.statusCode(HttpStatus.SC_OK)
				.when()
				.put("/test/filtered/" + oldConId);
	}

	@Test
	public void testFilteredUpdateByInclude() throws ParseException {
		final Map<String, Object> contact = doGetServerSideObject();

		final Long oldConId = (Long) contact.get("conId");
		final String oldName = (String) contact.get("name");
		final String oldFirstName = (String) contact.get("firstName");
		final String oldEmail = (String) contact.get("email");
		final String oldHonorificCode = (String) contact.get("honorificCode");
		final String oldBirthday = (String) contact.get("birthday");
		final String newFirstName = oldFirstName + "FNTest";
		final String newEmail = "ETest." + oldEmail;

		contact.remove("conId"); //can't modify conId
		contact.remove("name"); //can't modify name
		contact.put("firstName", newFirstName);
		contact.put("email", newEmail);
		contact.remove("honorificCode"); //can't modify conId
		contact.remove("birthday"); //can't modify name

		loggedAndExpect(given().body(contact))
				.body("conId", Matchers.equalTo(oldConId))//not changed
				.body("honorificCode", Matchers.equalTo(oldHonorificCode)) //not changed
				.body("name", Matchers.equalTo(oldName)) //not changed
				.body("firstName", Matchers.equalTo(newFirstName))// changed
				.body("birthday", Matchers.equalTo(oldBirthday))//not changed
				.body("email", Matchers.equalTo(newEmail))// changed
				.statusCode(HttpStatus.SC_OK)
				.when()
				.put("/test/filteredInclude/" + oldConId);

	}

	@Test
	public void testFilteredUpdateByIncludeErrors() throws ParseException {
		final Map<String, Object> contact = doGetServerSideObject();
		final Long oldConId = (Long) contact.get("conId");

		loggedAndExpect(given().body(contact))
				.statusCode(HttpStatus.SC_FORBIDDEN)
				.when()
				.put("/test/filteredInclude/" + oldConId);

		contact.put("conId", 1000L); //can't modify conId
		contact.remove("name"); //can't modify name
		contact.remove("firstName");
		contact.remove("email");
		contact.remove("honorificCode"); //can't modify conId
		contact.remove("birthday"); //can't modify name
		loggedAndExpect(given().body(contact))
				.statusCode(HttpStatus.SC_FORBIDDEN)
				.when()
				.put("/test/filteredInclude/" + oldConId);

		contact.remove("conId");//can't modify conId
		contact.put("firstName", "test"); //can't modify name
		loggedAndExpect(given().body(contact))
				.statusCode(HttpStatus.SC_OK)
				.when()
				.put("/test/filteredInclude/" + oldConId);

		contact.remove("firstName");
		contact.put("name", "test"); //can't modify name
		loggedAndExpect(given().body(contact))
				.statusCode(HttpStatus.SC_FORBIDDEN)
				.when()
				.put("/test/filteredInclude/" + oldConId);

		contact.remove("name"); //can't modify name
		contact.put("email", "test@test.com");
		loggedAndExpect(given().body(contact))
				.statusCode(HttpStatus.SC_OK)
				.when()
				.put("/test/filteredInclude/" + oldConId);

		contact.put("honorificCode", "test"); //can't modify honorificCode
		loggedAndExpect(given().body(contact))
				.statusCode(HttpStatus.SC_FORBIDDEN)
				.when()
				.put("/test/filteredInclude/" + oldConId);

		contact.remove("honorificCode"); //can't modify honorificCode
		contact.put("birthday", convertDate("24/10/1985")); //can't modify birthday
		loggedAndExpect(given().body(contact))
				.statusCode(HttpStatus.SC_FORBIDDEN)
				.when()
				.put("/test/filteredInclude/" + oldConId);

		contact.remove("birthday"); //can't modify honorificCode
		loggedAndExpect(given().body(contact))
				.statusCode(HttpStatus.SC_OK)
				.when()
				.put("/test/filteredInclude/" + oldConId);
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

	@Test
	public void testGetContactExtended() {
		loggedAndExpect()
				.body("conId", Matchers.equalTo(1))
				.body("vanillaUnsupportedMultipleIds", Matchers.iterableWithSize(3))
				.body("vanillaUnsupportedMultipleIds", Matchers.hasItems(1, 2, 3))
				.header("content-type", Matchers.containsString("json+entity:Contact+meta"))
				.statusCode(HttpStatus.SC_OK)
				.when()
				.get("/test/contactExtended/1");
	}

	@Test
	public void testPutContactExtended() throws ParseException {
		final Map<String, Object> newContact = createDefaultContact(103L);
		newContact.remove("conId");
		newContact.put("vanillaUnsupportedMultipleIds", new int[] { 3, 4, 5 });

		loggedAndExpect(given().body(newContact))
				.body("conId", Matchers.equalTo(103))
				.body("vanillaUnsupportedMultipleIds", Matchers.iterableWithSize(3))
				.body("vanillaUnsupportedMultipleIds", Matchers.hasItems(3, 4, 5))
				.statusCode(HttpStatus.SC_OK)
				.when()
				.put("/test/contactExtended/103");
	}

	@Test
	public void testPutContactExtendedValidator() throws ParseException {
		final Map<String, Object> newContact = createDefaultContact(104L);
		newContact.put("vanillaUnsupportedMultipleIds", new int[] { 3, 4, 5 });

		loggedAndExpect(given().body(newContact))
				.body("fieldErrors.conId", Matchers.contains("Id must not be set"))
				.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
				.when()
				.put("/test/contactExtended/104");

		newContact.remove("conId");
		newContact.put("birthday", convertDate("24/10/2012"));
		loggedAndExpect(given().body(newContact))
				.body("fieldErrors.birthday", Matchers.contains("You can't add contact younger than 16"))
				.statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
				.when()
				.put("/test/contactExtended/104");
	}

	@Test
	public void testPostCharset() throws UnsupportedEncodingException {
		final String testFirstName = "Gérard";
		final String testJson = "{ \"firstName\" : \"" + testFirstName + "\" }";
		given().filter(sessionFilter) //logged
				.contentType("application/json;charset=UTF-8")
				.body(Collections.singletonMap("firstName", testFirstName)) //RestAssured read encodetype and encode as UTF8
				.expect()
				.body("firstName", Matchers.equalTo(testFirstName))
				.statusCode(HttpStatus.SC_OK)
				.when()
				.post("/test/charset");

		given().filter(sessionFilter) //logged
				.contentType("application/json;charset=UTF-8")
				.body(testJson.getBytes("UTF-8")) //We force the encode charset
				.expect()
				.body("firstName", Matchers.equalTo(testFirstName))
				.statusCode(HttpStatus.SC_OK)
				.when()
				.post("/test/charset");

		given().filter(sessionFilter) //logged
				.contentType("application/json;charset=ISO-8859-1")
				.body(Collections.singletonMap("firstName", testFirstName)) //RestAssured read encodetype and encode as ISO-8859-1
				.expect()
				.body("firstName", Matchers.equalTo(testFirstName))
				.statusCode(HttpStatus.SC_OK)
				.when()
				.post("/test/charset");

		given().filter(sessionFilter) //logged
				.contentType("application/json;charset=ISO-8859-1")
				.body(testJson.getBytes("ISO-8859-1")) //We force the encode charset
				.expect()
				.body("firstName", Matchers.equalTo(testFirstName))
				.statusCode(HttpStatus.SC_OK)
				.when()
				.post("/test/charset");
	}

	@Test
	public void testPostCharsetUtf8() throws UnsupportedEncodingException {
		final String testFirstName = UTF8_TEST_STRING;
		final String testJson = "{ \"firstName\" : \"" + testFirstName + "\" }";
		given().filter(sessionFilter) //logged
				.contentType("application/json;charset=UTF-8")
				.body(Collections.singletonMap("firstName", testFirstName)) //RestAssured read encodetype and encode as UTF8
				.expect()
				.body("firstName", Matchers.equalTo(testFirstName))
				.statusCode(HttpStatus.SC_OK)
				.when()
				.post("/test/charset");

		given().filter(sessionFilter) //logged
				.contentType("application/json;charset=UTF-8")
				.body(testJson.getBytes("UTF-8")) //We force the encode charset
				.expect()
				.body("firstName", Matchers.equalTo(testFirstName))
				.statusCode(HttpStatus.SC_OK)
				.when()
				.post("/test/charset");
	}

	@Test
	public void testPostCharsetDefaultUtf8() throws UnsupportedEncodingException {
		final String testFirstName = UTF8_TEST_STRING;
		final String testJson = "{ \"firstName\" : \"" + testFirstName + "\" }";

		given().filter(sessionFilter) //logged
				.contentType("application/json;charset") //We precise an incomplete charset otherwise Restassured add a default charset=ISO-8859-1 to contentType
				.body(testJson.getBytes("UTF-8")) //We force the encode charset
				.expect()
				.body("firstName", Matchers.equalTo(testFirstName))
				.statusCode(HttpStatus.SC_OK)
				.when()
				.post("/test/charset");
	}

	@Test
	public void testPutContactCharsetIso8859() throws UnsupportedEncodingException {
		final String testedCharset = "ISO-8859-1";
		final String testFirstName = UTF8_TEST_STRING;
		final String testFirstNameIso = new String(UTF8_TEST_STRING.getBytes(testedCharset), testedCharset);
		final String testJson = "{ \"firstName\" : \"" + testFirstName + "\" }";

		given().filter(sessionFilter) //logged
				.contentType("application/json;charset=" + testedCharset)
				.body(Collections.singletonMap("firstName", testFirstName))
				.expect()
				.body("firstName", Matchers.equalTo(testFirstNameIso))
				.statusCode(HttpStatus.SC_OK)
				.when()
				.post("/test/charset");

		given().filter(sessionFilter) //logged
				.contentType("application/json;charset=" + testedCharset)
				.body(testJson.getBytes(testedCharset))
				.expect()
				.body("firstName", Matchers.equalTo(testFirstNameIso))
				.statusCode(HttpStatus.SC_OK)
				.when()
				.post("/test/charset");
	}

	@Test
	public void testSearchQueryPagined() throws ParseException {
		doTestSearchPagined(false);
	}

	@Test
	public void testSearchAutoPagined() throws ParseException {
		doTestSearchPagined(true);
	}

	private void doTestSearchPagined(final boolean isAuto) throws ParseException {
		final Map<String, Object> criteriaContact = new HashMap<>();
		criteriaContact.put("birthdayMin", convertDate("19/05/1978"));
		criteriaContact.put("birthdayMax", convertDate("19/05/1985"));

		final String serverSideToken;
		serverSideToken = doPaginedSearch(criteriaContact, 3, 0, "name", false, null, 3, "Dubois", "Garcia", isAuto);
		doPaginedSearch(criteriaContact, 3, 2, "name", false, serverSideToken, 3, "Garcia", "Moreau", isAuto);
		doPaginedSearch(criteriaContact, 3, 5, "name", false, serverSideToken, 1, "Petit", "Petit", isAuto);
		doPaginedSearch(criteriaContact, 10, 10, "name", false, serverSideToken, 0, "Petit", "Petit", isAuto);
	}

	@Test
	public void testSearchQueryPaginedSortName() throws ParseException {
		doTestSearchPaginedSortName(false);
	}

	@Test
	public void testSearchAutoPaginedSortName() throws ParseException {
		doTestSearchPaginedSortName(true);
	}

	private void doTestSearchPaginedSortName(final boolean isAuto) throws ParseException {
		final Map<String, Object> criteriaContact = new HashMap<>();
		criteriaContact.put("birthdayMin", convertDate("19/05/1978"));
		criteriaContact.put("birthdayMax", convertDate("19/05/1985"));
		//gets : "Dubois","Durant","Garcia","Martin","Moreau","Petit"

		final String serverSideToken;
		serverSideToken = doPaginedSearch(criteriaContact, 3, 0, "name", false, null, 3, "Dubois", "Garcia", isAuto);
		doPaginedSearch(criteriaContact, 10, 0, "name", false, serverSideToken, 6, "Dubois", "Petit", isAuto);
		doPaginedSearch(criteriaContact, 4, 0, "name", false, serverSideToken, 4, "Dubois", "Martin", isAuto);
		doPaginedSearch(criteriaContact, 4, 0, "name", true, serverSideToken, 4, "Petit", "Garcia", isAuto);
		doPaginedSearch(criteriaContact, 4, 1, "name", true, serverSideToken, 4, "Moreau", "Durant", isAuto);
		doPaginedSearch(criteriaContact, 4, 1, "name", false, serverSideToken, 4, "Durant", "Moreau", isAuto);
	}

	@Test
	public void testSearchQueryPaginedSortDate() throws ParseException {
		doTestSearchPaginedSortDate(false);
	}

	@Test
	public void testSearchAutoPaginedSortDate() throws ParseException {
		doTestSearchPaginedSortDate(true);
	}

	private void doTestSearchPaginedSortDate(final boolean isAuto) throws ParseException {
		final Map<String, Object> criteriaContact = new HashMap<>();
		criteriaContact.put("birthdayMin", convertDate("19/05/1978"));
		criteriaContact.put("birthdayMax", convertDate("19/05/1985"));

		final String serverSideToken;
		serverSideToken = doPaginedSearch(criteriaContact, 3, 0, "name", false, null, 3, "Dubois", "Garcia", isAuto);
		doPaginedSearch(criteriaContact, 10, 0, "birthday", false, serverSideToken, 6, "Petit", "Garcia", isAuto);
		doPaginedSearch(criteriaContact, 3, 1, "birthday", false, serverSideToken, 3, "Martin", "Durant", isAuto);
		doPaginedSearch(criteriaContact, 3, 1, "birthday", true, serverSideToken, 3, "Moreau", "Dubois", isAuto);
	}

	@Test
	public void testSearchQueryPaginedMissing() throws ParseException {
		doTestSearchPaginedMissing(false);
	}

	@Test
	public void testSearchAutoPaginedMissing() throws ParseException {
		doTestSearchPaginedMissing(true);
	}

	private void doTestSearchPaginedMissing(final boolean isAuto) throws ParseException {
		final Map<String, Object> criteriaContact = new HashMap<>();
		criteriaContact.put("birthdayMin", convertDate("19/05/1978"));
		criteriaContact.put("birthdayMax", convertDate("19/05/1985"));

		String serverSideToken;
		serverSideToken = doPaginedSearch(criteriaContact, 3, 0, "name", false, null, 3, "Dubois", "Garcia", isAuto);
		doPaginedSearch(criteriaContact, null, null, null, null, serverSideToken, 6, "Martin", "Garcia", isAuto);
		doPaginedSearch(criteriaContact, 5, null, null, null, serverSideToken, 5, "Martin", "Moreau", isAuto);
		doPaginedSearch(criteriaContact, 5, 5, null, null, serverSideToken, 1, "Garcia", "Garcia", isAuto);
	}

	private String doPaginedSearch(final Map<String, Object> criteriaContact, final Integer top, final Integer skip, final String sortFieldName, final Boolean sortDesc, final String listServerToken, final int expectedSize, final String firstContactName, final String lastContactName, final boolean isAuto) {
		final RequestSpecification given = given().filter(sessionFilter);
		final String wsUrl = isAuto ? "/test/searchAutoPagined" : "/test/searchQueryPagined";
		if (top != null) {
			given.queryParam("top", top);
		}
		if (skip != null) {
			given.queryParam("skip", skip);
		}
		if (sortFieldName != null) {
			given.queryParam("sortFieldName", sortFieldName);
		}
		if (sortDesc != null) {
			given.queryParam("sortDesc", sortDesc);
		}
		if (listServerToken != null) {
			given.queryParam("listServerToken", listServerToken);
		}
		ResponseSpecification responseSpecification = given.body(criteriaContact)
				.expect()
				.body("size()", Matchers.equalTo(expectedSize));
		if (expectedSize > 0) {
			responseSpecification = responseSpecification.body("get(0).name", Matchers.equalTo(firstContactName))
					.body("get(" + (expectedSize - 1) + ").name", Matchers.equalTo(lastContactName));
		}
		final String serverSideToken = responseSpecification.statusCode(HttpStatus.SC_OK)
				.when()
				.post(wsUrl)
				.header("serverSideToken");
		return serverSideToken;
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
				.expect().log().ifValidationFails();
	}

	private ResponseSpecification loggedAndExpect(final RequestSpecification given) {
		return given
				.filter(sessionFilter)
				.expect().log().ifValidationFails();
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
		final Map<String, Object> newContact = createContact2(conId, "Mrs", "Fournier", "Catherine", convertDate("24/10/1985"),
				createAddress("10, avenue Claude Vellefaux", "", "Paris", "75010", "France"),
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
