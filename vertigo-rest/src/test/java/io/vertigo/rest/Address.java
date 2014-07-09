package io.vertigo.rest;

import io.vertigo.dynamo.domain.model.DtObject;

public class Address implements DtObject {

	private String street1;
	private String street2;
	private String city;
	private String postalCode;
	private String Country;

	public String getStreet1() {
		return street1;
	}

	public void setStreet1(final String street1) {
		this.street1 = street1;
	}

	public String getStreet2() {
		return street2;
	}

	public void setStreet2(final String street2) {
		this.street2 = street2;
	}

	public String getCity() {
		return city;
	}

	public void setCity(final String city) {
		this.city = city;
	}

	public String getPostalCode() {
		return postalCode;
	}

	public void setPostalCode(final String postalCode) {
		this.postalCode = postalCode;
	}

	public String getCountry() {
		return Country;
	}

	public void setCountry(final String country) {
		Country = country;
	}

}
