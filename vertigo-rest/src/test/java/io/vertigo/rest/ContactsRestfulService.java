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

import io.vertigo.kernel.exception.VUserException;
import io.vertigo.kernel.lang.MessageText;
import io.vertigo.persona.security.KSecurityManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

//basé sur http://www.restapitutorial.com/lessons/httpmethods.html

public final class ContactsRestfulService implements RestfulService {

	@Inject
	private KSecurityManager securityManager;

	/*private final enum Group {
		Friends("FRD", "Friends"), //
		Familly("FAM", "Familly"), //
		CoWorkers("CWO", "Colleagues"), Familiar("FAR", "Familiar"),
	}*/

	private static enum Honorific {
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
		private final String abbreviation;
		private final String label;

		Honorific(final String code, final String abbreviation, final String label) {
			this.code = code;
			this.abbreviation = abbreviation;
			this.label = label;
		}

		public String getCode() {
			return code;
		}
	}

	private final Map<Long, Contact> contacts = new HashMap<Long, Contact>();

	public ContactsRestfulService() throws ParseException {
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
		contact.setId(conId);
		contact.setHonorificCode(honorific.code);
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

	@GET("/contacts/search")
	public List<Contact> readList(final ListCriteria listCriteria) {
		//offset + range ?
		//code 200
		return new ArrayList<Contact>(contacts.values());
	}

	@AnonymousAccessAllowed
	@GET("/login")
	public void login() {
		//code 200
		securityManager.getCurrentUserSession().get().authenticate();
	}

	@SessionLess
	@AnonymousAccessAllowed
	@GET("/contacts")
	public List<Contact> readAllList() {
		//offset + range ?
		//code 200
		return new ArrayList<Contact>(contacts.values());
	}

	@GET("/contacts/{conId}")
	public Contact read(@PathParam("conId") final long conId) {
		final Contact contact = contacts.get(conId);
		if (contact == null) {
			//404 ?
			throw new VUserException(new MessageText("Contact #" + conId + " unknown", null));
		}
		//200
		return contact;
	}

	//@POST is non-indempotent
	@POST("/contacts")
	public Contact insert(//
			final @Validate({ ContactValidator.class, MandatoryPkValidator.class }) Contact contact) {
		if (contact.getId() != null) {
			throw new VUserException(new MessageText("Contact #" + contact.getId() + " already exist", null));
		}
		if (contact.getName() == null || contact.getName().isEmpty()) {
			throw new VUserException(new MessageText("Name is mandatory", null));
		}
		final long nextId = getNextId();
		contact.setId(nextId);
		contacts.put(nextId, contact);
		//code 201 + location header : GET route
		return contact;
	}

	//PUT is indempotent : ID obligatoire
	@PUT("/contacts/{conId}")
	public Contact update(final Contact contact) {
		if (contact.getName() == null || contact.getName().isEmpty()) {
			//400
			throw new VUserException(new MessageText("Name is mandatory", null));
		}
		if (contact.getId() != null) {
			contacts.put(contact.getId(), contact);
			//200
			return contact;
		} else {
			throw new VUserException(new MessageText("Id is mandatory", null));
		}
	}

	@DELETE("/contacts/{conId}")
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

	private long getNextId() {
		final long nextId = UUID.randomUUID().getMostSignificantBits();
		if (contacts.containsKey(nextId)) {
			return getNextId();
		}
		return nextId;
	}
}
