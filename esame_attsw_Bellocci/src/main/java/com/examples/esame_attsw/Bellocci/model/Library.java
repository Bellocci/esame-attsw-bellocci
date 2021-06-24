package com.examples.esame_attsw.Bellocci.model;

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
	private List<Book> list_books = new ArrayList<>();
	
	public Library() { }
	
	public Library(String id, String name) {
		this.id = id;
		this.name = name;
		//this.list_books = new ArrayList<Book>();
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
		return this.list_books;
	}
	
	public void setListBooks(List<Book> list_books) {
		this.list_books = list_books;
	}
	
	@Override
	public String toString() {
		return id + " - " + name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((list_books == null) ? 0 : list_books.hashCode());
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
		if(list_books.size() != library.list_books.size())
			return false;
		for(int i = 0; i < list_books.size(); i++) {
			if(!list_books.get(i).equals(library.list_books.get(i)))
				return false;
		}
		return true;
	}
}
