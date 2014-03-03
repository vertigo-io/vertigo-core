package io.vertigo.dynamo.kvdatastore;

public final class Flower {
	private String name;
	private Double price;
	
	public Double getPrice() {
		return price;
	}
	public String getName() {
		return name;
	} 
	
	public void setPrice(Double price) {
		this.price = price;
	}
	public void setName(String name) {
		this.name = name;
	}
	
}
