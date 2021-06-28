package com.examples.esameattswbellocci.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "Libraries")
public class Library {

	@Id
	@Column(length = 50)
	private String id;
	
	@Column(name = "Name", length = 50)
	private String name;
	
	/*
	 * mappedBy : Specifies the field that owns the relationship.
	 * CascadeType : The operations that must be cascaded to the target of the association.
	 * orphanRemoval : Whether to apply the remove operation to entities that have been 
	 * 				   removed from the relationship and to cascade the remove operation to those entities.
	 */
	
	@OneToMany(mappedBy = "library", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Book> listBooks = new ArrayList<>();
	
	public Library() { }
	
	public Library(String id, String name) {
		this.id = id;
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public List<Book> getListBooks() {
		return this.listBooks;
	}
	
	public void setListBooks(List<Book> listBooks) {
		this.listBooks = listBooks;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((listBooks == null) ? 0 : listBooks.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj)
			return true;
		if(obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Library library = (Library) obj;
		if(!id.equals(library.id))
			return false;
		if(!name.equals(library.name))
			return false;
		if(listBooks.size() != library.listBooks.size())
			return false;
		for(int i = 0; i < listBooks.size(); i++) {
			if(!listBooks.get(i).equals(library.listBooks.get(i)))
				return false;
		}
		return true;
	}
}
