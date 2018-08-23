package com.tranfode.domain;

import java.util.List;

public class BinderList {
	String id ;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	String name;
	String classification;
	List<Children> children;
	
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public String getClassification() {
		return classification;
	}
	public void setClassification(String classification) {
		this.classification = classification;
	}
	public List<Children> getChildren() {
		return children;
	}
	public void setChildrenList(List<Children> children) {
		this.children = children;
	}
	

	

}
