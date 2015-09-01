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
package io.vertigo.vega.rest.data.ws;

import io.vertigo.lang.MessageText;
import io.vertigo.lang.VUserException;
import io.vertigo.vega.rest.WebServices;
import io.vertigo.vega.rest.data.domain.Contact;
import io.vertigo.vega.rest.data.domain.ContactCriteria;
import io.vertigo.vega.rest.data.domain.ContactDao;
import io.vertigo.vega.rest.data.domain.ContactValidator;
import io.vertigo.vega.rest.data.domain.MandatoryPkValidator;
import io.vertigo.vega.rest.stereotype.AnonymousAccessAllowed;
import io.vertigo.vega.rest.stereotype.DELETE;
import io.vertigo.vega.rest.stereotype.GET;
import io.vertigo.vega.rest.stereotype.POST;
import io.vertigo.vega.rest.stereotype.PUT;
import io.vertigo.vega.rest.stereotype.PathParam;
import io.vertigo.vega.rest.stereotype.PathPrefix;
import io.vertigo.vega.rest.stereotype.SessionLess;
import io.vertigo.vega.rest.stereotype.Validate;

import java.util.List;

import javax.inject.Inject;

//basé sur http://www.restapitutorial.com/lessons/httpmethods.html

@PathPrefix("/contacts")
public final class WsContactsWebServices implements WebServices {

	@Inject
	private ContactDao contactDao;

	@GET("/search")
	public List<Contact> readList(final ContactCriteria listCriteria) {
		//offset + range ?
		//code 200
		return contactDao.getList();
	}

	@SessionLess
	@AnonymousAccessAllowed
	@GET("")
	public List<Contact> readAllList() {
		//offset + range ?
		//code 200
		return contactDao.getList();
	}

	@GET("/{conId}")
	public Contact read(@PathParam("conId") final long conId) {
		final Contact contact = contactDao.get(conId);
		if (contact == null) {
			//404 ?
			throw new VUserException(new MessageText("Contact #" + conId + " unknown", null));
		}
		//200
		return contact;
	}

	//@POST is non-indempotent
	@POST("")
	public Contact insert(//
			final @Validate({ ContactValidator.class, MandatoryPkValidator.class }) Contact contact) {
		if (contact.getConId() != null) {
			throw new VUserException(new MessageText("Contact #" + contact.getConId() + " already exist", null));
		}
		if (contact.getName() == null || contact.getName().isEmpty()) {
			throw new VUserException(new MessageText("Name is mandatory", null));
		}
		contactDao.post(contact);
		//code 201 + location header : GET route
		return contact;
	}

	//PUT is indempotent : ID obligatoire
	@PUT("/*")
	public Contact update(final Contact contact) {
		if (contact.getName() == null || contact.getName().isEmpty()) {
			//400
			throw new VUserException(new MessageText("Name is mandatory", null));
		}
		if (contact.getConId() == null) {
			throw new VUserException(new MessageText("Id is mandatory", null));
		}
		contactDao.put(contact);
		//200
		return contact;
	}

	@DELETE("/{conId}")
	public void delete(@PathParam("conId") final long conId) {
		if (!contactDao.containsKey(conId)) {
			//404
			throw new VUserException(new MessageText("Contact #" + conId + " unknown", null));
		}
		if (conId < 5) {
			//401
			throw new VUserException(new MessageText("You don't have enought rights", null));
		}
		//200
		contactDao.remove(conId);
	}
}
