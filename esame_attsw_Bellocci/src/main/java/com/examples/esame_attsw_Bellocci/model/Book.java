package com.examples.esame_attsw_Bellocci.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "Books")
public class Book {

	@Id
	@Column(length = 50)
	private String id;
	
	@Column(name = "Name", length = 50)
	private String name;
	
	@ManyToOne
	@JoinColumn(name = "id_library", nullable = false)
	private Library library;

	public Book(String id, String name) {
		this.id = id;
		this.name = name;
	}

	public Book() {}

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
	
	public Library getLibrary() {
		return library;
	}
	
	public void setLibrary(Library library) {
		this.library = library;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((library == null) ? 0 : library.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		Book book = (Book) obj;
		if (id == null) {
			if (book.id != null)
				return false;
		} else if (!id.equals(book.id))
			return false;
		if (library == null) {
			if (book.library != null)
				return false;
		} else if (!library.getId().equals(book.library.getId()))
			return false;
		if (name == null) {
			if (book.name != null)
				return false;
		} else if (!name.equals(book.name))
			return false;
		return true;
	}


}
