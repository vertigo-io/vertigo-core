package io.vertigo.rest;

import io.vertigo.dynamo.domain.metamodel.annotation.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.annotation.Field;
import io.vertigo.dynamo.domain.model.DtObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@DtDefinition
public final class Contact implements DtObject {

	@Field(domain = "DO_ID", type = "PRIMARY_KEY", notNull = true, label = "Contact Id")
	private Long conId;
	@Field(domain = "DO_CODE", label = "Honorific title")
	private String honorificCode;
	//mandatory
	@Field(domain = "DO_TEXTE_50", notNull = true, label = "Name")
	private String name;
	@Field(domain = "DO_TEXTE_50", label = "Firstname")
	private String firstName;
	@Field(domain = "DO_DATE", label = "Birthday")
	private Date birthday;
	@Field(domain = "DO_EMAIL", label = "Email")
	private String email;

	private List<String> tels;
	private Address address;

	public Long getId() {
		return conId;
	}

	public void setId(final Long conId) {
		this.conId = conId;
	}

	public String getHonorificCode() {
		return honorificCode;
	}

	public void setHonorificCode(final String honorificCode) {
		this.honorificCode = honorificCode;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(final String firstName) {
		this.firstName = firstName;
	}

	public Date getBirthday() {
		return birthday;
	}

	public void setBirthday(final Date birthday) {
		this.birthday = birthday;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(final String email) {
		this.email = email;
	}

	public Address getAddress() {
		return address;
	}

	public void setAddress(final Address address) {
		this.address = address;
	}

	public List<String> getTels() {
		return Collections.unmodifiableList(tels);
	}

	public void setTels(final List<String> tels) {
		this.tels = new ArrayList<String>(tels);
	}

}
