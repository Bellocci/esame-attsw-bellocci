package com.examples.esameattswbellocci.model;

import java.util.Objects;

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
	
	@Column(name = "Name", length = 50, nullable = false)
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
		return Objects.hash(id, name);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (this.getClass() != obj.getClass())
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

	@Override
	public String toString() {
		return "Book [id=" + id + ", name=" + name + "]";
	}

}
