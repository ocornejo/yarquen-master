package org.yarquen.article;

public class KeywordTrust {
	private String name;
	private String id;
	private double trust;
	private String color;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public void setTrust(double trust){
		this.trust= trust;
	}
	
	public double getTrust(){
		return trust;
	}
	
	public void setColor(){
		
		if (isBetween(trust, 1, 3)) {
		  color = "bad";
		} else if (isBetween(trust, 3, 5)) {
			color = "poor";
		} else if (isBetween(trust, 5, 7)) {
			color = "regular";
		} else if (isBetween(trust, 7, 9)) {
			color = "good";
		} else if (isBetween(trust, 9, 10)) {
			color = "excellent";
		} else{
			color= "info";
		}
	}
	public String getColor(){
		return color;
	}
	
	private boolean isBetween(double x, double lower, double upper) {
		  return lower <= x && x <= upper;
	}
}
